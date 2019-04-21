package chentian

import types.base.global.FIND_HOSTILE_CREEPS
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.findCreeps

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
