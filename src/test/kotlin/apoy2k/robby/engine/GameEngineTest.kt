package apoy2k.robby.engine

import apoy2k.robby.DatabaseBackedTest
import apoy2k.robby.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Test
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
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        val s1 = Session(1)
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())

        val robots = database.robotsFor(game.id)
        val robot = robots.firstOrNull { it.userId == s1.userId }
        assertNotNull(robot)

        val card = database.cardsForRobot(robot.id)[0]
        gameEngine.perform(Action.selectCard(1, card.id).also {
            it.session = s1
            it.game = game
        }, board, robots)

        val registerCard = database.cardsForRobot(robot.id, 1)
        assertNotNull(registerCard)
        assertEquals(card.id, registerCard.id)
    }

    @Test
    fun `card selection override from different register`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        val s1 = Session(1)
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())

        val robots = database.robotsFor(game.id)
        val robot = robots.firstOrNull { it.userId == s1.userId }
        assertNotNull(robot)

        val card = database.cardsForRobot(robot.id)[0]
        gameEngine.perform(Action.selectCard(1, card.id).also {
            it.session = s1
            it.game = game
        }, board, robots)
        gameEngine.perform(Action.selectCard(2, card.id).also {
            it.session = s1
            it.game = game
        }, board, robots)

        val register1 = database.cardsForRobot(robot.id, 1)
        val register2 = database.cardsForRobot(robot.id, 2)
        assertNull(register1)
        assertNotNull(register2)
        assertEquals(card.id, register2.id)
    }

    @Test
    fun `join parallel games`() {
        val game1 = gameEngine.createNewGame(BoardType.SANDBOX)
        val game2 = gameEngine.createNewGame(BoardType.SANDBOX)
        database.add(User.new("1", "1"))
        val session = Session(1)

        val join1 = Action.joinGame(RobotModel.GEROG, Direction.DOWN).also {
            it.game = game1
            it.session = session
        }
        val join2 = Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.game = game2
            it.session = session
        }

        val board1 = database.fieldsFor(game1.id).toBoard()
        val board2 = database.fieldsFor(game2.id).toBoard()
        gameEngine.perform(join1, board1, emptySet())
        gameEngine.perform(join2, board2, emptySet())
        val robot1 = database.robotFor(game1.id, (session.userId ?: -1))
        val robot2 = database.robotFor(game2.id, (session.userId ?: -1))

        assertEquals(1, database.robotCount(game1.id))
        assertEquals(1, database.robotCount(game2.id))
        assertNotNull(database.robotFor(game1.id, (session.userId ?: -1)))
        assertNotNull(database.robotFor(game2.id, (session.userId ?: -1)))
        assertEquals(robot1?.id, board1.fieldAt(0, 0).robotId)
        assertEquals(robot2?.id, board2.fieldAt(0, 0).robotId)
        assertNull(board1.fieldAt(0, 1).robotId)
        assertNull(board2.fieldAt(0, 1).robotId)
    }

    @Test
    fun `join game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        val s1 = Session(1)

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())
        assertEquals(1, database.robotCount(game.id))
    }

    @Test
    fun `join game without name`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()

        assertFailsWith(Exception::class) {
            gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
                it.session = Session(1)
                it.game = game
            }, board, emptySet())
        }
    }

    @Test
    fun `multiple players join same game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        database.add(User.new("2", "2"))
        val s1 = Session(1)
        val s2 = Session(2)

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s2
            it.game = game
        }, board, emptySet())
        assertEquals(2, database.robotCount(game.id))
    }

    @Test
    fun `leave game`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        val s1 = Session(1)

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())

        val robots = database.robotsFor(game.id)
        gameEngine.perform(Action.leaveGame().also {
            it.session = s1
            it.game = game
        }, board, robots)
        assertEquals(0, database.robotCount(game.id))
    }

    @Test
    fun `leave game with multiple players`() {
        val game = gameEngine.createNewGame(BoardType.SANDBOX)
        val board = database.fieldsFor(game.id).toBoard()
        database.add(User.new("1", "1"))
        database.add(User.new("2", "2"))
        val s1 = Session(1)
        val s2 = Session(2)

        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s1
            it.game = game
        }, board, emptySet())
        var robots = database.robotsFor(game.id)
        gameEngine.perform(Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also {
            it.session = s2
            it.game = game
        }, board, robots)
        robots = database.robotsFor(game.id)
        gameEngine.perform(Action.leaveGame().also {
            it.session = s1
            it.game = game
        }, board, robots)
        assertEquals(1, database.robotCount(game.id))
    }
}
