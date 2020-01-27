package chentian.loop

import chentian.extensions.memory.role
import chentian.strategy.CreepStrategyHarvesterRemote
import chentian.utils.createMoveOptions
import chentian.utils.harvestEnergyAndDoJobRemote
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.Game
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 *
 *
 * @author chentian
 */

val creepRemoteHarvesters: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyHarvesterRemote.CREEP_ROLE_HARVESTER_REMOTE }
}

private val MOVE_OPTION = createMoveOptions("#aaffaa")

fun runRemoteHarvesters() {
    creepRemoteHarvesters.forEach {
        harvestEnergyAndDoJobRemote(it) {
            upgradeController(it)
        }
    }
}

private fun upgradeController(creep: Creep) {
    val controller = creep.room.controller
    if (controller != null) {
        if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller.pos, MOVE_OPTION)
        }
        println("$creep is upgrading controller")
    }
}
