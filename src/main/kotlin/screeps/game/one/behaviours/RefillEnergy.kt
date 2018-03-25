package screeps.game.one.behaviours

import screeps.game.one.BetterCreepMemory
import screeps.game.one.CreepState
import screeps.game.one.kreeps.BodyDefinition
import types.*
import kotlin.math.roundToInt

object RefillEnergy {
    const val MAX_MINER_PER_SOURCE = 3

    fun run(creep: Creep, creepMemory: BetterCreepMemory) {
        val energySources = creep.room.findEnergy()

        if (shouldContinueMininig(creep) && energySources.isNotEmpty()) {
            var assignedSource = creepMemory.assignedEnergySource
            if (assignedSource == null) {
                val source = creep.requestSource(energySources)!!
                creepMemory.assignedEnergySource = source.id
                assignedSource = source.id

            }

            val source = Game.getObjectById<Source>(assignedSource)!!


            val code = creep.harvest(source)
            when (code) {
                ERR_NOT_IN_RANGE -> {
                    val moveCode = creep.moveTo(source.pos, VisualizePath())
                    when (moveCode) {
                        OK -> kotlin.run { }
                    //TODO handle no path
                        else -> println("unexpected code $moveCode when moving $creep to ${source.pos}")
                    }
                }
            }

        } else {
            creepMemory.state = CreepState.IDLE
        }
    }

    fun Creep.requestSource(energySources: Array<Source>): Source? {

        val usedSourcesWithCreepCounts = Game.creepsMap()
                .map { BetterCreepMemory(it.value.memory).assignedEnergySource }
                .filterNotNull()
                .groupingBy { it }
                .eachCount()

        println("usedSourcesWithCreepCounts=$usedSourcesWithCreepCounts")

        //find a source that is close and has some free spots
        energySources.sort({ a, b -> (dist2(this.pos, a.pos) - dist2(this.pos, b.pos)).roundToInt() })

        for (energySource in energySources) {
            if (usedSourcesWithCreepCounts.getOrElse(energySource.id, { 0 }) < MAX_MINER_PER_SOURCE) {
                //assign creep to energy source
                return energySource

            }
        }


        return null
    }

    fun shouldContinueMininig(creep: Creep): Boolean {
        if (creep.name.startsWith(BodyDefinition.BASIC_WORKER.name)) {
            return creep.carry.energy < creep.carryCapacity
        } else if (creep.name.startsWith(BodyDefinition.MINER.name)) {
            return true
        }

        throw IllegalStateException("stop mining not handled")

    }

    fun dist2(from: RoomPosition, to: RoomPosition) =
            (to.x - from.x) * (to.x - from.x) + (to.y - from.y) * (to.y - from.y)


}