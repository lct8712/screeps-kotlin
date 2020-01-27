package chentian.creep

import chentian.extensions.memory.creepCreatedCount
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.role
import chentian.extensions.memory.targetFlagName
import chentian.utils.createCreepName
import chentian.utils.creepAttackers
import screeps.api.ATTACK
import screeps.api.CreepMemory
import screeps.api.Flag
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.OK
import screeps.api.Room
import screeps.api.options
import screeps.api.structures.StructureSpawn
import screeps.api.values
import screeps.utils.unsafe.jsObject

/**
 * 进攻
 *   手动创建一个名为 FlagAttack_{RoomName} 的 flag，例如 FlagAttack_E18S19
 *
 * @author chentian
 */
class CreepStrategyAttacker(val room: Room) : CreepStrategy {

    private val targetFlag by lazy {
        Game.flags.values.firstOrNull { flag -> flag.name.contains(room.name) }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn, targetFlag!!)
        }
    }

    override fun runLoop() {
        // 在 runCreepAttack() 中执行
    }

    private fun shouldCreate(): Boolean {
        val flag = targetFlag ?: return false
        return flag.memory.creepCreatedCount < MAX_ATTACKER_COUNT_TOTAL && creepAttackers.size < MAX_ATTACKER_COUNT_AT_SAME_TIME
    }

    private fun create(spawn: StructureSpawn, flag: Flag) {
        val bodyList = mutableListOf(MOVE, MOVE, MOVE, ATTACK, ATTACK, ATTACK)
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_ATTACKER), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_ATTACKER
                this.homeRoomName = spawn.room.name
                this.targetFlagName = flag.name
            }
        })
        if (result == OK) {
            flag.memory.creepCreatedCount++
        }
        println("create new creep $CREEP_ROLE_ATTACKER. code: $result, $bodyList")
    }

    companion object {

        const val CREEP_ROLE_ATTACKER = "attacker"
        const val MAX_ATTACKER_COUNT_AT_SAME_TIME = 4
        const val MAX_ATTACKER_COUNT_TOTAL = 1_000
    }
}
