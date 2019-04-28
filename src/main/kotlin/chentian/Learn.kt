package chentian

import chentian.extensions.findFirstConstructionToBuild
import chentian.extensions.isBuildFinished
import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isWorking
import chentian.extensions.setWorking
import chentian.utils.createCreepIfNecessary
import chentian.utils.towerAttack
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_TOWER
import screeps.api.Source
import screeps.api.get
import screeps.api.structures.StructureExtension
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower
import screeps.game.one.houseKeeping
import screeps.utils.toMap
import kotlin.collections.component1
import kotlin.collections.component2
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
            var defence = creep.room.findFirstConstructionToBuild(STRUCTURE_RAMPART)
            if (defence == null) {
                defence = creep.room.findFirstConstructionToBuild(STRUCTURE_TOWER)
            }
            if (defence != null) {
                buildConstructionSite(creep, defence)
                defenceCount--
                continue
            }
        }

        val extension = creep.room.findFirstConstructionToBuild(STRUCTURE_EXTENSION)
        if (extension != null) {
            buildConstructionSite(creep, extension)
            continue
        }

        val container = creep.room.findFirstConstructionToBuild(STRUCTURE_CONTAINER)
        if (container != null) {
            buildConstructionSite(creep, container)
            continue
        }

        val construction = creep.room.find(FIND_CONSTRUCTION_SITES).firstOrNull { !it.isBuildFinished() }
        if (construction != null) {
            buildConstructionSite(creep, construction)
            continue
        }

        upgradeController(creep)
    }
}

private fun fillEnergy(creep: Creep) {
    harvestAndDoJob(creep) {
        val target = creep.room.find(FIND_STRUCTURES)
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

