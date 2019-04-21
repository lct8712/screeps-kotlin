package chentian

import types.base.global.FIND_HOSTILE_CREEPS
import types.base.global.FIND_MY_CREEPS
import types.base.prototypes.Creep

/**
 *
 *
 * @author chentian
 */

fun towerAttack() {
    GameContext.towers.forEach { tower ->
        tower.pos.findClosestByRange<Creep>(FIND_HOSTILE_CREEPS)?.let {
            tower.attack(it)
            return@forEach
        }
        tower.pos.findClosestByRange<Creep>(FIND_MY_CREEPS)?.let {
            tower.heal(it)
        }
    }
}
