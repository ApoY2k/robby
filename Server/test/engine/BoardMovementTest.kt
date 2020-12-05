package apoy2k.robby.engine

import apoy2k.robby.model.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BoardMovementTest {
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
    fun testMoveRobot1() {
        val card = MovementCard(Movement.STRAIGHT, 1)
        card.player = player1

        board.cells[1][1].robot = player1.robot
        board.execute(card)

        assertNull(board.cells[1][1].robot)
        assertNotNull(board.cells[2][1].robot)
        assertEquals(player1.robot, board.cells[2][1].robot)
    }
}
