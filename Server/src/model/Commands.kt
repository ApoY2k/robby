package apoy2k.robby.model

import apoy2k.robby.CommandField
import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.IncompleteCommandException
import apoy2k.robby.exceptions.UnknownCommandException
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.Charset

/**
 * Base class for all commands, handles serialization and comparing between command instances
 */
abstract class Command {
    /**
     * Player that issued the command (null if server command)
     */
    var sender: Player? = null

    /**
     * Players that should receive the command (empty: broadcast to all players)
     */
    val recipients = emptySet<Player>()

    private var parameters: List<NameValuePair> = ArrayList()

    constructor(label: CommandLabel, recipients: Set<Player>) : this(label, emptyMap(), recipients)

    constructor(label: CommandLabel, rest: Map<CommandField, String?>, recipients: Set<Player>) {
        parameters = listOf(BasicNameValuePair(CommandField.LABEL.name, label.name))
            .plus(rest.filter { !it.value.isNullOrBlank() }.map { BasicNameValuePair(it.key.name, it.value) })
    }

    companion object {

        /**
         * Convert a url encoded query string to a typed Command instance
         * @throws UnknownCommandException if the string cannot be converted
         */
        fun fromString(input: String): Command {
            try {
                val query = URLEncodedUtils.parse(input, Charset.defaultCharset())
                val labelField = query.firstOrNull { it.name == CommandField.LABEL.name }?.value ?: "no_label"

                return when (CommandLabel.valueOf(labelField)) {
                    CommandLabel.JOIN_GAME -> JoinGameCommand(query.first(CommandField.PLAYER_NAME))
                    CommandLabel.LEAVE_GAME -> LeaveGameCommand()
                    CommandLabel.PLACE_ROBOT -> PlaceRobotCommand(
                        query.first(CommandField.FIELD_ID),
                        query.first(CommandField.MODEL)
                    )
                    CommandLabel.SELECT_CARD -> SelectCardCommand(query.first(CommandField.CARD_ID))
                    CommandLabel.REMOVE_CARD -> RemoveCardCommand(query.first(CommandField.CARD_ID))
                    CommandLabel.CONFIRM_CARDS -> ConfirmCardsCommand()
                    CommandLabel.RESET_BOARD -> ResetBoardCommand()
                    CommandLabel.REFRESH_VIEW -> RefreshViewCommand(query.first(CommandField.VIEW_NAME))
                }
            } catch (err: Throwable) {
                throw UnknownCommandException(input, err)
            }
        }
    }

    fun get(field: CommandField): List<String> {
        return parameters.filter { field.name == it.name }.map { it.value }
    }

    fun getFirst(field: CommandField): String? {
        return get(field).firstOrNull()
    }

    override fun toString(): String {
        return URLEncodedUtils.format(parameters.filter { !it.value.isNullOrBlank() }, Charset.defaultCharset())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        return parameters.hashCode()
    }
}

fun List<NameValuePair>.first(field: CommandField): String? {
    return this.firstOrNull { it.name == field.name }?.value
}

class JoinGameCommand(name: String?, recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.JOIN_GAME, mapOf(CommandField.PLAYER_NAME to name), recipients) {
    val name get() = getFirst(CommandField.PLAYER_NAME)
}

class LeaveGameCommand(recipients: Set<Player> = emptySet()) : Command(CommandLabel.LEAVE_GAME, recipients)

class PlaceRobotCommand(fieldId: String?, model: String?, recipients: Set<Player> = emptySet()) :
    Command(
        CommandLabel.PLACE_ROBOT,
        mapOf(CommandField.FIELD_ID to fieldId, CommandField.MODEL to model),
        recipients
    ) {
    val fieldId get() = getFirst(CommandField.FIELD_ID)
    val model get() = getFirst(CommandField.MODEL)
}

class SelectCardCommand(cardId: String?, recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.SELECT_CARD, mapOf(CommandField.CARD_ID to cardId), recipients) {
    val cardId get() = getFirst(CommandField.CARD_ID)
}

class RemoveCardCommand(cardId: String?, recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.JOIN_GAME, mapOf(CommandField.CARD_ID to cardId), recipients) {
    val cardId get() = getFirst(CommandField.CARD_ID)
}

class ConfirmCardsCommand(recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.CONFIRM_CARDS, recipients)

class ResetBoardCommand(recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.RESET_BOARD, recipients)

class RefreshViewCommand(name: String?, recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.REFRESH_VIEW, mapOf(CommandField.VIEW_NAME to name), recipients) {
    val name get() = getFirst(CommandField.VIEW_NAME)
}
