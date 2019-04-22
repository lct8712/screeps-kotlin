package chentian

import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isWorking
import chentian.extensions.setWorking
import types.base.global.AcitveBodyPartConstant
import types.base.global.CARRY
import types.base.global.CreepMemory
import types.base.global.ERR_NOT_IN_RANGE
import types.base.global.FIND_SOURCES
import types.base.global.FIND_STRUCTURES
import types.base.global.Game
import types.base.global.MOVE
import types.base.global.OK
import types.base.global.RESOURCE_ENERGY
import types.base.global.STRUCTURE_CONTAINER
import types.base.global.WORK
import types.base.prototypes.Creep
import types.base.prototypes.Source
import types.base.prototypes.structures.SpawnOptions
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureContainer
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap

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

fun createNormalCreep(spawn: StructureSpawn, role: String = "") {
    val partCount = spawn.room.energyAvailable / 300
    if (partCount <= 0) {
        return
    }

    // 每 2 个 work 配 一对 carry & move
    val bodyList = mutableListOf<AcitveBodyPartConstant>().apply {
        for (i in 0 until partCount) {
            add(MOVE)
            add(CARRY)
            add(WORK)
            add(WORK)
        }
    }

    val options = object : SpawnOptions {
        @Suppress("unused")
        override val memory = object : CreepMemory {
            val role = role
        }
    }

    val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(role), options)
    println("create new creep $role. code: $result, $bodyList")
    if (result != OK) {
        Game.notify("create creep error: $result")
    }
}

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

        val source: Source? = creep.pos.findClosestByPath(FIND_SOURCES)
        val container: StructureContainer? = source?.pos?.findInRange<Structure>(FIND_STRUCTURES, 1)?.firstOrNull {
            it.structureType == STRUCTURE_CONTAINER
        } as StructureContainer?
        if (source == null && container == null) {
            creep.say("container not found")
            return
        }

        if (container != null) {
            if (creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(container.pos)
            }
        } else if (source != null) {
            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos)
            }
        }
        println("$creep is harvesting")
        return
    }

    creep.say("action")
    jobAction()
    return
}
