package chentian.creep

import chentian.createSingleCreep
import chentian.extensions.findCreepByRole
import chentian.extensions.findStructureByType
import chentian.harvestEnergyAndDoJob
import types.base.global.ERR_NOT_IN_RANGE
import types.base.global.FIND_SOURCES
import types.base.global.RESOURCE_ENERGY
import types.base.global.STRUCTURE_CONTAINER
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.Source
import types.base.prototypes.findStructures
import types.base.prototypes.structures.StructureExtension
import types.base.prototypes.structures.StructureSpawn
import types.base.prototypes.structures.StructureTower

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room) {

    private val containerMap = room.findStructureByType(STRUCTURE_CONTAINER)
    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER)

    fun shouldCreate(): Boolean {
        return room.find<Source>(FIND_SOURCES).size * 3 > creeps.size
    }

    fun create(spawn: StructureSpawn) {
        createSingleCreep(spawn, CREEP_ROLE_HARVESTER)
    }

    fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            val target = creep.room.findStructures().firstOrNull {
                when (it) {
                    is StructureExtension -> it.energy < it.energyCapacity
                    is StructureSpawn -> it.energy < it.energyCapacity
                    is StructureTower -> it.energy < it.energyCapacity
                    else -> false
                }
            }

            target?.let {
                if (creep.transfer(target, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(target.pos)
                }
            }
            println("$creep is filling energy $target")
        }
    }

    companion object {

        private const val CREEP_ROLE_HARVESTER = "harvester"

    }
}
