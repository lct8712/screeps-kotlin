package chentian

import types.base.get
import types.base.global.*
import types.base.prototypes.ConstructionSite
import types.base.prototypes.Creep
import types.base.prototypes.RoomPosition
import types.base.prototypes.Source
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap

/**
 *
 *
 * @author chentian
 */

const val MAX_CREEP_COUNT = 20

fun gameLoopChentian() {
    val spawn = Game.spawns["Spawn1"]!!

    if (Game.creeps.toMap().count() < MAX_CREEP_COUNT) {
        val result = spawn.spawnCreep(arrayOf(WORK, WORK, MOVE, CARRY), "creep_${Game.time}")
        println("create new creep, code: $result")
    }

    for ((_, creep) in Game.creeps.toMap()) {
        when {
            !spawn.isFullEnergy() -> transferToSpawn(creep, spawn)
            creep.room.find<ConstructionSite>(FIND_CONSTRUCTION_SITES).isNotEmpty() -> buildConstruction(creep)
            spawn.isFullEnergy() -> upgradeController(creep)
        }
    }
}

private fun transferToSpawn(creep: Creep, spawn: StructureSpawn) {
    harvestAndDoJob(creep) {
        if (creep.transfer(spawn, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn.pos)
        }
        println("$creep is transferring to spawn")
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
        val targetPos = RoomPosition(36, 22, creep.room.name)
//        val targetPos = creep.pos
        val construction: ConstructionSite = targetPos.findClosestByPath(FIND_CONSTRUCTION_SITES) ?: return@harvestAndDoJob

        if (creep.build(construction) == ERR_NOT_IN_RANGE) {
            creep.moveTo(construction.pos)
        }
        println("$creep is building construction")
    }
}

private fun harvestAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullEnergy()) {
        jobAction()
    } else {
        val source: Source? = creep.pos.findClosestByPath(FIND_SOURCES)
        if (source == null) {
            creep.say("source not found")
            return
        }

        if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
            creep.moveTo(source.pos)
        }
        println("$creep is harvesting")
    }
}
