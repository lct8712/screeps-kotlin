package chentian

import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.Creep
import screeps.api.Room

/**
 *
 *
 * @author chentian
 */

fun towerAttack(room: Room) {
    GameContext.towers.forEach { tower ->
        tower.pos.findClosestByRange<Creep>(FIND_HOSTILE_CREEPS)?.let {
            tower.attack(it)
            return@forEach
        }

        room.findCreeps().firstOrNull {
            it.my && it.hits < it.hitsMax
        }?.let {
            tower.heal(it)
        }
    }
}
