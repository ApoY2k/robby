package apoy2k.robby.exceptions

class UnknownRobotModelException(model: String) : Exception("Unknown robot model [$model]") {}
