package chentian.loop

import chentian.GameContext
import chentian.extensions.controlLevel
import chentian.extensions.energy
import chentian.extensions.energyCapacity
import chentian.extensions.memory.repairTargetCountDown
import chentian.extensions.memory.repairTargetId
import chentian.extensions.memory.role
import chentian.strategy.CreepStrategyHealer.Companion.CREEP_ROLE_HEALER
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_MY_CREEPS
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_WALL
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower

/**
 * 塔
 * 攻击 & 回血 & 修理
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

fun runTowerAttack() {
    GameContext.myTowers.forEach { tower ->
        // Attack Enemy
        tower.pos.findClosestByRange(FIND_HOSTILE_CREEPS)?.let {
            val result = tower.attack(it)
            println("$tower is attacking enemy: $result")
            return@forEach
        }

        // Heal Creeps
        tower.room.find(FIND_MY_CREEPS).firstOrNull {
            it.hits < it.hitsMax && it.memory.role != CREEP_ROLE_HEALER
        }?.let {
            val result = tower.heal(it)
            println("$tower is healing strategy: $result")
            return@forEach
        }

        if (tower.store.energy() <= tower.store.energyCapacity() / 3) {
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
    val maxHits = if (target.room.controlLevel() < 6) 1_000_000L else 3_000_000L
    return when (target.structureType) {
        STRUCTURE_RAMPART -> target.hits < target.hitsMax && target.hits < maxHits
        STRUCTURE_WALL -> target.hits < maxHits
        else -> target.hits < target.hitsMax - 1000
    }
}
