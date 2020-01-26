package chentian.creep

import chentian.GameContext
import chentian.extensions.findCreepByRole
import chentian.extensions.findFirstConstructionToBuild
import chentian.extensions.role
import chentian.utils.MOD_16_CREATE_BUILDER_REMOTE
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
        GameContext.myRooms.firstOrNull { it.findFirstConstructionToBuild(STRUCTURE_SPAWN) != null }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != MOD_16_CREATE_BUILDER_REMOTE) {
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
            creep.room.controller?.let { controller ->
                // RCL 快要降级了
                if (controller.ticksToDowngrade <= 4000) {
                    if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
                        creep.moveTo(controller.pos)
                    }
                    return@harvestEnergyAndDoJobRemote
                }
            }

            val spawnToBuild = creep.room.findFirstConstructionToBuild(STRUCTURE_SPAWN)
            if (spawnToBuild == null) {
                // 转换为 Harvester
                creep.memory.role = CreepStrategyHarvester.CREEP_ROLE_HARVESTER
                return@harvestEnergyAndDoJobRemote
            }

            if (creep.build(spawnToBuild) == ERR_NOT_IN_RANGE) {
                creep.moveTo(spawnToBuild.pos)
                println("$creep is building remote $spawnToBuild")
                return@harvestEnergyAndDoJobRemote
            }
        }
    }

    companion object {

        private const val CREEP_ROLE_BUILDER_REMOTE = "builder-remote"
        private const val MAX_REMOTE_BUILDER_COUNT = 3
    }
}
