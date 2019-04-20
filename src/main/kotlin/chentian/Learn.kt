package chentian

import types.base.get
import types.base.global.*
import types.base.prototypes.*
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap

/**
 *
 *
 * @author chentian
 */

const val MAX_CREEP_COUNT = 16
const val MIN_CREEP_COUNT_FOR_CONTROLLER = 6

fun gameLoopChentian() {
    val spawn = Game.spawns["Spawn1"]!!

    if (Game.creeps.toMap().count() < MAX_CREEP_COUNT) {
        val result = spawn.spawnCreep(arrayOf(WORK, WORK, MOVE, CARRY), "creep_${Game.time}")
        println("create new creep, code: $result")
    }

    var controllerCreepCount = 0
    for ((_, creep) in Game.creeps.toMap()) {
        if (controllerCreepCount < MIN_CREEP_COUNT_FOR_CONTROLLER || creep.memory.asDynamic().upgrade != null) {
            upgradeController(creep)
            controllerCreepCount++
            continue
        }

        if (!creep.room.isFullEnergy()) {
            fillEnergy(creep)
            continue
        }

        val extension = creep.room.findConstructionSites()
            .filter { (it.structureType == STRUCTURE_EXTENSION) }.firstOrNull { !it.isBuildFinished() }
        if (extension != null) {
            buildExtension(creep, extension)
            continue
        }

        if (creep.room.find<ConstructionSite>(FIND_CONSTRUCTION_SITES).isNotEmpty()) {
            buildConstruction(creep)
            continue
        }

        upgradeController(creep)
    }
}

private fun fillEnergy(creep: Creep) {
    harvestAndDoJob(creep) {
        val target = creep.room.findStructures()
            .filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
            .map { (it as StructureSpawn) }
            .firstOrNull { it.energy < it.energyCapacity }

        target?.let {
            if (creep.transfer(target, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(target.pos)
            }
        }
        println("$creep is filling energy")
    }
}

private fun buildExtension(creep: Creep, extension: ConstructionSite) {
    harvestAndDoJob(creep) {
        if (creep.build(extension) == ERR_NOT_IN_RANGE) {
            creep.moveTo(extension.pos)
        }
        println("$creep is building extension")
    }
}

private fun upgradeController(creep: Creep) {
    harvestAndDoJob(creep) {
        val controller = creep.room.controller
        if (controller == null) {
            creep.say("controller not found")
            return@harvestAndDoJob
        }

        if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller.pos)
        }
        println("$creep is upgrading controller")
    }
}

private fun buildConstruction(creep: Creep) {
    harvestAndDoJob(creep) {
//        val targetPos = RoomPosition(36, 22, creep.room.name)
        val targetPos = creep.pos
        val construction: ConstructionSite = targetPos.findClosestByPath(FIND_CONSTRUCTION_SITES) ?: return@harvestAndDoJob

        if (creep.build(construction) == ERR_NOT_IN_RANGE) {
            creep.moveTo(construction.pos)
        }
        println("$creep is building construction")
    }
}

private fun harvestAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (!creep.isFullEnergy()) {
        val source: Source? = creep.pos.findClosestByPath(FIND_SOURCES)
        if (source == null) {
            creep.say("source not found")
            return
        }

        if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
            creep.moveTo(source.pos)
        }
        println("$creep is harvesting")
    } else {
        jobAction()
    }
}
