package solution

import geode.GeodeProjection

interface Solver {
    fun name(): String

    fun solve(proj: GeodeProjection): Solution
}