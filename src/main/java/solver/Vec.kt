package solver

import kotlin.math.*

data class Vec2(val x: Int, val y: Int) {

    fun left() = Vec2(x - 1, y)
    fun right() = Vec2(x + 1, y)
    fun up() = Vec2(x, y - 1)
    fun down() = Vec2(x, y + 1)

    fun neighbors() = listOf(left(), right(), up(), down())

    override fun toString() = "(${x}, ${y})"

    operator fun plus(other: Vec2) = Vec2(this.x + other.x, this.y + other.y)

    fun isNeighborOf(other: Vec2) =
        this.left() == other || this.right() == other || this.up() == other || this.down() == other
}

enum class Vec3Dir {
    X,
    Y,
    Z
}

data class Vec3(val x: Int, val y: Int, val z: Int) {

    companion object {
        fun randomOnSphere(radius: Double): Vec3 {
            // adapted from https://math.stackexchange.com/a/1586015/713643
            val z = Math.random() * 2.0 * radius - radius

            val angle = Math.random() * 2 * PI
            val percent = sqrt(radius.pow(2) - z.pow(2)) / radius
            val x = cos(angle) * radius * percent
            val y = sin(angle) * radius * percent

            return Vec3(x.roundToInt(), y.roundToInt(), z.roundToInt())
        }
    }

    fun withoutX() = Vec2(y, z)
    fun withoutY() = Vec2(x, z)
    fun withoutZ() = Vec2(x, y)

    fun without(dir: Vec3Dir) = when (dir) {
        Vec3Dir.X -> withoutX()
        Vec3Dir.Y -> withoutY()
        Vec3Dir.Z -> withoutZ()
    }

    fun dist(other: Vec3): Double {
        val dx = (this.x - other.x).toDouble()
        val dy = (this.y - other.y).toDouble()
        val dz = (this.z - other.z).toDouble()

        return sqrt(dx.pow(2) + dy.pow(2) + dz.pow(2))
    }

    override fun toString() = "(${x}, ${y}, ${z})"
}
