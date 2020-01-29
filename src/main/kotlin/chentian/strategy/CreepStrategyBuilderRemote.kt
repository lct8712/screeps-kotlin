package chentian.strategy

import chentian.GameContext
import chentian.extensions.isBuildFinished
import chentian.extensions.memory.buildRemoteRoomName
import chentian.extensions.memory.role
import chentian.utils.MOD_16_CREATE_BUILDER_REMOTE
import chentian.utils.createRemoteCreep
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_MY_CREEPS
import screeps.api.Room
import screeps.api.structures.StructureSpawn
import kotlin.math.min

/**
 * 在其他房间建建筑
 * 需要手动在源房间设置 buildRemoteRoomName
 *
 * @author chentian
 */
class CreepStrategyBuilderRemote(val room: Room) : CreepStrategy {

    private val targetRoom: Room? by lazy {
        GameContext.rooms[room.memory.buildRemoteRoomName]
    }
    private val constructionsToBuild by lazy {
        targetRoom?.find(FIND_CONSTRUCTION_SITES)?.filter { !it.isBuildFinished() }.orEmpty()
    }
    private val allCreepsBuilder by lazy {
        targetRoom?.find(FIND_MY_CREEPS)?.filter { it.memory.role == CREEP_ROLE_BUILDER_REMOTE }.orEmpty()
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
        // 在 buildStructure() 中执行
    }

    private fun shouldCreate(): Boolean {
        // 建设完成
        if (targetRoom != null && constructionsToBuild.isEmpty()) {
            room.memory.buildRemoteRoomName = ""
            return false
        }

        return constructionsToBuild.isNotEmpty() &&
            allCreepsBuilder.size <= min(MAX_REMOTE_BUILDER_COUNT, constructionsToBuild.size)
    }

    private fun create(spawn: StructureSpawn) {
        targetRoom?.let {
            createRemoteCreep(spawn, CREEP_ROLE_BUILDER_REMOTE, it.name)
        }
    }

    companion object {

        const val CREEP_ROLE_BUILDER_REMOTE = "builder-remote"
        private const val MAX_REMOTE_BUILDER_COUNT = 3
    }
}
