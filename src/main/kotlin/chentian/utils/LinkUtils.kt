package chentian.utils

import chentian.creep.CreepStrategyMinerLink
import screeps.api.Game
import screeps.api.structures.StructureLink

/**
 *
 *
 * @author chentian
 */


private val TARGET_ROOM_LINK = listOf(
    CreepStrategyMinerLink.RoomLinkInfo("E18S18", "5cdad0cdf9cba63e6c385dfd", "5cdacbb4e470435ac71db0cf")
)

fun linkTransfer() {
    TARGET_ROOM_LINK.forEach { roomLinkInfo ->
        val linkFrom = Game.getObjectById<StructureLink>(roomLinkInfo.fromLinkId) ?: return
        val linkTo = Game.getObjectById<StructureLink>(roomLinkInfo.toLinkId) ?: return

        val energy = linkFrom.energy
        if (linkFrom.cooldown == 0 && energy > 0 && linkTo.energyCapacity > linkTo.energy + energy) {
            linkFrom.transferEnergy(linkTo, energy)
            println("$linkFrom transfer energy to $linkTo, $energy")
        }
    }
}
