package chentian

import chentian.creep.CreepStrategyBuilder
import chentian.creep.CreepStrategyBuilderRemote
import chentian.creep.CreepStrategyClaimer
import chentian.creep.CreepStrategyDefenceBuilder
import chentian.creep.CreepStrategyHarvester
import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyMiner
import chentian.utils.towerAttack
import screeps.api.Game
import screeps.api.values
import screeps.game.one.houseKeeping

/**
 *
 *
 * @author chentian
 */
object CreepStrategyController {

    fun gameLoop() {
        houseKeeping()
        towerAttack()

        Game.spawns.values.forEach { spawn ->
            val room = spawn.room
            listOf(
                CreepStrategyMiner(room),
                CreepStrategyHarvester(room),
                CreepStrategyBuilder(room),
                CreepStrategyDefenceBuilder(room),
                CreepStrategyHarvesterRemote(room),
                CreepStrategyClaimer(room),
                CreepStrategyBuilderRemote(room)
            ).forEach { strategy ->
                strategy.tryToCreate(spawn)
                strategy.runLoop()
            }
        }
    }
}
