package chentian.strategy

import screeps.api.structures.StructureSpawn

/**
 * 每种 creep 策略
 *
 * @author chentian
 */
interface CreepStrategy {
    fun tryToCreate(spawn: StructureSpawn)
    fun runLoop()
}
