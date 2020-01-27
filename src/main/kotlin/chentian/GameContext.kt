package chentian

import chentian.extensions.findCreepByRole
import chentian.strategy.CreepStrategyMiner
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Room
import screeps.api.STRUCTURE_TOWER
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower
import screeps.api.values
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
    val myRooms: List<Room> by lazyPerTick {
        Game.rooms.values.filter { it.controller?.my == true }
    }
    val myStuctures: Map<String, Structure> by lazyPerTick { Game.structures.toMap() }
    val constructionSites: Map<String, ConstructionSite> by lazyPerTick { Game.constructionSites.toMap() }
    val timeMod16Result: Int by lazyPerTick { Game.time % 16 }

    val myTowers: List<StructureTower> by lazyPerTick {
        myStuctures.values.filter { it.structureType == STRUCTURE_TOWER }.map { it as StructureTower }
    }

    val creepsMiner: Map<String, List<Creep>> by lazyPerTick {
        rooms.values.map { it.name to it.findCreepByRole(CreepStrategyMiner.CREEP_ROLE_MINER) }.toMap()
    }
}
