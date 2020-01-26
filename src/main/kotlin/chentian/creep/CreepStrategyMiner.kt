package chentian.creep

import chentian.GameContext
import chentian.extensions.containerId
import chentian.extensions.energy
import chentian.extensions.energyCapacity
import chentian.extensions.findStructureMapByType
import chentian.extensions.homeRoomName
import chentian.extensions.role
import chentian.utils.BODY_COST_FOR_MINER_CREEP
import chentian.utils.BODY_PART_FOR_MINER_CREEP
import chentian.utils.createCreepName
import chentian.utils.createMoveOptions
import screeps.api.CreepMemory
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_LINK
import screeps.api.Source
import screeps.api.options
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

/**
 *
 *
 * @author chentian
 */
class CreepStrategyMiner(room: Room) : CreepStrategy {

    private val containerMap = room.findStructureMapByType(STRUCTURE_CONTAINER)
    private val creeps = GameContext.creepsMiner[room.name].orEmpty()

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            create(spawn)
        }
    }

    override fun runLoop() {
        creeps.forEach { creep ->
            val container = containerMap[creep.memory.containerId] as? StructureContainer
            val source: Source? = container?.pos?.findInRange(FIND_SOURCES, 1)?.getOrNull(0)
            if (source == null) {
                creep.say("error")
                println("source not found: ${creep.name}")
                return@forEach
            }

            // 移动到 container 的位置
            if (!creep.pos.isEqualTo(container.pos)) {
                creep.moveTo(container.pos, MOVE_OPTION)
                creep.say("move")
                return
            }

            // 传输到 link
            if (container.store.energy() >= MIN_CONTAINER_ENERGY && creep.store.energy() >= ENERGY_AMOUNT_TO_LINK) {
                container.pos.findInRange(FIND_STRUCTURES, 1).firstOrNull {
                    it.structureType == STRUCTURE_LINK
                }?.let {
                    val link = it as StructureLink
                    if (link.store.energyCapacity() >= link.store.energy() + ENERGY_AMOUNT_TO_LINK) {
                        creep.transfer(link, RESOURCE_ENERGY)
                        return
                    }
                }
            }

            // 正常采集
            creep.harvest(source)
            creep.say("mine")
        }
    }

    private fun shouldCreate(): Boolean {
        return containerMap.size > creeps.size
    }

    private fun create(spawn: StructureSpawn) {
        if (spawn.room.energyAvailable < BODY_COST_FOR_MINER_CREEP) {
            return
        }

        val containerIds = containerMap.keys.toMutableSet()
        creeps.forEach { containerIds.remove(it.memory.containerId) }
        val targetId = containerIds.firstOrNull() ?: return

        val result = spawn.spawnCreep(BODY_PART_FOR_MINER_CREEP.toTypedArray(), createCreepName(CREEP_ROLE_MINER), options {
            memory = jsObject<CreepMemory> {
                this.role = CREEP_ROLE_MINER
                this.homeRoomName = spawn.room.name
                this.containerId = targetId
            }
        })
        println("create new creep $CREEP_ROLE_MINER. code: $result, $BODY_PART_FOR_MINER_CREEP")
    }

    companion object {

        const val CREEP_ROLE_MINER = "miner"

        private val MOVE_OPTION = createMoveOptions("#584f60")
        private const val ENERGY_AMOUNT_TO_LINK = 50
        private const val MIN_CONTAINER_ENERGY = 500
    }
}
