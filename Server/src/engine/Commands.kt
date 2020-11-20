package apoy2k.robby.engine

import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.UnknownCommandException

/**
 * Base class for all commands, handles serialization and comparing between command instances
 */
abstract class Command(val label: CommandLabel, vararg val parameters: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (label != other.label) return false
        if (!parameters.contentEquals(other.parameters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + parameters.contentHashCode()
        return result
    }

    override fun toString(): String {
        return listOf(label, parameters.joinToString(";")).joinToString(":")
    }
}

class SwitchFieldCommand(val id: String) : Command(CommandLabel.SWITCH_FIELD, id)
class RefreshBoardCommand(): Command(CommandLabel.REFRESH_BOARD)
class ResetBoardCommand(): Command(CommandLabel.RESET_BOARD)

/**
 * Convert a string to a typed Command instance
 * @throws UnknownCommandException if the string cannot be converted
 */
fun String.toCommand(): Command {
    val parts = this.split(":", ";")

    if (parts.count() == 0) {
        throw UnknownCommandException(this)
    }

    val label = CommandLabel.values().find { it.toString() == parts[0] } ?: throw UnknownCommandException(this)

    return when (label) {
        CommandLabel.SWITCH_FIELD -> SwitchFieldCommand(parts[1])
        CommandLabel.REFRESH_BOARD -> RefreshBoardCommand()
        CommandLabel.RESET_BOARD -> ResetBoardCommand()
    }
}
