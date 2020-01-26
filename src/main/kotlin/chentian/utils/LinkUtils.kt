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
//    RoomLinkInfo("E18S18", "5cdad0cdf9cba63e6c385dfd", "5cdacbb4e470435ac71db0cf"),
//    RoomLinkInfo("E18S18", "5d2071da14a08a72e00d134c", "5cdacbb4e470435ac71db0cf"),
    RoomLinkInfo("E18S18", "5cdad0cdf9cba63e6c385dfd", "5e2d0b60ece8181d45d266b4"),
    RoomLinkInfo("E18S18", "5d2071da14a08a72e00d134c", "5e2d0b60ece8181d45d266b4"),
    RoomLinkInfo("E17S17", "5d43c038d16c4b73af5acfa0", "5d44004ccb3b537470a4633e"),
    RoomLinkInfo("E17S17", "5e2c3ab6ece8182cd4d219eb", "5d44004ccb3b537470a4633e"),
    RoomLinkInfo("W8N3", "1f0f0e7b012ae92", "d87f0e9465b0b50")
)

fun linkTransfer() {
    val roomSet = mutableSetOf<String>()
    TARGET_ROOM_LINK.forEach { roomLinkInfo ->
        // 每个房间每 tick 只传输一个 Link
        if (roomSet.contains(roomLinkInfo.targetRoom)) {
            return@forEach
        }

        val linkFrom = Game.getObjectById<StructureLink>(roomLinkInfo.fromLinkId) ?: return@forEach
        val linkTo = Game.getObjectById<StructureLink>(roomLinkInfo.toLinkId) ?: return@forEach

        val energy = transferAmount(linkFrom, linkTo)
        if (energy > 0) {
            linkFrom.transferEnergy(linkTo, energy)
            roomSet.add(roomLinkInfo.targetRoom)
            println("$linkFrom transfer energy to $linkTo, $energy")
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
