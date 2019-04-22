package chentian.creep

import chentian.createNormalCreep
import chentian.extensions.findCreepByRole
import chentian.extensions.isEmptyEnergy
import chentian.extensions.targetDefenceId
import chentian.harvestEnergyAndDoJob
import types.base.global.ERR_NOT_IN_RANGE
import types.base.global.Game
import types.base.global.STRUCTURE_RAMPART
import types.base.global.STRUCTURE_WALL
import types.base.prototypes.Creep
import types.base.prototypes.MoveToOpts
import types.base.prototypes.Room
import types.base.prototypes.findStructures
import types.base.prototypes.structures.OwnedStructure
import types.base.prototypes.structures.Structure
import types.base.prototypes.structures.StructureSpawn
import types.base.prototypes.structures.StructureWall
import types.extensions.LineStyle
import types.extensions.Style

/**
 *
 *
 * @author chentian
 */
class CreepStrategyDefenceBuilder(val room: Room): CreepStrategy {

    private val structureList = room.findStructures().filter {
        (it.structureType == STRUCTURE_RAMPART || it.structureType == STRUCTURE_WALL) && it is OwnedStructure && it.my
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
        return creeps.isEmpty() && structureList.isNotEmpty()
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
        private val MOVE_OPTION = MoveToOpts(visualizePathStyle = Style(stroke = "#ffaa00", lineStyle = LineStyle.DOTTED))
    }
}
