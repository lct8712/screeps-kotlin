package chentian.creep

import chentian.extensions.containerTargetId
import chentian.extensions.findCreepByRole
import chentian.extensions.isFullEnergy
import chentian.extensions.transferTargetId
import chentian.utils.createMoveOptions
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.EnergyContainer
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_SPAWN
import screeps.api.STRUCTURE_TOWER
import screeps.api.keys
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn
import screeps.game.one.findClosest

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room) : CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER)
    private val isFullEnergy = room.isFullEnergy()
    private val towerTargetIdSet = mutableSetOf<String>()

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        towerTargetIdSet.clear()
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        val sourceSize = room.find(FIND_SOURCES).size
        // 最少 2 倍
        if (creeps.size < sourceSize * 2) {
            return true
        }
        // 最多 4 倍
        if (creeps.size > sourceSize * 4) {
            return false
        }

        // 看 Container 容量
        val containers = room.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
        val totalStore = containers.sumBy { (it as StructureContainer).store.energy }
        val totalCapacity = containers.sumBy { (it as StructureContainer).storeCapacity }
        return totalStore.toFloat() / totalCapacity.toFloat() >= 0.75f
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_HARVESTER)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            transferEnergy(creep)
        }
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun transferEnergy(creep: Creep) {
        // 附近有扩展
        if (!isFullEnergy) {
            creep.pos.findInRange(FIND_STRUCTURES, 1)
                .filter { it.structureType == STRUCTURE_EXTENSION }
                .map { it as EnergyContainer }
                .firstOrNull { it.energy < it.energyCapacity }?.let {
                    if (creep.transfer(it as Structure, RESOURCE_ENERGY) == OK) {
                        println("$creep transfer to a near extension")
                        return
                    }
                }
        }

        // 已有 transfer 目标
        Game.getObjectById<Structure>(creep.memory.transferTargetId)?.let { target ->
            if (transferOrMove(creep, target)) {
                return
            }
        }

        STRUCTURE_PRIORITY.forEach { structureType ->
            // 所有可以充能的建筑列表
            val energyStructures = room.find(FIND_STRUCTURES)
                .filter { it.structureType == structureType && !towerTargetIdSet.contains(it.id) }
                .map { it as EnergyContainer }
                .filter { it.energy < it.energyCapacity }
                .map { it as Structure }

            // 找最近的一个建筑去充能
            creep.findClosest(energyStructures)?.let { target ->
                creep.memory.transferTargetId = target.id
                if (transferOrMove(creep, target)) {
                    return
                }
            }
        }

        // 已有 controller 目标
        Game.getObjectById<StructureController>(creep.memory.containerTargetId)?.let { target ->
            upgradeOrMove(creep, target)
            return
        }

        // 去升级 controller
        val controller = creep.room.controller
        if (controller != null) {
            creep.memory.containerTargetId = controller.id
            upgradeOrMove(creep, controller)
        }
    }

    private fun upgradeOrMove(creep: Creep, controller: StructureController) {
        if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller.pos, MOVE_OPTION)
        }
        println("$creep is upgrading controller")
    }

    private fun transferOrMove(creep: Creep, target: Structure): Boolean {
        if (target.structureType == STRUCTURE_TOWER) {
            towerTargetIdSet.add(target.id)
        }

        creep.carry.keys.forEach { resourceType ->
            val transferResult = creep.transfer(target, resourceType)
            when (transferResult) {
                ERR_NOT_IN_RANGE -> {
                    creep.moveTo(target.pos, MOVE_OPTION)
                    println("$creep is filling energy $target")
                    return true
                }
                OK -> {
                    return true
                }
                else -> {
                    println("$creep transfer failed: $transferResult")
                }
            }
        }
        return false
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
