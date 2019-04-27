package chentian.extensions

import types.base.global.CreepMemory

/**
 *
 *
 * @author chentian
 */


var CreepMemory.containerId: String
    get() = (asDynamic().containerId as String?).orEmpty()
    set(value) = run { this.asDynamic().containerId = value }

var CreepMemory.role: String
    get() = (asDynamic().role as String?).orEmpty()
    set(value) = run { this.asDynamic().role = value }

var CreepMemory.targetDefenceId: String
    get() = (asDynamic().targetDefenceId as String?).orEmpty()
    set(value) = run { this.asDynamic().targetDefenceId = value }

var CreepMemory.targetRoomName: String
    get() = (asDynamic().targetRoomName as String?).orEmpty()
    set(value) = run { this.asDynamic().targetRoomName = value }
