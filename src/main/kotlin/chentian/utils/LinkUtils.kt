package chentian.utils

import chentian.extensions.energy
import chentian.extensions.energyCapacity
import chentian.extensions.isFull
import screeps.api.Game
import screeps.api.structures.StructureLink

/**
 *
 *
 * @author chentian
 */

class RoomLinkInfo(
    val targetRoom: String,
    val fromLinkId: String,
    val toLinkId: String
)

val TARGET_ROOM_LINK = listOf(
    RoomLinkInfo("E18S18", "5cdad0cdf9cba63e6c385dfd", "5cdacbb4e470435ac71db0cf"),
    RoomLinkInfo("E18S18", "5d2071da14a08a72e00d134c", "5cdacbb4e470435ac71db0cf"),
    RoomLinkInfo("E17S17", "5d43c038d16c4b73af5acfa0", "5d44004ccb3b537470a4633e"),
    RoomLinkInfo("E17S17", "5e2c3ab6ece8182cd4d219eb", "5d44004ccb3b537470a4633e")
)

fun linkTransfer() {
    TARGET_ROOM_LINK.forEach { roomLinkInfo ->
        val linkFrom = Game.getObjectById<StructureLink>(roomLinkInfo.fromLinkId) ?: return
        val linkTo = Game.getObjectById<StructureLink>(roomLinkInfo.toLinkId) ?: return

        val energy = transferAmount(linkFrom, linkTo)
        if (energy > 0) {
            linkFrom.transferEnergy(linkTo, energy)
            println("$linkFrom transfer energy to $linkTo, $energy")

            // 每 tick 只传输一个 Link
            return
        }
    }
}

/**
 * 应该传输多少能量
 */
private fun transferAmount(linkFrom: StructureLink, linkTo: StructureLink): Int {
    if (linkFrom.cooldown != 0) {
        return 0
    }

    val energy = linkFrom.store.energy()
    if (energy > 0 && linkTo.store.energyCapacity() >= linkTo.store.energy() + energy) {
        return energy
    }
    if (linkFrom.isFull() && linkTo.store.energyCapacity() >= linkTo.store.energy() + energy / 2) {
        return energy / 2
    }
    return 0
}
