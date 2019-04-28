package chentian

import screeps.api.ConstructionSite
import screeps.api.structures.StructureSpawn

fun StructureSpawn.isFullEnergy(): Boolean {
    return energy == energyCapacity
}

fun ConstructionSite.isBuildFinished(): Boolean {
    return progress == progressTotal
}
