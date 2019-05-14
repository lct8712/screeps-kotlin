package chentian.creep

import chentian.GameContext
import chentian.extensions.containerId
import chentian.extensions.findCreepByRole
import chentian.extensions.findStructureMapByType
import chentian.extensions.linkFromAId
import chentian.extensions.linkToAId
import chentian.extensions.role
import chentian.utils.createCreepName
import screeps.api.CreepMemory
import screeps.api.FIND_SOURCES
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.Source
import screeps.api.WORK
import screeps.api.options
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */
class CreepStrategyMinerLink(private val room: Room): CreepStrategy {

    private lateinit var creeps lazy by { room.findCreepByRole(CREEP_ROLE_MINER_LINK) }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (GameContext.timeMod16Result != 5) {
            return
        }

        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { creep ->
            val container = containerMap[creep.memory.containerId]
            val source: Source? = container?.pos?.findInRange(FIND_SOURCES, 1)?.getOrNull(0)
            if (source == null) {
                creep.say("error")
                println("source not found: ${creep.name}")
                return@forEach
            }

            if (!creep.pos.isEqualTo(container.pos)) {
                // 移动到 container 的位置
                creep.moveTo(container.pos)
                creep.say("move")
            } else {
                // 正常采集
                creep.harvest(source)
                creep.say("mine")
            }
        }
    }

    private fun shouldCreate(): Boolean {
        TARGET_ROOM_LINK.forEach { roomLinkInfo ->
            if (roomLinkInfo.targetRoom != room.name) {
                return@forEach
            }

            room.memory.linkFromAId = roomLinkInfo.fromLinkId
            room.memory.linkToAId = roomLinkInfo.toLinkId
            return creeps.isEmpty()
        }
        return false
    }

    private fun create(spawn: StructureSpawn) {
        val workerCount = (spawn.room.energyAvailable - 50) / 100
        if (workerCount < MAX_WORKER_BODY_COUNT) {
            return
        }

        val containerIds = containerMap.keys.toMutableSet()
        creeps.forEach { containerIds.remove(it.memory.containerId) }
        val targetId = containerIds.firstOrNull() ?: return

        val bodyList = mutableListOf(MOVE).apply {
            for (i in 0 until MAX_WORKER_BODY_COUNT) {
                add(WORK)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_MINER_LINK), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_MINER_LINK
                this.containerId = targetId
            }
        })
        println("create new creep $CREEP_ROLE_MINER_LINK. code: $result, $bodyList")
    }


    private class RoomLinkInfo(
        val targetRoom: String,
        val fromLinkId: String,
        val toLinkId: String
    )

    companion object {

        private const val CREEP_ROLE_MINER_LINK = "miner-link"
        private const val MAX_WORKER_BODY_COUNT = 5

        private val TARGET_ROOM_LINK = listOf(
            RoomLinkInfo("E18S18", "5cdac520a6a54c60f25c8358", "5cdac54a058ace60ea047ae3")
        )
    }
}
