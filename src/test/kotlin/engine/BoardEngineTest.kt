package apoy2k.robby.kotlin.engine

import apoy2k.robby.engine.*
import apoy2k.robby.kotlin.apo2k.robby.assignIds
import apoy2k.robby.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.*

class BoardEngineTest {

    private lateinit var board: Board
    private lateinit var emptyBoard: Board

    private lateinit var robot1: Robot
    private lateinit var robot2: Robot
    private lateinit var robot3: Robot

    @BeforeEach
    fun setup() {
        robot1 = Robot.new(RobotModel.ZIPPY, "1", 1).also { it.id = 1 }
        robot2 = Robot.new(RobotModel.ZIPPY, "2", 2).also { it.id = 2 }
        robot3 = Robot.new(RobotModel.ZIPPY, "3", 3).also { it.id = 3 }

        //   0 1 2 3
        // 0
        // 1
        // 2 R L U D
        // 3
        board = listOf(
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(
                Field.new(FieldElement.BELT, Direction.RIGHT),
                Field.new(FieldElement.BELT, Direction.LEFT),
                Field.new(FieldElement.BELT, Direction.UP),
                Field.new(FieldElement.BELT, Direction.DOWN)
            ),
            listOf(Field.new(), Field.new(), Field.new(), Field.new())
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(robot1, robot2, robot3))

        emptyBoard = listOf(
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(), Field.new(), Field.new())
        )
        emptyBoard.assignIds()
        emptyBoard.updateLaserOverlays(setOf(robot1, robot2, robot3))
    }

    @Test
    fun `belt right moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = board.fieldAt(1, 0)
        val target = board.fieldAt(2, 1)

        source.robotId = robot1.id
        board.execute(card, robot1, setOf(robot1))
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `belt left moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = board.fieldAt(1, 1)
        val target = board.fieldAt(2, 0)

        source.robotId = robot1.id
        board.execute(card, robot1, setOf(robot1))
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `continous belt doesn't change robot orientation`() {
        val board = listOf(
            listOf(
                Field.new(FieldElement.BELT, Direction.LEFT),
                Field.new(FieldElement.BELT, Direction.LEFT),
                Field.new(FieldElement.BELT, Direction.LEFT),
            ),
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(robot1))

        board.fieldAt(0, 2).robotId = robot1.id
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertEquals(robot1.id, board.fieldAt(0, 1).robotId)
        assertEquals(Direction.DOWN, robot1.facing)
    }

    @Test
    fun `belt up moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = board.fieldAt(1, 2)

        source.robotId = robot1.id
        board.execute(card, robot1, setOf(robot1))
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertNotNull(source.robotId)
        assertEquals(robot1.id, source.robotId)
    }

    @Test
    fun `belt down moves robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = board.fieldAt(1, 3)
        val target = board.fieldAt(3, 3)

        source.robotId = robot1.id
        board.execute(card, robot1, setOf(robot1))
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `belt move of robot is blocked by other robot`() {
        val board = listOf(
            listOf(Field.new(FieldElement.BELT, Direction.DOWN)),
            listOf(Field.new()),
            listOf(Field.new(FieldElement.BELT, Direction.UP)),
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(robot1, robot2))

        val up = board.fieldAt(0, 0)
        val middle = board.fieldAt(1, 0)
        val down = board.fieldAt(2, 0)

        up.robotId = robot1.id
        down.robotId = robot2.id
        board.moveBelts(FieldElement.BELT, setOf(robot1, robot2))

        assertNotNull(up.robotId)
        assertNull(middle.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, up.robotId)
        assertEquals(robot2.id, down.robotId)
    }

    @Test
    fun `curve rotates robot`() {
        val board = listOf(
            listOf(Field.new(FieldElement.BELT, Direction.DOWN)),
            listOf(Field.new(FieldElement.BELT, Direction.RIGHT, Direction.DOWN)),
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(robot1))

        val start = board.fieldAt(0, 0)
        val end = board.fieldAt(1, 0)

        start.robotId = robot1.id
        board.moveBelts(FieldElement.BELT, setOf(robot1))

        assertNull(start.robotId)
        assertNotNull(end.robotId)
        assertEquals(robot1.id, end.robotId)
        assertEquals(Direction.RIGHT, robot1.facing)
    }

    @Test
    fun `robot moves straight`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val source = emptyBoard.fieldAt(1, 1)
        val target = emptyBoard.fieldAt(2, 1)

        source.robotId = robot1.id
        emptyBoard.execute(card, robot1, setOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
    }

    @Test
    fun `robot moves 3 steps straight`() {
        val card = MovementCard.new(Movement.STRAIGHT_3, 1)
        card.robotId = robot1.id
        robot1.facing = Direction.RIGHT

        val source = emptyBoard.fieldAt(1, 1)
        val target = emptyBoard.fieldAt(1, 3)

        source.robotId = robot1.id
        emptyBoard.execute(card, robot1, setOf(robot1))
        emptyBoard.execute(card, robot1, setOf(robot1))
        emptyBoard.execute(card, robot1, setOf(robot1))

        assertNull(source.robotId)
        assertNotNull(target.robotId)
        assertEquals(robot1.id, target.robotId)
        assertEquals(Direction.RIGHT, robot1.facing)
    }

    @Test
    fun `robot turns left`() {
        val card = MovementCard.new(Movement.TURN_LEFT, 1)
        card.robotId = robot1.id

        val field = emptyBoard.fieldAt(1, 1)

        field.robotId = robot1.id
        emptyBoard.execute(card, robot1, setOf(robot1))

        assertNotNull(field.robotId)
        assertEquals(robot1.id, field.robotId)
        assertEquals(Direction.RIGHT, robot1.facing)
    }

    @Test
    fun `robot turns right`() {
        val card = MovementCard.new(Movement.TURN_RIGHT, 1)
        card.robotId = robot1.id
        robot1.facing = Direction.RIGHT

        val field = emptyBoard.fieldAt(1, 1)

        field.robotId = robot1.id
        emptyBoard.execute(card, robot1, setOf(robot1))

        assertNotNull(field.robotId)
        assertEquals(robot1.id, field.robotId)
        assertEquals(Direction.DOWN, robot1.facing)
    }

    @Test
    fun `robot pushes other robot`() {
        val card = MovementCard.new(Movement.STRAIGHT, 1)
        card.robotId = robot1.id

        val top = emptyBoard.fieldAt(1, 1)
        val middle = emptyBoard.fieldAt(2, 1)
        val down = emptyBoard.fieldAt(3, 1)

        top.robotId = robot1.id
        middle.robotId = robot2.id
        emptyBoard.execute(card, robot1, setOf(robot1, robot2))

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

        val top = emptyBoard.fieldAt(1, 1)
        val middle = emptyBoard.fieldAt(2, 1)
        val down = emptyBoard.fieldAt(3, 1)

        top.robotId = robot1.id
        middle.robotId = robot2.id
        down.robotId = robot3.id
        emptyBoard.execute(card, robot1, setOf(robot1, robot2, robot3))

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

        val top = emptyBoard.fieldAt(2, 1)
        val down = emptyBoard.fieldAt(3, 1)

        top.robotId = robot1.id
        down.robotId = robot2.id
        emptyBoard.execute(card, robot1, setOf(robot1, robot2))

        assertNotNull(top.robotId)
        assertNotNull(down.robotId)
        assertEquals(robot1.id, top.robotId)
        assertEquals(robot2.id, down.robotId)
    }

    @Test
    fun `laser damages robot`() {
        val zippy = Robot.new(RobotModel.ZIPPY).also { it.id = 1 }
        val klaus = Robot.new(RobotModel.KLAUS).also { it.id = 2 }

        val board = listOf(
            listOf(Field.new(FieldElement.LASER, Direction.DOWN), Field.new()),
            listOf(Field.new(), Field.new()),
            listOf(Field.new(), Field.new()),
            listOf(Field.new(), Field.new()),
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(zippy, klaus))

        board.fieldAt(2, 0).robotId = zippy.id
        board.fieldAt(2, 1).robotId = klaus.id

        board.updateLaserOverlays(setOf(zippy, klaus))
        board.fireLasers(FieldElement.LASER, setOf(zippy, klaus))

        assertEquals(1, zippy.damage)
        assertEquals(0, klaus.damage)
    }

    @Test
    fun `laser overlay applied`() {
        val board = listOf(
            listOf(
                Field.new(FieldElement.LASER, Direction.DOWN),
                Field.new(FieldElement.WALL, Direction.DOWN),
                Field.new(FieldElement.LASER_2, Direction.DOWN)
            ),
            listOf(Field.new(), Field.new(FieldElement.LASER_2, Direction.UP), Field.new()),
            listOf(Field.new(), Field.new(), Field.new()),
            listOf(Field.new(), Field.new(FieldElement.LASER, Direction.UP), Field.new())
        )
        board.assignIds()
        board.updateLaserOverlays(emptySet())

        assertTrue(board.fieldAt(0, 1).elements.none { it == FieldElement.LASER_V })
        assertTrue(board.fieldAt(1, 1).elements.none { it == FieldElement.LASER_V })
        assertContains(board.fieldAt(1, 2).elements, FieldElement.LASER_2_V)
        assertContains(board.fieldAt(2, 2).elements, FieldElement.LASER_2_V)
        assertContains(board.fieldAt(1, 0).elements, FieldElement.LASER_V)
        assertContains(board.fieldAt(2, 0).elements, FieldElement.LASER_V)
        assertContains(board.fieldAt(3, 0).elements, FieldElement.LASER_V)

        board.fieldAt(1, 0).robotId = robot1.id
        board.updateLaserOverlays(setOf(robot1))

        assertContains(board.fieldAt(1, 0).elements, FieldElement.LASER_V)
        assertTrue(board.fieldAt(2, 0).elements.none { it == FieldElement.LASER_V })
        assertTrue(board.fieldAt(3, 0).elements.none { it == FieldElement.LASER_V })
    }

    @Test
    fun `place robots on start fields in order`() {
        val board = listOf(
            listOf(Field.new(FieldElement.START_5)),
            listOf(Field.new(FieldElement.START_8)),
            listOf(Field.new(FieldElement.START_1)),
            listOf(Field.new(FieldElement.START_2)),
            listOf(Field.new(FieldElement.START_4)),
        )
        board.assignIds()
        board.updateLaserOverlays(emptySet())

        board.placeRobot(1)
        board.placeRobot(2)
        board.placeRobot(3)
        board.placeRobot(4)
        board.placeRobot(5)

        assertEquals(1, board.fieldAt(2, 0).robotId)
        assertEquals(2, board.fieldAt(3, 0).robotId)
        assertEquals(3, board.fieldAt(4, 0).robotId)
        assertEquals(4, board.fieldAt(0, 0).robotId)
        assertEquals(5, board.fieldAt(1, 0).robotId)
    }

    @Test
    fun `touch checkpoint flags in order`() {
        val robot = Robot.new(RobotModel.ZIPPY).also { it.id = 1 }

        val board = listOf(
            listOf(Field.new(FieldElement.FLAG_2)),
            listOf(Field.new(FieldElement.FLAG_1)),
        )
        board.assignIds()
        board.updateLaserOverlays(setOf(robot))

        board.fieldAt(0, 0).robotId = robot.id
        board.touchCheckpoints(setOf(robot))
        assertEquals(0, robot.passedCheckpoints)
        board.fieldAt(0, 0).robotId = null

        board.fieldAt(1, 0).robotId = robot.id
        board.touchCheckpoints(setOf(robot))
        assertEquals(1, robot.passedCheckpoints)
        board.fieldAt(1, 0).robotId = null

        board.fieldAt(0, 0).robotId = robot.id
        board.touchCheckpoints(setOf(robot))
        assertEquals(2, robot.passedCheckpoints)
        board.fieldAt(0, 0).robotId = null
    }

    @ParameterizedTest
    @MethodSource("provideLastLaserHitField")
    fun `find last laser hit field`(
        board: Board,
        startField: Field,
        direction: Direction,
        expectedEndField: Field
    ) {
        val endField = board.findLastLaserHitField(startField, direction)
        assertEquals(expectedEndField, endField)
    }

    @ParameterizedTest
    @MethodSource("provideTurnMovement")
    fun `calculate turn movement`(
        incomingDirection: Direction,
        outgoingDirection: Direction,
        expectedMovement: Movement,
    ) {
        assertEquals(expectedMovement, getTurnMovement(incomingDirection, outgoingDirection))
    }

    companion object {

        @JvmStatic
        fun provideLastLaserHitField(): Stream<Arguments> {
            val wallBoard = listOf(
                listOf(Field.new(), Field.new(FieldElement.WALL, Direction.LEFT), Field.new()),
                listOf(Field.new(FieldElement.WALL, Direction.DOWN), Field.new(), Field.new()),
                listOf(Field.new(), Field.new(), Field.new(FieldElement.WALL, Direction.RIGHT)),
                listOf(Field.new(), Field.new(FieldElement.WALL, Direction.UP), Field.new()),
            )
            wallBoard.assignIds()
            wallBoard.updateLaserOverlays(emptySet())

            val robot = Robot.new(RobotModel.ZIPPY).also { it.id = 1 }
            val robotBoard = listOf(
                listOf(Field.new()),
                listOf(Field.new()),
                listOf(Field.new().also { it.robotId = robot.id }),
                listOf(Field.new()),
            )
            robotBoard.assignIds()
            robotBoard.updateLaserOverlays(setOf(robot))

            val laserBoard = listOf(
                listOf(
                    Field.new(),
                    Field.new(FieldElement.WALL, Direction.DOWN),
                    Field.new(FieldElement.LASER_2, Direction.DOWN)
                ),
                listOf(Field.new(), Field.new(FieldElement.LASER_2, Direction.UP), Field.new()),
                listOf(Field.new(), Field.new(), Field.new()),
                listOf(Field.new(), Field.new(FieldElement.LASER, Direction.UP), Field.new())
            )
            laserBoard.assignIds()
            laserBoard.updateLaserOverlays(emptySet())

            return Stream.of(
                Arguments.of(wallBoard, wallBoard.fieldAt(0, 0), Direction.RIGHT, wallBoard.fieldAt(0, 0)),
                Arguments.of(wallBoard, wallBoard.fieldAt(1, 1), Direction.UP, wallBoard.fieldAt(0, 1)),
                Arguments.of(wallBoard, wallBoard.fieldAt(1, 1), Direction.LEFT, wallBoard.fieldAt(1, 0)),
                Arguments.of(wallBoard, wallBoard.fieldAt(0, 2), Direction.DOWN, wallBoard.fieldAt(3, 2)),
                Arguments.of(wallBoard, wallBoard.fieldAt(3, 2), Direction.LEFT, wallBoard.fieldAt(3, 0)),
                Arguments.of(wallBoard, wallBoard.fieldAt(3, 0), Direction.RIGHT, wallBoard.fieldAt(3, 2)),
                Arguments.of(wallBoard, wallBoard.fieldAt(3, 1), Direction.UP, wallBoard.fieldAt(0, 1)),
                Arguments.of(wallBoard, wallBoard.fieldAt(0, 0), Direction.LEFT, wallBoard.fieldAt(0, 0)),
                Arguments.of(wallBoard, wallBoard.fieldAt(0, 0), Direction.UP, wallBoard.fieldAt(0, 0)),
                Arguments.of(wallBoard, wallBoard.fieldAt(2, 2), Direction.RIGHT, wallBoard.fieldAt(2, 2)),
                Arguments.of(wallBoard, wallBoard.fieldAt(3, 1), Direction.DOWN, wallBoard.fieldAt(3, 1)),
                Arguments.of(robotBoard, robotBoard.fieldAt(0, 0), Direction.DOWN, robotBoard.fieldAt(2, 0)),
                Arguments.of(robotBoard, robotBoard.fieldAt(3, 0), Direction.UP, robotBoard.fieldAt(2, 0)),
                Arguments.of(laserBoard, laserBoard.fieldAt(3, 1), Direction.UP, laserBoard.fieldAt(2, 1)),
                Arguments.of(laserBoard, laserBoard.fieldAt(0, 2), Direction.DOWN, laserBoard.fieldAt(3, 2)),
            )
        }

        @JvmStatic
        fun provideTurnMovement(): Stream<Arguments> = Stream.of(
            Arguments.of(Direction.UP, Direction.RIGHT, Movement.TURN_LEFT),
            Arguments.of(Direction.UP, Direction.LEFT, Movement.TURN_RIGHT),
            Arguments.of(Direction.UP, Direction.DOWN, Movement.STAY),
            Arguments.of(Direction.UP, Direction.UP, Movement.STAY),
            Arguments.of(Direction.DOWN, Direction.RIGHT, Movement.TURN_RIGHT),
            Arguments.of(Direction.DOWN, Direction.LEFT, Movement.TURN_LEFT),
            Arguments.of(Direction.DOWN, Direction.DOWN, Movement.STAY),
            Arguments.of(Direction.DOWN, Direction.UP, Movement.STAY),
            Arguments.of(Direction.LEFT, Direction.RIGHT, Movement.STAY),
            Arguments.of(Direction.LEFT, Direction.LEFT, Movement.STAY),
            Arguments.of(Direction.LEFT, Direction.DOWN, Movement.TURN_RIGHT),
            Arguments.of(Direction.LEFT, Direction.UP, Movement.TURN_LEFT),
            Arguments.of(Direction.RIGHT, Direction.RIGHT, Movement.STAY),
            Arguments.of(Direction.RIGHT, Direction.LEFT, Movement.STAY),
            Arguments.of(Direction.RIGHT, Direction.DOWN, Movement.TURN_LEFT),
            Arguments.of(Direction.RIGHT, Direction.UP, Movement.TURN_RIGHT),
        )
    }
}
