package apoy2k.robby.engine

import apoy2k.robby.DatabaseBackedTest
import apoy2k.robby.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.entity.add
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RobotEngineTest : DatabaseBackedTest() {
    var robot = Robot.new(RobotModel.ZIPPY).also { it.gameId = 1 }
    var robotEngine = RobotEngine(database)

    @BeforeEach
    fun setup() {
        val card1 = MovementCard.new(Movement.STRAIGHT, 1).also { it.gameId = 1 }
        database.cards.add(card1)
        val card2 = MovementCard.new(Movement.STRAIGHT, 1).also { it.gameId = 1 }
        database.cards.add(card2)
        val card3 = MovementCard.new(Movement.STRAIGHT, 1).also { it.gameId = 1 }
        database.cards.add(card3)
        val card4 = MovementCard.new(Movement.STRAIGHT, 1).also { it.gameId = 1 }
        database.cards.add(card4)
        val card5 = MovementCard.new(Movement.STRAIGHT, 1).also { it.gameId = 1 }
        database.cards.add(card5)
        database.robots.add(robot)
        robotEngine.selectCard(robot, 1, card1.id)
        robotEngine.selectCard(robot, 2, card2.id)
        robotEngine.selectCard(robot, 3, card3.id)
        robotEngine.selectCard(robot, 4, card4.id)
        robotEngine.selectCard(robot, 5, card5.id)
    }

    @Test
    fun `all registers are cleared with 4 damage`() {
        robot.damage = 4
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertNull(robotEngine.getRegister(robot.id, 3))
        assertNull(robotEngine.getRegister(robot.id, 4))
        assertNull(robotEngine.getRegister(robot.id, 5))
    }

    @Test
    fun `register 5 is locked with 5 damage`() {
        robot.damage = 5
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertNull(robotEngine.getRegister(robot.id, 3))
        assertNull(robotEngine.getRegister(robot.id, 4))
        assertEquals(5, robotEngine.getRegister(robot.id, 5)?.id)
    }

    @Test
    fun `registers over 4 are locked with 6 damage`() {
        robot.damage = 6
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertNull(robotEngine.getRegister(robot.id, 3))
        assertEquals(4, robotEngine.getRegister(robot.id, 4)?.id)
        assertEquals(5, robotEngine.getRegister(robot.id, 5)?.id)
    }

    @Test
    fun `registers over 3 are locked with 7 damage`() {
        robot.damage = 7
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertEquals(3, robotEngine.getRegister(robot.id, 3)?.id)
        assertEquals(4, robotEngine.getRegister(robot.id, 4)?.id)
        assertEquals(5, robotEngine.getRegister(robot.id, 5)?.id)
    }

    @Test
    fun `registers over 2 are locked with 8 damage`() {
        robot.damage = 8
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertEquals(2, robotEngine.getRegister(robot.id, 2)?.id)
        assertEquals(3, robotEngine.getRegister(robot.id, 3)?.id)
        assertEquals(4, robotEngine.getRegister(robot.id, 4)?.id)
        assertEquals(5, robotEngine.getRegister(robot.id, 5)?.id)
    }

    @Test
    fun `all registers are locked with 9 damage`() {
        robot.damage = 9
        robotEngine.clearRegisters(robot)
        assertEquals(1, robotEngine.getRegister(robot.id, 1)?.id)
        assertEquals(2, robotEngine.getRegister(robot.id, 2)?.id)
        assertEquals(3, robotEngine.getRegister(robot.id, 3)?.id)
        assertEquals(4, robotEngine.getRegister(robot.id, 4)?.id)
        assertEquals(5, robotEngine.getRegister(robot.id, 5)?.id)
    }
}
