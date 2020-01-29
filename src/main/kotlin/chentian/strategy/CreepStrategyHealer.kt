package chentian.strategy

import chentian.GameContext
import chentian.extensions.memory.healRemoteRoomName
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.role
import chentian.extensions.memory.targetRoomName
import chentian.utils.createCreepName
import screeps.api.ActiveBodyPartConstant
import screeps.api.BODYPART_COST
import screeps.api.CreepMemory
import screeps.api.HEAL
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.TOUGH
import screeps.api.get
import screeps.api.options
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 * 回血
 *   - 去其他房间等着 tower 打
 *   - 快没血的时候回自己房间
 *
 * 需要手动在源房间设置 healRemoteRoomName
 *
 * @author chentian
 */
class CreepStrategyHealer(val room: Room) : CreepStrategy {

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        // 在 runCreepHelper() 中执行
    }

    private fun shouldCreate(): Boolean {
        if (room.memory.healRemoteRoomName.isEmpty()) {
            return false
        }

        val creepsSize = GameContext.creeps.values.count {
            it.memory.role == CREEP_ROLE_HEALER && it.memory.targetRoomName == room.memory.healRemoteRoomName
        }
        return creepsSize < MAX_HEALER_COUNT_AT_SAME_TIME
    }

    private fun create(spawn: StructureSpawn) {
        val bodyCost = BODYPART_COST[TOUGH]!! * 5 + BODYPART_COST[MOVE]!! + BODYPART_COST[HEAL]!!
        val healerBodyParCount = spawn.room.energyAvailable / bodyCost
        if (healerBodyParCount < MIN_HEALER_BODY_PART_COUNT) {
            return
        }

        val bodyList = mutableListOf<ActiveBodyPartConstant>().apply {
            for (i in 0 until healerBodyParCount) {
                addAll(listOf(TOUGH, TOUGH, TOUGH, TOUGH, TOUGH))
            }
            for (i in 0 until healerBodyParCount) {
                add(MOVE)
            }
            for (i in 0 until healerBodyParCount) {
                add(HEAL)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_HEALER), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_HEALER
                this.homeRoomName = spawn.room.name
                this.targetRoomName = room.memory.healRemoteRoomName
            }
        })
        println("create new strategy ${CREEP_ROLE_HEALER}. code: $result, $bodyList")
    }

    companion object {

        const val CREEP_ROLE_HEALER = "healer"

        const val MIN_HEALER_BODY_PART_COUNT = 5
        const val MAX_HEALER_COUNT_AT_SAME_TIME = 2
    }
}
