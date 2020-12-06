package apoy2k.robby.exceptions

import apoy2k.robby.model.Action

class UnknownAction : Throwable {
    constructor(action: String) : super("Unknown action [$action]")
    constructor(action: String, cause: Throwable) : super("Unknown action [$action]", cause)
    constructor(action: Action) : this(action.toString())
}
