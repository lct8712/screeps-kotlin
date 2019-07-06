package chentian.utils

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
    RoomLinkInfo("E18S18", "5d204ee4213aac3b59daac53", "5cdacbb4e470435ac71db0cf")
)

fun linkTransfer() {
    TARGET_ROOM_LINK.forEach { roomLinkInfo ->
        val linkFrom = Game.getObjectById<StructureLink>(roomLinkInfo.fromLinkId) ?: return
        val linkTo = Game.getObjectById<StructureLink>(roomLinkInfo.toLinkId) ?: return

        val energy = linkFrom.energy
        if (linkFrom.cooldown == 0 && energy > 0 && linkTo.energyCapacity > linkTo.energy + energy) {
            linkFrom.transferEnergy(linkTo, energy)
            println("$linkFrom transfer energy to $linkTo, $energy")

            // 每 tick 只传输一个 Link
            return
        }
    }
}
