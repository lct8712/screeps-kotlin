package chentian.extensions

import screeps.api.structures.StructureLink

/**
 *
 *
 * @author chentian
 */

fun StructureLink.isFull(): Boolean {
    return store.getFreeCapacity() == 0
}