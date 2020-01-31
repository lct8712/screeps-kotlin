package chentian.extensions

import chentian.extensions.memory.isWorking
import chentian.extensions.memory.transferTargetId
import screeps.api.BOTTOM
import screeps.api.Creep
import screeps.api.ERR_FULL
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.LEFT
import screeps.api.MoveToOptions
import screeps.api.OK
import screeps.api.RIGHT
import screeps.api.RoomObject
import screeps.api.RoomPosition
import screeps.api.TOP
import screeps.api.keys
import screeps.api.structures.Structure

/**
 *
 *
 * @author chentian
 */

fun Creep.isFullCarry(): Boolean {
    return store.getFreeCapacity() == 0
}

fun Creep.isEmptyCarry(): Boolean {
    return totalCarry() == 0
}

fun Creep.isWorking(): Boolean {
    return memory.isWorking
}

fun Creep.setWorking(isWorking: Boolean) {
    memory.isWorking = isWorking
}

fun Creep.isInTargetRoom(roomName: String): Boolean {
    return room.name == roomName
}

fun Creep.needHeal(): Boolean {
    return hits < hitsMax
}

private val UNSAFE_TARGET_ROOMS = setOf("E15S19", "E14S21", "E13S21")
private val FIRST_PART_ROOMS = setOf("E17S20", "E17S19", "E18S19")
private const val MIDDLE_SAFE_ROOM = "E16S20"

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
fun Creep.moveToTargetRoom(roomName: String, opts: MoveToOptions? = null) {
    val targetRoomName = if (roomName in UNSAFE_TARGET_ROOMS && room.name in FIRST_PART_ROOMS) {
        MIDDLE_SAFE_ROOM
    } else {
        roomName
    }

    val exit = room.findExitTo(targetRoomName)
    val target = pos.findClosestByRange(exit) as RoomPosition

    if (opts == null) {
        moveTo(target)
    } else {
        moveTo(target, opts)
    }
}

fun Creep.transferAllTypeOrMove(target: Structure): Boolean {
    store.keys.forEach { resourceType ->
        val transferResult = transfer(target, resourceType)
        when (transferResult) {
            OK -> {
                return true
            }
            ERR_NOT_IN_RANGE -> {
                moveTo(target.pos)
                println("$this is filling energy $target")
                return true
            }
            ERR_FULL -> {
                memory.transferTargetId = ""
                println("$this transfer failed: target full")
            }
            else -> {
                println("$this transfer resource $resourceType failed: $transferResult")
            }
        }
    }
    return false
}

fun <T : RoomObject> Creep.findClosest(roomObjects: Collection<T>): T? {
    var closest: T? = null
    var minDistance = Int.MAX_VALUE
    for (roomObject in roomObjects) {
        val dist = (roomObject.pos.x - this.pos.x) * (roomObject.pos.x - this.pos.x) +
            (roomObject.pos.y - this.pos.y) * (roomObject.pos.y - this.pos.y)

        if (dist < minDistance) {
            minDistance = dist
            closest = roomObject
        }
    }
    return closest
}

fun <T : RoomObject> Creep.findClosest(roomObjects: Array<out T>): T? {
    var closest: T? = null
    var minDistance = Int.MAX_VALUE
    for (roomObject in roomObjects) {
        val dist = (roomObject.pos.x - this.pos.x) * (roomObject.pos.x - this.pos.x) +
            (roomObject.pos.y - this.pos.y) * (roomObject.pos.y - this.pos.y)

        if (dist < minDistance) {
            minDistance = dist
            closest = roomObject
        }
    }
    return closest
}

fun <T : RoomObject> Creep.findClosestNotEmpty(roomObjects: Array<out T>): T {
    require(roomObjects.isNotEmpty())
    return findClosest(roomObjects)!!
}

private const val ROOM_SIZE = 50

fun Creep.moveAwayFromRoomEdge() {
    val direction = when {
        pos.x == 0 -> RIGHT
        pos.x == (ROOM_SIZE - 1) -> LEFT
        pos.y == 0 -> BOTTOM
        pos.y == (ROOM_SIZE - 1) -> TOP
        else -> null
    }
    direction?.let {
        move(it)
    }
}

private fun Creep.totalCarry(): Int {
    return store.getUsedCapacity()
}
