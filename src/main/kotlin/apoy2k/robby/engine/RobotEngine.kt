package apoy2k.robby.engine

import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Robot

class RobotEngine {

    /**
     * Clear all registers of a robot.
     * @param respectDamageLock If true, will not clear registers that should be locked based
     * on the current damage value of this robot
     */
    fun clearRegisters(robot: Robot, respectDamageLock: Boolean = true) {
        val newRegisters = mutableMapOf<Int, MovementCard?>()
        newRegisters.putAll(registers)

        if (respectDamageLock) {
            if (robot.damage <= 8) {
                newRegisters[1] = null
            }

            if (robot.damage <= 7) {
                newRegisters[2] = null
            }

            if (robot.damage <= 6) {
                newRegisters[3] = null
            }

            if (robot.damage <= 5) {
                newRegisters[4] = null
            }

            if (robot.damage <= 4) {
                newRegisters[5] = null
            }
        } else {
            newRegisters.clear()
        }

        registers = newRegisters
    }

    /**
     * Returns true if the robot has movement cards in all registers
     */
    fun hasAllRegistersFilled(): Boolean = registers.all { it.value != null }

    /**
     * Return the movement card in a register (or null if no card is set)
     */
    fun getRegister(register: Int) = registers[register]

}
