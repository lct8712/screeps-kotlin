package chentian.utils

import chentian.GameContext
import screeps.api.FIND_CREEPS
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.Room

/**
 *
 *
 * @author chentian
 */

fun towerAttack(room: Room) {
    GameContext.towers.forEach { tower ->
        tower.pos.findClosestByRange(FIND_HOSTILE_CREEPS)?.let {
            tower.attack(it)
            return@forEach
        }

        room.find(FIND_CREEPS).firstOrNull {
            it.my && it.hits < it.hitsMax
        }?.let {
            tower.heal(it)
        }
    }
}
