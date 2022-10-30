package apoy2k.robby.kotlin.engine

import apoy2k.robby.engine.BoardEngine
import apoy2k.robby.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BoardEngineTest {
    //   0 1 2 3
    // 0
    // 1
    // 2 R L U D
    // 3
    private val boardEngine = BoardEngine(
        listOf(
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(
                Field.new(FieldType.BELT, Direction.RIGHT),
                Field.new(FieldType.BELT, Direction.LEFT),
                Field.new(FieldType.BELT, Direction.UP),
                Field.new(FieldType.BELT, Direction.DOWN)
            ),
            listOf(Field.new(), Field.new(), Field.new(), Field.new())
        ), assignIds = true
    )

    private val emptyBoardEngine = BoardEngine(
        listOf(
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new())
        ), assignIds = true
    )

    private val robot1 = Robot.new(RobotModel.ZIPPY, Session("s1", "s1")).also { it.id = 1 }
    private val robot2 = Robot.new(RobotModel.ZIPPY, Session("s2", "s2")).also { it.id = 2 }
    private val robot3 = Robot.new(RobotModel.ZIPPY, Session("s3", "s3")).also { it.id = 3 }

    @BeforeEach
    fun setup() {
        resetBoard(boardEngine)
        resetBoard(emptyBoardEngine)
    }

    @Test
    fun `belt right moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = boardEngine.fieldAt(1, 0)
        val target = boardEngine.fieldAt(2, 1)

        source.robotId = robot1.id
        boardEngine.execute(card, robot1)
        boardEngine.moveBelts(FieldType.BELT, listOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `belt left moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = boardEngine.fieldAt(1, 1)
        val target = boardEngine.fieldAt(2, 0)

        source.robotId = robot1.id
        boardEngine.execute(card, robot1)
        boardEngine.moveBelts(FieldType.BELT, listOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `belt up moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = boardEngine.fieldAt(1, 2)

        source.robotId = robot1.id
        boardEngine.execute(card, robot1)
        boardEngine.moveBelts(FieldType.BELT, listOf(robot1))

        assertNotNull(source.robotId)
        assertEquals(robot1.id, source.robotId)
    }

    @Test
    fun `belt down moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = boardEngine.fieldAt(1, 3)
        val target = boardEngine.fieldAt(3, 3)

        source.robotId = robot1.id
        boardEngine.execute(card, robot1)
        boardEngine.moveBelts(FieldType.BELT, listOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `belt move of robot is blocked by other robot`() {
        val board = BoardEngine(
            listOf(
                listOf(Field.new(FieldType.BELT, Direction.DOWN)),
                listOf(Field.new()),
                listOf(Field.new(FieldType.BELT, Direction.UP)),
            )
        )

        val up = board.fieldAt(0, 0)
        val middle = board.fieldAt(1, 0)
        val down = board.fieldAt(2, 0)

        up.robotId = robot1.id
        down.robotId = robot2.id
        board.moveBelts(FieldType.BELT, listOf(robot1, robot2))

        assertNotNull(up.robotId)
        assertNull(middle.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, up.robotId)
        assertEquals(robot2.id, down.robotId)
    }

    @Test
    fun `curve rotates robot`() {
        val board = BoardEngine(
            listOf(
                listOf(Field.new(FieldType.BELT, Direction.DOWN)),
                listOf(Field.new(FieldType.BELT, Direction.DOWN, Direction.RIGHT)),
            ), assignIds = true
        )

        val start = board.fieldAt(0, 0)
        val end = board.fieldAt(1, 0)

        start.robotId = robot1.id
        board.moveBelts(FieldType.BELT, listOf(robot1))

        assertNull(start.robotId)
        assertNotNull(end.robotId)
        assertEquals(robot1.id, end.robotId)
        assertEquals(robot1.facing, Direction.RIGHT)
    }

    @Test
    fun `robot moves straight`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = emptyBoardEngine.fieldAt(1, 1)
        val target = emptyBoardEngine.fieldAt(2, 1)

        source.robotId = robot1.id
        emptyBoardEngine.execute(card, robot1)

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `robot moves 3 steps straight`() {
        val card = MovementCard.new(Movement.STRAIGHT_3, 1)
        card.robotId = robot1.id
        robot1.facing = Direction.RIGHT

        val source = emptyBoardEngine.fieldAt(1, 1)
        val target = emptyBoardEngine.fieldAt(1, 3)

        source.robotId = robot1.id
        emptyBoardEngine.execute(card, robot1)
        emptyBoardEngine.execute(card, robot1)
        emptyBoardEngine.execute(card, robot1)

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
        assertEquals(Direction.RIGHT, robot1.facing)
    }

    @Test
    fun `robot turns left`() {
        val card = MovementCard.new(Movement.TURN_LEFT, 1)
        card.robotId = robot1.id

        val field = emptyBoardEngine.fieldAt(1, 1)

        field.robotId = robot1.id
        emptyBoardEngine.execute(card, robot1)

        assertNotNull(field.robotId)
        assertEquals(robot1.id, field.robotId)
        assertEquals(Direction.RIGHT, robot1.facing)
    }

    @Test
    fun `robot turns right`() {
        val card = MovementCard.new(Movement.TURN_RIGHT, 1)
        card.robotId = robot1.id
        robot1.facing = Direction.RIGHT

        val field = emptyBoardEngine.fieldAt(1, 1)

        field.robotId = robot1.id
        emptyBoardEngine.execute(card, robot1)

        assertNotNull(field.robotId)
        assertEquals(robot1.id, field.robotId)
        assertEquals(Direction.DOWN, robot1.facing)
    }

    @Test
    fun `robot pushes other robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val top = emptyBoardEngine.fieldAt(1, 1)
        val middle = emptyBoardEngine.fieldAt(2, 1)
        val down = emptyBoardEngine.fieldAt(3, 1)

        top.robotId = robot1.id
        middle.robotId = robot2.id
        emptyBoardEngine.execute(card, robot1)

        assertNull(top.robotId)
        assertNotNull(middle.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, middle.robotId)
        assertEquals(robot2.id, down.robotId)
    }

    @Test
    fun `push is blocked by third robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val top = emptyBoardEngine.fieldAt(1, 1)
        val middle = emptyBoardEngine.fieldAt(2, 1)
        val down = emptyBoardEngine.fieldAt(3, 1)

        top.robotId = robot1.id
        middle.robotId = robot2.id
        down.robotId = robot3.id
        emptyBoardEngine.execute(card, robot1)

        assertNotNull(top.robotId)
        assertNotNull(middle.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, top.robotId)
        assertEquals(robot2.id, middle.robotId)
        assertEquals(robot3.id, down.robotId)
    }

    @Test
    fun `push is blocked on board edge`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val top = emptyBoardEngine.fieldAt(2, 1)
        val down = emptyBoardEngine.fieldAt(3, 1)

        top.robotId = robot1.id
        down.robotId = robot2.id
        emptyBoardEngine.execute(card, robot1)

        assertNotNull(top.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, top.robotId)
        assertEquals(robot2.id, down.robotId)
    }

    @ParameterizedTest
    @MethodSource("provideTestFirstFieldByDirection")
    fun `find field by direction`(
        board: BoardEngine,
        startField: Field,
        direction: Direction,
        expectedEndField: Field
    ) {
        val endField = board.findLastNonBlockedField(startField, direction)
        assertEquals(expectedEndField, endField)
    }

    @Test
    fun `laser damages robot`() {
        val board = BoardEngine(
            listOf(
                listOf(Field.new(FieldType.LASER, Direction.DOWN), Field.new()),
                listOf(Field.new(), Field.new()),
                listOf(Field.new(), Field.new()),
                listOf(Field.new(), Field.new()),
            ), assignIds = true
        )

        val zippy = Robot.new(RobotModel.ZIPPY).also { it.id = 1 }
        board.fieldAt(2, 0).robotId = zippy.id

        val klaus = Robot.new(RobotModel.KLAUS).also { it.id = 2 }
        board.fieldAt(2, 1).robotId = klaus.id

        board.fireLasers(FieldType.LASER, listOf(zippy, klaus))

        assertEquals(1, zippy.damage)
        assertEquals(0, klaus.damage)
    }

    companion object {
        @JvmStatic
        fun resetBoard(board: BoardEngine) {
            board.board.flatten().forEach {
                it.robotId = null
                it.conditions = mutableListOf()
            }
        }

        @JvmStatic
        fun provideTestFirstFieldByDirection(): Stream<Arguments> {
            val board = BoardEngine(
                listOf(
                    listOf(Field.new(), Field.new(FieldType.WALL, Direction.LEFT), Field.new()),
                    listOf(Field.new(FieldType.WALL, Direction.DOWN), Field.new(), Field.new()),
                    listOf(Field.new(), Field.new(), Field.new(FieldType.WALL, Direction.RIGHT)),
                    listOf(Field.new(), Field.new(FieldType.WALL, Direction.UP), Field.new()),
                ), assignIds = true
            )

            return Stream.of(
                Arguments.of(board, board.fieldAt(0, 0), Direction.RIGHT, board.fieldAt(0, 0)),
                Arguments.of(board, board.fieldAt(1, 1), Direction.UP, board.fieldAt(0, 1)),
                Arguments.of(board, board.fieldAt(1, 1), Direction.LEFT, board.fieldAt(1, 0)),
                Arguments.of(board, board.fieldAt(0, 2), Direction.DOWN, board.fieldAt(3, 2)),
                Arguments.of(board, board.fieldAt(3, 2), Direction.LEFT, board.fieldAt(3, 0)),
                Arguments.of(board, board.fieldAt(3, 0), Direction.RIGHT, board.fieldAt(3, 2)),
                Arguments.of(board, board.fieldAt(3, 1), Direction.UP, board.fieldAt(0, 1)),
                Arguments.of(board, board.fieldAt(0, 0), Direction.LEFT, board.fieldAt(0, 0)),
                Arguments.of(board, board.fieldAt(0, 0), Direction.UP, board.fieldAt(0, 0)),
                Arguments.of(board, board.fieldAt(2, 2), Direction.RIGHT, board.fieldAt(2, 2)),
                Arguments.of(board, board.fieldAt(3, 1), Direction.DOWN, board.fieldAt(3, 1)),
            )
        }
    }
}