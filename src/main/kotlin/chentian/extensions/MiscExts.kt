package chentian.extensions

import screeps.api.ConstructionSite
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
fun StructureSpawn.isFullCarry(): Boolean {
    return energy == energyCapacity
}

fun ConstructionSite.isBuildFinished(): Boolean {
    return progress == progressTotal
}
