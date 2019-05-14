package chentian.utils

import chentian.GameContext
import chentian.extensions.linkFromAId
import chentian.extensions.linkToAId
import screeps.api.Game
import screeps.api.structures.StructureLink

/**
 *
 *
 * @author chentian
 */

fun linkTransfer() {
    GameContext.rooms.values.forEach { room ->
        val linkFrom = Game.getObjectById<StructureLink>(room.memory.linkFromAId) ?: return
        val linkTo = Game.getObjectById<StructureLink>(room.memory.linkToAId) ?: return

        val energy = linkFrom.energy
        if (linkFrom.cooldown == 0 && energy > 0 && linkTo.energyCapacity > linkTo.energy + energy) {
            linkFrom.transferEnergy(linkTo, energy)
            println("$linkFrom transfer energy to $linkTo, $energy")
        }
    }
}
