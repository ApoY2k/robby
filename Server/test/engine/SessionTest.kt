package apoy2k.robby.engine

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.JoinGameAction
import apoy2k.robby.model.LeaveGameAction
import apoy2k.robby.model.Session
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionTest {
    private var storage = MemoryStorage()
    private var engine = GameEngine(storage, Channel(), Channel())
    private val s1 = Session("s1")
    private val s2 = Session("s2")

    @Before
    fun setup() {
        storage = MemoryStorage()
        engine = GameEngine(storage, Channel(), Channel())
    }

    @Test
    fun testPlayerAdd() {
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
        assertEquals(1, storage.game.players.count())
    }

    @Test
    fun testPlayerAddEmpty() {
        assertFailsWith(IncompleteAction::class) {
            engine.perform(JoinGameAction("").also { it.session = s1 })
        }
    }

    @Test
    fun testPlayerAddMulti() {
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
        engine.perform(JoinGameAction("player2").also { it.session = s2 })
        assertEquals(2, storage.game.players.count())
    }

    @Test
    fun testPlayerAddIdenticalSession() {
        assertFailsWith(InvalidGameState::class) {
            engine.perform(JoinGameAction("player1").also { it.session = s1 })
            engine.perform(JoinGameAction("player2").also { it.session = s1 })
        }
    }

    @Test
    fun testPlayerAddIdenticalName() {
        assertFailsWith(InvalidGameState::class) {
            engine.perform(JoinGameAction("player1").also { it.session = s1 })
            engine.perform(JoinGameAction("player1").also { it.session = s2 })
        }
    }

    @Test
    fun testPlayerRemove() {
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
        engine.perform(LeaveGameAction().also { it.session = s1 })
        assertEquals(0, storage.game.players.count())
    }

    @Test
    fun testPlayerMultiRemove() {
        engine.perform(JoinGameAction("player1").also { it.session = s1 })
        engine.perform(JoinGameAction("player2").also { it.session = s2 })
        engine.perform(LeaveGameAction().also { it.session = s1 })
        assertEquals(1, storage.game.players.count())
    }
}
