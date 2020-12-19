package apoy2k.robby.engine

import apoy2k.robby.model.Movement
import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Robot
import apoy2k.robby.model.RobotModel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RobotTest {
    var robot = Robot(RobotModel.ZIPPY)

    @Before
    fun setup() {
        robot = Robot(RobotModel.ZIPPY)
        robot.setRegister(1, MovementCard(Movement.STRAIGHT, 1))
        robot.setRegister(2, MovementCard(Movement.STRAIGHT, 2))
        robot.setRegister(3, MovementCard(Movement.STRAIGHT, 3))
        robot.setRegister(4, MovementCard(Movement.STRAIGHT, 4))
        robot.setRegister(5, MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun testRegisterResetDamage4() {
        robot.damage = 4
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertNull(robot.getRegister(4))
        assertNull(robot.getRegister(5))
    }

    @Test
    fun testRegisterResetDamage5() {
        robot.damage = 5
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertNull(robot.getRegister(4))
        assertEquals(robot.getRegister(5), MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun testRegisterResetDamage6() {
        robot.damage = 6
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertNull(robot.getRegister(3))
        assertEquals(robot.getRegister(4), MovementCard(Movement.STRAIGHT, 4))
        assertEquals(robot.getRegister(5), MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun testRegisterResetDamage7() {
        robot.damage = 7
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertNull(robot.getRegister(2))
        assertEquals(robot.getRegister(3), MovementCard(Movement.STRAIGHT, 3))
        assertEquals(robot.getRegister(4), MovementCard(Movement.STRAIGHT, 4))
        assertEquals(robot.getRegister(5), MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun testRegisterResetDamage8() {
        robot.damage = 8
        robot.clearRegisters()
        assertNull(robot.getRegister(1))
        assertEquals(robot.getRegister(2), MovementCard(Movement.STRAIGHT, 2))
        assertEquals(robot.getRegister(3), MovementCard(Movement.STRAIGHT, 3))
        assertEquals(robot.getRegister(4), MovementCard(Movement.STRAIGHT, 4))
        assertEquals(robot.getRegister(5), MovementCard(Movement.STRAIGHT, 5))
    }

    @Test
    fun testRegisterResetDamage9() {
        robot.damage = 9
        robot.clearRegisters()
        assertEquals(robot.getRegister(1), MovementCard(Movement.STRAIGHT, 1))
        assertEquals(robot.getRegister(2), MovementCard(Movement.STRAIGHT, 2))
        assertEquals(robot.getRegister(3), MovementCard(Movement.STRAIGHT, 3))
        assertEquals(robot.getRegister(4), MovementCard(Movement.STRAIGHT, 4))
        assertEquals(robot.getRegister(5), MovementCard(Movement.STRAIGHT, 5))
    }
}