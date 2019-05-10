package screeps.game.one

import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Game
import screeps.api.Memory
import screeps.api.Room
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureController
import screeps.game.one.Stats.stats

object Stats {

    const val FN_PREFIX = "cpu.usage.fn."
    private const val RESET_TICK_TIMEOUT = 19 // 60s / 3.2 (s/tick) = 19 ticks
    private var globalreset = true // is initialized on global reset
    private var resetInTicks = RESET_TICK_TIMEOUT
    private val efficiencyStats = UpgradeEfficiencyStats()

    var Memory.stats: dynamic
        get() = this.asDynamic().stats
        set(value) = run { this.asDynamic().stats = value }

    init {
        if (Memory.stats == null) {
            Memory.stats = Any()
        }
    }

    fun write(room: Room) {
        efficiencyStats.record(room)

        val roomName = "rooms.${room.name}"

        Memory.stats["$roomName.mine"] = room.controller?.level ?: 0
        Memory.stats["$roomName.energyAvailable"] = room.energyAvailable
        Memory.stats["$roomName.energyCapacityAvailable"] = room.energyCapacityAvailable
        room.storage?.let {
            Memory.stats["$roomName.storage"] = it.store
        }

        room.controller?.let {
            val controllerName = "$roomName.controller"
            Memory.stats["$controllerName.level"] = it.level
            Memory.stats["$controllerName.progress"] = it.progress
            Memory.stats["$controllerName.progressTotal"] = it.progressTotal

            room.find(FIND_STRUCTURES).filter { structure ->
                structure.structureType == STRUCTURE_CONTAINER
            }.map { structure ->
                structure as StructureContainer
            }.forEachIndexed { index, structureContainer ->
                val containerName = "$roomName.container$index"
                Memory.stats["$containerName.energy"] = structureContainer.store.energy
            }
        }
    }

    fun write(key: String, value: Any) {
        Memory.stats[key] = value
    }

    fun tickStarts() {
        resetInTicks -= 1
        if (resetInTicks < 0) {
            resetInTicks = RESET_TICK_TIMEOUT
            Memory.stats = Any()
        }
    }

    fun tickEnds() {
        Memory.stats["cpu.used"] = Game.cpu.getUsed()
        Memory.stats["cpu.limit"] = Game.cpu.limit
        Memory.stats["cpu.bucket"] = Game.cpu.bucket

        if (globalreset) {
            Memory.stats["globalreset"] = Game.time
            globalreset = false
        }
    }

    fun <R> profiled(name: String, prefix: String? = null, block: () -> R): R {
        val key = FN_PREFIX + if (prefix != null) ".$prefix." else "" + name
        val cpuBefore = Game.cpu.getUsed()

        val result = block()

        val cpuUsed = Game.cpu.getUsed() - cpuBefore
        Memory.stats[key] = cpuUsed
        return result
    }
}

class UpgradeEfficiencyStats {

    private val roomMap = mutableMapOf<String, UpgradeEfficiencyStatsRoom>()

    fun record(room: Room) {
        val roomName = "rooms.${room.name}"
        var stats = roomMap[roomName]
        if (stats == null) {
            stats = UpgradeEfficiencyStatsRoom(roomName, room.find(FIND_SOURCES).size)
            roomMap[roomName] = stats
        }
        room.controller?.let { stats.record(it) }
    }
}

class UpgradeEfficiencyStatsRoom(val roomName: String, sourceCount: Int) {

    private val controllerName = "$roomName.controller"
    private val energyExpected: Float = ENERGY_PER_SOURCE * COUNT_PER_STATS * sourceCount

    private var recordCount = 0
    private var totalDiffProgress = 0

    fun record(controller: StructureController) {
        (Memory.stats["$controllerName.progress"] as Int?)?.let { lastProgress ->
            val diff: Int = controller.progress - lastProgress
            totalDiffProgress += diff
        }
        if (recordCount++ >= COUNT_PER_STATS) {
            Memory.stats["$controllerName.efficiency"] = totalDiffProgress / energyExpected
            totalDiffProgress = 0
            recordCount = 0
        }
    }

    companion object {
        /**
         * 每 100 tick 记录一次
         */
        private const val COUNT_PER_STATS = 100

        /**
         * 每 tick 每个 source 的产出
         */
        private const val ENERGY_PER_SOURCE = 10f
    }
}

fun <R> profiled(name: String, prefix: String? = null, block: () -> R) = Stats.profiled(name, prefix, block)
