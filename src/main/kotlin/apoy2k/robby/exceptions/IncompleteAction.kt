package apoy2k.robby.exceptions

import apoy2k.robby.model.Action

class IncompleteAction(message: String) : Throwable(message) {
    constructor(action: Action) : this("Incomplete action [$action]")
}
