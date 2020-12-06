package apoy2k.robby.engine

import apoy2k.robby.model.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.asserter

class MovementTest {
    private var board = Board(emptyList())

    private val sess1 = Session("s1")
    private val player1 = Player("p1", sess1)

    @Before
    fun setup() {
        board = Board(listOf(
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field())
        ))

        player1.robot = Robot(RobotModel.ZIPPY)
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
    fun testMoveBackwards2() {
        val card = MovementCard(Movement.BACKWARDS_2, 1)
        card.player = player1

        val source = board.fields[1][1]
        val target = board.fields[0][1]

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
        player1.robot?.orientation = Orientation.RIGHT

        val source = board.fields[1][1]
        val target = board.fields[1][3]

        source.robot = player1.robot
        board.execute(card)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
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
        assertEquals(Orientation.RIGHT, field.robot?.orientation)
    }

    @Test
    fun testTurnRight() {
        val card = MovementCard(Movement.TURN_RIGHT, 1)
        card.player = player1
        player1.robot?.orientation = Orientation.RIGHT

        val field = board.fields[1][1]

        field.robot = player1.robot
        board.execute(card)

        assertNotNull(field.robot)
        assertEquals(player1.robot, field.robot)
        assertEquals(Orientation.DOWN, field.robot?.orientation)
    }
}
