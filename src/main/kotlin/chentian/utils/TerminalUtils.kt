package chentian.utils

import chentian.GameContext
import screeps.api.Game
import screeps.api.Market
import screeps.api.ORDER_BUY
import screeps.api.RESOURCE_ENERGY
import screeps.api.structures.StructureTerminal

/**
 *
 *
 * @author chentian
 */

private const val MIN_ENERGY_TO_SELL = 1000

private class RoomTerminalInfo(
    val targetRoom: String,
    val terminalId: String
)

private val TARGET_ROOM_TERMINAL = listOf(
    RoomTerminalInfo("E18S19", "5d2d768f24bc272472a28c8a")
)

fun sellEnergy() {
    if (GameContext.timeMod16Result != 14) {
        return
    }

    TARGET_ROOM_TERMINAL.forEach { info ->
        val terminal = Game.getObjectById<StructureTerminal>(info.terminalId) ?: return@forEach
        if (terminal.cooldown > 0 || terminal.store.energy < MIN_ENERGY_TO_SELL) {
            return@forEach
        }

        val filter = screeps.utils.unsafe.jsObject<Market.Order.Filter> {
            this.resourceType = RESOURCE_ENERGY
            this.type = ORDER_BUY
        }
        Game.market.getAllOrders(filter).maxBy { it.price }?.let { order ->
            Game.market.deal(order.id, terminal.store.energy)
            // 每次成交一单即可
            return
        }
    }
}
