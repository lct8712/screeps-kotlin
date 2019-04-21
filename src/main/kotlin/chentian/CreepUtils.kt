package chentian

import types.base.global.CARRY
import types.base.global.Game
import types.base.global.MOVE
import types.base.global.WORK
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap

/**
 *
 *
 * @author chentian
 */

const val MAX_CREEP_COUNT = 16

fun createCreepIfNecessary(spawn: StructureSpawn) {
    if (Game.creeps.toMap().count() < MAX_CREEP_COUNT) {
        createSingleCreep(spawn)
    }
}

private fun createSingleCreep(spawn: StructureSpawn) {
    var workerCount = (spawn.room.energyCapacityAvailable - 100) / 100
    val bodyList = mutableListOf(MOVE, CARRY).apply {
        if (workerCount > 2) {
            workerCount--
            add(MOVE)
            add(CARRY)
        }

        for (i in 0 until workerCount) {
            add(WORK)
        }
    }

    val result = spawn.spawnCreep(bodyList.toTypedArray(), "creep_${Game.time}")
    println("create new creep, $bodyList. code: $result")
}
