package chentian.utils

import chentian.GameContext
import chentian.extensions.energy
import chentian.extensions.findClosest
import chentian.extensions.firstResourceType
import chentian.extensions.isEmptyCarry
import chentian.extensions.isFullCarry
import chentian.extensions.isInTargetRoom
import chentian.extensions.isWorking
import chentian.extensions.memory.containerTargetId
import chentian.extensions.memory.harvestedEnergy
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.mineralTargetId
import chentian.extensions.memory.role
import chentian.extensions.memory.sourceTargetId
import chentian.extensions.memory.targetLinkId
import chentian.extensions.memory.targetRoomName
import chentian.extensions.memory.transferTargetId
import chentian.extensions.memory.withdrawTargetId
import chentian.extensions.moveToTargetRoom
import chentian.extensions.setWorking
import screeps.api.ActiveBodyPartConstant
import screeps.api.BODYPART_COST
import screeps.api.CARRY
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_BUSY
import screeps.api.ERR_NAME_EXISTS
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_MINERALS
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.FIND_TOMBSTONES
import screeps.api.Game
import screeps.api.IStructure
import screeps.api.LINE_STYLE_DOTTED
import screeps.api.MOVE
import screeps.api.Mineral
import screeps.api.MoveToOptions
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.RenewableHarvestable
import screeps.api.ResourceConstant
import screeps.api.RoomVisual
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.Source
import screeps.api.Store
import screeps.api.StoreOwner
import screeps.api.TOP
import screeps.api.WORK
import screeps.api.get
import screeps.api.options
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureSpawn
import screeps.api.value
import screeps.utils.unsafe.jsObject
import kotlin.math.min

/**
 *
 *
 * @author chentian
 */

fun createCreepName(role: String): String {
    return "creep_${role}_${Game.time}"
}

val BODY_PART_FOR_MINER_CREEP = listOf(MOVE, CARRY, WORK, WORK, WORK, WORK, WORK)
val BODY_COST_FOR_MINER_CREEP = BODY_PART_FOR_MINER_CREEP.sumBy { (BODYPART_COST[it])!! }

val BODY_PART_FOR_REMOTE_CREEP = mutableListOf(MOVE, MOVE, CARRY, CARRY, WORK, WORK)
val BODY_COST_FOR_REMOTE_CREEP = BODY_PART_FOR_REMOTE_CREEP.sumBy { (BODYPART_COST[it])!! }

fun createRemoteCreep(spawn: StructureSpawn, role: String, targetRoomName: String): Boolean {
    if (spawn.room.energyAvailable < BODY_COST_FOR_REMOTE_CREEP) {
        return false
    }

    return doCreateCreep(role, targetRoomName, spawn, BODY_PART_FOR_REMOTE_CREEP)
}

const val MAX_BODY_PART = 50
val BODY_PART_FOR_NORMAL_CREEP = listOf(MOVE, CARRY, CARRY, WORK, WORK)
val BODY_COST_FOR_NORMAL_CREEP = BODY_PART_FOR_NORMAL_CREEP.sumBy { (BODYPART_COST[it])!! }
val MAX_BODY_PART_COUNT_FOR_NORMAL_CREEP = MAX_BODY_PART / BODY_PART_FOR_NORMAL_CREEP.size

val BODY_PART_FOR_MIN_CREEP = listOf(MOVE, CARRY, WORK)
val BODY_COST_FOR_MIN_CREEP = BODY_PART_FOR_MIN_CREEP.sumBy { (BODYPART_COST[it])!! }

fun createNormalCreep(spawn: StructureSpawn, role: String = "", forceCreate: Boolean = false) {
    // 一个小房间，创建最基本的 strategy
    if (forceCreate || spawn.room.energyCapacityAvailable < BODY_COST_FOR_NORMAL_CREEP) {
        if (spawn.room.energyAvailable >= BODY_COST_FOR_MIN_CREEP) {
            doCreateCreep(role, "", spawn, BODY_PART_FOR_MIN_CREEP.toMutableList())
        }
        return
    }

    val partCount = min(spawn.room.energyAvailable / BODY_COST_FOR_NORMAL_CREEP, MAX_BODY_PART_COUNT_FOR_NORMAL_CREEP)
    if (partCount < 1) {
        return
    }

    // 储备不够，等待
    if (spawn.room.energyAvailable < spawn.room.energyCapacityAvailable / 2 && !spawn.isFullCarry()) {
        println("spawn $role: not enough energy")
        return
    }

    println("partCount: $partCount, max: $MAX_BODY_PART_COUNT_FOR_NORMAL_CREEP")
    // https://screeps.fandom.com/wiki/Creep#Fatigue
    val bodyList = mutableListOf<ActiveBodyPartConstant>().apply {
        for (i in 0 until partCount) {
            addAll(BODY_PART_FOR_NORMAL_CREEP)
        }
    }

    doCreateCreep(role, "", spawn, bodyList)
}

