package chentian.loop

import chentian.extensions.findClosest
import chentian.extensions.isInTargetRoom
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.role
import chentian.extensions.memory.targetRoomName
import chentian.extensions.moveAwayFromRoomEdge
import chentian.extensions.moveToTargetRoom
import chentian.extensions.needHeal
import chentian.strategy.CreepStrategyHealer
import chentian.utils.createMoveOptions
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_MY_CREEPS
import screeps.api.Game
import screeps.api.OK
import screeps.api.ScreepsReturnCode
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 * 控制回血的 creeps
 * [CreepStrategyHealer]
 *
 * @author chentian
 */

private val creepHealers: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyHealer.CREEP_ROLE_HEALER }
}

private val MOVE_OPTION = createMoveOptions("#3EA055")
private const val LOW_HIT_PERCENT = 0.6

fun runCreepHealer() {
    creepHealers.forEach { creep ->
        if (creep.isInTargetRoom(creep.memory.targetRoomName)) {
            handleInTargetRoom(creep)
        } else {
            handleInHome(creep)
        }
    }
}

private fun handleInTargetRoom(creep: Creep) {
    // 快没血了，回房间回血
    if (creep.hits <= creep.hitsMax * LOW_HIT_PERCENT) {
        creep.moveToTargetRoom(creep.memory.homeRoomName, MOVE_OPTION)
        return
    }

    // 给自己回血
    if (creep.needHeal()) {
        healAndSay(creep, creep)
        return
    }

    // 看周围有没有需要回血的
    val creepsNeedHeal = creep.room.find(FIND_MY_CREEPS).filter { it.needHeal() }
    creep.findClosest(creepsNeedHeal)?.let { targetCreep ->
        if (healAndSay(creep, targetCreep) == ERR_NOT_IN_RANGE) {
            creep.moveTo(targetCreep, MOVE_OPTION)
        }
        return
    }

    // 提前回血
    healAndSay(creep, creep)
}

private fun handleInHome(creep: Creep) {
    if (creep.hits == creep.hitsMax) {
        creep.moveToTargetRoom(creep.memory.targetRoomName, MOVE_OPTION)
    } else {
        healAndSay(creep, creep)
    }
}

private fun healAndSay(creep: Creep, targetCreep: Creep): ScreepsReturnCode {
    if (creep == targetCreep) {
        creep.moveAwayFromRoomEdge()
    }
    return creep.heal(targetCreep).apply {
        if (this == OK) {
            creep.say("♥")
        }
    }
}
