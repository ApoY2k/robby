package apoy2k.robby.exceptions

class UnknownRobotModel(model: String) : Throwable("Unknown robot model [$model]") {}
