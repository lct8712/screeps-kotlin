package screeps.game.one

import types.base.global.CreepMemory
import types.base.global.Memory
import types.base.global.get
import types.base.global.set
import types.base.prototypes.Room
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MemoryDelegate<R : Any?, T>(private val key: String, private val default: T, private val memoryObjectName: String? = null) : ReadWriteProperty<R, T> {

    private val memoryObject: dynamic
        get() {
            if (memoryObjectName == null) {
                return Memory
            } else {
                if (Memory[memoryObjectName] == null) {
                    Memory[memoryObjectName] = Any()
                }
                return Memory[memoryObjectName]
            }
        }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (memoryObject[key] == null) return default

        return memoryObject[key][property.name] as T? ?: default
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        if (memoryObject[key] == null) {
            memoryObject[key] = Any()
        }
        memoryObject[key][property.name] = value
    }
}

fun <T> Room.memory() = MemoryDelegate<Any?, T?>(this.name, null, "rooms")
fun <T : Any> Room.memoryOrDefault(default: T) = MemoryDelegate<Nothing?, T>(this.name, default, "rooms")

class CreepMemoryDelegate<T>(private val default: T) : ReadWriteProperty<CreepMemory, T> {

    override fun getValue(thisRef: dynamic, property: KProperty<*>): T {
        return thisRef[property.name] as T? ?: default
    }

    override fun setValue(thisRef: dynamic, property: KProperty<*>, value: T) {
        thisRef[property.name] = value
    }
}

fun <T> memory() = CreepMemoryDelegate<T?>(null)
fun <T : Any> memoryOrDefault(default: T) = CreepMemoryDelegate(default)


var CreepMemory.magicCounter: Int by memoryOrDefault(0)
var CreepMemory.someValue: Int by memoryOrDefault(0)

class H {
    val t: String by MemoryDelegate("bla", "")
}