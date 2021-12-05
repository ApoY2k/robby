package apoy2k.robby.engine

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameEngineTest {
    private val engine = GameEngine(MutableSharedFlow())

    @Test
    fun `select card for register`() {
        val game = Game()
        val s1 = Session("s1", "player1")
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })

        val player = game.playerFor(s1)
        assertNotNull(player)

        val card = player.drawnCards[0]
        engine.perform(SelectCardAction("1", card.id).also {
            it.session = s1
            it.game = game
        })

        val registerCard = player.robot?.getRegister(1)
        assertNotNull(registerCard)
        assertEquals(card, registerCard)
    }

    @Test
    fun `card selection override from different register`() {
        val game = Game()
        val s1 = Session("s1", "player1")
        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })

        val player = game.playerFor(s1)
        assertNotNull(player)

        val card = player!!.drawnCards[0]
        engine.perform(SelectCardAction("1", card.id).also {
            it.session = s1
            it.game = game
        })
        engine.perform(SelectCardAction("2", card.id).also {
            it.session = s1
            it.game = game
        })

        val register1 = player.robot?.getRegister(1)
        val register2 = player.robot?.getRegister(2)
        assertNull(register1)
        assertNotNull(register2)
        assertEquals(card, register2)
    }

    @Test
    fun `join parallel games`() {
        val storage = MemoryStorage()
        val game1 = storage.createGame()
        val game2 = storage.createGame()
        val session = Session("s1", "player1")

        val join1 = JoinGameAction(RobotModel.GEROG.name).also {
            it.game = game1
            it.session = session
        }
        val join2 = JoinGameAction(RobotModel.ZIPPY.name).also {
            it.game = game2
            it.session = session
        }

        val engine = GameEngine(MutableSharedFlow())
        engine.perform(join1)
        engine.perform(join2)

        assertEquals(1, game1.players.size)
        assertEquals(1, game2.players.size)
        assertEquals(session, game1.playerFor(session)?.session)
        assertEquals(session, game1.playerFor(session)?.session)
        assertEquals(game1.playerFor(session)?.robot, game1.board.fieldAt(0, 0).robot)
        assertEquals(game2.playerFor(session)?.robot, game2.board.fieldAt(0, 0).robot)
        assertNull(game1.board.fieldAt(0, 1).robot)
        assertNull(game2.board.fieldAt(0, 1).robot)
    }

    @Test
    fun `join game`() {
        val game = Game()
        val s1 = Session("s1", "player1")

        engine.perform(JoinGameAction().also {
            it.session = s1
            it.game = game
        })
        assertEquals(1, game.players.count())
    }

    @Test
    fun `join game without name`() {
        val game = Game()

        assertFailsWith(IncompleteAction::class) {
            engine.perform(JoinGameAction().also {
                it.session = Session("s3")
                it.game = game
            })
        }
    }

    @Test
    fun `multiple players join same game`() {
        val game = Game()
        val s1 = Session("s1", "player1")
        val s2 = Session("s2", "player2")

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
    fun `join same game twice`() {
        val game = Game()
        val s1 = Session("s1", "player1")

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
    fun `leave game`() {
        val game = Game()
        val s1 = Session("s1", "player1")

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
    fun `leave game with multiple players`() {
        val game = Game()
        val s1 = Session("s1", "player1")
        val s2 = Session("s2", "player2")

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
