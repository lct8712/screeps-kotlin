package chentian

import chentian.creep.CreepStrategyBuilder
import chentian.creep.CreepStrategyDefenceBuilder
import chentian.creep.CreepStrategyHarvester
import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyMiner
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
