package chentian.strategy

import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
interface CreepStrategy {
    fun tryToCreate(spawn: StructureSpawn)
    fun runLoop()
}
