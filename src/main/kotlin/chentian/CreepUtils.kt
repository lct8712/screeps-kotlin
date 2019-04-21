package chentian

import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isWorking
import chentian.extensions.setWorking
import types.base.global.*
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
        createSingleCreep(spawn)
    }
}

fun createCreepName(role: String): String {
    return "creep_${role}_${Game.time}"
}

fun createSingleCreep(spawn: StructureSpawn, role: String = "") {
    var workerCount = (spawn.room.energyAvailable - 100) / 100
    if (workerCount <= 1) {
        return
    }

    val bodyList = mutableListOf(MOVE, CARRY).apply {
        if (workerCount > 2) {
            workerCount--
            add(MOVE)
            add(CARRY)
        }

        for (i in 0 until workerCount) {
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
            } else if (container.hits < container.hitsMax) {
                val repairResult = creep.repair(container)
                if (repairResult != OK) {
                    println("repair failed: $repairResult")
                }
                creep.say("repair")
            }
        } else if (source != null) {
            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos)
            }
        }
        println("harvesting")
        return
    }

    creep.say("action")
    jobAction()
    return
}
