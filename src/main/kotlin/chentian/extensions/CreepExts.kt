package chentian.extensions

import screeps.api.Creep
import screeps.api.RoomPosition

/**
 *
 *
 * @author chentian
 */

fun Creep.isFullEnergy(): Boolean {
    return carry.energy == carryCapacity
}

fun Creep.isEmptyEnergy(): Boolean {
    return carry.energy == 0
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
    if (room.name == pos.roomName) {
        moveTo(pos)
    } else {
        moveToTargetRoom(pos.roomName)
    }
}
