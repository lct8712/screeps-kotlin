package chentian.creep

import chentian.createNormalCreep
import chentian.extensions.findCreepByRole
import chentian.extensions.findFirstConstructionToBuild
import chentian.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_TOWER
import screeps.api.STRUCTURE_WALL
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyBuilder(val room: Room): CreepStrategy {

    private val constructionSites = room.find(FIND_CONSTRUCTION_SITES)
    private val creeps = room.findCreepByRole(CREEP_ROLE_BUILDER)

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        return constructionSites.isNotEmpty() && creeps.size < MAX_BUILDER_COUNT
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_BUILDER)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            if (constructionSites.isEmpty()) {
                return@harvestEnergyAndDoJob
            }

            STRUCTURE_PRIORITY.forEach { structureType ->
                room.findFirstConstructionToBuild(structureType)?.let { target ->
                    if (creep.build(target) == ERR_NOT_IN_RANGE) {
                        creep.moveTo(target.pos)
                        println("$creep is building $target")
                        return@harvestEnergyAndDoJob
                    }
                }
            }
        }
    }

    companion object {

        private const val CREEP_ROLE_BUILDER = "builder"
        private const val MAX_BUILDER_COUNT = 2

        private val STRUCTURE_PRIORITY = listOf(
            STRUCTURE_RAMPART,
            STRUCTURE_TOWER,
            STRUCTURE_EXTENSION,
            STRUCTURE_CONTAINER,
            STRUCTURE_WALL,
            STRUCTURE_ROAD
        )
    }
}
