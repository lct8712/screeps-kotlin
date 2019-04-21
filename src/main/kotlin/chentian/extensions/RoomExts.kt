package chentian.extensions

import chentian.GameContext
import chentian.isBuildFinished
import types.base.global.BuildableStructureConstant
import types.base.prototypes.*
import types.base.prototypes.structures.Structure

/**
 *
 *
 * @author chentian
 */
fun Room.isFullEnergy(): Boolean {
    return energyAvailable == energyCapacityAvailable
}

fun Room.findFirstConstructionToBuild(type: BuildableStructureConstant): ConstructionSite? {
    return findConstructionSites()
        .filter { (it.structureType == type) }
        .firstOrNull { !it.isBuildFinished() }
}

fun Room.findFirstStructureByType(type: BuildableStructureConstant): Structure? {
    return findStructures()
        .firstOrNull { it.structureType == type }
}

fun Room.findStructureByType(type: BuildableStructureConstant): List<Structure> {
    return findStructures().filter { it.structureType == type }
}

fun Room.findStructureMapByType(type: BuildableStructureConstant): Map<String, Structure> {
    return findStructures()
        .filter { it.structureType == type }
        .map { it.id to it }
        .toMap()
}

fun Room.findCreepByRole(role: String): List<Creep> {
    return GameContext.creeps.values.filter {
        it.room.name == this.name && it.memory.role == role
    }
}
