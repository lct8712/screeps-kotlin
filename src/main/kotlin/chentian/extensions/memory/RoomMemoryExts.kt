package chentian.extensions.memory

import screeps.api.RoomMemory
import screeps.utils.memory.memory

/**
 *
 *
 * @author chentian
 */


var RoomMemory.claimerRoomName: String by memory { "" }
var RoomMemory.buildRemoteRoomName: String by memory { "" }
var RoomMemory.harvestRemoteRoomName: String by memory { "" }
var RoomMemory.repairTargetId: String by memory { "" }
var RoomMemory.repairTargetCountDown: Int by memory { 0 }

var RoomMemory.terminalId: String by memory { "" }

var RoomMemory.linkIdFrom1: String by memory { "" }
var RoomMemory.linkIdFrom2: String by memory { "" }
var RoomMemory.linkIdTo: String by memory { "" }
