package apoy2k.robby.engine

import apoy2k.robby.model.Game
import apoy2k.robby.model.JoinGameAction
import apoy2k.robby.model.SelectCardAction
import apoy2k.robby.model.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class CardsTest {
    private var game = Game()
    private val s1 = Session("s1", "player1")
    private var engine = GameEngine(Channel())

    @BeforeEach
    fun setup() {
        game = Game()
        engine = GameEngine(Channel())
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
    }

    @Test
    fun testSelectCard() {
        val player = game.playerFor(s1)

        assertNotNull(player)

        val card = player.drawnCards[0]
        engine.perform(SelectCardAction("1", card.id.toString()).also {
            it.session = s1
            it.game = game
        })

        val registerCard = player.robot?.getRegister(1)
        assertNotNull(registerCard)
        assertEquals(card, registerCard)
    }


    @Test
    fun testSelectSameCard() {
        val player = game.playerFor(s1)

        assertNotNull(player)

        val card = player.drawnCards[0]
        engine.perform(SelectCardAction("1", card.id.toString()).also {
            it.session = s1
            it.game = game
        })
        engine.perform(SelectCardAction("2", card.id.toString()).also {
            it.session = s1
            it.game = game
        })

        val register1 = player.robot?.getRegister(1)
        val register2 = player.robot?.getRegister(2)
        assertNull(register1)
        assertNotNull(register2)
        assertEquals(card, register2)
    }
}
