package apoy2k.robby.exceptions

import apoy2k.robby.model.Command

class IncompleteCommandException(message: String) : Throwable(message) {
    constructor(command: Command) : this("Incomplete command [$command]")
}
