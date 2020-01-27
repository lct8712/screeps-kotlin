package chentian.utils

import chentian.creep.CreepStrategyAttacker
import chentian.extensions.isInTargetRoom
import chentian.extensions.memory.role
import chentian.extensions.memory.targetFlagName
import chentian.extensions.moveToTargetRoom
import screeps.api.Creep
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_HOSTILE_STRUCTURES
import screeps.api.Game
import screeps.utils.getOrDefault
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 * 控制进攻的 creeps
 *
 * @author chentian
 */

private val attackers: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyAttacker.CREEP_ROLE_ATTACKER }
}

private val MOVE_OPTION = createMoveOptions("#f51b00")

fun runCreepAttack() {
    attackers.forEach { creep ->
        tryToAttack(creep)
    }
}

private fun tryToAttack(creep: Creep) {
    val targetFlag = Game.flags.getOrDefault(creep.memory.targetFlagName, null) ?: return
    val targetRoomName = targetFlag.pos.roomName

    // 进入目标房间
//    println("roomName: ${targetFlag.pos} ${targetFlag.pos.roomName} $targetRoomName")
    if (!creep.isInTargetRoom(targetRoomName)) {
        creep.moveToTargetRoom(targetRoomName, MOVE_OPTION)
        return
    }

    // 附近有敌方 creep
    targetFlag.pos.findInRange(FIND_HOSTILE_CREEPS, 1).getOrNull(0)?.let { targetCreep ->
        creep.attack(targetCreep)
        return
    }

    // 走到 flag 附近
    if (!targetFlag.pos.isNearTo(targetFlag.pos)) {
        creep.moveTo(targetFlag.pos, MOVE_OPTION)
        return
    }

    // 攻击离 flag 最近的目标
    targetFlag.pos.findClosestByRange(FIND_HOSTILE_STRUCTURES)?.let { structure ->
        creep.attack(structure)
        return
    }

    creep.say("error")
}