fun createMoveOptions(lineColor: String): MoveToOptions {
    val pathStyle = options<RoomVisual.ShapeStyle> {
        this.stroke = lineColor
        this.strokeWidth = .1
        this.opacity = .25
        this.lineStyle = LINE_STYLE_DOTTED
    }
    return options { visualizePathStyle = pathStyle }
}

private fun doCreateCreep(
    role: String,
    targetRoomName: String,
    spawn: StructureSpawn,
    bodyList: MutableList<ActiveBodyPartConstant>
): Boolean {
    if (spawn.spawning != null) {
        println("spawning, existing")
        return false
    }

    val result = spawn.spawnCreep(bodyList.toTypedArray(), createCreepName(role), options {
        memory = jsObject<CreepMemory> {
            this.role = role
            this.homeRoomName = spawn.room.name
            this.targetRoomName = targetRoomName
            this.homeRoomName = spawn.room.name
        }
    })
    println("create new strategy $role. code: $result, $bodyList")
    if (result != OK && result != ERR_BUSY && result != ERR_NAME_EXISTS) {
        Game.notify("create strategy $role error at ${spawn.room.name} code: ${result.value}. $bodyList")
    }
    return result == OK
}

private val moveToOptions = createMoveOptions("00aaff")

fun harvestEnergyAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullCarry()) {
        creep.memory.withdrawTargetId = ""
        creep.memory.transferTargetId = ""
        creep.memory.containerTargetId = ""
        creep.setWorking(true)
        creep.say("full")
        jobAction()
        return
    }

    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡坟墓上的，只捡能量
        if (tryToPickUpFromTomb(creep)) {
            return
        }

        // 捡地上掉的，只捡能量
        if (tryToPickUpFromGround(creep)) {
            return
        }

        // 特殊情况，没有 miner
        val minerCreepCount = GameContext.creepsMiner[creep.room.name].orEmpty().size
        if (minerCreepCount == 0) {
            tryToMineFromHarvestable(creep) {
                creep.pos.findClosestByPath(FIND_SOURCES)
            }
            return
        }

        // 已经有目标
        Game.getObjectById<StructureContainer>(creep.memory.withdrawTargetId)?.let { container ->
            tryToWithdraw(creep, container)
            return
        }

        // 找 Link
        Game.getObjectById<StructureLink>(creep.memory.targetLinkId)?.let { targetLink ->
            tryToWithdraw(creep, targetLink)
            return
        }

        // Container 存量不平衡
        val containers = creep.room.find(FIND_STRUCTURES).filter {
            it.structureType == STRUCTURE_CONTAINER
        }.map { it as StructureContainer }
        val minContainer = containers.minBy { it.store.energy() }
        val maxContainer = containers.maxBy { it.store.energy() }
        if (minContainer != null && maxContainer != null && minContainer.store.energy() * 6 < maxContainer.store.energy()) {
            tryToWithdraw(creep, maxContainer)
            println("$creep to full container $maxContainer")
            return
        }

        // 找最近的 container
        val containerList = creep.room
            .find(FIND_STRUCTURES)
            .filter { it.structureType == STRUCTURE_CONTAINER }
            .map { it as StructureContainer }
        creep.findClosest(containerList)?.let { container ->
            tryToWithdraw(creep, container)
            println("$creep is withdraw container")
            return
        }

        // 找最近的 source
        tryToMineFromHarvestable(creep) {
            creep.pos.findClosestByPath(FIND_SOURCES)
        }
    }

    creep.say("action")
    jobAction()
    return
}

fun harvestEnergyAndDoJobRemote(creep: Creep, jobAction: () -> Unit) {
    // 先去到目标房间
    if (!creep.isWorking() && !creep.isInTargetRoom(creep.memory.targetRoomName)) {
        creep.moveToTargetRoom(creep.memory.targetRoomName)
        return
    }

    // 已采集好资源
    if (creep.isFullCarry()) {
        creep.setWorking(true)
        creep.say("full")
        jobAction()
    }

    // 去采集资源
    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡坟墓上的，只捡能量
        if (tryToPickUpFromTomb(creep)) {
            return
        }

        // 捡地上掉的，只捡能量
        if (tryToPickUpFromGround(creep)) {
            return
        }

        if (!creep.isInTargetRoom(creep.memory.targetRoomName)) {
            return
        }

        // 从 source 中采集
        tryToMineFromHarvestable(creep) { ->
            val sourceList = creep.room.find(FIND_SOURCES).apply {
                sortBy { it.id }
            }
            val index = creep.name[creep.name.length - 2].toInt() % sourceList.size
            sourceList.getOrNull(index)
        }
        return
    }

    creep.say("action")
    jobAction()
}

