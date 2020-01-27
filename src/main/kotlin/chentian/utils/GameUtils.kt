package chentian.utils

/**
 *
 *
 * @author chentian
 */

fun runHouseKeeping() {
    js(
        """
        for (var name in Memory.creeps) {
            if (!Game.creeps[name]) {
                delete Memory.creeps[name];
                console.log('Clearing non-existing strategy memory:', name);
            }
        }
        """
    )
}
