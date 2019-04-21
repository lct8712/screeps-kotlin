package chentian

import chentian.extensions.*
import screeps.game.one.houseKeeping
import types.base.get
import types.base.global.*
import types.base.prototypes.*
import types.base.prototypes.structures.StructureExtension
import types.base.prototypes.structures.StructureSpawn
import types.base.prototypes.structures.StructureTower
import types.base.toMap
import kotlin.math.min

/**
 *
 *
 * @author chentian
 */

const val MIN_CREEP_COUNT_FOR_CONTROLLER = 3
const val MIN_CREEP_COUNT_FOR_DEFENCE = 2

fun gameLoopChentianLearn() {
    val spawn = Game.spawns["Spawn1"]!!

    houseKeeping()
    towerAttack(spawn.room)
    createCreepIfNecessary(spawn)

    val creeps = Game.creeps.toMap()
    var controllerCreepCount = min(MIN_CREEP_COUNT_FOR_CONTROLLER, creeps.size / 2)
    var defenceCount = min(MIN_CREEP_COUNT_FOR_DEFENCE, creeps.size / 2)
    for ((_, creep) in creeps) {
        if (controllerCreepCount > 0 || creep.memory.asDynamic().upgrade != null) {
            upgradeController(creep)
            controllerCreepCount--
            continue
        }

        if (!creep.room.isFullEnergy()) {
            fillEnergy(creep)
            continue
        }

        if (defenceCount > 0) {
            var defence = creep.room.findConstructionToBuild(STRUCTURE_RAMPART)
            if (defence == null) {
                defence = creep.room.findConstructionToBuild(STRUCTURE_TOWER)
            }
            if (defence != null) {
                buildConstructionSite(creep, defence)
                defenceCount--
                continue
            }
        }

        val extension = creep.room.findConstructionToBuild(STRUCTURE_EXTENSION)
        if (extension != null) {
            buildConstructionSite(creep, extension)
            continue
        }

        val container = creep.room.findConstructionToBuild(STRUCTURE_CONTAINER)
        if (container != null) {
            buildConstructionSite(creep, container)
            continue
        }

        val construction = creep.room.findConstructionSites().firstOrNull { !it.isBuildFinished() }
        if (construction != null) {
            buildConstructionSite(creep, construction)
            continue
        }

        upgradeController(creep)
    }
}

private fun fillEnergy(creep: Creep) {
    harvestAndDoJob(creep) {
        val target = creep.room.findStructures()
            .firstOrNull {
                when (it) {
                    is StructureTower -> it.energy < it.energyCapacity
                    is StructureExtension -> it.energy < it.energyCapacity
                    is StructureSpawn -> it.energy < it.energyCapacity
                    else -> false
                }
            }

        target?.let {
            if (creep.transfer(target, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(target.pos)
            }
        }
        println("$creep is filling energy")
    }
}

private fun buildConstructionSite(creep: Creep, construction: ConstructionSite) {
    harvestAndDoJob(creep) {
        if (creep.build(construction) == ERR_NOT_IN_RANGE) {
            creep.moveTo(construction.pos)
        }
        println("$creep is building ${construction.structureType}")
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

private fun harvestAndDoJob(creep: Creep, jobAction: () -> Unit) {
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
        if (source == null) {
            creep.say("source not found")
            return
        }

        if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
            creep.moveTo(source.pos)
        }
        println("$creep is harvesting")
        return
    }

    creep.say("action")
    jobAction()
    return
}

