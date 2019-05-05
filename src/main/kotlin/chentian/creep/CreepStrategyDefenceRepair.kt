package chentian.creep

import chentian.GameContext
import chentian.extensions.findCreepByRole
import chentian.extensions.isEmptyEnergy
import chentian.extensions.targetDefenceId
import chentian.utils.createMoveOptions
import chentian.utils.createNormalCreep
import chentian.utils.harvestEnergyAndDoJob
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.Room
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_WALL
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyDefenceRepair(val room: Room): CreepStrategy {

    private val structureList by lazy {
        room.find(FIND_STRUCTURES).filter {
            (it.structureType == STRUCTURE_RAMPART || it.structureType == STRUCTURE_WALL) && it.hits < it.hitsMax
        }
    }
    private val creeps = room.findCreepByRole(CREEP_ROLE_DEFENCE_BUILDER)

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { build(it) }
    }

    private fun shouldCreate(): Boolean {
        // 最多一个
        if (creeps.isNotEmpty() || GameContext.timeMod16Result != 2) {
            return false
        }
        // 修到 3M 就不修了
        return structureList.isNotEmpty() && structureList.minBy { it.hits }!!.hits <= MAX_HITS_TO_REPAIR
    }

    private fun create(spawn: StructureSpawn) {
        createNormalCreep(spawn, CREEP_ROLE_DEFENCE_BUILDER)
    }

    private fun build(creep: Creep) {
        if (creep.isEmptyEnergy()) {
            creep.memory.targetDefenceId = ""
        }

        harvestEnergyAndDoJob(creep) {
            if (creep.memory.targetDefenceId.isNotEmpty()) {
                Game.getObjectById<Structure>(creep.memory.targetDefenceId)?.let { target ->
                    buildDefenceOrMove(creep, target)
                    return@harvestEnergyAndDoJob
                }
            }

            structureList.minBy { it.hits }?.let { defence ->
                creep.memory.targetDefenceId = defence.id
                buildDefenceOrMove(creep, defence)
            }
        }
    }

    private fun buildDefenceOrMove(creep: Creep, defence: Structure) {
        if (creep.repair(defence) == ERR_NOT_IN_RANGE) {
            creep.moveTo(defence.pos, MOVE_OPTION)
        }
        println("$creep is building $defence")
    }

    companion object {

        const val MAX_HITS_TO_REPAIR = 3_000_000L
        private const val CREEP_ROLE_DEFENCE_BUILDER = "defence-builder"
        private val MOVE_OPTION = createMoveOptions("#ffaa00")
    }
}
