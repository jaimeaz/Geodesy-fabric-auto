import geode.BlockType
import geode.GeodeProjection
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import solution.Solution
import solver.Solver
import solver.Vec2
import java.io.File
import kotlin.math.roundToInt

internal class SolverTest {

    @Test
    fun solve() {
        val file = "./src/test/resources/geodes.txt"
        val solver = Solver()

        val geodes = fromFile(file).subList(0, 1000)

        var percentTotals = 0.0
        var groupTotals = 0.0
        var blockTotals = 0.0
        val invalidSolutions = mutableListOf<Solution>()
        var worstSolution: Solution? = null
        var bestSolution: Solution? = null
        var bunnySolution: Solution? = null

        var iterations = 1
        geodes.forEach { proj ->
            println("$iterations/${geodes.size}")

            // Solve the for each geode projection
            val solution = solver.solve(proj)

            val percent = solution.crystalPercentage()
            percentTotals += percent
            groupTotals += solution.crystalCount() / solution.groupCount().toDouble()
            blockTotals += solution.stickyBlockCount().toDouble() / solution.crystalCount()
            if (solution.checkIfValid().isNotEmpty()) {
                invalidSolutions.add(solution)
            }
            if (worstSolution == null || worstSolution!!.betterThan(solution)) {
                worstSolution = solution
            }
            if (bestSolution == null || solution.betterThan(bestSolution!!)) {
                bestSolution = solution
            }
            if (iterations == 1) {
                bunnySolution = solution
            }

            iterations++
        }

        println("  Avg. Crystal Percentage: ${p(percentTotals / iterations)}%")
        println("  Avg. Crystals / Group : ${r(groupTotals / iterations)}")
        println("  Avg. Blocks / Crystal: ${r(blockTotals / iterations)}")
        println("  Num of Invalid Solutions: ${invalidSolutions.size} (${invalidSolutions.size / iterations * 100}%)")
        show(worstSolution, "Worst")
        show(bestSolution, "Best")
        show(bunnySolution, "\"Bunny\"")
        println()

        assertTrue(invalidSolutions.isEmpty())
    }

    private fun fromFile(path: String): List<GeodeProjection> {
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

    private fun p(num: Double): String =
        "${(num * 10000).roundToInt().toDouble() / 100}"

    private fun r(num: Double): String =
        "${(num * 100).roundToInt().toDouble() / 100}"

    private fun show(solution: Solution?, name: String) {
        if (solution != null && solution.groupCount() != 0) {
            println(
                "  $name Solution: ${p(solution.crystalPercentage())}% crystals, ${solution.groupCount()} groups, ${solution.stickyBlockCount()} blocks for ${solution.crystalCount()} crystals (${
                    r(
                        solution.crystalCount().toDouble() / solution.groupCount()
                    )
                } Crystals / Group)"
            )
            solution.prettyPrint(true)
        }
    }
}
