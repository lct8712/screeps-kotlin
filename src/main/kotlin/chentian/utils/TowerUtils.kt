package chentian.utils

import chentian.GameContext
import screeps.api.FIND_CREEPS
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.STRUCTURE_ROAD
import screeps.api.structures.StructureRoad

/**
 *
 *
 * @author chentian
 */

fun towerAttack() {
    GameContext.towers.forEach { tower ->
        // Attack Enemy
        tower.pos.findClosestByRange(FIND_HOSTILE_CREEPS)?.let {
            val result = tower.attack(it)
            println("$tower is attacking enemy: $result")
            return@forEach
        }

        // Heal Creeps
        tower.room.find(FIND_CREEPS).firstOrNull {
            it.my && it.hits < it.hitsMax
        }?.let {
            val result = tower.heal(it)
            println("$tower is healing creep: $result")
            return@forEach
        }

        // Repair Road
        if (Game.time % 64 == 0) {
            tower.room.find(FIND_STRUCTURES).filter {
                it.structureType == STRUCTURE_ROAD
            }.firstOrNull {
                val road = it as StructureRoad
                road.hits < road.hitsMax - 1000
            }?.let { road ->
                val result = tower.repair(road)
                println("$tower is repairing road: $result")
                return@forEach
            }
        }
    }
}
