package chentian.creep

import chentian.GameContext
import chentian.extensions.findCreepByRole
import chentian.extensions.findFirstConstructionToBuild
import chentian.extensions.role
import chentian.utils.createRemoteCreep
import chentian.utils.harvestEnergyAndDoJobRemote
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.Game
import screeps.api.Room
import screeps.api.STRUCTURE_SPAWN
import screeps.api.structures.StructureSpawn
import screeps.utils.toMap

/**
 *
 *
 * @author chentian
 */
class CreepStrategyBuilderRemote(val room: Room) : CreepStrategy {

    private val roomToBuild by lazy {
        GameContext.rooms.values.firstOrNull { it.findFirstConstructionToBuild(STRUCTURE_SPAWN) != null }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != 3) {
            return
        }

        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        val creepsInAllRoom = Game.creeps.toMap().values.filter { it.memory.role == CREEP_ROLE_BUILDER_REMOTE }
        creepsInAllRoom.forEach { buildStructure(it) }
    }

    private fun shouldCreate(): Boolean {
        val size = roomToBuild?.findCreepByRole(CREEP_ROLE_BUILDER_REMOTE)?.size ?: return false
        return size < MAX_REMOTE_BUILDER_COUNT
    }

    private fun create(spawn: StructureSpawn) {
        createRemoteCreep(spawn, CREEP_ROLE_BUILDER_REMOTE, roomToBuild!!.name)
    }

    private fun buildStructure(creep: Creep) {
        harvestEnergyAndDoJobRemote(creep) {
            val structure = creep.room.findFirstConstructionToBuild(STRUCTURE_SPAWN)
            if (structure == null) {
                // 转换为 Harvester
                creep.memory.role = CreepStrategyHarvester.CREEP_ROLE_HARVESTER
                return@harvestEnergyAndDoJobRemote
            }

            if (creep.build(structure) == ERR_NOT_IN_RANGE) {
                creep.moveTo(structure.pos)
                println("$creep is building remote $structure")
                return@harvestEnergyAndDoJobRemote
            }
        }
    }

    companion object {

        private const val CREEP_ROLE_BUILDER_REMOTE = "builder-remote"
        private const val MAX_REMOTE_BUILDER_COUNT = 8
    }
}
