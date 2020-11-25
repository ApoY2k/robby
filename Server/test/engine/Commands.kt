package apoy2k.robby.engine

import apoy2k.robby.CommandField
import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.UnknownCommandException
import apoy2k.robby.model.Command
import apoy2k.robby.model.JoinGameCommand
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandsTest {
    @Test
    fun testToCommandJoinGame() {
        assertEquals(JoinGameCommand("player1"), Command.fromString(
            "${CommandField.LABEL}=${CommandLabel.JOIN_GAME}&${CommandField.PLAYER_NAME}=player1"))
    }

    @Test
    fun testToCommandMissingParam() {
        assertEquals(JoinGameCommand(""), Command.fromString(
            "${CommandField.LABEL}=${CommandLabel.JOIN_GAME}"))
    }

    @Test
    fun testToCommandWrongSyntaxParam() {
        assertFailsWith(UnknownCommandException::class) {
            Command.fromString("${CommandField.LABEL}=${CommandLabel.JOIN_GAME}_player1")
        }
    }

    @Test
    fun testToCommandFailsUnknown() {
        assertFailsWith(UnknownCommandException::class) {
            Command.fromString("test")
        }
    }
}
