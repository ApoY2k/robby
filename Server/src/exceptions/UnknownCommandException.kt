package apoy2k.robby.exceptions

import apoy2k.robby.engine.Command
import java.lang.Exception

class UnknownCommandException(command: String) : Exception("Unknown command [$command]") {
    constructor(command: Command) : this(command.toString())
}