fun harvestResourceAndDoJob(creep: Creep, jobAction: () -> Unit) {
    if (creep.isFullCarry()) {
        creep.memory.withdrawTargetId = ""
        creep.memory.transferTargetId = ""
        creep.memory.containerTargetId = ""
        creep.setWorking(true)
        creep.say("full")
        jobAction()
        return
    }

    if (creep.isEmptyCarry() || !creep.isWorking()) {
        creep.setWorking(false)

        val message = if (creep.isEmptyCarry()) "empty" else "fill"
        creep.say(message)

        // 捡坟墓上的，只捡非能量
        if (tryToPickUpFromTomb(creep, false)) {
            return
        }

        // 捡地上掉的，只捡非能量
        if (tryToPickUpFromGround(creep, false)) {
            return
        }

        // 找最近的 minerals
        tryToMineFromHarvestable(creep) {
            creep.pos.findClosestByPath(FIND_MINERALS)
        }
        return
    }

    creep.say("action")
    jobAction()
    return
}

private val MOVE_OPTION_BUILDER = createMoveOptions("#FFA500")

fun tryToBuild(creep: Creep, target: ConstructionSite): Boolean {
    return when (creep.build(target)) {
        OK -> {
            true
        }
        ERR_NOT_IN_RANGE -> {
            creep.moveTo(target.pos, MOVE_OPTION_BUILDER)
            true
        }
        else -> {
            println("$creep build ${target.structureType} failed at ${target.pos}, result: ${creep.build(target)}")
            false
        }
    }
}

private fun tryToPickUpFromTomb(creep: Creep, energyOnly: Boolean = true): Boolean {
    fun getType(store: Store): ResourceConstant? {
        if (energyOnly) {
            return RESOURCE_ENERGY
        }
        return store.firstResourceType()
    }

    creep.pos.findInRange(FIND_TOMBSTONES, 2).firstOrNull { it.store.energy() > 0 }?.let { tombstone ->
        val resourceType = getType(tombstone.store) ?: return false
        if (creep.withdraw(tombstone, resourceType) == ERR_NOT_IN_RANGE) {
            creep.moveTo(tombstone.pos, moveToOptions)
        }
        println("$creep is withdrawing tombstone at $tombstone")
        return true
    }
    return false
}

private fun tryToPickUpFromGround(creep: Creep, energyOnly: Boolean = true): Boolean {
    fun isTypeMatch(resourceType: ResourceConstant): Boolean {
        return if (energyOnly) {
            resourceType == RESOURCE_ENERGY
        } else {
            resourceType != RESOURCE_ENERGY
        }
    }

    creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1).firstOrNull()?.let { resource ->
        if (isTypeMatch(resource.resourceType) && creep.pickup(resource) == OK) {
            println("$creep is picking up resource at $resource")
            return true
        }
    }
    return false
}

private fun tryToMineFromHarvestable(creep: Creep, findSourceAction: () -> RenewableHarvestable?) {
    fun harvestOrMove(creep: Creep, harvestable: RenewableHarvestable) {
        val (result, pos) = when (harvestable) {
            is Source -> {
                Pair(creep.harvest(harvestable), harvestable.pos)
            }
            is Mineral -> {
                Pair(creep.harvest(harvestable), harvestable.pos)
            }
            else -> {
                return
            }
        }
        if (result == ERR_NOT_IN_RANGE) {
            creep.moveTo(pos, moveToOptions)
        }
        if (result == OK) {
            creep.memory.harvestedEnergy += 2 * creep.body.count { it.type == WORK }
        }
        println("$creep is harvesting")
    }

    Game.getObjectById<Source>(creep.memory.sourceTargetId)?.let { source ->
        harvestOrMove(creep, source)
        return
    }

    Game.getObjectById<Mineral>(creep.memory.mineralTargetId)?.let { source ->
        harvestOrMove(creep, source)
        return
    }

    findSourceAction()?.let { harvestable ->
        creep.memory.sourceTargetId = (harvestable as? Source)?.id.orEmpty()
        creep.memory.mineralTargetId = (harvestable as? Mineral)?.id.orEmpty()
        harvestOrMove(creep, harvestable)
        return
    }
}

private fun tryToWithdraw(creep: Creep, container: IStructure) {
    fun shouldMove(): Boolean {
        val miners = GameContext.creepsMiner[creep.room.name].orEmpty()
        return creep.pos.isEqualTo(container.pos) && miners.any { it.pos.isNearTo(creep.pos) }
    }

    creep.memory.sourceTargetId = ""
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    creep.memory.withdrawTargetId = if ((container as? StoreOwner)?.store?.energy() ?: 0 == 0) "" else container.id

    if (creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
        creep.moveTo(container.pos, moveToOptions)
    } else if (shouldMove()) {
        // 让开采矿的位置
        creep.move(TOP)
    }
}
