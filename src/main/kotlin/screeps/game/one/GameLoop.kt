package screeps.game.one


import types.base.global.Game
import types.base.global.STRUCTURE_TOWER
import types.base.prototypes.ConstructionSite
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureTower
import types.base.toMap
import types.extensions.lazyPerTick

object Context {
    //built-in
    val creeps: Map<String, Creep> by lazyPerTick { Game.creeps.toMap() }
    //val rooms: Map<String, Room> by lazyPerTick { Game.rooms.toMap() }
    val rooms: Map<String, Room> = Game.rooms.toMap()
    val myStuctures: Map<String, Structure> by lazyPerTick { Game.structures.toMap() }
    val constructionSites: Map<String, ConstructionSite> by lazyPerTick { Game.constructionSites.toMap() }

    //synthesized
    val targets: Map<String, Creep> by lazyPerTick { creepsByTarget() }
    //val towers: List<StructureTower> by lazyPerTick {
    //    myStuctures.values.filter { it.structureType == STRUCTURE_TOWER }.map { it as StructureTower }
    //}
    val towers: List<StructureTower> by lazyPerTick { myStuctures.values.filter { it.structureType == STRUCTURE_TOWER } as List<StructureTower> }

    private fun creepsByTarget(): Map<String, Creep> {
        return Context.creeps.filter { it.value.memory.targetId != null }
            .mapKeys { (_, creep) -> creep.memory.targetId!! }
    }
}


fun gameLoop() = profiled("testmemory") {
    for ((_, room) in Context.rooms) {
        var test: Int by room.memoryOrDefault(0)
        println("memory test=$test")
        test += 1
    }

    for ((_, creep) in Game.creeps.toMap()) {
        println("${creep.name}.memory.count=${creep.memory.magicCounter++}")
    }


}


public fun houseKeeping() {
    js(
        """
        for (var name in Memory.creeps) {
            if (!Game.creeps[name]) {
                delete Memory.creeps[name];
                console.log('Clearing non-existing creep memory:', name);
            }
        }
        """
    )
}
