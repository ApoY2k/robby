package apoy2k.robby.engine

import apoy2k.robby.model.DrawCardsCommand
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.Session
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GameCardsTest {
    private var game = Game()
    private val s1 = Session("s1")

    @Before
    fun setup() {
        game = Game()
        game.perform(JoinGameCommand("player1"), s1)
    }

    @Test
    fun testDrawCards() {
        game.perform(DrawCardsCommand(), s1)
        val player = game.playerFor(s1)

        assertNotNull(player)
        assertEquals(5, player.getDrawnCards().count())
    }
}
