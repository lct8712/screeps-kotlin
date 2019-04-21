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

        with(CreepStrategyMiner(room)) {
            if (shouldCreate()) {
                create(spawn)
            }
            runLoop()
        }

        with(CreepStrategyHarvester(room)) {
            if (shouldCreate()) {
                create(spawn)
            }
            runLoop()
        }
    }
}
