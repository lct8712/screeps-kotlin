package chentian

import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 *
 *
 * @author chentian
 */
object GameContext {

    val creeps: Map<String, Creep> by lazyPerTick { Game.creeps.toMap() }
    val rooms: Map<String, Room> = Game.rooms.toMap()
    val myStuctures: Map<String, Structure> by lazyPerTick { Game.structures.toMap() }
    val constructionSites: Map<String, ConstructionSite> by lazyPerTick { Game.constructionSites.toMap() }

    //val towers: List<StructureTower> by lazyPerTick {
    //    myStuctures.values.filter { it.structureType == STRUCTURE_TOWER }.map { it as StructureTower }
    //}
    @Suppress("UNCHECKED_CAST")
    val towers: List<StructureTower> by lazyPerTick {
        myStuctures.values.filter { it.structureType == STRUCTURE_TOWER } as List<StructureTower>
    }
}
