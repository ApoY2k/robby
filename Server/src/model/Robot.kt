package apoy2k.robby.model

import apoy2k.robby.exceptions.UnknownRobotModel
import java.util.*

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

enum class Orientation {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

data class Robot(val model: RobotModel, val id: UUID = UUID.randomUUID()) {
    var orientation = Orientation.DOWN
}
