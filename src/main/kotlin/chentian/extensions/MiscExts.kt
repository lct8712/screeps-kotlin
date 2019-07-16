package chentian.extensions

import screeps.api.CONTROLLER_DOWNGRADE_SAFEMODE_THRESHOLD
import screeps.api.CONTROLLER_LEVELS
import screeps.api.ConstructionSite
import screeps.api.size
import screeps.api.structures.StructureController
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

fun StructureController.needUpgrade(): Boolean {
    return level <= CONTROLLER_LEVELS.size || ticksToDowngrade < CONTROLLER_DOWNGRADE_SAFEMODE_THRESHOLD
}
