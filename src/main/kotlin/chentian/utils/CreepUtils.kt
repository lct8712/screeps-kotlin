package chentian.utils

import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isInTargetRoom
import chentian.extensions.isWorking
import chentian.extensions.moveToTargetRoom
import chentian.extensions.role
import chentian.extensions.setWorking
import chentian.extensions.targetRoomName
import screeps.api.ActiveBodyPartConstant
import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_BUSY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.LINE_STYLE_DOTTED
import screeps.api.LineStyleConstant
import screeps.api.MOVE
import screeps.api.MoveToOptions
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.RoomVisual
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.Source
import screeps.api.WORK
import screeps.api.options
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureSpawn
import screeps.api.value
import screeps.utils.toMap
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */

const val MAX_CREEP_COUNT = 12

fun createCreepIfNecessary(spawn: StructureSpawn) {
    if (Game.creeps.toMap().count() < MAX_CREEP_COUNT) {
        createNormalCreep(spawn)
    }
}

fun createCreepName(role: String): String {
    return "creep_${role}_${Game.time}"
}

fun createRemoteCreep(spawn: StructureSpawn, roomName: String, role: String = "") {
    if (spawn.room.energyAvailable < 250) {
        return
    }

    val bodyList = mutableListOf(MOVE, CARRY, CARRY, WORK)
    doCreateCreep(role, roomName, spawn, bodyList)
}

fun createNormalCreep(spawn: StructureSpawn, role: String = "") {
    val partCount = spawn.room.energyAvailable / 300
    if (partCount <= 0) {
        return
    }

    // 储备不够，等待
    if (spawn.room.energyAvailable < spawn.room.energyCapacityAvailable / 2 && !spawn.isFullEnergy()) {
        return
    }

    // 每 2 个 work 配 一对 carry & move
    val bodyList = mutableListOf<ActiveBodyPartConstant>().apply {
        for (i in 0 until partCount) {
            add(MOVE)
            add(CARRY)
            add(WORK)
            add(WORK)
        }
    }

    doCreateCreep(role, "", spawn, bodyList)
}

fun createMoveOptions(color: String): MoveToOptions {
    return object : MoveToOptions {
        override val visualizePathStyle: RoomVisual.Style?
            get() = object : RoomVisual.Style {
                override var lineStyle: LineStyleConstant?
                    get() = LINE_STYLE_DOTTED
                    set(value) {}
                override var opacity: Double?
                    get() = 0.3
                    set(value) {}
            }
    }
}

private fun doCreateCreep(role: String, targetRoomName: String, spawn: StructureSpawn, bodyList: MutableList<ActiveBodyPartConstant>) {
    if (spawn.spawning != null) {
        println("spawning, existing")
        return
    }

    val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(role), options {
        memory = jsObject<CreepMemory> {
            this.role = role
            this.targetRoomName = targetRoomName
        }
    })
    println("create new creep $role. code: $result, $bodyList")
    if (result != OK && result != ERR_BUSY) {
        Game.notify("create creep error: ${result.value}")
    }
}

private val moveToOptions = createMoveOptions("00aaff")

fun harvestEnergyAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullEnergy()) {
        creep.setWorking(true)
        creep.say("full")
        jobAction()
        return
    }

    if (creep.isEmptyEnergy() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyEnergy()) "empty" else "fill"
        creep.say(message)

        val containers = creep.room.find(FIND_STRUCTURES).filter {
            it.structureType == STRUCTURE_CONTAINER
        }.map { it as StructureContainer }
        val minContainer = containers.minBy { it.store.energy }
        val maxContainer = containers.maxBy { it.store.energy }
        if (minContainer != null && maxContainer != null && minContainer.store.energy * 10 < maxContainer.store.energy) {
            if (creep.withdraw(maxContainer, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(maxContainer.pos, moveToOptions)
            }
            println("$creep to full container $maxContainer")
            return
        }

        val source: Source? = creep.pos.findClosestByPath(FIND_SOURCES)
        val container: StructureContainer? = source?.pos?.findInRange(FIND_STRUCTURES, 1)?.firstOrNull {
            it.structureType == STRUCTURE_CONTAINER
        } as StructureContainer?
        if (source == null && container == null) {
            creep.say("container not found")
            return
        }

        if (container != null) {
            if (creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(container.pos, moveToOptions)
            }
        } else if (source != null) {
            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos, moveToOptions)
            }
        }
        println("$creep is harvesting")
        return
    }

    creep.say("action")
    jobAction()
    return
}

const val ROOM_NAME_HOME = "E18S19"

fun harvestEnergyAndDoJobRemote(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullEnergy()) {
        creep.setWorking(true)
        creep.say("full")
        if (creep.isInTargetRoom(ROOM_NAME_HOME)) {
            jobAction()
        } else {
            creep.moveToTargetRoom(ROOM_NAME_HOME)
        }
        return
    }

    if (creep.isEmptyEnergy() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyEnergy()) "empty" else "fill"
        creep.say(message)

        val roomNameTarget = creep.memory.targetRoomName
        if (creep.isInTargetRoom(roomNameTarget)) {
            println("$creep in target room now")
            val source = creep.room.find(FIND_SOURCES).getOrNull(0)
            if (source == null) {
                creep.say("source not found")
                println("$creep source in room not found harvesting")
                return
            }

            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos, moveToOptions)
            }
        } else {
            creep.moveToTargetRoom(roomNameTarget)
        }

        println("$creep is harvesting remote")
        return
    }

    creep.say("action")
    jobAction()
    return
}
