package chentian.creep

import chentian.createCreepName
import chentian.extensions.findCreepByRole
import chentian.extensions.findStructureByType
import chentian.extensions.getMemoryContainerId
import types.base.global.*
import types.base.prototypes.Room
import types.base.prototypes.Source
import types.base.prototypes.structures.SpawnOptions
import types.base.prototypes.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyMiner(room: Room) {

    private val containerMap = room.findStructureByType(STRUCTURE_CONTAINER)
    private val creeps = room.findCreepByRole(CREEP_ROLE_MINER)

    fun shouldCreate(): Boolean {
        return containerMap.size > creeps.size
    }

    fun create(spawn: StructureSpawn) {
        val workerCount = (spawn.room.energyAvailable - 50) / 100
        if (workerCount < MAX_WORKER_BODY_COUNT) {
            return
        }

        val containerIds = containerMap.keys.toMutableSet()
        creeps.forEach { containerIds.remove(it.getMemoryContainerId()) }
        val targetId = containerIds.firstOrNull() ?: return

        val options = object : SpawnOptions {
            @Suppress("unused")
            override val memory = object : CreepMemory {
                val role = CREEP_ROLE_MINER
                val containerId = targetId
            }
        }

        val bodyList = mutableListOf(MOVE).apply {
            for (i in 0 until MAX_WORKER_BODY_COUNT) {
                add(WORK)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_MINER), options)
        println("create new creep $CREEP_ROLE_MINER. code: $result, $bodyList")
    }

    fun runLoop() {
        creeps.forEach { creep ->
            val container = containerMap[creep.getMemoryContainerId()]
            val source = container?.pos?.findClosestByRange<Source>(FIND_SOURCES, 1)
            if (source == null) {
                creep.say("error")
                println("source not found: ${creep.name}")
                return@forEach
            }

            if (!creep.pos.isEqualTo(container.pos)) {
                creep.moveTo(container.pos)
                creep.say("move")
            } else {
                creep.harvest(source)
                creep.say("mine")
            }
        }
    }

    companion object {

        private const val CREEP_ROLE_MINER = "miner"
        private const val MAX_WORKER_BODY_COUNT = 5

    }
}
