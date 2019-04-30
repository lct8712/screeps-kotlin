package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.isFullEnergy
import chentian.utils.createMoveOptions
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.EnergyContainer
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_SPAWN
import screeps.api.STRUCTURE_TOWER
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureRoad
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room) : CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER)
    private val isFullEnergy = room.isFullEnergy()

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        val sourceSize = room.find(FIND_SOURCES).size
        // 最少 2 倍
        if (creeps.size < sourceSize * 2) {
            println("less then 3")
            return true
        }
        // 最多 4 倍
        if (creeps.size > sourceSize * 4) {
            println("more then 5")
            return false
        }

        // 看 Container 容量
        val containers = room.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
        val totalStore = containers.sumBy { (it as StructureContainer).store.energy }
        val totalCapacity = containers.sumBy { (it as StructureContainer).storeCapacity }
        return totalStore.toFloat() / totalCapacity.toFloat() > 0.6f
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_HARVESTER)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            if (transferEnergy(creep)) {
                return@harvestEnergyAndDoJob
            }

            if (repairContainer(creep)) {
                return@harvestEnergyAndDoJob
            }

            if (repairRoad(creep)) {
                return@harvestEnergyAndDoJob
            }

            upgradeController(creep)
        }
    }

    private fun repairContainer(creep: Creep): Boolean {
        val container = creep.pos.findInRange(FIND_STRUCTURES, 2).firstOrNull {
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
        if (!isFullEnergy) {
            creep.pos.findInRange<Structure>(FIND_STRUCTURES, 1)
                .filter { it.structureType == STRUCTURE_EXTENSION }
                .map { it as EnergyContainer }
                .firstOrNull { it.energy < it.energyCapacity }?.let {
                    if (creep.transfer(it as Structure, RESOURCE_ENERGY) == OK) {
                        println("$creep transfer to a near extension")
                        return true
                    }
                }
        }

        STRUCTURE_PRIORITY.forEach { structureType ->
            room.find(FIND_STRUCTURES)
                .filter { it.structureType == structureType }
                .map { it as EnergyContainer }
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
        val road = creep.pos.findInRange(FIND_STRUCTURES, 2).filter {
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

        const val CREEP_ROLE_HARVESTER = "harvester"

        private val MOVE_OPTION = createMoveOptions("#aaff00")

        private val STRUCTURE_PRIORITY = listOf(
            STRUCTURE_EXTENSION,
            STRUCTURE_SPAWN,
            STRUCTURE_TOWER
        )
    }
}
