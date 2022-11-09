package solver

import geode.BlockType
import geode.GeodeProjection
import solution.SolutionGroup

val OFFSETS = listOf(
    Vec2(-1, 0),
    Vec2(1, 0),
    Vec2(0, 1),
    Vec2(0, -1)
)

fun findPath(
    proj: GeodeProjection,
    start: Vec2,
    goal: Vec2,
    myGroup: SolutionGroup?,
    groups: Collection<SolutionGroup>
): List<Vec2> {
    data class Node(val pos: Vec2, val parent: Node?) {
        fun toPath(): List<Vec2> = listOf(*(parent?.toPath()?.toTypedArray() ?: arrayOf()), pos)
    }

    fun groupFor(pos: Vec2): SolutionGroup? =
        groups.find { pos in it.blockLocations }

    fun IntRange.expand(amount: Int = 1) =
        IntRange(this.first - amount, this.last + amount)

    val xRange = proj.xRange.expand()
    val yRange = proj.yRange.expand()

    fun isValid(pos: Vec2): Boolean {
        val group = groupFor(pos)
        return pos.x in xRange &&
                pos.y in yRange &&
                proj[pos] != BlockType.BUD &&
                (group == null || group == myGroup)
    }

    val startNode = Node(start, null)
    val queue = ArrayDeque<Node>()
    val visited: MutableSet<Vec2> = mutableSetOf()
    queue.add(startNode)
    visited.add(start)

    while (queue.isNotEmpty()) {
        val curr = queue.removeFirst()

        if (curr.pos == goal) return curr.toPath()

        for (offset in OFFSETS) {
            val newPos = curr.pos + offset
            if (isValid(newPos) && newPos !in visited) {
                queue.add(Node(newPos, curr))
                visited.add(newPos)
            }
        }
    }

    return listOf()
}