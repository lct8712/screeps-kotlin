package chentian.creep

import chentian.GameContext
import chentian.extensions.extraResourceAmount
import chentian.extensions.homeRoomName
import chentian.extensions.role
import chentian.extensions.targetRoomName
import chentian.utils.MOD_16_CREATE_RESOURCE_CARRIER
import chentian.utils.createCreepName
import chentian.utils.resourceCarriers
import screeps.api.CARRY
import screeps.api.CreepMemory
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.get
import screeps.api.options
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */
class CreepStrategyResourceCarrier(val room: Room) : CreepStrategy {

    private val targetRoomIno by lazy { TARGET_ROOM_MAP[room.name] }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != MOD_16_CREATE_RESOURCE_CARRIER) {
            return
        }

        targetRoomIno?.let { targetRoomIno ->
            val creeps = resourceCarriers.filter {
                val creepRoom = it.room.name
                creepRoom == room.name || creepRoom == targetRoomIno.targetRoom
            }

            if (creeps.size >= targetRoomIno.creepCount) {
                return
            }

            var extraResourceAmount = room.extraResourceAmount()
            if (targetRoomIno.targetRoom != room.name) {
                extraResourceAmount += (Game.rooms[targetRoomIno.targetRoom]?.extraResourceAmount() ?: 0)
            }
//            println("extraResourceAmount: $extraResourceAmount")
            if (creeps.size < extraResourceAmount / targetRoomIno.extraResourceAmount) {
                create(spawn, targetRoomIno.targetRoom)
            }
        }
    }

    override fun runLoop() {
    }

    private fun create(spawn: StructureSpawn, roomName: String) {
        val bodyList = mutableListOf(MOVE, MOVE, MOVE, CARRY, CARRY, CARRY)
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_RESOURCE_CARRIER), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_RESOURCE_CARRIER
                this.homeRoomName = spawn.room.name
                this.targetRoomName = roomName
            }
        })
        println("create new creep $CREEP_ROLE_RESOURCE_CARRIER. code: $result, $bodyList")
    }

    private class TargetRoomInfo(
        val targetRoom: String,
        val creepCount: Int,
        val extraResourceAmount: Int
    )

    companion object {

        const val CREEP_ROLE_RESOURCE_CARRIER = "resource-carrier"
        private const val MAX_COUNT_PER_ROOM = 1
        private const val EXTRA_RESOURCE_PER_ROOM = 750

        private val TARGET_ROOM_MAP = mapOf(
            "E18S19" to TargetRoomInfo("E18S19", MAX_COUNT_PER_ROOM, EXTRA_RESOURCE_PER_ROOM),
            "E18S18" to TargetRoomInfo("E18S18", MAX_COUNT_PER_ROOM, EXTRA_RESOURCE_PER_ROOM)
        )
    }
}
