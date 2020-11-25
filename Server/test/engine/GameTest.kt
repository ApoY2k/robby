package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteCommandException
import apoy2k.robby.model.Command
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.LeaveGameCommand
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
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        assertEquals(1, game.players.count())
    }

    @Test
    fun testPlayerAddEmpty() {
        assertFailsWith(IncompleteCommandException::class) {
            game.perform(listOf<Command>(JoinGameCommand("")))
        }
    }

    @Test
    fun testPlayerAddMulti() {
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        game.perform(listOf<Command>(JoinGameCommand("player2")))
        assertEquals(2, game.players.count())
    }

    @Test
    fun testPlayerAddIdenticalName() {
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        assertEquals(1, game.players.count())
    }

    @Test
    fun testPlayerRemove() {
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        game.perform(listOf<Command>(LeaveGameCommand("player1")))
        assertEquals(0, game.players.count())
    }

    @Test
    fun testPlayerMultiRemove() {
        game.perform(listOf<Command>(JoinGameCommand("player1")))
        game.perform(listOf<Command>(JoinGameCommand("player2")))
        game.perform(listOf<Command>(LeaveGameCommand("player1")))
        assertEquals(1, game.players.count())
    }
}
