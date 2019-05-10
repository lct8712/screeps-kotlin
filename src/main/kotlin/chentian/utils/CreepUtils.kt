package chentian.utils

import chentian.extensions.containerTargetId
import chentian.extensions.homeRoomName
import chentian.extensions.isEmptyCarry
import chentian.extensions.isFullCarry
import chentian.extensions.isInTargetRoom
import chentian.extensions.isMine
import chentian.extensions.isWorking
import chentian.extensions.moveToTargetRoom
import chentian.extensions.role
import chentian.extensions.setWorking
import chentian.extensions.targetRoomName
import chentian.extensions.transferTargetId
import chentian.extensions.withdrawTargetId
import screeps.api.ActiveBodyPartConstant
import screeps.api.BODYPART_COST
import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_BUSY
import screeps.api.ERR_NAME_EXISTS
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.FIND_TOMBSTONES
import screeps.api.Game
import screeps.api.LINE_STYLE_DOTTED
import screeps.api.MOVE
import screeps.api.MoveToOptions
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.RoomVisual
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.TOP
import screeps.api.WORK
import screeps.api.get
import screeps.api.options
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureSpawn
import screeps.api.value
import screeps.game.one.findClosest
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

val BODY_PART_FOR_REMOTE_CREEP = mutableListOf(MOVE, MOVE, CARRY, CARRY, WORK, WORK)
val BODY_COST_FOR_REMOTE_CREEP = BODY_PART_FOR_REMOTE_CREEP.sumBy { (BODYPART_COST[it])!! }

fun createRemoteCreep(spawn: StructureSpawn, role: String, roomName: String): Boolean {
    if (spawn.room.energyAvailable < BODY_COST_FOR_REMOTE_CREEP) {
        return false
    }

    return doCreateCreep(role, roomName, spawn, BODY_PART_FOR_REMOTE_CREEP)
}

val BODY_PART_FOR_NORMAL_CREEP = listOf(MOVE, CARRY, CARRY, WORK, WORK)
val BODY_COST_FOR_NORMAL_CREEP = BODY_PART_FOR_NORMAL_CREEP.sumBy { (BODYPART_COST[it])!! }

fun createNormalCreep(spawn: StructureSpawn, role: String = "") {
    // 一个小房间，创建最基本的 creep
    if (spawn.room.energyCapacityAvailable < BODY_COST_FOR_NORMAL_CREEP) {
        doCreateCreep(role, "", spawn, mutableListOf(MOVE, CARRY, WORK))
        return
    }

    val partCount = spawn.room.energyAvailable / BODY_COST_FOR_NORMAL_CREEP
    if (partCount < 1) {
        return
    }

    // 储备不够，等待
    if (spawn.room.energyAvailable < spawn.room.energyCapacityAvailable / 2 && !spawn.isFullCarry()) {
        println("spawn: not enough energy")
        return
    }

    // https://screeps.fandom.com/wiki/Creep#Fatigue
    val bodyList = mutableListOf<ActiveBodyPartConstant>().apply {
        for (i in 0 until partCount) {
            addAll(BODY_PART_FOR_NORMAL_CREEP)
        }
    }

    doCreateCreep(role, "", spawn, bodyList)
}

fun createMoveOptions(color: String): MoveToOptions {
    val pathStyle = jsObject<RoomVisual.LineStyle> {
        this.color = color
        this.width = 1.0
        this.opacity = .5
        this.lineStyle = LINE_STYLE_DOTTED
    }
    return object : MoveToOptions {
        override val visualizePathStyle: RoomVisual.LineStyle?
            get() = pathStyle
    }
}

private fun doCreateCreep(
    role: String,
    targetRoomName: String,
    spawn: StructureSpawn,
    bodyList: MutableList<ActiveBodyPartConstant>
): Boolean {
    if (spawn.spawning != null) {
        println("spawning, existing")
        return false
    }

    val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(role), options {
        memory = jsObject<CreepMemory> {
            this.role = role
            this.targetRoomName = targetRoomName
            this.homeRoomName = spawn.room.name
        }
    })
    println("create new creep $role. code: $result, $bodyList")
    if (result != OK && result != ERR_BUSY && result != ERR_NAME_EXISTS) {
        Game.notify("create creep error: ${result.value}")
    }
    return result == OK
}

