package chentian.utils

import chentian.GameContext
import chentian.extensions.energy
import chentian.extensions.memory.terminalId
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.Market
import screeps.api.ORDER_BUY
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_TERMINAL
import screeps.api.structures.StructureTerminal
import screeps.utils.unsafe.jsObject
import kotlin.math.min

/**
 * 卖出能量
 *
 * @author chentian
 */

private const val MIN_ENERGY_TO_SELL = 20_000

fun runSellEnergy() {
    if (GameContext.timeMod16Result != MOD_16_SELL_ENERGY) {
        return
    }

    GameContext.myRooms.forEach { room ->
        if (room.memory.terminalId.isNotEmpty()) {
            Game.getObjectById<StructureTerminal>(room.memory.terminalId)?.let {
                tryToSellEnergy(it)
                return@forEach
            }
        }

        room.find(FIND_STRUCTURES).firstOrNull { it.structureType == STRUCTURE_TERMINAL }?.let {
            room.memory.terminalId = it.id
            tryToSellEnergy(it as StructureTerminal)
        }
    }
}

fun tryToSellEnergy(terminal: StructureTerminal) {
    if (terminal.cooldown > 0 || terminal.store.energy() < MIN_ENERGY_TO_SELL) {
        return
    }

    val filter = jsObject<Market.Order.Filter> {
        this.resourceType = RESOURCE_ENERGY
        this.type = ORDER_BUY
    }
    Game.market.getAllOrders(filter).maxBy { it.price }?.let { order ->
        val amount = min(terminal.store.energy(), order.remainingAmount) / 2
        val result = Game.market.deal(order.id, amount, terminal.room.name)
        println("sell energy at ${terminal.room.name}, result: $result, amount: $amount")
    }
}
