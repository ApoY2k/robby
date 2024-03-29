package apoy2k.robby.model

import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.Charset

enum class ActionLabel {
    JOIN_GAME,
    LEAVE_GAME,
    SELECT_CARD,
    TOGGLE_READY,
    TOGGLE_POWERDOWN,
}

enum class ActionField {
    LABEL,
    ROBOT_MODEL,
    CARD_ID,
    REGISTER,
    ROBOT_FACING,
}

/**
 * Transferrable model object for communicating actions by users to the server
 */
data class Action(
    val label: ActionLabel,
    private val parameters: List<NameValuePair> = emptyList()
) {

    /**
     * Session that issued the action (null if server action)
     */
    var session: Session? = null

    /**
     * Game that this action was created for
     */
    var game: Game? = null

    companion object {

        /**
         * Convert a serialized, url encoded query string to a typed action instance.
         * Counterpart for the `serializeForSocket` method.
         */
        @JvmStatic
        fun deserializeFromSocket(serializedAction: String): Action {
            val query = URLEncodedUtils.parse(serializedAction, Charset.defaultCharset())
            val label = query.firstOrNull { it.name == ActionField.LABEL.name }?.value.orEmpty()

            // Remove label from query, so it's not duplicated in the action fields
            query.removeIf { it.name == ActionField.LABEL.name }
            return Action(
                ActionLabel.valueOf(label),
                query
            )
        }

        fun joinGame(model: RobotModel? = null, facing: Direction? = null) = Action(
            ActionLabel.JOIN_GAME,
            listOf(
                BasicNameValuePair(ActionField.ROBOT_MODEL.name, model?.name.orEmpty()),
                BasicNameValuePair(ActionField.ROBOT_FACING.name, facing?.name.orEmpty()),
            )
        )

        fun leaveGame() = Action(ActionLabel.LEAVE_GAME)

        fun selectCard(register: Int, cardId: Int) = Action(
            ActionLabel.SELECT_CARD,
            listOf(
                BasicNameValuePair(ActionField.CARD_ID.name, cardId.toString()),
                BasicNameValuePair(ActionField.REGISTER.name, register.toString()),
            )
        )

        fun toggleReady() = Action(ActionLabel.TOGGLE_READY)

        fun togglePowerDown() = Action(ActionLabel.TOGGLE_POWERDOWN)
    }

    /**
     * Format this action as a URL Encoded string of name/value pairs, so it can be sent over the websocket
     */
    fun serializeForSocket(): String {
        val data = mutableListOf<NameValuePair>(
            BasicNameValuePair(ActionField.LABEL.name, label.name)
        )
        data.addAll(parameters)
        return URLEncodedUtils.format(data.filter { !it.value.isNullOrBlank() }, Charset.defaultCharset())
    }

    fun getString(field: ActionField, fallback: String? = null) =
        parameters.firstOrNull { it.name == field.name }?.value ?: fallback

    fun getInt(field: ActionField, fallback: Int? = null) = getString(field)?.toInt() ?: fallback
}
