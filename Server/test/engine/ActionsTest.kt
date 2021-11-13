package apoy2k.robby.engine

import apoy2k.robby.exceptions.UnknownAction
import apoy2k.robby.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionsTest {
    var game = Game()

    @BeforeEach
    fun setUp() {
        game = Game()
    }

    @Test
    fun testToCommandJoinGame() {
        val game = Game()
        assertEquals(
            JoinGameAction().also { it.game = game },
            Action.deserializeFromSocket(game, "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}")
        )
    }

    @Test
    fun testToCommandWrongSyntaxParam() {
        val game = Game()
        assertFailsWith(UnknownAction::class) {
            Action.deserializeFromSocket(game, "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}_player1")
        }
    }

    @Test
    fun testToCommandFailsUnknown() {
        val game = Game()
        assertFailsWith(UnknownAction::class) {
            Action.deserializeFromSocket(game, "test")
        }
    }
}
