package chentian.utils

import chentian.creep.CreepStrategyHarvesterRemote
import chentian.creep.CreepStrategyResourceCarrier
import chentian.extensions.homeRoomName
import chentian.extensions.isEmptyEnergy
import chentian.extensions.isFullEnergy
import chentian.extensions.isInTargetRoom
import chentian.extensions.isMine
import chentian.extensions.isWorking
import chentian.extensions.moveToTargetRoom
import chentian.extensions.role
import chentian.extensions.setWorking
import chentian.extensions.targetRoomName
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_SOURCES
import screeps.api.FIND_TOMBSTONES
import screeps.api.Game
import screeps.api.RESOURCE_ENERGY
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 *
 *
 * @author chentian
 */

val resourceCarriers: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyResourceCarrier.CREEP_ROLE_RESOURCE_CARRIER }
}

private val MOVE_OPTION = createMoveOptions("#aaffaa")

fun runResourceCarriers() {
    resourceCarriers.forEach {
        runSingleResourceCarriers(it)
    }
}

fun runSingleResourceCarriers(creep: Creep) {


    if (creep.isFullEnergy()) {
        creep.setWorking(true)
        creep.say("full")
        if (creep.room.name == creep.memory.homeRoomName) {
            transferResourceToStorage(creep)
        } else {
            val homeRoomName = if (creep.memory.homeRoomName.isEmpty()) ROOM_NAME_HOME else creep.memory.homeRoomName
            creep.moveToTargetRoom(homeRoomName)
        }
        return
    }

    if (creep.isEmptyEnergy() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyEnergy()) "empty" else "fill"
        creep.say(message)


        val roomNameTarget = creep.memory.targetRoomName
        val inTargetRoom = creep.isInTargetRoom(roomNameTarget)
        if (inTargetRoom) {
            val sourceList = creep.room.find(FIND_SOURCES)
            val index = creep.name[creep.name.length - 2].toInt() % sourceList.size
            val source = sourceList.getOrNull(index)
            if (source == null) {
                creep.say("source not found")
                println("$creep source in room not found harvesting")
                return
            }

            if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
                creep.moveTo(source.pos, moveToOptions)
            }
        } else {
            creep.moveToTargetRoom(roomNameTarget)
        }

        println("$creep is harvesting remote, in target room: $inTargetRoom")
        return
    }

    creep.say("action")
    jobAction()
    return
}

fun transferResourceToStorage(creep: Creep) {

}
