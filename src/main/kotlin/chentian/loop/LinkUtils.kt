package chentian.loop

import chentian.GameContext
import chentian.extensions.controlLevel
import chentian.extensions.energy
import chentian.extensions.energyCapacity
import chentian.extensions.isFull
import chentian.extensions.memory.linkIdFrom1
import chentian.extensions.memory.linkIdFrom2
import chentian.extensions.memory.linkIdTo
import chentian.extensions.needUpgrade
import chentian.utils.MOD_16_REBUILD_LINK_IDS
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.FilterOption
import screeps.api.Game
import screeps.api.Room
import screeps.api.STRUCTURE_LINK
import screeps.api.STRUCTURE_STORAGE
import screeps.api.options
import screeps.api.structures.Structure
import screeps.api.structures.StructureLink

/**
 *
 *
 * @author chentian
 */

/**
 * 每回合通过 Link 传输能量
 */
fun runLinkTransfer() {
    GameContext.myRooms.forEach { room ->
        // 低等级的房间没有 Link
        if (room.controlLevel() < 5) {
            return@forEach
        }

        // 每隔一段时间重建 memory
        if (GameContext.timeMod16Result == MOD_16_REBUILD_LINK_IDS) {
            rebuildRoomLinkId(room)
        }

        // 每个房间每 tick 只传输一个 Link
        if (room.memory.linkIdFrom1.isNotEmpty() &&
            tryToTransfer(room.memory.linkIdFrom1, room.memory.linkIdTo)) {
            return@forEach
        }

        if (room.memory.linkIdFrom2.isNotEmpty()) {
            tryToTransfer(room.memory.linkIdFrom2, room.memory.linkIdTo)
        }
    }
}

/**
 * 查找 link，并将 id 写入存储
 *   - 离 miner 近的是 from
 *   - 离 controller 或 storage 近的是 to
 *
 * 如果两个 link 都找过，则不再寻找
 */
fun rebuildRoomLinkId(room: Room) {
    if (isAllLinkReadyInRoom(room)) {
        return
    }

    // 在两个 source 附近找 from
    val linkFromIds = room.find(FIND_SOURCES).flatMap { source ->
        source.pos.findInRange(FIND_STRUCTURES, 2)
            .filter { it.structureType == STRUCTURE_LINK }
            .map { it as StructureLink }
    }.distinctBy { link ->
        link.id
    }.map { link ->
        link.id
    }
    room.memory.linkIdFrom1 = linkFromIds.getOrNull(0).orEmpty()
    room.memory.linkIdFrom2 = linkFromIds.getOrNull(1).orEmpty()

    // 找 to。需要升级就在 controller 附近找，否则就在 storage 附近找
    val pos = if (room.controller?.needUpgrade() == true) {
        room.controller?.pos
    } else {
        room.find(FIND_STRUCTURES).firstOrNull { it.structureType == STRUCTURE_STORAGE }?.pos
    }
    room.memory.linkIdTo = pos?.findClosestByRange(FIND_STRUCTURES, options<FilterOption<Structure>> {
        filter = { it.structureType == STRUCTURE_LINK }
    })?.id.orEmpty()
}

/**
 * 一个房间两个 link 是否都齐备
 */
fun isAllLinkReadyInRoom(room: Room): Boolean {
    return room.memory.linkIdFrom2.isNotEmpty() && room.memory.linkIdTo.isNotEmpty()
}

/**
 * 传输一次能量
 *
 * @return 是否传输成功
 */
private fun tryToTransfer(fromLinkId: String, toLinkId: String): Boolean {
    val linkFrom = Game.getObjectById<StructureLink>(fromLinkId) ?: return false
    val linkTo = Game.getObjectById<StructureLink>(toLinkId) ?: return false

    val energy = getTransferAmount(linkFrom, linkTo)
    if (energy > 0) {
        linkFrom.transferEnergy(linkTo, energy)
        println("$linkFrom transfer energy to $linkTo, $energy")
        return true
    }
    return false
}

/**
 * 应该传输多少能量
 */
private fun getTransferAmount(linkFrom: StructureLink, linkTo: StructureLink): Int {
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