private val moveToOptions = createMoveOptions("00aaff")

fun harvestEnergyAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullCarry()) {
        creep.memory.withdrawTargetId = ""
        creep.memory.transferTargetId = ""
        creep.memory.containerTargetId = ""
        creep.setWorking(true)
        creep.say("full")
        jobAction()
        return
    }

    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡坟墓上的
        creep.pos.findInRange(FIND_TOMBSTONES, 2).firstOrNull { it.store.energy > 0 }?.let { tombstone ->
            if (creep.withdraw(tombstone, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(tombstone.pos)
            }
            println("$creep is withdrawing tombstone at $tombstone")
            return
        }
        // 捡地上掉的
        creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1).firstOrNull()?.let { resource ->
            if (creep.pickup(resource) == OK) {
                println("$creep is picking up resource at $resource")
                return
            }
        }

        // 已经有目标
        Game.getObjectById<StructureContainer>(creep.memory.withdrawTargetId)?.let { container ->
            tryToWithdraw(creep, container)
            return
        }

        // Container 存量不平衡
        val containers = creep.room.find(FIND_STRUCTURES).filter {
            it.structureType == STRUCTURE_CONTAINER
        }.map { it as StructureContainer }
        val minContainer = containers.minBy { it.store.energy }
        val maxContainer = containers.maxBy { it.store.energy }
        if (minContainer != null && maxContainer != null && minContainer.store.energy * 10 < maxContainer.store.energy) {
            tryToWithdraw(creep, maxContainer)
            creep.memory.withdrawTargetId = maxContainer.id
            println("$creep to full container $maxContainer")
            return
        }

        // 找最近的 container
        val containerList = creep.room.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
        (creep.findClosest(containerList) as StructureContainer?)?.let { container ->
            tryToWithdraw(creep, container)
            creep.memory.withdrawTargetId = container.id
            // 让开采矿的位置
            if (creep.pos == container.pos) {
                creep.move(TOP)
            }
            println("$creep is withdraw container")
            return
        }

        // 找最近的 source
        creep.pos.findClosestByPath(FIND_SOURCES)?.let { source ->
            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos, moveToOptions)
            }
            println("$creep is harvesting")
            return
        }
    }

    creep.say("action")
    jobAction()
    return
}

private fun tryToWithdraw(creep: Creep, maxContainer: StructureContainer) {
    if (creep.withdraw(maxContainer, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
        creep.moveTo(maxContainer.pos, moveToOptions)
    }
}

const val ROOM_NAME_HOME = "E18S19"

fun harvestEnergyAndDoJobRemote(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullCarry()) {
        creep.setWorking(true)
        creep.say("full")
        if (creep.room.isMine()) {
            jobAction()
        } else {
            val homeRoomName = if (creep.memory.homeRoomName.isEmpty()) ROOM_NAME_HOME else creep.memory.homeRoomName
            creep.moveToTargetRoom(homeRoomName)
        }
        return
    }

    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡坟墓上的
        creep.pos.findInRange(FIND_TOMBSTONES, 3).firstOrNull { it.store.energy > 0 }?.let { tombstone ->
            if (creep.withdraw(tombstone, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(tombstone.pos)
            }
            println("$creep is withdrawing tombstone at $tombstone")
            return
        }
        // 捡地上掉的
        creep.pos.findInRange(FIND_DROPPED_RESOURCES, 2).firstOrNull()?.let { resource ->
            if (creep.pickup(resource) == OK) {
                println("$creep is picking up resource at $resource")
                return
            }
        }

        val roomNameTarget = creep.memory.targetRoomName
        val inTargetRoom = creep.isInTargetRoom(roomNameTarget)
        if (inTargetRoom) {
            val sourceList = creep.room.find(FIND_SOURCES)
            val index = creep.name[creep.name.length - 2].toInt() % sourceList.size
            val source = sourceList.getOrNull(index)
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

        println("$creep is harvesting remote, in target room: $inTargetRoom")
        return
    }

    creep.say("action")
    jobAction()
    return
}
