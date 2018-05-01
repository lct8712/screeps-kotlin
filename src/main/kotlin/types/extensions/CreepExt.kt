package types.extensions

import traveler.TravelToOptions
import traveler.TravelerCreep
import types.base.global.ScreepsReturnCode
import types.base.prototypes.Creep
import types.base.prototypes.RoomPosition

/**
 * Use Traveler to travel to target destination.
 */
fun Creep.travelTo(target: RoomPosition, travelToOptions: TravelToOptions? = null): ScreepsReturnCode {
    return if (travelToOptions == null) {
        (this as TravelerCreep).travelTo(target)
    } else {
        (this as TravelerCreep).travelTo(target, travelToOptions)
    }
}