package chentian.strategy

import chentian.extensions.memory.claimerRoomName
import chentian.extensions.memory.homeRoomName
import chentian.extensions.isInTargetRoom
import chentian.extensions.moveToTargetRoom
import chentian.extensions.memory.role
import chentian.extensions.memory.targetRoomName
import chentian.utils.createCreepName
import screeps.api.CLAIM
import screeps.api.CreepMemory
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.OK
import screeps.api.Room
import screeps.api.options
import screeps.api.structures.StructureSpawn
import screeps.utils.toMap
import screeps.utils.unsafe.jsObject

/**
 * 占领新房间，开分基地时用一次
 *   1. 手动在原房间设置 room.memory.claimerRoomName
 *   2. 占领成功后，手动在目标房间内放置 spawn
 *
 * @author chentian
 */
class CreepStrategyClaimer(val room: Room) : CreepStrategy {

    private val creeps by lazy {
        Game.creeps.toMap().values.filter { it.memory.role == CREEP_ROLE_CLAIMER }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { creep ->
            if (creep.isInTargetRoom(creep.memory.targetRoomName)) {
                val controller = creep.room.controller ?: return@forEach
                println("$creep ${controller.room} $controller")
                val result = creep.claimController(controller)
                if (result == ERR_NOT_IN_RANGE) {
                    creep.moveTo(controller.pos)
                } else {
                    println("$creep claim controller error. strategy $CREEP_ROLE_CLAIMER. code: $result")
                }
            } else {
                creep.moveToTargetRoom(creep.memory.targetRoomName)
            }
            creep.say("claim")
        }
    }

    private fun shouldCreate(): Boolean {
        return room.memory.claimerRoomName.isNotEmpty()
    }

    private fun create(spawn: StructureSpawn) {
        val bodyList = mutableListOf(CLAIM, MOVE, MOVE, MOVE, MOVE, MOVE)
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_CLAIMER), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_CLAIMER
                this.homeRoomName = spawn.room.name
                this.targetRoomName = room.memory.claimerRoomName
            }
        })
        if (result == OK) {
            room.memory.claimerRoomName = ""
        }
        println("create new strategy $CREEP_ROLE_CLAIMER. code: $result, $bodyList")
    }

    companion object {

        private const val CREEP_ROLE_CLAIMER = "claimer"
    }
}
