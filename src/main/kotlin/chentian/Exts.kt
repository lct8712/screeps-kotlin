package chentian

import types.base.prototypes.Creep
import types.base.prototypes.structures.StructureSpawn

/**
 * 扩展
 *
 * @author chentian
 */

fun Creep.isFullEnergy(): Boolean {
    return carry.energy == carryCapacity
}

fun StructureSpawn.isFullEnergy(): Boolean {
    return energy == energyCapacity
}
