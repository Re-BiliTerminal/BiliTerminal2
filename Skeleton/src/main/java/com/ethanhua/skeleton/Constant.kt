package com.ethanhua.skeleton

/**
 * @author darren
 * @date 20-11-12
 */
enum class Direction(val value: Int) {
    LEFT_TO_RIGHT(0),
    TOP_TO_BOTTOM(1),
    RIGHT_TO_LEFT(2),
    BOTTOM_TO_TOP(3)
}

enum class RepeatMode(val value: Int) {
    RESTART(1),
    REVERSE(2)
}

enum class Shape(val value: Int) {
    LINEAR(0),
    RADIAL(1)
}