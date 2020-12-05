package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteCommand
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.LeaveGameCommand
import apoy2k.robby.model.Session
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionTest {
    private var game = Game()
    private val s1 = Session("s1")
    private val s2 = Session("s2")

    @Before
    fun setup() {
        game = Game()
    }

    @Test
    fun testPlayerAdd() {
        game.perform(JoinGameCommand("player1"), s1)
        assertEquals(1, game.getPlayers().count())
    }

    @Test
    fun testPlayerAddEmpty() {
        assertFailsWith(IncompleteCommand::class) {
            game.perform(JoinGameCommand(""), s1)
        }
    }

    @Test
    fun testPlayerAddMulti() {
        game.perform(JoinGameCommand("player1"), s1)
        game.perform(JoinGameCommand("player2"), s2)
        assertEquals(2, game.getPlayers().count())
    }

    @Test
    fun testPlayerAddIdenticalSession() {
        assertFailsWith(InvalidGameState::class) {
            game.perform(JoinGameCommand("player1"), s1)
            game.perform(JoinGameCommand("player2"), s1)
        }
    }

    @Test
    fun testPlayerAddIdenticalName() {
        assertFailsWith(InvalidGameState::class) {
            game.perform(JoinGameCommand("player1"), s1)
            game.perform(JoinGameCommand("player1"), s2)
        }
    }

    @Test
    fun testPlayerRemove() {
        game.perform(JoinGameCommand("player1"), s1)
        game.perform(LeaveGameCommand(), s1)
        assertEquals(0, game.getPlayers().count())
    }

    @Test
    fun testPlayerMultiRemove() {
        game.perform(JoinGameCommand("player1"), s1)
        game.perform(JoinGameCommand("player2"), s2)
        game.perform(LeaveGameCommand(), s1)
        assertEquals(1, game.getPlayers().count())
    }
}
