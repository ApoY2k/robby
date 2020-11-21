package apoy2k.robby.engine

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

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
