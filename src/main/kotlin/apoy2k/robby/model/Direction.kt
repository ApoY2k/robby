package apoy2k.robby.model

enum class Direction {
    NONE,
    UP,
    RIGHT,
    DOWN,
    LEFT;

    fun isVertical() = this == UP || this == DOWN
    fun isHorizontal() = this == LEFT || this == RIGHT
}

data class Vec2(val x: Float, val y: Float)

fun Direction.toVec2() = when (this) {
    Direction.NONE -> Vec2(0f, 0f)
    Direction.UP -> Vec2(0f, 1f)
    Direction.RIGHT -> Vec2(1f, 0f)
    Direction.DOWN -> Vec2(0f, -1f)
    Direction.LEFT -> Vec2(-1f, 0f)
}
