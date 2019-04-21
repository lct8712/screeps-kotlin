package chentian.creep

import types.base.prototypes.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
interface CreepStrategy {
    fun tryToCreate(spawn: StructureSpawn)
    fun runLoop()
}
