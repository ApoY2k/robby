package apoy2k.robby.exceptions

import apoy2k.robby.model.Command

class UnknownCommandException : Throwable {
    constructor(command: String) : super("Unknown command [$command]")
    constructor(command: String, cause: Throwable) : super("Unknown command [$command]", cause)
    constructor(command: Command) : this(command.toString())
}
