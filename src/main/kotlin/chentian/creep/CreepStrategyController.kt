package chentian.creep

import chentian.towerAttack
import screeps.game.one.houseKeeping
import types.base.get
import types.base.global.Game

/**
 *
 *
 * @author chentian
 */
object CreepStrategyController {

    fun gameLoop() {
        val spawn = Game.spawns["Spawn1"]!!
        val room = spawn.room

        houseKeeping()
        towerAttack(room)

        listOf(
            CreepStrategyMiner(room),
            CreepStrategyHarvester(room),
            CreepStrategyBuilder(room)
        ).forEach { strategy ->
            strategy.tryToCreate(spawn)
            strategy.runLoop()
        }
    }
}
