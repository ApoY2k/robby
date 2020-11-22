package apoy2k.robby.engine

import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.UnknownCommandException
import java.lang.IndexOutOfBoundsException

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

class JoinGameCommand(val name: String): Command(CommandLabel.JOIN_GAME, name)
class LeaveGameCommand(val name: String): Command(CommandLabel.LEAVE_GAME, name)
class PlaceRobotCommand(val fieldId: String, val model: String): Command(CommandLabel.PLACE_ROBOT, fieldId, model)
class SelectCardCommand(val cardId: String): Command(CommandLabel.SELECT_CARD, cardId)
class RemoveCardCommand(val cardId: String): Command(CommandLabel.JOIN_GAME, cardId)
class ConfirmCardsCommand(): Command(CommandLabel.CONFIRM_CARDS)
class ResetBoardCommand(): Command(CommandLabel.RESET_BOARD)
class RefreshViewCommand(val name: String): Command(CommandLabel.REFRESH_VIEW, name)

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

    try {
        return when (label) {
            CommandLabel.JOIN_GAME -> JoinGameCommand(parts[1])
            CommandLabel.LEAVE_GAME -> LeaveGameCommand(parts[1])
            CommandLabel.PLACE_ROBOT -> PlaceRobotCommand(parts[1], parts[2])
            CommandLabel.SELECT_CARD -> SelectCardCommand(parts[1])
            CommandLabel.REMOVE_CARD -> RemoveCardCommand(parts[1])
            CommandLabel.CONFIRM_CARDS -> ConfirmCardsCommand()
            CommandLabel.RESET_BOARD -> ResetBoardCommand()
            CommandLabel.REFRESH_VIEW -> RefreshViewCommand(parts[1])
        }
    } catch (err: IndexOutOfBoundsException) {
        throw UnknownCommandException(this)
    }
}
