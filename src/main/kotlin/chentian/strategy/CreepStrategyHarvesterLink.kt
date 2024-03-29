package chentian.strategy

import chentian.GameContext
import chentian.extensions.controlLevel
import chentian.extensions.memory.containerTargetId
import chentian.extensions.memory.healRemoteRoomName
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.linkIdTo
import chentian.extensions.memory.role
import chentian.extensions.memory.targetLinkId
import chentian.extensions.needUpgrade
import chentian.extensions.transferAllTypeOrMove
import chentian.utils.MOD_16_CREATE_HARVESTER_LINK
import chentian.utils.createCreepName
import chentian.utils.createMoveOptions
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.STRUCTURE_STORAGE
import screeps.api.STRUCTURE_TERMINAL
import screeps.api.WORK
import screeps.api.options
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureStorage
import screeps.api.structures.StructureTerminal
import screeps.utils.unsafe.jsObject

/**
 * 从 link 中吸取能量，升级 controller 或传到 terminal
 *
 * @author chentian
 */
class CreepStrategyHarvesterLink(val room: Room) : CreepStrategy {

    private val creeps = GameContext.creepsHarvesterLink[room.name].orEmpty()

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != MOD_16_CREATE_HARVESTER_LINK) {
            return
        }

        if (room.memory.linkIdTo.isEmpty() || creeps.count() >= MAX_CREEP_COUNT_PER_ROOM) {
            return
        }

        // 在 heal 模式下，有一个 link creep 即可
        if (room.memory.healRemoteRoomName.isNotEmpty() && creeps.isNotEmpty()) {
            return
        }

        create(spawn, room.memory.linkIdTo)
    }

    override fun runLoop() {
        creeps.forEach { creep ->
            harvestEnergyAndDoJob(creep) {
                transferToStorage(creep) || upgradeController(creep) || transferToTerminal(creep)
            }
        }
    }

    private fun create(spawn: StructureSpawn, linkId: String) {
        val workerCount = (spawn.room.energyAvailable - 50) / 100
        if (workerCount < WORKER_BODY_COUNT) {
            return
        }

        val bodyList = mutableListOf(CARRY, MOVE, MOVE).apply {
            for (i in 0 until WORKER_BODY_COUNT) {
                add(WORK)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_HARVESTER_LINK), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_HARVESTER_LINK
                this.homeRoomName = spawn.room.name
                this.targetLinkId = linkId
            }
        })
        println("create new strategy $CREEP_ROLE_HARVESTER_LINK. code: $result, $bodyList")
    }

    /**
     * 传输到 storage
     */
    private fun transferToStorage(creep: Creep): Boolean {
        // RCL >= 6 时，才往 storage 充能
        if (room.controlLevel() < 6) {
            return false
        }

        val storage = room.find(FIND_STRUCTURES)
            .firstOrNull { it.structureType == STRUCTURE_STORAGE }
            as? StructureStorage
            ?: return false

        if (storage.store.getFreeCapacity() == 0) {
            return false
        }

        return transferOrMove(creep, storage)
    }

    /**
     * 升级 Controller
     */
    private fun upgradeController(creep: Creep): Boolean {
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

        return transferOrMove(creep, terminal)
    }

    private fun upgradeOrMove(creep: Creep, controller: StructureController) {
        if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller.pos, MOVE_OPTION)
        }
        println("$creep is upgrading controller")
    }

    private fun transferOrMove(creep: Creep, target: Structure): Boolean {
        return creep.transferAllTypeOrMove(target)
    }

    companion object {

        const val CREEP_ROLE_HARVESTER_LINK = "harvester-link"

        private val MOVE_OPTION = createMoveOptions("#aaff00")

        private const val MAX_CREEP_COUNT_PER_ROOM = 2
        private const val WORKER_BODY_COUNT = 5
    }
}
