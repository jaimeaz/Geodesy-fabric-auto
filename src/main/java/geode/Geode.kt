package geode

import solver.Vec2
import solver.Vec3
import solver.Vec3Dir
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt

enum class StickyBlockType {
    SLIME,
    SLIME_OFFSET,
    HONEY,
    HONEY_OFFSET;
}

class Geode(val buds: List<Vec3>) {

    companion object {
        const val BUD_DENSITY = 0.083

        fun random(radius: Int, density: Double = BUD_DENSITY): Geode {
            return random(radius.toDouble(), density)
        }

        fun random(radius: Double, density: Double = BUD_DENSITY, crack: Boolean = true): Geode {
            val surfaceArea = 4.0 / 3.0 * PI * radius.pow(3)
            val numBuds = (surfaceArea * density).roundToInt()
            var buds = (0 until numBuds).map { Vec3.randomOnSphere(radius) }

            if (crack) {
                val crackPoint = Vec3.randomOnSphere(radius)
                val crackDist = (2 * PI * radius) / 8
                buds = buds.filter { it.dist(crackPoint) > crackDist }
            }

            return Geode(buds)
        }
    }

    fun toProjection(dir: Vec3Dir): GeodeProjection {
        val cells: MutableMap<Vec2, BlockType> = mutableMapOf()

        for (budPosition in buds) {
            val budXY = budPosition.without(dir)

            cells[budXY] = BlockType.BUD

            cells[budXY.left()] = BlockType.CRYSTAL.max(cells[budXY.left()] ?: BlockType.AIR)
            cells[budXY.right()] = BlockType.CRYSTAL.max(cells[budXY.right()] ?: BlockType.AIR)
            cells[budXY.up()] = BlockType.CRYSTAL.max(cells[budXY.up()] ?: BlockType.AIR)
            cells[budXY.down()] = BlockType.CRYSTAL.max(cells[budXY.down()] ?: BlockType.AIR)
        }

        // Remove completely surrounded crystals
        for ((loc, type) in cells) {
            if (type == BlockType.CRYSTAL && cells[loc.up()] == BlockType.BUD && cells[loc.down()] == BlockType.BUD && cells[loc.left()] == BlockType.BUD && cells[loc.right()] == BlockType.BUD) {
                cells[loc] = BlockType.BUD
            }
        }

        return GeodeProjection(cells)
    }

}