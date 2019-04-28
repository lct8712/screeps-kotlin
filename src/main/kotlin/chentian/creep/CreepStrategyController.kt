package chentian.creep

import chentian.utils.towerAttack
import screeps.api.Game
import screeps.api.get
import screeps.game.one.houseKeeping

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
            CreepStrategyBuilder(room),
            CreepStrategyDefenceBuilder(room),
            CreepStrategyHarvesterRemote(room)
        ).forEach { strategy ->
            strategy.tryToCreate(spawn)
            strategy.runLoop()
        }
    }
}
