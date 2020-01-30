package chentian.strategy

import chentian.extensions.findCreepByRole
import chentian.extensions.transferAllTypeOrMove
import chentian.utils.createNormalCreep
import chentian.utils.harvestResourceAndDoJob
import screeps.api.Creep
import screeps.api.FIND_MINERALS
import screeps.api.FIND_STRUCTURES
import screeps.api.Room
import screeps.api.STRUCTURE_TERMINAL
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTerminal

/**
 * 从 Extractor 中挖矿
 *
 * @author chentian
 */
class CreepStrategyHarvesterResource(val room: Room) : CreepStrategy {

    private val creeps = room.findCreepByRole(CREEP_ROLE_HARVESTER_RESOURCE)
    private val miners = room.find(FIND_MINERALS).filter { it.mineralAmount > 0 }

    override fun tryToCreate(spawn: StructureSpawn) {
        if (shouldCreate()) {
            createNormalCreep(spawn, CREEP_ROLE_HARVESTER_RESOURCE)
        }
    }

    override fun runLoop() {
        creeps.forEach { fillEnergy(it) }
    }

    private fun shouldCreate(): Boolean {
        return creeps.size < MAX_CREEP_COUNT_PER_ROOM && miners.isNotEmpty()
    }

    private fun fillEnergy(creep: Creep) {
        harvestResourceAndDoJob(creep) {
            transferToTerminal(creep)
        }
    }

    /**
     * 传输到 terminal
     */
    private fun transferToTerminal(creep: Creep): Boolean {
        val terminal = room.find(FIND_STRUCTURES)
            .firstOrNull { it.structureType == STRUCTURE_TERMINAL }
            as? StructureTerminal
            ?: return false

        if (creep.transferAllTypeOrMove(terminal)) {
            return true
        }
        return false
    }

    companion object {

        private const val CREEP_ROLE_HARVESTER_RESOURCE = "harvester-resource"
        private const val MAX_CREEP_COUNT_PER_ROOM = 2
    }
}
