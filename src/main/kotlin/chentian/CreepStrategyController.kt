package chentian

import chentian.creep.CreepStrategyBuilder
import chentian.creep.CreepStrategyBuilderRemote
import chentian.creep.CreepStrategyClaimer
import chentian.creep.CreepStrategyHarvester
import chentian.creep.CreepStrategyHarvesterLink
import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyMiner
import chentian.creep.CreepStrategyResourceCarrier
import chentian.utils.linkTransfer
import chentian.utils.runRemoteHarvesters
import chentian.utils.runResourceCarriers
import chentian.utils.sellEnergy
import chentian.utils.towerAttack
import screeps.api.Game
import screeps.api.values
import chentian.utils.Stats
import chentian.utils.houseKeeping

/**
 *
 *
 * @author chentian
 */
object CreepStrategyController {

    fun gameLoop() {
        Stats.tickStarts()

        houseKeeping()
        towerAttack()
        linkTransfer()
        sellEnergy()

        println("Game Loop Start")
        Game.spawns.values.forEach { spawn ->
            val room = spawn.room
            Stats.write(room)
            println("Room $room")

            listOf(
                CreepStrategyMiner(room),
                CreepStrategyHarvester(room),
                CreepStrategyBuilder(room),
//                CreepStrategyDefenceRepair(room),
                CreepStrategyHarvesterRemote(room),
                CreepStrategyClaimer(room),
                CreepStrategyBuilderRemote(room),
                CreepStrategyResourceCarrier(room),
                CreepStrategyHarvesterLink(room)
//                CreepStrategyMinerLink(room)
            ).forEach { strategy ->
                strategy.tryToCreate(spawn)
                strategy.runLoop()
            }
        }

        runRemoteHarvesters()
        runResourceCarriers()

        Stats.tickEnds()
    }
}
