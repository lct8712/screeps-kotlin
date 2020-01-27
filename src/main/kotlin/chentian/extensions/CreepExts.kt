package chentian.extensions

import chentian.extensions.memory.transferTargetId
import screeps.api.Creep
import screeps.api.ERR_FULL
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RoomObject
import screeps.api.RoomPosition
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
    return memory.asDynamic().working != null
}

fun Creep.setWorking(isWorking: Boolean) {
    memory.asDynamic().working = if (isWorking) "true" else null
}

fun Creep.isInTargetRoom(roomName: String): Boolean {
    return room.name == roomName
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
fun Creep.moveToTargetRoom(roomName: String) {
    val exit = room.findExitTo(roomName)
    val target = pos.findClosestByRange(exit) as RoomPosition
    moveTo(target)
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

private fun Creep.totalCarry(): Int {
    return store.getUsedCapacity()
}
