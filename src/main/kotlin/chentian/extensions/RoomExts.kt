package chentian.extensions

import chentian.GameContext
import screeps.api.BuildableStructureConstant
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_STORAGE
import screeps.api.structures.Structure

/**
 *
 *
 * @author chentian
 */

fun Room.isFullCarry(): Boolean {
    return energyAvailable == energyCapacityAvailable
}

fun Room.findConstructionsToBuild(type: BuildableStructureConstant): List<ConstructionSite> {
    return find(FIND_CONSTRUCTION_SITES).filter { it.structureType == type }
}

fun Room.findFirstConstructionToBuild(type: BuildableStructureConstant): ConstructionSite? {
    return find(FIND_CONSTRUCTION_SITES)
        .filter { it.structureType == type }
        .firstOrNull { !it.isBuildFinished() }
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

fun Room.isMine(): Boolean {
    return controller?.my == true
}

fun Room.hasStructureStorage(): Boolean {
    return find(FIND_STRUCTURES).any { it.structureType == STRUCTURE_STORAGE }
}

fun Room.extraResourceAmount(): Int {
    return find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }.sumBy { container ->
        container.pos.findInRange(FIND_DROPPED_RESOURCES, 1).firstOrNull()?.amount ?: 0
    }
}

fun Room.controlLevel(): Int {
    return controller?.level ?: 0
}
