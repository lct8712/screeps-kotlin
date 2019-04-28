package chentian.extensions

import chentian.GameContext
import screeps.api.*
import screeps.api.structures.Structure

/**
 *
 *
 * @author chentian
 */
fun Room.isFullEnergy(): Boolean {
    return energyAvailable == energyCapacityAvailable
}

fun Room.findFirstConstructionToBuild(type: BuildableStructureConstant): ConstructionSite? {
    return find(FIND_CONSTRUCTION_SITES)
        .filter { (it.structureType == type) }
        .firstOrNull { !it.isBuildFinished() }
}

fun Room.findFirstStructureByType(type: BuildableStructureConstant): Structure? {
    return find(FIND_STRUCTURES)
        .firstOrNull { it.structureType == type }
}

fun Room.findStructureByType(type: BuildableStructureConstant): List<Structure> {
    return find(FIND_STRUCTURES).filter { it.structureType == type }
}

fun Room.findStructureMapByType(type: BuildableStructureConstant): Map<String, Structure> {
    return find(FIND_STRUCTURES)
        .filter { it.structureType == type }
        .map { it.id to it }
        .toMap()
}

fun Room.findCreepByRole(role: String): List<Creep> {
    return GameContext.creeps.values.filter {
        it.room.name == this.name && it.memory.role == role
    }
}
