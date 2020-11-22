package apoy2k.robby.engine

import apoy2k.robby.CommandLabel
import apoy2k.robby.exceptions.UnknownCommandException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandsTest {
    @Test
    fun testToCommandJoinGame() {
        assertEquals(JoinGameCommand("player1"), "${CommandLabel.JOIN_GAME}:player1".toCommand())
    }

    @Test
    fun testToCommandMissingParam() {
        assertEquals(JoinGameCommand(""), "${CommandLabel.JOIN_GAME}:".toCommand())
    }

    @Test
    fun testToCommandWrongSyntaxParam() {
        assertFailsWith(UnknownCommandException::class) {
            "${CommandLabel.JOIN_GAME}_player1".toCommand()
        }
    }

    @Test
    fun testToCommandNoSeparatorParam() {
        assertFailsWith(UnknownCommandException::class) {
            "${CommandLabel.JOIN_GAME}".toCommand()
        }
    }

    @Test
    fun testToCommandFailsUnknown() {
        assertFailsWith(UnknownCommandException::class) {
            "test".toCommand()
        }
    }
}
