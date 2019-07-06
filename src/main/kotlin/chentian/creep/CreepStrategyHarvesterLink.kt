package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.isEmptyCarry
import chentian.extensions.isWorking
import chentian.extensions.role
import chentian.extensions.setWorking
import chentian.extensions.targetLinkId
import chentian.utils.TARGET_ROOM_LINK
import chentian.utils.createCreepName
import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.WORK
import screeps.api.options
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvesterLink(val room: Room) : CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER_LINK)

    override fun tryToCreate(spawn: StructureSpawn) {
//        if (GameContext.timeMod16Result != 6) {
//            return
//        }

        getToLinkId()?.let { linkId ->
            create(spawn, linkId)
        }
    }

    override fun runLoop() {
        creeps.forEach { upgradeController(it) }
    }

    private fun getToLinkId(): String? {
        TARGET_ROOM_LINK.forEach { roomLinkInfo ->
            if (roomLinkInfo.targetRoom != room.name) {
                return@forEach
            }

            return if (creeps.count() < MAX_CREEP_COUNT_PER_ROOM) roomLinkInfo.toLinkId else null
        }
        return null
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
                this.targetLinkId = linkId
            }
        })
        println("create new creep $CREEP_ROLE_HARVESTER_LINK. code: $result, $bodyList")
    }

    private fun upgradeController(creep: Creep) {
        val targetLink = Game.getObjectById<StructureLink>(creep.memory.targetLinkId) ?: return
        if (!creep.isWorking()) {
            if (!targetLink.pos.inRangeTo(creep.pos, 1)) {
                creep.moveTo(targetLink.pos)
                println("$creep is moving to link")
                return
            }

            creep.setWorking(true)
        }

        // 从 Link 中充能
        if (creep.isEmptyCarry()) {
            creep.withdraw(targetLink, RESOURCE_ENERGY)
            println("$creep is withdraw from link")
        }

        // 升级 controller
        val controller = creep.room.controller ?: return
        creep.upgradeController(controller)
        println("$creep is upgrading controller")
        return
    }

    companion object {

        const val CREEP_ROLE_HARVESTER_LINK = "harvester-link"
        private const val MAX_CREEP_COUNT_PER_ROOM = 1
        private const val WORKER_BODY_COUNT = 5
    }
}
