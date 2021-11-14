package apoy2k.robby.model

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BoardTest {
    private var board = Board(emptyList())
    private var emptyBoard = Board(emptyList())
    private val sess1 = Session("s1")
    private val player1 = Player("p1", sess1).also {
        it.robot = Robot(RobotModel.ZIPPY)
    }
    private val sess2 = Session("s2")
    private val player2 = Player("p2", sess2).also {
        it.robot = Robot(RobotModel.HUZZA)
    }
    private val sess3 = Session("s3")
    private val player3 = Player("p3", sess3).also {
        it.robot = Robot(RobotModel.KLAUS)
    }

    @BeforeEach
    fun setup() {
        //   0 1 2 3
        // 0
        // 1
        // 2 R L U D
        // 3
        board = Board(
            listOf(
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field()),
                listOf(
                    Field(FieldType.BELT, Direction.RIGHT),
                    Field(FieldType.BELT, Direction.LEFT),
                    Field(FieldType.BELT, Direction.UP),
                    Field(FieldType.BELT, Direction.DOWN)
                ),
                listOf(Field(), Field(), Field(), Field())
            )
        )

        emptyBoard = Board(
            listOf(
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field())
            )
        )
    }

    @Test
    fun `belt right moves robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fieldAt(1, 0)
        val target = board.fieldAt(2, 1)

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun `belt left moves robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fieldAt(1, 1)
        val target = board.fieldAt(2, 0)

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun `belt up moves robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fieldAt(1, 2)

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNotNull(source.robot)
        assertEquals(player1.robot, source.robot)
    }

    @Test
    fun `belt down moves robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fieldAt(1, 3)
        val target = board.fieldAt(3, 3)

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun `belt move of robot is blocked by other robot`() {
        val board = Board(
            listOf(
                listOf(Field(FieldType.BELT, Direction.DOWN)),
                listOf(Field()),
                listOf(Field(FieldType.BELT, Direction.UP)),
            )
        )

        val up = board.fieldAt(0, 0)
        val middle = board.fieldAt(1, 0)
        val down = board.fieldAt(2, 0)

        up.robot = player1.robot
        down.robot = player2.robot
        board.moveBelts(FieldType.BELT)

        assertNotNull(up.robot)
        assertNull(middle.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, up.robot)
        assertEquals(player2.robot, down.robot)
    }

    @Test
    fun `curve rotates robot`() {
        val board = Board(
            listOf(
                listOf(Field(FieldType.BELT, Direction.DOWN)),
                listOf(Field(FieldType.BELT, Direction.DOWN, Direction.RIGHT)),
            )
        )

        val start = board.fieldAt(0, 0)
        val end = board.fieldAt(1, 0)

        start.robot = player1.robot
        board.moveBelts(FieldType.BELT)

        assertNull(start.robot)
        assertNotNull(end.robot)
        assertEquals(player1.robot, end.robot)
        assertEquals(player1.robot?.facing, Direction.RIGHT)
    }

    @Test
    fun `robot moves straight`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = emptyBoard.fields[1][1]
        val target = emptyBoard.fields[2][1]

        source.robot = player1.robot
        emptyBoard.execute(card)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun `robot moves 3 steps straight`() {
        val card = MovementCard(Movement.STRAIGHT_3, 1)
        card.player = player1
        player1.robot?.facing = Direction.RIGHT

        val source = emptyBoard.fields[1][1]
        val target = emptyBoard.fields[1][3]

        source.robot = player1.robot
        emptyBoard.execute(card)
        emptyBoard.execute(card)
        emptyBoard.execute(card)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
        assertEquals(Direction.RIGHT, target.robot?.facing)
    }

    @Test
    fun `robot turns left`() {
        val card = MovementCard(Movement.TURN_LEFT, 1)
        card.player = player1

        val field = emptyBoard.fields[1][1]

        field.robot = player1.robot
        emptyBoard.execute(card)

        assertNotNull(field.robot)
        assertEquals(player1.robot, field.robot)
        assertEquals(Direction.RIGHT, field.robot?.facing)
    }

    @Test
    fun `robot turns right`() {
        val card = MovementCard(Movement.TURN_RIGHT, 1)
        card.player = player1
        player1.robot?.facing = Direction.RIGHT

        val field = emptyBoard.fields[1][1]

        field.robot = player1.robot
        emptyBoard.execute(card)

        assertNotNull(field.robot)
        assertEquals(player1.robot, field.robot)
        assertEquals(Direction.DOWN, field.robot?.facing)
    }

    @Test
    fun `robot pushes other robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = emptyBoard.fields[1][1]
        val middle = emptyBoard.fields[2][1]
        val down = emptyBoard.fields[3][1]

        top.robot = player1.robot
        middle.robot = player2.robot
        emptyBoard.execute(card)

        assertNull(top.robot)
        assertNotNull(middle.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, middle.robot)
        assertEquals(player2.robot, down.robot)
    }

    @Test
    fun `push is blocked by third robot`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = emptyBoard.fields[1][1]
        val middle = emptyBoard.fields[2][1]
        val down = emptyBoard.fields[3][1]

        top.robot = player1.robot
        middle.robot = player2.robot
        down.robot = player3.robot
        emptyBoard.execute(card)

        assertNotNull(top.robot)
        assertNotNull(middle.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, top.robot)
        assertEquals(player2.robot, middle.robot)
        assertEquals(player3.robot, down.robot)
    }

    @Test
    fun `push is blocked on board edge`() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = emptyBoard.fields[2][1]
        val down = emptyBoard.fields[3][1]

        top.robot = player1.robot
        down.robot = player2.robot
        emptyBoard.execute(card)

        assertNotNull(top.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, top.robot)
        assertEquals(player2.robot, down.robot)
    }

    @ParameterizedTest
    @MethodSource("provideTestFirstFieldByDirection")
    fun `find field by direction`(board: Board, startField: Field, direction: Direction, expectedEndField: Field) {
        val endField = board.firstFieldByDirection(startField, direction, FieldType.WALL)
        assertEquals(expectedEndField, endField)
    }

    companion object {
        @JvmStatic
        fun provideTestFirstFieldByDirection(): Stream<Arguments> {
            val board = Board(
                listOf(
                    listOf(Field(), Field(), Field()),
                    listOf(Field(FieldType.WALL), Field(), Field()),
                    listOf(Field(), Field(), Field(FieldType.WALL)),
                    listOf(Field(), Field(FieldType.WALL), Field()),
                )
            )

            return Stream.of(
                Arguments.of(board, board.fieldAt(0, 0), Direction.RIGHT, board.fieldAt(0, 2)),
                Arguments.of(board, board.fieldAt(1, 1), Direction.UP, board.fieldAt(0, 1)),
                Arguments.of(board, board.fieldAt(1, 1), Direction.LEFT, board.fieldAt(1, 0)),
                Arguments.of(board, board.fieldAt(0, 2), Direction.DOWN, board.fieldAt(2, 2)),
                Arguments.of(board, board.fieldAt(3, 2), Direction.LEFT, board.fieldAt(3, 1)),
                Arguments.of(board, board.fieldAt(3, 0), Direction.RIGHT, board.fieldAt(3, 1)),
                Arguments.of(board, board.fieldAt(3, 1), Direction.UP, board.fieldAt(0, 1)),
                Arguments.of(board, board.fieldAt(0, 0), Direction.LEFT, board.fieldAt(0, 0)),
                Arguments.of(board, board.fieldAt(0, 0), Direction.UP, board.fieldAt(0, 0)),
                Arguments.of(board, board.fieldAt(2, 2), Direction.RIGHT, board.fieldAt(2, 2)),
                Arguments.of(board, board.fieldAt(3, 1), Direction.DOWN, board.fieldAt(3, 1)),
            )
        }
    }
}
