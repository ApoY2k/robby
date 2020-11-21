package apoy2k.robby.model

import apoy2k.robby.exceptions.UnknownRobotModelException

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

data class Robot(val model: RobotModel) {
    companion object {
        fun create(model: String): Robot {
            val robotModel = RobotModel.values().find { it.toString() == model }
                ?: throw UnknownRobotModelException(model)
            return Robot(robotModel)
        }
    }
}
