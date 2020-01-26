package chentian.utils

import chentian.creep.CreepStrategyResourceCarrier
import chentian.extensions.containerTargetId
import chentian.extensions.homeRoomName
import chentian.extensions.isEmptyCarry
import chentian.extensions.isFullCarry
import chentian.extensions.isWorking
import chentian.extensions.moveToTargetPos
import chentian.extensions.moveToTargetRoom
import chentian.extensions.role
import chentian.extensions.setWorking
import chentian.extensions.storageTargetId
import chentian.extensions.targetRoomName
import chentian.extensions.transferAllTypeOrMove
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.OK
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_STORAGE
import screeps.api.get
import screeps.api.structures.Structure
import screeps.api.structures.StructureStorage
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 *
 *
 * @author chentian
 */

val resourceCarriers: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyResourceCarrier.CREEP_ROLE_RESOURCE_CARRIER }
}

fun runResourceCarriers() {
    resourceCarriers.forEach {
        runSingleResourceCarriers(it)
    }
}

fun runSingleResourceCarriers(creep: Creep) {
    // 已满，回家
    if (creep.isFullCarry()) {
        creep.say("full")
        creep.setWorking(true)
        creep.memory.containerTargetId = ""
        if (creep.room.name == creep.memory.homeRoomName) {
            transferResourceToStorage(creep)
        } else {
            creep.moveToTargetRoom(creep.memory.homeRoomName)
        }
        return
    }

    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡地上掉的
        creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1).firstOrNull()?.let { resource ->
            if (creep.pickup(resource) == OK) {
                println("$creep is picking up resource at $resource")
                return
            }
        }

        // 找地方捡能量
        val targetContainer = findContainerToCarrier(creep)
        targetContainer?.pos?.findInRange(FIND_DROPPED_RESOURCES, 1)?.maxBy { it.amount }?.let { resource ->
            if (creep.pickup(resource) == ERR_NOT_IN_RANGE) {
                creep.moveToTargetPos(resource.pos)
            }
            println("$creep is carrying resource, in room: $targetContainer")
            return
        }
    }

    if (creep.isWorking()) {
        transferResourceToStorage(creep)
        return
    }
}

private fun transferResourceToStorage(creep: Creep) {
    // 已有 storage 目标
    Game.getObjectById<StructureStorage>(creep.memory.storageTargetId)?.let { storage ->
        creep.transferAllTypeOrMove(storage)
        return
    }

    // 找新的 storage
    creep.room.find(FIND_STRUCTURES).firstOrNull { it.structureType == STRUCTURE_STORAGE }?.let { storage ->
        creep.memory.storageTargetId = storage.id
        creep.transferAllTypeOrMove(storage)
        return
    }
}

private fun findContainerToCarrier(creep: Creep): Structure? {
    fun findContainerInRoom(room: Room?): Structure? {
        return room?.find(FIND_STRUCTURES)?.firstOrNull {
            it.structureType == STRUCTURE_CONTAINER &&
                (it.pos.findInRange(FIND_DROPPED_RESOURCES, 1).firstOrNull()?.amount ?: 0) > 200
        }
    }

    // 已有
    Game.getObjectById<Structure>(creep.memory.containerTargetId)?.let { container ->
        return container
    }

    // 找一个新的
    var container = findContainerInRoom(creep.room)
    if (container == null) {
        val isInHome = creep.room.name == creep.memory.homeRoomName
        val otherRoom = if (isInHome) Game.rooms[creep.memory.targetRoomName] else creep.room
        container = findContainerInRoom(otherRoom)
    }

    container?.let {
        creep.memory.containerTargetId = container.id
        return container
    }

    return null
}
