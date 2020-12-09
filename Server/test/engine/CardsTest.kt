package apoy2k.robby.engine

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.model.JoinGameAction
import apoy2k.robby.model.SelectCardAction
import apoy2k.robby.model.Session
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CardsTest {
    private var storage = MemoryStorage()
    private val s1 = Session("s1")
    private var engine = GameEngine(storage, Channel(), Channel())

    @Before
    fun setup() {
        storage = MemoryStorage()
        engine = GameEngine(storage, Channel(), Channel())
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
    }

    @Test
    fun testSelectCard() {
        val player = storage.game.playerFor(s1)

        assertNotNull(player)

        val card = player.drawnCards[0]
        engine.perform(SelectCardAction(card.id.toString()).also { it.session = s1 })

        val selectedCards = player.selectedCards
        assertEquals(1, selectedCards.count())
        assertEquals(card, selectedCards[0])
    }
}
