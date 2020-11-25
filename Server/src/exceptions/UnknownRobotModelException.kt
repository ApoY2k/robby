package apoy2k.robby.exceptions

class UnknownRobotModelException(model: String) : Throwable("Unknown robot model [$model]") {}
