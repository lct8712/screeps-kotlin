package chentian

import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isWorking
import chentian.extensions.setWorking
import types.base.global.*
import types.base.prototypes.Creep
import types.base.prototypes.MoveToOpts
import types.base.prototypes.Source
import types.base.prototypes.findStructures
import types.base.prototypes.structures.SpawnOptions
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureContainer
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap
import types.extensions.LineStyle
import types.extensions.Style

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

    // 储备不够，等待
    if (spawn.room.energyAvailable < spawn.room.energyCapacityAvailable / 2 && !spawn.isFullEnergy()) {
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

private val moveToOpts = MoveToOpts(visualizePathStyle = Style(stroke = "#00aaff", lineStyle = LineStyle.DOTTED))

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

        val containers = creep.room.findStructures().filter {
            it.structureType == STRUCTURE_CONTAINER
        }.map { it as StructureContainer }
        val minContainer = containers.minBy { it.store.energy }
        val maxContainer = containers.maxBy { it.store.energy }
        if (minContainer != null && maxContainer != null && minContainer.store.energy * 10 < maxContainer.store.energy) {
            if (creep.withdraw(maxContainer, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(maxContainer.pos, moveToOpts)
            }
            println("$creep to full container $maxContainer")
            return
        }

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
                creep.moveTo(container.pos, moveToOpts)
            }
        } else if (source != null) {
            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos, moveToOpts)
            }
        }
        println("$creep is harvesting")
        return
    }

    creep.say("action")
    jobAction()
    return
}
