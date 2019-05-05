package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.targetRoomName
import chentian.utils.createRemoteCreep
import chentian.utils.remoteHarvesters
import screeps.api.FIND_CREEPS
import screeps.api.Game
import screeps.api.Room
import screeps.api.get
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvesterRemote(val room: Room) : CreepStrategy {

    private val creeps by lazy { remoteHarvesters }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (Game.time % 64 != 0) {
            return
        }

        TARGET_ROOM_MAP[room.name]?.forEach { roomName ->
            if (shouldCreate(roomName)) {
                create(spawn, roomName)
            }
        }
    }

    override fun runLoop() {
    }

    private fun shouldCreate(roomName: String): Boolean {
        val creepsHarvesterCount = room.findCreepByRole(CreepStrategyHarvester.CREEP_ROLE_HARVESTER).count()
        if (creepsHarvesterCount <= 3) {
            return false
        }

        val hasEnemy = Game.rooms[roomName]?.find(FIND_CREEPS)?.any { !it.my } ?: false
        if (hasEnemy) {
            return false
        }
        return creeps.count { it.memory.targetRoomName == roomName } < CREEP_PER_TARGET_ROOM
    }

    private fun create(spawn: StructureSpawn, roomName: String) {
        createRemoteCreep(spawn, CREEP_ROLE_HARVESTER_REMOTE, roomName)
    }

    companion object {

        const val CREEP_ROLE_HARVESTER_REMOTE = "harvester-remote"

        private const val CREEP_PER_TARGET_ROOM = 6

        private val TARGET_ROOM_MAP = mapOf(
            "E18S19" to listOf("E17S19")
        )
    }
}
