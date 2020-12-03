package apoy2k.robby.model

import apoy2k.robby.CommandField
import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.UnknownCommand
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.Charset

/**
 * Find the first value in a list of NameValuePairs
 */
fun List<NameValuePair>.first(field: CommandField): String? {
    return this.firstOrNull { it.name == field.name }?.value
}

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
    var recipients = emptySet<Player>()

    private var parameters: MutableCollection<NameValuePair> = mutableListOf()

    constructor(label: CommandLabel) : this(label, emptySet())

    constructor(label: CommandLabel, rest: Map<CommandField, String?>) : this(label, rest, emptySet())

    constructor(label: CommandLabel, recipients: Set<Player>) : this(label, emptyMap(), recipients)

    constructor(label: CommandLabel, rest: Map<CommandField, String?>, recipients: Set<Player>) {
        parameters = mutableListOf(BasicNameValuePair(CommandField.LABEL.name, label.name))
        parameters.addAll(
            rest.filter { !it.value.isNullOrBlank() }
                .map { BasicNameValuePair(it.key.name, it.value) }
        )

        this.recipients = recipients
    }

    companion object {

        /**
         * Convert a url encoded query string to a typed Command instance
         * @throws UnknownCommand if the string cannot be converted
         */
        fun fromString(input: String): Command {
            try {
                val query = URLEncodedUtils.parse(input, Charset.defaultCharset())
                val labelField = query.firstOrNull { it.name == CommandField.LABEL.name }?.value ?: "no_label"

                return when (CommandLabel.valueOf(labelField)) {
                    CommandLabel.JOIN_GAME -> JoinGameCommand(query.first(CommandField.PLAYER_NAME))
                    CommandLabel.LEAVE_GAME -> LeaveGameCommand()
                    CommandLabel.SELECT_CARD -> SelectCardCommand(query.first(CommandField.CARD_ID))
                    CommandLabel.CONFIRM_CARDS -> ConfirmCardsCommand()
                    CommandLabel.RESET_BOARD -> ResetBoardCommand()
                    CommandLabel.REFRESH_VIEW -> RefreshViewCommand(query.first(CommandField.VIEW_NAME))
                    CommandLabel.DRAW_CARDS -> DrawCardsCommand()
                }
            } catch (err: Throwable) {
                throw UnknownCommand(input, err)
            }
        }
    }

    /**
     * Get all parameters that have the provided field name
     */
    fun get(field: CommandField): List<String> {
        return parameters.filter { field.name == it.name }.map { it.value }
    }

    /**
     * Get the first parameter value with the provided field name
     */
    fun getFirst(field: CommandField): String? {
        return get(field).firstOrNull()
    }

    /**
     * Format this command as a URL Encoded string of name/value pairs
     */
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

class RefreshViewCommand(name: String?, recipients: Set<Player> = emptySet()) :
    Command(CommandLabel.REFRESH_VIEW, mapOf(CommandField.VIEW_NAME to name), recipients)

class JoinGameCommand(name: String?) :
    Command(CommandLabel.JOIN_GAME, mapOf(CommandField.PLAYER_NAME to name)) {
    val name get() = getFirst(CommandField.PLAYER_NAME)
}

class LeaveGameCommand : Command(CommandLabel.LEAVE_GAME)

class SelectCardCommand(cardId: String?) :
    Command(CommandLabel.SELECT_CARD, mapOf(CommandField.CARD_ID to cardId)) {
    val cardId get() = getFirst(CommandField.CARD_ID)
}

class ConfirmCardsCommand : Command(CommandLabel.CONFIRM_CARDS)

class ResetBoardCommand : Command(CommandLabel.RESET_BOARD)

class DrawCardsCommand : Command(CommandLabel.DRAW_CARDS)
