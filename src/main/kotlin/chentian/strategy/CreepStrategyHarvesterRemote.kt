package chentian.strategy

import chentian.GameContext
import chentian.extensions.findCreepByRole
import chentian.extensions.memory.targetRoomName
import chentian.extensions.needUpgrade
import chentian.loop.creepRemoteHarvesters
import chentian.utils.MOD_16_CREATE_HARVESTER_REMOTE
import chentian.utils.createRemoteCreep
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.Game
import screeps.api.Room
import screeps.api.get
import screeps.api.structures.StructureSpawn

/**
 * 去隔壁房间采矿，升级本房间的 controller
 *
 * @author chentian
 */
class CreepStrategyHarvesterRemote(val room: Room) : CreepStrategy {

    private val creeps by lazy { creepRemoteHarvesters }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != MOD_16_CREATE_HARVESTER_REMOTE) {
            return
        }

        if (room.controller?.needUpgrade() != true) {
            return
        }

        TARGET_ROOM_MAP[room.name]?.forEach { remoteRoomName ->
            if (shouldCreate(remoteRoomName)) {
                create(spawn, remoteRoomName)
            }
        }
    }

    override fun runLoop() {
        // 在 runRemoteHarvesters() 中执行
    }

    private fun shouldCreate(roomName: String): Boolean {
        val creepsHarvesterCount = room.findCreepByRole(CreepStrategyHarvester.CREEP_ROLE_HARVESTER).count()
        if (creepsHarvesterCount <= 3) {
            return false
        }

        val hasEnemy = (Game.rooms[roomName]?.find(FIND_HOSTILE_CREEPS)?.size ?: 0) > 0
        if (hasEnemy) {
            return false
        }
        return creeps.count { it.memory.targetRoomName == roomName } <= CREEP_PER_TARGET_ROOM
    }

    private fun create(spawn: StructureSpawn, roomName: String) {
        createRemoteCreep(spawn, CREEP_ROLE_HARVESTER_REMOTE, roomName)
    }

    companion object {

        const val CREEP_ROLE_HARVESTER_REMOTE = "harvester-remote"

        private const val CREEP_PER_TARGET_ROOM = 7

        private val TARGET_ROOM_MAP = mapOf(
//            "E18S19" to listOf("E17S19"),
            "E18S18" to listOf("E17S18"),
            "W8N3" to listOf("W8N2"),
            "W7N2" to listOf("W8N2")
//            "W9N8" to listOf("W8N8")
        )
    }
}
