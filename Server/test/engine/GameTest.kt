package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteCommandException
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.Command
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.LeaveGameCommand
import apoy2k.robby.model.Session
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameTest {
    var game = Game.create()

    @Before
    fun setup() {
        game = Game.create()
    }

    @Test
    fun testPlayerAdd() {
        game.perform(JoinGameCommand("player1"), Session("s1"))
        assertEquals(1, game.players.count())
    }

    @Test
    fun testPlayerAddEmpty() {
        assertFailsWith(IncompleteCommandException::class) {
            game.perform(JoinGameCommand(""), Session("s1"))
        }
    }

    @Test
    fun testPlayerAddMulti() {
        game.perform(JoinGameCommand("player1"), Session("s1"))
        game.perform(JoinGameCommand("player2"), Session("s2"))
        assertEquals(2, game.players.count())
    }

    @Test
    fun testPlayerAddIdenticalSession() {
        assertFailsWith(InvalidGameState::class) {
            game.perform(JoinGameCommand("player1"), Session("s1"))
            game.perform(JoinGameCommand("player2"), Session("s1"))
        }
    }

    @Test
    fun testPlayerAddIdenticalName() {
        assertFailsWith(InvalidGameState::class) {
            game.perform(JoinGameCommand("player1"), Session("s1"))
            game.perform(JoinGameCommand("player1"), Session("s2"))
        }
    }

    @Test
    fun testPlayerRemove() {
        game.perform(JoinGameCommand("player1"), Session("s1"))
        game.perform(LeaveGameCommand(), Session("s1"))
        assertEquals(0, game.players.count())
    }

    @Test
    fun testPlayerMultiRemove() {
        game.perform(JoinGameCommand("player1"), Session("s1"))
        game.perform(JoinGameCommand("player2"), Session("s2"))
        game.perform(LeaveGameCommand(), Session("s1"))
        assertEquals(1, game.players.count())
    }
}
