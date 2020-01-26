package chentian.creep

import chentian.extensions.containerTargetId
import chentian.extensions.energy
import chentian.extensions.energyCapacity
import chentian.extensions.findClosest
import chentian.extensions.findCreepByRole
import chentian.extensions.isFullCarry
import chentian.extensions.needUpgrade
import chentian.extensions.transferAllTypeOrMove
import chentian.extensions.transferTargetId
import chentian.utils.createMoveOptions
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_SPAWN
import screeps.api.STRUCTURE_TERMINAL
import screeps.api.STRUCTURE_TOWER
import screeps.api.StoreOwner
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTerminal

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvester(val room: Room) : CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER)
    private val isFullEnergy = room.isFullCarry()
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
        // 最少个数
        if (creeps.size < sourceSize * 1.5) {
            return true
        }
        // 最多个数
        if (creeps.size > sourceSize * 3) {
            return false
        }

        // 看 Container 容量
        val containers = room.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
        val totalStore = containers.sumBy { (it as StructureContainer).store.getUsedCapacity() }
        val totalCapacity = containers.sumBy { (it as StructureContainer).store.getCapacity() }
        return totalStore.toFloat() / totalCapacity.toFloat() >= 0.8f
    }

    private fun create(spawn: StructureSpawn) {
        val forceCreate = creeps.isEmpty()
        createNormalCreep(spawn, CREEP_ROLE_HARVESTER, forceCreate)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJob(creep) {
            transferEnergy(creep)
        }
    }

    private fun transferEnergy(creep: Creep): Boolean {
        return transferToExtension(creep) ||
            transferToStoreOwner(creep) ||
            updateController(creep) ||
            transferToTerminal(creep)
    }

    /**
     * 附近有扩展
     */
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun transferToExtension(creep: Creep): Boolean {
        if (isFullEnergy) {
            return false
        }

        creep.pos.findInRange(FIND_STRUCTURES, 1).filter {
            it.structureType == STRUCTURE_EXTENSION
        }.mapNotNull {
            it as? StoreOwner
        }.firstOrNull {
            it.store.energy() < it.store.energyCapacity()
        }?.let {
            if (creep.transfer(it as Structure, RESOURCE_ENERGY) == OK) {
                println("$creep transfer to a near extension")
                return true
            }
        }
        return false
    }

    /**
     * 给建筑充能
     */
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun transferToStoreOwner(creep: Creep): Boolean {
        Game.getObjectById<Structure>(creep.memory.transferTargetId)?.let { target ->
            if (transferOrMove(creep, target)) {
                return true
            }
        }

        STRUCTURE_PRIORITY.forEach { structureType ->
            // 所有可以充能的建筑列表
            val energyStructures = room.find(FIND_STRUCTURES)
                .filter { it.structureType == structureType && !towerTargetIdSet.contains(it.id) }
                .map { it as StoreOwner }
                .filter { it.store.energy() < it.store.energyCapacity() }
                .map { it as Structure }

            // 找最近的一个建筑去充能
            creep.findClosest(energyStructures)?.let { target ->
                creep.memory.transferTargetId = target.id
                if (transferOrMove(creep, target)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 升级 Controller
     */
    private fun updateController(creep: Creep): Boolean {
        // 已有目标
        Game.getObjectById<StructureController>(creep.memory.containerTargetId)?.let { target ->
            upgradeOrMove(creep, target)
            return true
        }

        // 去升级 controller
        val controller = creep.room.controller
        if (controller?.needUpgrade() == true) {
            creep.memory.containerTargetId = controller.id
            upgradeOrMove(creep, controller)
            return true
        }
        return false
    }

    /**
     * 传输到 terminal
     */
    private fun transferToTerminal(creep: Creep): Boolean {
        val terminal = room.find(FIND_STRUCTURES)
            .firstOrNull { it.structureType == STRUCTURE_TERMINAL }
            as? StructureTerminal
            ?: return false

        if (transferOrMove(creep, terminal)) {
            return true
        }
        return false
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

        return creep.transferAllTypeOrMove(target)
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
