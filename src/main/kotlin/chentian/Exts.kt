package chentian

import types.base.prototypes.ConstructionSite
import types.base.prototypes.structures.StructureSpawn

fun StructureSpawn.isFullEnergy(): Boolean {
    return energy == energyCapacity
}

fun ConstructionSite.isBuildFinished(): Boolean {
    return progress == progressTotal
}
