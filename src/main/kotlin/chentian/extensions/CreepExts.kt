package chentian.extensions

import types.base.global.FindConstant
import types.base.prototypes.Creep
import types.base.prototypes.RoomObject
import types.base.prototypes.RoomPosition

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
    val exit = room.findExitTo(roomName) as FindConstant
    val target = pos.findClosestByRange<RoomObject>(exit) as RoomPosition
    moveTo(target)
//    println("$pos $target ${pos.isEqualTo(target)}")
//    if (pos.isEqualTo(target)) {
//        move(exit as DirectionConstant)
//    } else {
//        moveTo(target)
//    }
}
