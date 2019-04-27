package chentian.creep

import chentian.createFastCreep
import chentian.extensions.role
import chentian.harvestEnergyAndDoJobRemote
import types.base.global.*
import types.base.prototypes.Creep
import types.base.prototypes.MoveToOpts
import types.base.prototypes.Room
import types.base.prototypes.findStructures
import types.base.prototypes.structures.EnergyContainingStructure
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureRoad
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap
import types.extensions.LineStyle
import types.extensions.Style

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvesterRemote(val room: Room) : CreepStrategy {

    private val creeps by lazy {
        Game.creeps.toMap().values.filter { it.memory.role == CREEP_ROLE_HARVESTER_REMOTE }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        return creeps.isEmpty()
    }

    private fun create(spawn: StructureSpawn) {
        createFastCreep(spawn, CREEP_ROLE_HARVESTER_REMOTE)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJobRemote(creep) {
            if (transferEnergy(creep)) {
                return@harvestEnergyAndDoJobRemote
            }

            if (repairRoad(creep)) {
                return@harvestEnergyAndDoJobRemote
            }

            upgradeController(creep)
        }
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun transferEnergy(creep: Creep): Boolean {
        STRUCTURE_PRIORITY.forEach { structureType ->
            room.findStructures()
                .filter { it.structureType == structureType }
                .map { it as EnergyContainingStructure }
                .firstOrNull { it.energy < it.energyCapacity }?.let { target ->
                    val transferResult = creep.transfer(target as Structure, RESOURCE_ENERGY)
                    if (transferResult == ERR_NOT_IN_RANGE) {
                        creep.moveTo(target.pos, MOVE_OPTION)
                        println("$creep is filling energy $target")
                        return true
                    } else if (transferResult != OK) {
                        println("$creep transfer failed: $transferResult")
                    }
                }
        }
        return false
    }

    private fun repairRoad(creep: Creep): Boolean {
        val road = creep.pos.findInRange<Structure>(FIND_STRUCTURES, 2).filter {
            it.structureType == STRUCTURE_ROAD
        }.firstOrNull {
            val road = it as StructureRoad
            road.hits < road.hitsMax
        } as StructureRoad?
        if (road != null && road.hits < road.hitsMax) {
            val repair = creep.repair(road)
            creep.say("repair")
            if (repair != OK) {
                println("$creep repair road failed: $repair")
            }
            return true
        }
        return false
    }

    private fun upgradeController(creep: Creep) {
        val controller = creep.room.controller
        if (controller != null) {
            if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
                creep.moveTo(controller.pos, MOVE_OPTION)
            }
            println("$creep is upgrading controller")
        }
    }

    companion object {

        private const val CREEP_ROLE_HARVESTER_REMOTE = "harvester-remote"

        private val MOVE_OPTION = MoveToOpts(visualizePathStyle = Style(stroke = "#aaff00", lineStyle = LineStyle.DOTTED))

        private val STRUCTURE_PRIORITY = listOf(
            STRUCTURE_EXTENSION,
            STRUCTURE_SPAWN,
            STRUCTURE_TOWER
        )
    }
}
