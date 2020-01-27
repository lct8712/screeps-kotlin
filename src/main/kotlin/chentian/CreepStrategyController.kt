package chentian

import chentian.creep.CreepStrategyAttacker
import chentian.creep.CreepStrategyBuilder
import chentian.creep.CreepStrategyBuilderRemote
import chentian.creep.CreepStrategyClaimer
import chentian.creep.CreepStrategyHarvester
import chentian.creep.CreepStrategyHarvesterLink
import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyMiner
import chentian.creep.CreepStrategyResourceCarrier
import chentian.utils.Stats
import chentian.utils.runCreepAttack
import chentian.utils.runHouseKeeping
import chentian.utils.runLinkTransfer
import chentian.utils.runRemoteHarvesters
import chentian.utils.runResourceCarriers
import chentian.utils.runSellEnergy
import chentian.utils.runTowerAttack
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
                CreepStrategyHarvester(room),
                CreepStrategyBuilder(room),
                CreepStrategyHarvesterRemote(room),
                CreepStrategyClaimer(room),
                CreepStrategyBuilderRemote(room),
                CreepStrategyResourceCarrier(room),
                CreepStrategyHarvesterLink(room),
                CreepStrategyAttacker(room)
            ).forEach { strategy ->
                strategy.tryToCreate(spawn)
                strategy.runLoop()
            }
        }

        runTowerAttack()
        runLinkTransfer()
        runSellEnergy()
        runRemoteHarvesters()
        runResourceCarriers()
        runCreepAttack()

        runHouseKeeping()

        Stats.tickEnds()
    }
}
