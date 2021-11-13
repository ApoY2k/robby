package apoy2k.robby.exceptions

import apoy2k.robby.model.Action

class UnknownAction(action: String, cause: Throwable) : Throwable("Unknown action [$action]", cause)
