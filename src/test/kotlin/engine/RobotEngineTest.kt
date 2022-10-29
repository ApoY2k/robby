package apoy2k.robby.kotlin.engine

import apoy2k.robby.engine.RobotEngine
import apoy2k.robby.kotlin.DatabaseBackedTest
import apoy2k.robby.model.*
import org.junit.jupiter.api.Test
import org.ktorm.dsl.deleteAll
import org.ktorm.entity.add
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RobotEngineTest : DatabaseBackedTest() {
    var robot = Robot.new(RobotModel.ZIPPY)
    var robotEngine = RobotEngine(database)

    override fun setupBeforeEach() {
        database.cards.add(MovementCard.new(Movement.STRAIGHT, 1))
        database.cards.add(MovementCard.new(Movement.STRAIGHT, 1))
        database.cards.add(MovementCard.new(Movement.STRAIGHT, 1))
        database.cards.add(MovementCard.new(Movement.STRAIGHT, 1))
        database.cards.add(MovementCard.new(Movement.STRAIGHT, 1))
        database.robots.add(robot)
        robotEngine.selectCard(robot, 1, 1)
        robotEngine.selectCard(robot, 1, 2)
        robotEngine.selectCard(robot, 1, 3)
        robotEngine.selectCard(robot, 1, 4)
        robotEngine.selectCard(robot, 1, 5)
    }

    override fun tearDownAfterEach() {
        database.deleteAll(MovementCards)
        database.deleteAll(Robots)
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
        assertEquals(MovementCard.new(Movement.STRAIGHT, 5), robotEngine.getRegister(robot.id, 5))
    }

    @Test
    fun `registers over 4 are locked with 6 damage`() {
        robot.damage = 6
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertNull(robotEngine.getRegister(robot.id, 3))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 4), robotEngine.getRegister(robot.id, 4))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 5), robotEngine.getRegister(robot.id, 5))
    }

    @Test
    fun `registers over 3 are locked with 7 damage`() {
        robot.damage = 7
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertNull(robotEngine.getRegister(robot.id, 2))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 3), robotEngine.getRegister(robot.id, 3))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 4), robotEngine.getRegister(robot.id, 4))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 5), robotEngine.getRegister(robot.id, 5))
    }

    @Test
    fun `registers over 2 are locked with 8 damage`() {
        robot.damage = 8
        robotEngine.clearRegisters(robot)
        assertNull(robotEngine.getRegister(robot.id, 1))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 2), robotEngine.getRegister(robot.id, 2))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 3), robotEngine.getRegister(robot.id, 3))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 4), robotEngine.getRegister(robot.id, 4))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 5), robotEngine.getRegister(robot.id, 5))
    }

    @Test
    fun `all registers are locked with 9 damage`() {
        robot.damage = 9
        robotEngine.clearRegisters(robot)
        assertEquals(MovementCard.new(Movement.STRAIGHT, 1), robotEngine.getRegister(robot.id, 1))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 2), robotEngine.getRegister(robot.id, 2))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 3), robotEngine.getRegister(robot.id, 3))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 4), robotEngine.getRegister(robot.id, 4))
        assertEquals(MovementCard.new(Movement.STRAIGHT, 5), robotEngine.getRegister(robot.id, 5))
    }
}
