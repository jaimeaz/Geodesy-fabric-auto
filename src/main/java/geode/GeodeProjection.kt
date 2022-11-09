package geode

import solver.Vec2
import solution.ANSI_GRAY
import solution.ANSI_RESET
import java.io.File

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
        fun fromFile(path: String): List<GeodeProjection> {
            val geodes = mutableListOf<GeodeProjection>()

            val lines = File(path).readLines()
            var curr = mutableMapOf<Vec2, BlockType>()
            var row = 0
            for (line in lines) {
                if (line == "") {
                    geodes.add(GeodeProjection(curr))
                    curr = mutableMapOf()
                    row = 0
                    continue
                }

                for ((i, block) in line.filterIndexed { i, _ -> i % 2 == 0 }.withIndex()) {
                    curr[Vec2(i, row)] = when (block) {
                        '.' -> BlockType.CRYSTAL
                        '#' -> BlockType.BUD
                        else -> BlockType.AIR
                    }
                }
                row += 1
            }

            return geodes
        }
    }
}