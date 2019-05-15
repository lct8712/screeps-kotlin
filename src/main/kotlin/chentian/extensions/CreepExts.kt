package chentian.extensions

import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RoomPosition
import screeps.api.keys
import screeps.api.structures.Structure
import screeps.api.values

/**
 *
 *
 * @author chentian
 */

fun Creep.isFullCarry(): Boolean {
    return totalCarry() == carryCapacity
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

fun Creep.moveToTargetPos(pos: RoomPosition) {
    moveTo(pos)
//    if (room.name == pos.roomName) {
//    } else {
//        moveToTargetRoom(pos.roomName)
//    }
}

fun Creep.totalCarry(): Int {
    return carry.values.sum()
}

fun Creep.transferAllTypeOrMove(target: Structure): Boolean {
    carry.keys.forEach { resourceType ->
        val transferResult = transfer(target, resourceType)
        when (transferResult) {
            ERR_NOT_IN_RANGE -> {
                moveTo(target.pos)
                println("$this is filling energy $target")
                return true
            }
            OK -> {
                return true
            }
            else -> {
                println("$this transfer failed: $transferResult")
            }
        }
    }
    return false
}
