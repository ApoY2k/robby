package kotlin.model

import apoy2k.robby.model.Movement
import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Robot
import apoy2k.robby.model.RobotModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RobotTest {
    var robot = Robot(RobotModel.ZIPPY)

    @BeforeEach
    fun setup() {
        robot = Robot(RobotModel.ZIPPY)
        robot.setRegister(1, MovementCard(Movement.STRAIGHT, 1))
        robot.setRegister(2, MovementCard(Movement.STRAIGHT, 2))
        robot.setRegister(3, MovementCard(Movement.STRAIGHT, 3))
        robot.setRegister(4, MovementCard(Movement.STRAIGHT, 4))
        robot.setRegister(5, MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun `all registers are cleared with 4 damage`() {
        robot.damage = 4
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertNull(robot.getRegister(4))
        assertNull(robot.getRegister(5))
    }

    @Test
    fun `register 5 is locked with 5 damage`() {
        robot.damage = 5
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertNull(robot.getRegister(4))
        assertEquals(MovementCard(Movement.STRAIGHT, 5), robot.getRegister(5))
    }

    @Test
    fun `registers over 4 are locked with 6 damage`() {
        robot.damage = 6
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertEquals(MovementCard(Movement.STRAIGHT, 4), robot.getRegister(4))
        assertEquals(MovementCard(Movement.STRAIGHT, 5), robot.getRegister(5))
    }

    @Test
    fun `registers over 3 are locked with 7 damage`() {
        robot.damage = 7
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertEquals(MovementCard(Movement.STRAIGHT, 3), robot.getRegister(3))
        assertEquals(MovementCard(Movement.STRAIGHT, 4), robot.getRegister(4))
        assertEquals(MovementCard(Movement.STRAIGHT, 5), robot.getRegister(5))
    }

    @Test
    fun `registers over 2 are locked with 8 damage`() {
        robot.damage = 8
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertEquals(MovementCard(Movement.STRAIGHT, 2), robot.getRegister(2))
        assertEquals(MovementCard(Movement.STRAIGHT, 3), robot.getRegister(3))
        assertEquals(MovementCard(Movement.STRAIGHT, 4), robot.getRegister(4))
        assertEquals(MovementCard(Movement.STRAIGHT, 5), robot.getRegister(5))
    }

    @Test
    fun `all registers are locked with 9 damage`() {
        robot.damage = 9
        robot.clearRegisters()
        assertEquals(MovementCard(Movement.STRAIGHT, 1), robot.getRegister(1))
        assertEquals(MovementCard(Movement.STRAIGHT, 2), robot.getRegister(2))
        assertEquals(MovementCard(Movement.STRAIGHT, 3), robot.getRegister(3))
        assertEquals(MovementCard(Movement.STRAIGHT, 4), robot.getRegister(4))
        assertEquals(MovementCard(Movement.STRAIGHT, 5), robot.getRegister(5))
    }
}