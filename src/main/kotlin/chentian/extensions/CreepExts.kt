package chentian.extensions

import types.base.prototypes.Creep

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

fun Creep.getMemoryContainerId(): String {
    return (memory.asDynamic().containerId as String?).orEmpty()
}

fun Creep.getMemoryRole(): String {
    return (memory.asDynamic().role as String?).orEmpty()
}
