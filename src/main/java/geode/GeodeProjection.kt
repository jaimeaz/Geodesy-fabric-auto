package geode

import geode.StickyBlockType.Companion.isOffset
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import pl.kosma.geodesy.GeodesyCore
import pl.kosma.geodesy.GeodesyCore.WALL_OFFSET
import pl.kosma.geodesy.IterableBlockBox
import solution.ANSI_GRAY
import solution.ANSI_RESET
import solver.Vec2

enum class BlockType {
    AIR,
    CRYSTAL,
    BUD;

    fun max(other: BlockType) =
        when {
            this == BUD || other == BUD -> BUD
            this == CRYSTAL || other == CRYSTAL -> CRYSTAL
            else -> AIR
        }

}

class GeodeProjection(private val cells: Map<Vec2, BlockType>) {
    fun IntRange.expand(amount: Int = 1) =
        IntRange(this.first - amount, this.last + amount)


    val xRange =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.x }!!..cells.keys.maxOfOrNull { it.x }!!

    val yRange =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.y }!!..cells.keys.maxOfOrNull { it.y }!!

    fun isInBounds(cell: Vec2) = cell.x in xRange.expand() && cell.y in yRange.expand()

    operator fun get(x: Int, y: Int): BlockType =
        get(Vec2(x, y))

    operator fun get(pos: Vec2): BlockType =
        cells[pos] ?: BlockType.AIR

    fun crystals() =
        cells.entries.filter { it.value == BlockType.CRYSTAL }.map { it.key }

    fun bud() =
        cells.entries.filter { it.value == BlockType.BUD }.map { it.key }

    fun bridges() =
        cells
            .entries
            .filter { it.value == BlockType.CRYSTAL }
            .flatMap { it.key.neighbors() }
            .filter { cell ->
                this[cell] == BlockType.AIR && cell.neighbors().map { this[it] }.count { it == BlockType.CRYSTAL } >= 2
            }

    fun print() {
        for (y in yRange.expand()) {
            for (x in xRange.expand()) {
                when (get(x, y)) {
                    BlockType.AIR -> print(" ")
                    BlockType.CRYSTAL -> print("$ANSI_GRAY..$ANSI_RESET")
                    BlockType.BUD -> print("$ANSI_GRAY##$ANSI_RESET")
                }
            }
            println()
        }
    }

    companion object {

        private fun getProjectedPos(geodeBox: IterableBlockBox, blockPos: BlockPos, direction: Direction): Vec2 {
            return when (direction) {
                Direction.EAST, Direction.WEST -> Vec2(geodeBox.maxY - blockPos.y, geodeBox.maxZ - blockPos.z)
                Direction.UP, Direction.DOWN -> Vec2(geodeBox.maxX - blockPos.x, geodeBox.maxZ - blockPos.z)
                Direction.SOUTH, Direction.NORTH -> Vec2(geodeBox.maxX - blockPos.x, geodeBox.maxY - blockPos.y)
            }
        }

        @JvmStatic
        fun getWorldPos(geodeBox: IterableBlockBox, projectedPos: Vec2, type: StickyBlockType, direction: Direction): BlockPos {
            val offset = if(isOffset(type)) WALL_OFFSET + 1 else WALL_OFFSET + 2
            return when (direction) {
                Direction.EAST -> BlockPos(geodeBox.maxX + offset, geodeBox.maxY - projectedPos.x, geodeBox.maxZ - projectedPos.y)
                Direction.WEST -> BlockPos(geodeBox.minX - offset, geodeBox.maxY - projectedPos.x, geodeBox.maxZ - projectedPos.y)
                Direction.UP -> BlockPos(geodeBox.maxX - projectedPos.x, geodeBox.maxY + offset, geodeBox.maxZ - projectedPos.y)
                Direction.DOWN -> BlockPos(geodeBox.maxX - projectedPos.x, geodeBox.maxY - offset, geodeBox.maxZ - projectedPos.y)
                Direction.SOUTH -> BlockPos(geodeBox.maxX - projectedPos.x, geodeBox.maxY - projectedPos.y, geodeBox.maxZ + offset)
                Direction.NORTH -> BlockPos(geodeBox.maxX - projectedPos.x, geodeBox.maxY - projectedPos.y, geodeBox.maxZ - offset)
            }
        }

        @JvmStatic
        fun fromGeodesyCore(geodeCore: GeodesyCore, direction: Direction): GeodeProjection {
            val cells = mutableMapOf<Vec2, BlockType>()

            for (blockPos in geodeCore.buddingAmethystPositions!!) {
                val projectedPos: Vec2 = getProjectedPos(geodeCore.geode!!, blockPos, direction)
                cells[projectedPos] = BlockType.BUD
            }

            for (blockPosDirectionPair in geodeCore.amethystClusterPositions!!) {
                if (geodeCore.world.getBlockState(blockPosDirectionPair.left).block === Blocks.AMETHYST_CLUSTER) {
                    val projectedPos: Vec2 = getProjectedPos(geodeCore.geode!!, blockPosDirectionPair.left, direction)
                    if (!cells.containsKey(projectedPos)) cells[projectedPos] = BlockType.CRYSTAL
                }
            }

            for (x in 0 until 16) {
                for (y in 0 until 16) {
                    val pos = Vec2(x, y)
                    if (!cells.containsKey(pos)) cells[pos] = BlockType.AIR
                }
            }

            return GeodeProjection(cells)
        }
    }
}