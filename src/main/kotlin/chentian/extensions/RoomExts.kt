package chentian.extensions

import chentian.GameContext
import screeps.api.BuildableStructureConstant
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.FIND_MY_CONSTRUCTION_SITES
import screeps.api.FIND_STRUCTURES
import screeps.api.Room
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
    return find(FIND_MY_CONSTRUCTION_SITES)
        .filter { (it.structureType == type) }
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
    return GameContext.myRooms.containsKey(name)
}
