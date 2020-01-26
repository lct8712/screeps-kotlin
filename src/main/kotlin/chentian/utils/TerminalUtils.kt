package chentian.utils

import chentian.GameContext
import chentian.extensions.energy
import screeps.api.Game
import screeps.api.Market
import screeps.api.ORDER_BUY
import screeps.api.RESOURCE_ENERGY
import screeps.api.structures.StructureTerminal
import kotlin.math.min

/**
 *
 *
 * @author chentian
 */

private const val MIN_ENERGY_TO_SELL = 50_000

private class RoomTerminalInfo(
    val roomName: String,
    val terminalId: String
)

private val TARGET_ROOM_TERMINAL = listOf(
    RoomTerminalInfo("E18S19", "5d2e18efae29775e0162b5eb"),
    RoomTerminalInfo("E18S18", "5e2cb4b927eb82e7987a89ac")
)

fun runSellEnergy() {
    if (GameContext.timeMod16Result != MOD_16_SELL_ENERGY) {
        return
    }

    TARGET_ROOM_TERMINAL.forEach { info ->
        val terminal = Game.getObjectById<StructureTerminal>(info.terminalId) ?: return@forEach
        if (terminal.cooldown > 0 || terminal.store.energy() < MIN_ENERGY_TO_SELL) {
            return@forEach
        }

        val filter = screeps.utils.unsafe.jsObject<Market.Order.Filter> {
            this.resourceType = RESOURCE_ENERGY
            this.type = ORDER_BUY
        }
        Game.market.getAllOrders(filter).maxBy { it.price }?.let { order ->
            val amount = min(terminal.store.energy(), order.remainingAmount) / 2
            val result = Game.market.deal(order.id, amount, info.roomName)
            println("terminal: result: $result. $amount")
            // 每次成交一单即可
            return@forEach
        }
    }
}
