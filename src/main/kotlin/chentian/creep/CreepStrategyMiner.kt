package chentian.creep

import chentian.extensions.containerId
import chentian.extensions.findCreepByRole
import chentian.extensions.findStructureMapByType
import chentian.utils.createCreepName
import chentian.utils.createSpawnOption
import screeps.api.CreepMemory
import screeps.api.FIND_SOURCES
import screeps.api.MOVE
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.Source
import screeps.api.WORK
import screeps.api.structures.StructureSpawn

/**
 *
 *
 * @author chentian
 */
class CreepStrategyMiner(room: Room): CreepStrategy {

    private val containerMap = room.findStructureMapByType(STRUCTURE_CONTAINER)
    private val creeps = room.findCreepByRole(CREEP_ROLE_MINER)

    override fun tryToCreate(spawn: StructureSpawn) {
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
                creep.moveTo(container.pos)
                creep.say("move")
            } else {
                creep.harvest(source)
                creep.say("mine")
            }
        }
    }

    private fun shouldCreate(): Boolean {
        return containerMap.size > creeps.size
    }

    private fun create(spawn: StructureSpawn) {
        val workerCount = (spawn.room.energyAvailable - 50) / 100
        if (workerCount < MAX_WORKER_BODY_COUNT) {
            return
        }

        val containerIds = containerMap.keys.toMutableSet()
        creeps.forEach { containerIds.remove(it.memory.containerId) }
        val targetId = containerIds.firstOrNull() ?: return

        val memory = object : CreepMemory {
            val role = CREEP_ROLE_MINER
            val containerId = targetId
        }
        val options = createSpawnOption(memory)

        val bodyList = mutableListOf(MOVE).apply {
            for (i in 0 until MAX_WORKER_BODY_COUNT) {
                add(WORK)
            }
        }
        val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(CREEP_ROLE_MINER), options)
        println("create new creep $CREEP_ROLE_MINER. code: $result, $bodyList")
    }

    companion object {

        private const val CREEP_ROLE_MINER = "miner"
        private const val MAX_WORKER_BODY_COUNT = 5

    }
}
