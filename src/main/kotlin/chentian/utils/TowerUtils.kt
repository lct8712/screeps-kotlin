package chentian.utils

import chentian.GameContext
import chentian.creep.CreepStrategyDefenceRepair
import chentian.extensions.controlLevel
import chentian.extensions.repairTargetCountDown
import chentian.extensions.repairTargetId
import screeps.api.FIND_CREEPS
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_WALL
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower

/**
 *
 *
 * @author chentian
 */

private val STRUCTURE_PRIORITY = listOf(
    STRUCTURE_CONTAINER,
    STRUCTURE_ROAD,
    STRUCTURE_RAMPART,
    STRUCTURE_WALL
)

/**
 * 每多少 tick 重新选择目标
 */
private const val REPAIR_TARGET_COUNT_DOWN = 8

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

        if (tower.energy <= tower.energyCapacity / 3) {
            return@forEach
        }

        // Repair only when energy more than half
        val repairTarget = Game.getObjectById<Structure>(tower.room.memory.repairTargetId)
        repairTarget?.let {
            if (needRepair(repairTarget)) {
                val result = tower.repair(it)
                println("$tower is repairing $it, $result")

                if (tower.room.memory.repairTargetCountDown-- <= 0) {
                    tower.room.memory.repairTargetId = ""
                }

                return@forEach
            } else {
                tower.room.memory.repairTargetId = ""
            }
        }

        if (GameContext.timeMod16Result == 4) {
            findStructureToRepair(tower)
        }
    }
}

private fun findStructureToRepair(tower: StructureTower) {
    STRUCTURE_PRIORITY.forEach { structureType ->
        tower.room.find(FIND_STRUCTURES).filter {
            it.structureType == structureType && needRepair(it)
        }.minBy { it.hits }?.let {
            tower.room.memory.repairTargetId = it.id
            tower.room.memory.repairTargetCountDown = REPAIR_TARGET_COUNT_DOWN
            val result = tower.repair(it)
            println("$tower is repairing $it, $result")
            return
        }
    }
}

private fun needRepair(target: Structure): Boolean {
    val maxHits = if (target.room.controlLevel() < 6) 1_000_000L else CreepStrategyDefenceRepair.MAX_HITS_TO_REPAIR
    return when (target.structureType) {
        STRUCTURE_RAMPART -> target.hits < target.hitsMax && target.hits < maxHits
        STRUCTURE_WALL -> target.hits < maxHits
        else -> target.hits < target.hitsMax - 1000
    }
}
