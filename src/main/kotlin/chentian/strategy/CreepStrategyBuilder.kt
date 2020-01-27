package chentian.strategy

import chentian.extensions.memory.buildTargetId
import chentian.extensions.findClosest
import chentian.extensions.findConstructionsToBuild
import chentian.extensions.findCreepByRole
import chentian.extensions.isBuildFinished
import chentian.extensions.memory.role
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.Game
import screeps.api.OK
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_EXTRACTOR
import screeps.api.STRUCTURE_LINK
import screeps.api.STRUCTURE_NUKER
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_STORAGE
import screeps.api.STRUCTURE_TERMINAL
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
                println("$creep transfer to harvester")
                creep.memory.role = CreepStrategyHarvester.CREEP_ROLE_HARVESTER
                return@harvestEnergyAndDoJob
            }

            // 已经有要建造的目标
            Game.getObjectById<ConstructionSite>(creep.memory.buildTargetId)?.let { target ->
                if (target.isBuildFinished()) {
                    creep.memory.buildTargetId = ""
                } else if (tryToBuild(creep, target)) {
                    return@harvestEnergyAndDoJob
                }
                println("$creep build current target failed")
            }

            // 重新选择
            STRUCTURE_PRIORITY.forEach { structureType ->
                val constructionList = room.findConstructionsToBuild(structureType)
                creep.findClosest(constructionList)?.let { target ->
                    creep.memory.buildTargetId = target.id
                    println("$creep change build target to ${target.id}")
                    if (tryToBuild(creep, target)) {
                        return@harvestEnergyAndDoJob
                    }
                }
            }

            println("$creep build failed: no target")
        }
    }

    private fun tryToBuild(creep: Creep, target: ConstructionSite): Boolean {
        return when (creep.build(target)) {
            OK -> {
                true
            }
            ERR_NOT_IN_RANGE -> {
                creep.moveTo(target.pos)
                true
            }
            else -> {
                println("$creep build ${target.structureType} failed at ${target.pos}, result: ${creep.build(target)}")
                false
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
            STRUCTURE_STORAGE,
            STRUCTURE_LINK,
            STRUCTURE_TERMINAL,
            STRUCTURE_NUKER,
            STRUCTURE_WALL,
            STRUCTURE_ROAD
        )
    }
}
