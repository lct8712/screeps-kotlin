package chentian.extensions

import screeps.api.RoomMemory
import screeps.utils.memory.memory

/**
 *
 *
 * @author chentian
 */


var RoomMemory.claimerRoomName: String by memory { "" }
var RoomMemory.repairTargetId: String by memory { "" }
var RoomMemory.repairTargetCountDown: Int by memory { 0 }

var RoomMemory.linkFromAId: String by memory { "" }
var RoomMemory.linkToAId: String by memory { "" }
