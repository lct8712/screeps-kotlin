package chentian.strategy

import chentian.GameContext
import chentian.extensions.findCreepByRole
import chentian.extensions.findFirstConstructionToBuild
import chentian.extensions.memory.role
import chentian.utils.MOD_16_CREATE_BUILDER_SPAWN
import chentian.utils.createMoveOptions
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
 * 新的房间，有待建造的 spawn 时使用
 * 手动在刚 claim 的房间创建 spawn 即可
 *
 * @author chentian
 */
class CreepStrategyBuilderSpawn(val room: Room) : CreepStrategy {

    private val roomToBuild by lazy {
        GameContext.myRooms.firstOrNull { it.findFirstConstructionToBuild(STRUCTURE_SPAWN) != null }
    }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != MOD_16_CREATE_BUILDER_SPAWN) {
            return
        }

        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        val creepsInAllRoom = Game.creeps.toMap().values.filter { it.memory.role == CREEP_ROLE_BUILDER_SPAWN }
        creepsInAllRoom.forEach { buildStructure(it) }
    }

    private fun shouldCreate(): Boolean {
        if (room.name == "E17S17") {
            return false
        }
        val size = roomToBuild?.findCreepByRole(CREEP_ROLE_BUILDER_SPAWN)?.size ?: return false
        return size < MAX_REMOTE_BUILDER_COUNT
    }

    private fun create(spawn: StructureSpawn) {
        roomToBuild?.let {
            createRemoteCreep(spawn, CREEP_ROLE_BUILDER_SPAWN, it.name)
        }
    }

    private fun buildStructure(creep: Creep) {
        harvestEnergyAndDoJobRemote(creep) {
            creep.room.controller?.let { controller ->
                // RCL 快要降级了
                if (controller.ticksToDowngrade <= 4000) {
                    if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
                        creep.moveTo(controller.pos, MOVE_OPTION)
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
                creep.moveTo(spawnToBuild.pos, MOVE_OPTION)
                println("$creep is building spawn $spawnToBuild")
                return@harvestEnergyAndDoJobRemote
            }
        }
    }

    companion object {

        private val MOVE_OPTION = createMoveOptions("#0B6623")

        private const val CREEP_ROLE_BUILDER_SPAWN = "builder-spawn"
        private const val MAX_REMOTE_BUILDER_COUNT = 3
    }
}
