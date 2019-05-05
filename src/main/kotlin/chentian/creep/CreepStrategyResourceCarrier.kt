package chentian.creep

import chentian.extensions.extraResourceAmount
import chentian.extensions.homeRoomName
import chentian.extensions.role
import chentian.extensions.targetRoomName
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

    private val creeps by lazy { resourceCarriers }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (Game.time % 8 != 1) {
            return
        }

        TARGET_ROOM_MAP[room.name]?.forEach { roomName ->
            if (creeps.size >= MAX_RESOURCE_CARRIER_COUNT) {
                return
            }

            val extraResourceAmount = room.extraResourceAmount() + (Game.rooms[roomName]?.extraResourceAmount() ?: 0)
                println("extraResourceAmount: $extraResourceAmount")
            if (creeps.size < extraResourceAmount / 1000) {
                create(spawn, roomName)
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

    companion object {

        const val CREEP_ROLE_RESOURCE_CARRIER = "resource-carrier"
        private const val MAX_RESOURCE_CARRIER_COUNT = 8

        private val TARGET_ROOM_MAP = mapOf(
            "E18S19" to listOf("E18S18")
        )
    }
}
