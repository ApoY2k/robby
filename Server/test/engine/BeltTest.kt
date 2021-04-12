package apoy2k.robby.engine

import apoy2k.robby.model.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BeltTest {
    private var board = Board(emptyList())

    private val sess1 = Session("s1")
    private val player1 = Player("p1", sess1)

    @Before
    fun setup() {
        //   0 1 2 3
        // 0
        // 1
        // 2 R L U P
        // 3
        board = Board(listOf(
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(FieldType.BELT, Direction.RIGHT), Field(FieldType.BELT, Direction.LEFT), Field(FieldType.BELT, Direction.UP), Field(FieldType.BELT, Direction.DOWN)),
            listOf(Field(), Field(), Field(), Field())
        ))

        player1.robot = Robot(RobotModel.ZIPPY)
    }

    @Test
    fun testBeltR() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fields[1][0]
        val target = board.fields[2][1]

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun testBeltL() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fields[1][1]
        val target = board.fields[2][0]

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }

    @Test
    fun testBeltU() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fields[1][2]

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNotNull(source.robot)
        assertEquals(player1.robot, source.robot)
    }

    @Test
    fun testBeltD() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        val source = board.fields[1][3]
        val target = board.fields[3][3]

        source.robot = player1.robot
        board.execute(card)
        board.moveBelts(FieldType.BELT)

        assertNull(source.robot)
        assertNotNull(target.robot)
        assertEquals(player1.robot, target.robot)
    }
}
