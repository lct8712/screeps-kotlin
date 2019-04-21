package chentian

import types.base.global.Game
import types.base.global.STRUCTURE_TOWER
import types.base.prototypes.ConstructionSite
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureTower
import types.base.toMap
import types.extensions.lazyPerTick

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
