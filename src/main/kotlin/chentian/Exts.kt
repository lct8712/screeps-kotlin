package chentian

import types.base.prototypes.ConstructionSite
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.structures.StructureSpawn

/**
 * 扩展
 *
 * @author chentian
 */

fun Creep.isFullEnergy(): Boolean {
    return carry.energy == carryCapacity
}

fun Creep.isEmptyEnergy(): Boolean {
    return carry.energy == 0
}

fun StructureSpawn.isFullEnergy(): Boolean {
    return energy == energyCapacity
}

fun Room.isFullEnergy(): Boolean {
    return energyAvailable == energyCapacityAvailable
}

fun ConstructionSite.isBuildFinished(): Boolean {
    return progress == progressTotal
}
