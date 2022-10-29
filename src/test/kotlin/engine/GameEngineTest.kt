package apoy2k.robby.kotlin.engine

import apoy2k.robby.engine.BoardEngine
import apoy2k.robby.engine.BoardType
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.RobotEngine
import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.kotlin.DatabaseBackedTest
import apoy2k.robby.model.Action
import apoy2k.robby.model.RobotModel
import apoy2k.robby.model.Session
import apoy2k.robby.model.robots
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Test
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.count
import org.ktorm.entity.find
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameEngineTest : DatabaseBackedTest() {
    private val robotEngine = RobotEngine(database)
    private val gameEngine = GameEngine(
        Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC")),
        database,
        robotEngine,
        MutableSharedFlow()
    )

    @Test
    fun `select card for register`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
        }, boardEngine)

        val robot = database.robots.find { it.gameId eq game.id and (it.session eq s1.id) }
        assertNotNull(robot)

        val card = robotEngine.getDrawnCards(robot.id)[0]
        gameEngine.perform(Action.selectCard(1, card.id).also {
            it.session = s1
            it.game = game
        }, boardEngine)

        val registerCard = robotEngine.getRegister(robot.id, 1)
        assertNotNull(registerCard)
        assertEquals(card, registerCard)
    }

    @Test
    fun `card selection override from different register`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
            it.game = game
        }, boardEngine)

        val robot = database.robots.find { it.gameId eq game.id and (it.session eq s1.id) }
        assertNotNull(robot)

        val card = robotEngine.getDrawnCards(robot.id)[0]
        gameEngine.perform(Action.selectCard(1, card.id).also {
            it.session = s1
            it.game = game
        }, boardEngine)
        gameEngine.perform(Action.selectCard(2, card.id).also {
            it.session = s1
            it.game = game
        }, boardEngine)

        val register1 = robotEngine.getRegister(robot.id, 1)
        val register2 = robotEngine.getRegister(robot.id, 2)
        assertNull(register1)
        assertNotNull(register2)
        assertEquals(card, register2)
    }

    @Test
    fun `join parallel games`() {
        val game1 = gameEngine.createNewGame(BoardType.SANDBOX)
        val game2 = gameEngine.createNewGame(BoardType.SANDBOX)
        val session = Session("s1", "s1")

        val join1 = Action.joinGame(RobotModel.GEROG).also {
            it.game = game1
            it.session = session
        }
        val join2 = Action.joinGame(RobotModel.ZIPPY).also {
            it.game = game2
            it.session = session
        }

        val boardEngine1 = BoardEngine.build(game1.id, database)
        val boardEngine2 = BoardEngine.build(game2.id, database)
        gameEngine.perform(join1, boardEngine1)
        gameEngine.perform(join2, boardEngine2)
        val robot1 = database.robots.find { it.gameId eq game1.id and (it.session eq session.id) }
        val robot2 = database.robots.find { it.gameId eq game2.id and (it.session eq session.id) }

        assertEquals(1, database.robots.count { it.gameId eq game1.id })
        assertEquals(1, database.robots.count { it.gameId eq game2.id })
        assertNotNull(database.robots.find { it.gameId eq game1.id and (it.session eq session.id) })
        assertNotNull(database.robots.find { it.gameId eq game2.id and (it.session eq session.id) })
        assertEquals(robot1?.id, boardEngine1.fieldAt(0, 0).robotId)
        assertEquals(robot2?.id, boardEngine2.fieldAt(0, 0).robotId)
        assertNull(boardEngine1.fieldAt(0, 1).robotId)
        assertNull(boardEngine2.fieldAt(0, 1).robotId)
    }

    @Test
    fun `join game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
            it.game = game
        }, boardEngine)
        assertEquals(1, database.robots.count { it.gameId eq game.id })
    }

    @Test
    fun `join game without name`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)

        assertFailsWith(IncompleteAction::class) {
            gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
                it.session = Session("s3")
                it.game = game
            }, boardEngine)
        }
    }

    @Test
    fun `multiple players join same game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")
        val s2 = Session("s2", "player2")

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
            it.game = game
        }, boardEngine)
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s2
            it.game = game
        }, boardEngine)
        assertEquals(2, database.robots.count { it.gameId eq game.id })
    }

    @Test
    fun `join same game twice`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")

        assertFailsWith(InvalidGameState::class) {
            gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
                it.session = s1
                it.game = game
            }, boardEngine)
            gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
                it.session = s1
                it.game = game
            }, boardEngine)
        }
    }

    @Test
    fun `leave game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
            it.game = game
        }, boardEngine)
        gameEngine.perform(Action.leaveGame().also {
            it.session = s1
            it.game = game
        }, boardEngine)
        assertEquals(0, database.robots.count { it.gameId eq game.id })
    }

    @Test
    fun `leave game with multiple players`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val boardEngine = BoardEngine.build(game.id, database)
        val s1 = Session("s1", "s1")
        val s2 = Session("s2", "player2")

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s1
            it.game = game
        }, boardEngine)
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY).also {
            it.session = s2
            it.game = game
        }, boardEngine)
        gameEngine.perform(Action.leaveGame().also {
            it.session = s1
            it.game = game
        }, boardEngine)
        assertEquals(1, database.robots.count { it.gameId eq game.id })
    }
}
