package solver

import geode.StickyBlockType
import solution.Solution
import solution.SolutionGroup

val PREFERRED_STICK_BLOCK_ORDER = listOf(
    StickyBlockType.SLIME,
    StickyBlockType.HONEY,
    StickyBlockType.SLIME_OFFSET,
    StickyBlockType.HONEY_OFFSET
)

private fun solve(
    solution: Solution,
    currentAssignments: Map<SolutionGroup, StickyBlockType>
): Map<SolutionGroup, StickyBlockType>? {
    if (currentAssignments.size == solution.groupCount()) return currentAssignments

    val newGroup = (solution.groups - currentAssignments.keys).first()
    val neighbors = solution.neighborsOfGroup(newGroup)
    val neighborTypes = neighbors.mapNotNull { currentAssignments[it] }.toSet()

    for (type in PREFERRED_STICK_BLOCK_ORDER - neighborTypes) {
        val newAssignments = currentAssignments.toMutableMap()
        newAssignments[newGroup] = type
        val possibleSolution = solve(solution, newAssignments)
        if (possibleSolution != null) return possibleSolution
    }

    return null
}

fun solveStickyType(solution: Solution): Solution {
    // It is safe to unwrap by the four color theorem https://en.wikipedia.org/wiki/Four_color_theorem
    val assignments = solve(solution, mutableMapOf())!!
    assignments.forEach { (group, type) -> group.blockType = type }
    return solution
}