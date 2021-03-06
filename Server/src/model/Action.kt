package apoy2k.robby.model

import apoy2k.robby.exceptions.UnknownAction
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.Charset

enum class ActionLabel {
    JOIN_GAME,
    LEAVE_GAME,
    SET_REGISTER,
    CONFIRM_CARDS,
}

enum class ActionField {
    LABEL,
    PLAYER_NAME,
    ROBOT_MODEL,
    CARD_ID,
    REGISTER,
}

/**
 * Find the first value in a list of NameValuePairs
 */
fun List<NameValuePair>.first(field: ActionField): String? {
    return this.firstOrNull { it.name == field.name }?.value
}

/**
 * Base class for all game actions, handles serialization and comparing between action instances
 */
abstract class Action {

    /**
     * Session that issued the action (null if server action)
     */
    var session: Session? = null

    private var parameters: MutableCollection<NameValuePair> = mutableListOf()

    constructor(label: ActionLabel) : this(label, emptyMap())

    constructor(label: ActionLabel, rest: Map<ActionField, String?>) {
        parameters = mutableListOf(BasicNameValuePair(ActionField.LABEL.name, label.name))
        parameters.addAll(
            rest.filter { !it.value.isNullOrBlank() }
                .map { BasicNameValuePair(it.key.name, it.value) }
        )
    }

    companion object {

        /**
         * Convert a url encoded query string to a typed action instance
         * @throws UnknownAction if the string cannot be converted
         */
        fun fromString(input: String): Action {
            try {
                val query = URLEncodedUtils.parse(input, Charset.defaultCharset())
                val labelField = query.firstOrNull { it.name == ActionField.LABEL.name }?.value ?: "no_label"

                return when (ActionLabel.valueOf(labelField)) {
                    ActionLabel.JOIN_GAME -> JoinGameAction(
                        query.first(ActionField.PLAYER_NAME),
                        query.first(ActionField.ROBOT_MODEL)
                    )
                    ActionLabel.LEAVE_GAME -> LeaveGameAction()
                    ActionLabel.SET_REGISTER -> SelectCardAction(
                        query.first(ActionField.REGISTER),
                        query.first(ActionField.CARD_ID)
                    )
                    ActionLabel.CONFIRM_CARDS -> ConfirmCardsAction()
                }
            } catch (err: Throwable) {
                throw UnknownAction(input, err)
            }
        }
    }

    /**
     * Get all parameters that have the provided field name
     */
    fun get(field: ActionField): List<String> {
        return parameters.filter { field.name == it.name }.map { it.value }
    }

    /**
     * Get the first parameter value with the provided field name
     */
    fun getFirst(field: ActionField): String? {
        return get(field).firstOrNull()
    }

    /**
     * Format this action as a URL Encoded string of name/value pairs
     */
    override fun toString(): String {
        return URLEncodedUtils.format(parameters.filter { !it.value.isNullOrBlank() }, Charset.defaultCharset())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Action

        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        return parameters.hashCode()
    }
}

class JoinGameAction(name: String? = "", model: String? = "") :
    Action(ActionLabel.JOIN_GAME, mapOf(ActionField.PLAYER_NAME to name, ActionField.ROBOT_MODEL to model)) {
    val name get() = getFirst(ActionField.PLAYER_NAME)
    val model get() = getFirst(ActionField.ROBOT_MODEL)
}

class LeaveGameAction : Action(ActionLabel.LEAVE_GAME)

class SelectCardAction(register: String?, cardId: String?) :
    Action(ActionLabel.SET_REGISTER, mapOf(ActionField.CARD_ID to cardId, ActionField.REGISTER to register)) {
    val register get() = getFirst(ActionField.REGISTER)
    val cardId get() = getFirst(ActionField.CARD_ID)
}

class ConfirmCardsAction : Action(ActionLabel.CONFIRM_CARDS)
