package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.Game
import apoy2k.robby.model.JoinGameAction
import apoy2k.robby.model.LeaveGameAction
import apoy2k.robby.model.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class SessionTest {
    private var game = Game()
    private var engine = GameEngine(Channel())
    private val s1 = Session("s1", "player1")
    private val s2 = Session("s2", "player2")

    @BeforeEach
    fun setup() {
        game = Game()
        engine = GameEngine(Channel())
    }

    @Test
    fun testPlayerAdd() {
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
        assertEquals(1, game.players.count())
    }

    @Test
    fun testPlayerAddEmpty() {
        assertFailsWith(IncompleteAction::class) {
            engine.perform(JoinGameAction().also {
                it.session = Session("s3")
                it.game = game
            })
        }
    }

    @Test
    fun testPlayerAddMulti() {
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
        engine.perform(JoinGameAction().also {
            it.session = s2
            it.game = game
        })
        assertEquals(2, game.players.count())
    }

    @Test
    fun testPlayerAddIdenticalSession() {
        assertFailsWith(InvalidGameState::class) {
            engine.perform(JoinGameAction().also {
                it.session = s1
                it.game = game
            })
            engine.perform(JoinGameAction().also {
                it.session = s1
                it.game = game
            })
        }
    }

    @Test
    fun testPlayerRemove() {
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
        engine.perform(LeaveGameAction().also {
            it.session = s1
            it.game = game
        })
        assertEquals(0, game.players.count())
    }

    @Test
    fun testPlayerMultiRemove() {
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
        engine.perform(JoinGameAction().also {
            it.session = s2
            it.game = game
        })
        engine.perform(LeaveGameAction().also {
            it.session = s1
            it.game = game
        })
        assertEquals(1, game.players.count())
    }
}
