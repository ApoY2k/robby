package apoy2k.robby.engine

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.model.DrawCardsAction
import apoy2k.robby.model.JoinGameAction
import apoy2k.robby.model.Session
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GameEngineCardsTest {
    private var storage = MemoryStorage()
    private val s1 = Session("s1")
    private var engine = GameEngine(storage)

    @Before
    fun setup() {
        storage = MemoryStorage()
        engine = GameEngine(storage)
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
    }

    @Test
    fun testDrawCards() {
        engine.perform(DrawCardsAction().also { it.session = s1 })
        val player = storage.game.playerFor(s1)

        assertNotNull(player)
        assertEquals(5, player.drawnCards.count())
    }
}
