package chentian.creep

import chentian.createNormalCreep
import chentian.extensions.findCreepByRole
import chentian.harvestEnergyAndDoJob
import types.base.global.ERR_NOT_IN_RANGE
import types.base.global.FIND_SOURCES
import types.base.global.FIND_STRUCTURES
import types.base.global.OK
import types.base.global.RESOURCE_ENERGY
import types.base.global.STRUCTURE_CONTAINER
import types.base.global.STRUCTURE_EXTENSION
import types.base.global.STRUCTURE_ROAD
import types.base.global.STRUCTURE_SPAWN
import types.base.global.STRUCTURE_TOWER
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.Source
import types.base.prototypes.findStructures
import types.base.prototypes.structures.EnergyContainingStructure
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureContainer
import types.base.prototypes.structures.StructureRoad
import types.base.prototypes.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room) : CreepStrategy {

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
        val sourceSize = room.find<Source>(FIND_SOURCES).size
        // 最少三倍
        if (creeps.size < sourceSize * 3) {
            println("less then 3")
            return true
        }
        // 最多五倍
        if (creeps.size > sourceSize * 5) {
            println("more then 5")
            return false
        }

        // 看 Container 容量
        val containers = room.findStructures().filter { it.structureType == STRUCTURE_CONTAINER }
        val totalStore = containers.sumBy { (it as StructureContainer).store.energy }
        val totalCapacity = containers.sumBy { (it as StructureContainer).storeCapacity }
        println("$totalStore, $totalCapacity, ${totalStore.toFloat() / totalCapacity.toFloat() > 0.6f}")
        return totalStore.toFloat() / totalCapacity.toFloat() > 0.6f
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_HARVESTER)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            if (repairContainer(creep)) {
                return@harvestEnergyAndDoJob
            }

            if (transferEnergy(creep)) {
                return@harvestEnergyAndDoJob
            }

            if (repairRoad(creep)) {
                return@harvestEnergyAndDoJob
            }

            upgradeController(creep)
        }
    }

    private fun repairContainer(creep: Creep): Boolean {
        val container = creep.pos.findInRange<Structure>(FIND_STRUCTURES, 2).firstOrNull {
            it.structureType == STRUCTURE_CONTAINER
        } as StructureContainer?
        if (container != null && container.hits < container.hitsMax) {
            val repair = creep.repair(container)
            creep.say("repair")
            if (repair != OK) {
                println("repair container failed: $repair")
            }
            return true
        }
        return false
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
                        creep.moveTo(target.pos)
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
        } else {
            println("$creep not found road to repair")
        }
        return false
    }

    private fun upgradeController(creep: Creep) {
        val controller = creep.room.controller
        if (controller != null) {
            if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
                creep.moveTo(controller.pos)
            }
            println("$creep is upgrading controller")
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
