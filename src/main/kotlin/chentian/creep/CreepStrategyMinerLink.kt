package chentian.creep

import chentian.extensions.findCreepByRole
import chentian.extensions.linkFromAId
import chentian.extensions.linkToAId
import chentian.extensions.role
import chentian.extensions.targetLinkId
import chentian.utils.createCreepName
import screeps.api.CreepMemory
import screeps.api.FIND_SOURCES
import screeps.api.Game
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.Source
import screeps.api.WORK
import screeps.api.options
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */
class CreepStrategyMinerLink(private val room: Room): CreepStrategy {

    private val creeps by lazy { room.findCreepByRole(CREEP_ROLE_MINER_LINK) }

    override fun tryToCreate(spawn: StructureSpawn) {
        getFromLinkId()?.let { linkId ->
            create(spawn, linkId)
        }
    }

    override fun runLoop() {
        creeps.forEach { creep ->
            val link = Game.getObjectById<StructureLink>(creep.memory.targetLinkId)
            if (link == null) {
                creep.say("error")
                println("link not found: ${creep.name} ${creep.memory.targetLinkId}")
                return@forEach
            }

            if (!creep.pos.isEqualTo(link.pos)) {
                // 移动到 link 的位置
                creep.moveTo(link.pos)
                creep.say("move")
                return@forEach
            }

            val source: Source? = link.pos.findInRange(FIND_SOURCES, 1).getOrNull(0)
            if (source == null) {
                creep.say("error")
                println("source not found: ${creep.name}")
                return@forEach
            }

            // 正常采集
            creep.harvest(source)
            creep.say("mine")
        }
    }

    private fun getFromLinkId(): String? {
        TARGET_ROOM_LINK.forEach { roomLinkInfo ->
            if (roomLinkInfo.targetRoom != room.name) {
                return@forEach
            }

            room.memory.linkFromAId = roomLinkInfo.fromLinkId
            room.memory.linkToAId = roomLinkInfo.toLinkId
            if (creeps.isEmpty()) {
                return roomLinkInfo.fromLinkId
            }
            return null
        }
        return null
    }

    private fun create(spawn: StructureSpawn, linkId: String) {
        val workerCount = (spawn.room.energyAvailable - 50) / 100
        if (workerCount < MAX_WORKER_BODY_COUNT) {
            return
        }

        val bodyList = mutableListOf(MOVE).apply {
            for (i in 0 until MAX_WORKER_BODY_COUNT) {
                add(WORK)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_MINER_LINK), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_MINER_LINK
                this.targetLinkId = linkId
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
            RoomLinkInfo("E18S18", "5cdad0cdf9cba63e6c385dfd", "5cdacbb4e470435ac71db0cf")
        )
    }
}
