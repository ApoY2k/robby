package apoy2k.robby.exceptions

class UnknownAction(action: String, cause: Throwable) : Throwable("Unknown action [$action]", cause)
