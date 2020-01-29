package chentian.loop

import chentian.GameContext
import chentian.extensions.findClosest
import chentian.extensions.isBuildFinished
import chentian.extensions.isInTargetRoom
import chentian.extensions.memory.buildTargetId
import chentian.extensions.memory.homeRoomName
import chentian.extensions.memory.role
import chentian.extensions.memory.targetRoomName
import chentian.extensions.moveToTargetRoom
import chentian.strategy.CreepStrategyBuilderRemote
import chentian.strategy.CreepStrategyHarvester
import chentian.utils.harvestEnergyAndDoJobRemote
import chentian.utils.tryToBuild
import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.Game
import screeps.utils.lazyPerTick
import screeps.utils.toMap

/**
 * 远程建设
 * [CreepStrategyBuilderRemote]
 *
 * @author chentian
 */

val creepBuilderRemote: List<Creep> by lazyPerTick {
    Game.creeps.toMap().values.filter { it.memory.role == CreepStrategyBuilderRemote.CREEP_ROLE_BUILDER_REMOTE }
}

fun runBuilderRemote() {
    creepBuilderRemote.forEach { buildStructure(it) }
}

private fun buildStructure(creep: Creep) {
    harvestEnergyAndDoJobRemote(creep) {
        val targetRoom = GameContext.rooms[creep.memory.targetRoomName]
        val constructionsToBuild by lazy {
            targetRoom?.find(FIND_CONSTRUCTION_SITES)?.filter { !it.isBuildFinished() }.orEmpty()
        }

        // 全部建造完成
        if (targetRoom != null && constructionsToBuild.isEmpty()) {
            if (creep.isInTargetRoom(creep.memory.homeRoomName)) {
                // 转换为 Harvester
                creep.memory.role = CreepStrategyHarvester.CREEP_ROLE_HARVESTER
            } else {
                // 不在家，先回家
                creep.moveToTargetRoom(creep.memory.homeRoomName)
            }
            return@harvestEnergyAndDoJobRemote
        }

        // 已经有要建造的目标
        Game.getObjectById<ConstructionSite>(creep.memory.buildTargetId)?.let { target ->
            if (target.isBuildFinished()) {
                creep.memory.buildTargetId = ""
            } else if (tryToBuild(creep, target)) {
                return@harvestEnergyAndDoJobRemote
            }
            println("$creep build current target failed")
        }

        // 重新选择
        creep.findClosest(constructionsToBuild)?.let { target ->
            creep.memory.buildTargetId = target.id
            println("$creep change build target to ${target.id}")
            if (tryToBuild(creep, target)) {
                return@harvestEnergyAndDoJobRemote
            }
        }

        println("$creep build failed: no target")
    }
}
