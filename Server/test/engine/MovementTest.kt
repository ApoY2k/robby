package apoy2k.robby.engine

import apoy2k.robby.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MovementTest {
    private var board = Board(emptyList())

    private val sess1 = Session("s1")
    private val player1 = Player("p1", sess1)
    private val sess2 = Session("s2")
    private val player2 = Player("p2", sess2)
    private val sess3 = Session("s3")
    private val player3 = Player("p3", sess3)

    @BeforeEach
    fun setup() {
        board = Board(
            listOf(
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field()),
                listOf(Field(), Field(), Field(), Field())
            )
        )

        player1.robot = Robot(RobotModel.ZIPPY)
        player2.robot = Robot(RobotModel.HUZZA)
        player3.robot = Robot(RobotModel.KLAUS)
    }

    @Test
    fun testMoveStraight1() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fields[1][1]
        val target = board.fields[2][1]

        source.robot = player1.robot
        board.execute(card)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun testMoveStraightRight3() {
        val card = MovementCard(Movement.STRAIGHT_3, 1)
        card.player = player1
        player1.robot?.facing = Direction.RIGHT

        val source = board.fields[1][1]
        val target = board.fields[1][3]

        source.robot = player1.robot
        board.execute(card)
        board.execute(card)
        board.execute(card)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
        assertEquals(Direction.RIGHT, target.robot?.facing)
    }

    @Test
    fun testTurnLeft() {
        val card = MovementCard(Movement.TURN_LEFT, 1)
        card.player = player1

        val field = board.fields[1][1]

        field.robot = player1.robot
        board.execute(card)

        assertNotNull(field.robot)
        assertEquals(player1.robot, field.robot)
        assertEquals(Direction.RIGHT, field.robot?.facing)
    }

    @Test
    fun testTurnRight() {
        val card = MovementCard(Movement.TURN_RIGHT, 1)
        card.player = player1
        player1.robot?.facing = Direction.RIGHT

        val field = board.fields[1][1]

        field.robot = player1.robot
        board.execute(card)

        assertNotNull(field.robot)
        assertEquals(player1.robot, field.robot)
        assertEquals(Direction.DOWN, field.robot?.facing)
    }

    @Test
    fun testPush() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = board.fields[1][1]
        val middle = board.fields[2][1]
        val down = board.fields[3][1]

        top.robot = player1.robot
        middle.robot = player2.robot
        board.execute(card)

        assertNull(top.robot)
        assertNotNull(middle.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, middle.robot)
        assertEquals(player2.robot, down.robot)
    }

    @Test
    fun testPushBlocked() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = board.fields[1][1]
        val middle = board.fields[2][1]
        val down = board.fields[3][1]

        top.robot = player1.robot
        middle.robot = player2.robot
        down.robot = player3.robot
        board.execute(card)

        assertNotNull(top.robot)
        assertNotNull(middle.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, top.robot)
        assertEquals(player2.robot, middle.robot)
        assertEquals(player3.robot, down.robot)
    }

    @Test
    fun testPushEdge() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val top = board.fields[2][1]
        val down = board.fields[3][1]

        top.robot = player1.robot
        down.robot = player2.robot
        board.execute(card)

        assertNotNull(top.robot)
        assertNotNull(down.robot)
        assertEquals(player1.robot, top.robot)
        assertEquals(player2.robot, down.robot)
    }
}
