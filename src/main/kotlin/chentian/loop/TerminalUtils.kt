package chentian.loop

import chentian.GameContext
import chentian.extensions.energy
import chentian.extensions.firstResourceType
import chentian.extensions.memory.terminalId
import chentian.utils.MOD_16_SELL_ENERGY
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.Market
import screeps.api.ORDER_BUY
import screeps.api.RESOURCE_ENERGY
import screeps.api.ResourceConstant
import screeps.api.STRUCTURE_TERMINAL
import screeps.api.structures.StructureTerminal
import screeps.utils.unsafe.jsObject
import kotlin.math.min

/**
 * 卖出能量或资源
 *
 * @author chentian
 */

private const val MIN_ENERGY_TO_SELL = 20_000
private const val MIN_RESOURCE_TO_SELL = 10_000
private const val MIN_PRICE_TO_SELL_ENERGY = 0.025
private const val MIN_PRICE_TO_SELL_RESOURCE = 0.03

fun runSellEnergyAndResource() {
    if (GameContext.timeMod16Result != MOD_16_SELL_ENERGY) {
        return
    }

    GameContext.myRooms.forEach { room ->
        // 存下 terminal id，避免每次都找
        if (room.memory.terminalId.isEmpty()) {
            room.find(FIND_STRUCTURES).firstOrNull { it.structureType == STRUCTURE_TERMINAL }?.let {
                room.memory.terminalId = it.id
            }
        }

        Game.getObjectById<StructureTerminal>(room.memory.terminalId)?.let {
            tryToSellEnergy(it) || tryToSellResource(it)
            return@forEach
        }
    }
}

private fun tryToSellEnergy(terminal: StructureTerminal): Boolean {
    if (terminal.cooldown > 0 || terminal.store.energy() < MIN_ENERGY_TO_SELL) {
        return false
    }

    return tryToSell(terminal, RESOURCE_ENERGY)
}

private fun tryToSellResource(terminal: StructureTerminal): Boolean {
    val resourceType = terminal.store.firstResourceType() ?: return false
    if (terminal.cooldown > 0 ||
        (terminal.store.getUsedCapacity(resourceType) ?: 0) < MIN_RESOURCE_TO_SELL) {
        return false
    }

    return tryToSell(terminal, resourceType)
}

private fun tryToSell(terminal: StructureTerminal, resourceTypeToSell: ResourceConstant): Boolean {
    fun isPriceMatch(price: Double): Boolean {
        val targetPrice = if (resourceTypeToSell == RESOURCE_ENERGY) MIN_PRICE_TO_SELL_ENERGY else MIN_PRICE_TO_SELL_RESOURCE
        return price >= targetPrice
    }

    val filter = jsObject<Market.Order.Filter> {
        this.resourceType = resourceTypeToSell
        this.type = ORDER_BUY
    }
    Game.market.getAllOrders(filter).maxBy { it.price }?.let { order ->
        if (!isPriceMatch(order.price)) {
            return false
        }

        val amount = min(terminal.store.energy(), order.remainingAmount) / 2
        val result = Game.market.deal(order.id, amount, terminal.room.name)
        println("sell resource $resourceTypeToSell at ${terminal.room.name}, result: $result, amount: $amount, price: ${order.price}")
        return true
    }
    return false
}
