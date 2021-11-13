package apoy2k.robby.engine

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.data.Storage
import apoy2k.robby.exceptions.UnknownAction
import apoy2k.robby.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class ActionsTest {
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

    @Test
    fun testParallelGames() {
        val storage = MemoryStorage()
        val game1 = storage.createGame()
        val game2 = storage.createGame()
        val session = Session("s1", "player1")

        val join1 = JoinGameAction(RobotModel.GEROG.name).also {
            it.game = game1
            it.session = session
        }
        val join2 = JoinGameAction(RobotModel.ZIPPY.name).also {
            it.game = game2
            it.session = session
        }

        val engine = GameEngine(Channel())
        engine.perform(join1)
        engine.perform(join2)

        assertEquals(1, game1.players.size)
        assertEquals(1, game2.players.size)
        assertEquals(session, game1.playerFor(session)?.session)
        assertEquals(session, game1.playerFor(session)?.session)
        assertEquals(game1.playerFor(session)?.robot, game1.board.fieldAt(Position(0, 0)).robot)
        assertEquals(game2.playerFor(session)?.robot, game2.board.fieldAt(Position(0, 0)).robot)
        assertNull(game1.board.fieldAt(Position(0, 1)).robot)
        assertNull(game2.board.fieldAt(Position(0, 1)).robot)
    }
}
