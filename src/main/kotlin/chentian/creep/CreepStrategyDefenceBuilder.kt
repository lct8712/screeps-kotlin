package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.isEmptyEnergy
import chentian.extensions.targetDefenceId
import chentian.utils.harvestEnergyAndDoJob
import chentian.utils.createMoveOptions
import chentian.utils.createNormalCreep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.Game
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_WALL
import screeps.api.Creep
import screeps.api.FIND_MY_STRUCTURES
import screeps.api.Room
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureWall

/**
 *
 *
 * @author chentian
 */
class CreepStrategyDefenceBuilder(val room: Room): CreepStrategy {

    private val structureList by lazy {
        room.find(FIND_MY_STRUCTURES).filter {
            (it.structureType == STRUCTURE_RAMPART || it.structureType == STRUCTURE_WALL)
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
        if (creeps.isNotEmpty() || Game.time % 128 != 0) {
            return false
        }
        // 修到 2M 就不修了
        return structureList.isNotEmpty() && structureList.minBy { it.hits }!!.hits <= 3_000_000L
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
                Game.getObjectById<StructureWall>(creep.memory.targetDefenceId)?.let { wall ->
                    buildDefenceOrMove(creep, wall)
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

        private const val CREEP_ROLE_DEFENCE_BUILDER = "defence-builder"
        private val MOVE_OPTION = createMoveOptions("#ffaa00")
    }
}
