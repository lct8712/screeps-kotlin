package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.findFirstConstructionToBuild
import chentian.extensions.role
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.OK
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_EXTRACTOR
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_TOWER
import screeps.api.STRUCTURE_WALL
import screeps.api.structures.StructureSpawn
import kotlin.math.max

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
        creeps.forEach { buildStructure(it) }
    }

    private fun shouldCreate(): Boolean {
        val maxCount = max(MAX_BUILDER_COUNT, constructionSites.size / 6)
        return constructionSites.isNotEmpty() && creeps.size < maxCount
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_BUILDER)
    }

    private fun buildStructure(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            if (constructionSites.isEmpty()) {
                // 转换为 Harvester
                creep.memory.role = CreepStrategyHarvester.CREEP_ROLE_HARVESTER
                return@harvestEnergyAndDoJob
            }

            STRUCTURE_PRIORITY.forEach { structureType ->
                room.findFirstConstructionToBuild(structureType)?.let { target ->
                    val result = creep.build(target)
                    println("$creep is building ${target.structureType} at ${target.pos}, result: $result")
                    if (result == ERR_NOT_IN_RANGE) {
                        creep.moveTo(target.pos)
                    }
                    if (result == OK || result == ERR_NOT_IN_RANGE) {
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
            STRUCTURE_TOWER,
            STRUCTURE_EXTENSION,
            STRUCTURE_CONTAINER,
            STRUCTURE_EXTRACTOR,
            STRUCTURE_RAMPART,
            STRUCTURE_WALL,
            STRUCTURE_ROAD
        )
    }
}
