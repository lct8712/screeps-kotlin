package chentian

import chentian.loop.runBuilderRemote
import chentian.loop.runCreepAttack
import chentian.loop.runLinkTransfer
import chentian.loop.runRemoteHarvesters
import chentian.loop.runResourceCarriers
import chentian.loop.runSellEnergy
import chentian.loop.runTowerAttack
import chentian.strategy.CreepStrategyAttacker
import chentian.strategy.CreepStrategyBuilder
import chentian.strategy.CreepStrategyBuilderRemote
import chentian.strategy.CreepStrategyBuilderSpawn
import chentian.strategy.CreepStrategyClaimer
import chentian.strategy.CreepStrategyHarvester
import chentian.strategy.CreepStrategyHarvesterLink
import chentian.strategy.CreepStrategyHarvesterRemote
import chentian.strategy.CreepStrategyMiner
import chentian.strategy.CreepStrategyResourceCarrier
import chentian.utils.Stats
import chentian.utils.runHouseKeeping
import screeps.api.Game
import screeps.api.values

/**
 *
 *
 * @author chentian
 */
object CreepStrategyController {

    fun gameLoop() {
        Stats.tickStarts()

        println("Game Loop Start, mod16: ${GameContext.timeMod16Result}")

        Game.spawns.values.forEach { spawn ->
            val room = spawn.room
            Stats.write(room)
            println("Room $room")

            listOf(
                CreepStrategyMiner(room),
                CreepStrategyHarvesterLink(room),
                CreepStrategyHarvester(room),
                CreepStrategyBuilder(room),
                CreepStrategyHarvesterRemote(room),
                CreepStrategyClaimer(room),
                CreepStrategyBuilderSpawn(room),
                CreepStrategyBuilderRemote(room),
                CreepStrategyResourceCarrier(room),
                CreepStrategyAttacker(room)
            ).forEach { strategy ->
                strategy.tryToCreate(spawn)
                strategy.runLoop()
            }
        }

        runTowerAttack()
        runCreepAttack()
        runLinkTransfer()
        runSellEnergy()
        runRemoteHarvesters()
        runResourceCarriers()
        runBuilderRemote()

        runHouseKeeping()

        Stats.tickEnds()
    }
}
