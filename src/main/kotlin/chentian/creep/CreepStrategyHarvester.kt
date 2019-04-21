package chentian.creep

import chentian.createSingleCreep
import chentian.extensions.findCreepByRole
import chentian.harvestEnergyAndDoJob
import types.base.global.*
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.Source
import types.base.prototypes.findStructures
import types.base.prototypes.structures.EnergyContainingStructure
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room): CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER)

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        return room.find<Source>(FIND_SOURCES).size * 3 > creeps.size
    }

    private fun create(spawn: StructureSpawn) {
        createSingleCreep(spawn, CREEP_ROLE_HARVESTER)
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            STRUCTURE_PRIORITY.forEach { structureType ->
                room.findStructures()
                    .filter { it.structureType == structureType }
                    .map { it as EnergyContainingStructure }
                    .firstOrNull { it.energy < it.energyCapacity }?.let { target ->
                        val transferResult = creep.transfer(target as Structure, RESOURCE_ENERGY)
                        if (transferResult == ERR_NOT_IN_RANGE) {
                            creep.moveTo(target.pos)
                            println("$creep is filling energy $target")
                            return@harvestEnergyAndDoJob
                        } else if (transferResult != OK) {
                            println("$creep transfer failed: $transferResult")
                        }
                    }
            }

            val controller = creep.room.controller
            if (controller != null) {
                if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(controller.pos)
                }
                println("$creep is upgrading controller")
            }
        }
    }

    companion object {

        private const val CREEP_ROLE_HARVESTER = "harvester"

        private val STRUCTURE_PRIORITY = listOf(
            STRUCTURE_EXTENSION,
            STRUCTURE_SPAWN,
            STRUCTURE_TOWER
        )
    }
}
