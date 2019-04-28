package chentian.creep

import chentian.createRemoteCreep
import chentian.extensions.role
import chentian.extensions.targetRoomName
import chentian.harvestEnergyAndDoJobRemote
import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.toMap
import types.extensions.LineStyle
import types.extensions.Style

/**
 *
 *
 * @author chentian
 */
class CreepStrategyHarvesterRemote(val room: Room) : CreepStrategy {

    private val creeps by lazy {
        Game.creeps.toMap().values.filter { it.memory.role == CREEP_ROLE_HARVESTER_REMOTE }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        TARGET_ROOM_LIST.forEach { roomName ->
            if (shouldCreate(roomName)) {
                create(spawn, roomName)
            }
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(roomName: String): Boolean {
        return creeps.count { it.memory.targetRoomName == roomName } < CREEP_PER_TARGET_ROOM
    }

    private fun create(spawn: StructureSpawn, roomName: String) {
        createRemoteCreep(spawn, roomName, CREEP_ROLE_HARVESTER_REMOTE)
    }

    private fun fillEnergy(creep: Creep) {
        harvestEnergyAndDoJobRemote(creep) {
            upgradeController(creep)
        }
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

        private const val CREEP_ROLE_HARVESTER_REMOTE = "harvester-remote"

        private val MOVE_OPTION = MoveToOptions(visualizePathStyle = RoomVisual.Style(stroke = "#aaffaa", lineStyle = RoomVisual.LineStyle.DOTTED))

        private const val CREEP_PER_TARGET_ROOM = 4

        private val TARGET_ROOM_LIST = listOf(
            "E17S19",
            "E18S18"
        )
    }
}
