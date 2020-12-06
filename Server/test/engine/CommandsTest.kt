package apoy2k.robby.engine

import apoy2k.robby.exceptions.UnknownAction
import apoy2k.robby.model.Action
import apoy2k.robby.model.ActionField
import apoy2k.robby.model.ActionLabel
import apoy2k.robby.model.JoinGameAction
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandsTest {
    @Test
    fun testToCommandJoinGame() {
        assertEquals(JoinGameAction("player1"), Action.fromString(
            "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}&${ActionField.PLAYER_NAME}=player1"))
    }

    @Test
    fun testToCommandMissingParam() {
        assertEquals(JoinGameAction(""), Action.fromString(
            "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}"))
    }

    @Test
    fun testToCommandWrongSyntaxParam() {
        assertFailsWith(UnknownAction::class) {
            Action.fromString("${ActionField.LABEL}=${ActionLabel.JOIN_GAME}_player1")
        }
    }

    @Test
    fun testToCommandFailsUnknown() {
        assertFailsWith(UnknownAction::class) {
            Action.fromString("test")
        }
    }
}
