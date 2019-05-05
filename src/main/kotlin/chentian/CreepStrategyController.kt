package chentian

import chentian.creep.CreepStrategyBuilder
import chentian.creep.CreepStrategyBuilderRemote
import chentian.creep.CreepStrategyClaimer
import chentian.creep.CreepStrategyDefenceRepair
import chentian.creep.CreepStrategyHarvester
import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyMiner
import chentian.creep.CreepStrategyResourceCarrier
import chentian.utils.runRemoteHarvesters
import chentian.utils.runResourceCarriers
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

        println("Game Loop Start")
        Game.spawns.values.forEach { spawn ->
            val room = spawn.room
            println("Room $room")

            listOf(
                CreepStrategyMiner(room),
                CreepStrategyHarvester(room),
                CreepStrategyBuilder(room),
                CreepStrategyDefenceRepair(room),
                CreepStrategyHarvesterRemote(room),
                CreepStrategyClaimer(room),
                CreepStrategyBuilderRemote(room),
                CreepStrategyResourceCarrier(room)
            ).forEach { strategy ->
                strategy.tryToCreate(spawn)
                strategy.runLoop()
            }
        }

        runRemoteHarvesters()
        runResourceCarriers()
    }
}
