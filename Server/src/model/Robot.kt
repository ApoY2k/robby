package apoy2k.robby.model

import apoy2k.robby.exceptions.UnknownRobotModel

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
                ?: throw UnknownRobotModel(model)
            return Robot(robotModel)
        }
    }
}
