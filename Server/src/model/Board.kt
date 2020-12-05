package apoy2k.robby.model

import apoy2k.robby.exceptions.InvalidGameState
import org.slf4j.LoggerFactory
import java.lang.IndexOutOfBoundsException
import java.util.*

class Board(internal val cells: List<List<Field>>) {

    /**
     * Execute a single movement card with the associated robot
     */
    fun execute(card: MovementCard) {
        val robot = card.player?.robot ?: throw InvalidGameState("Movement $card has no player or robot")

        val direction = robot.getOrientationFrom(card.movement)
        val steps = when (card.movement) {
            Movement.STRAIGHT, Movement.BACKWARDS -> 1
            Movement.STRAIGHT_2, Movement.BACKWARDS_2 -> 2
            Movement.STRAIGHT_3, Movement.BACKWARDS_3 -> 3
            else -> 0
        }

        moveRobot(robot, direction, steps)
    }

    /**
     * Move a robot an amount of steps in a defined direction
     */
    private fun moveRobot(robot: Robot, direction: Orientation, steps: Int) {
        val sourceField = cells.flatten().firstOrNull { it.robot == robot }
            ?: throw InvalidGameState("Robot [$robot] could not be found on board cells")

        val targetField = findField(sourceField, direction, steps)

        targetField.robot = sourceField.robot
        sourceField.robot = null
    }

    /**
     * Find a field oriented an amount of steps away from a start field in a given direction
     */
    private fun findField(field: Field, direction: Orientation, steps: Int): Field {
        val row = cells.indexOfFirst { it.contains(field) }
        val col = cells[row].indexOf(field)

        var newRow = when (direction) {
            Orientation.UP -> row - steps
            Orientation.DOWN -> row + steps
            else -> row
        }

        var newCol = when (direction) {
            Orientation.LEFT -> col - steps
            Orientation.RIGHT -> col + steps
            else -> col
        }

        // Make sure robots can't move off grid by constraining them into the grid indexes
        newRow = newRow.coerceIn(0..cells.lastIndex)
        newCol = newCol.coerceIn(0..cells[newRow].lastIndex)

        return cells[newRow][newCol]
    }

    /**
     * Places a robot on a field
     */
    private fun place(fieldId: UUID, robot: Robot) {
        cells.forEach { it.find { it.id == fieldId }?.robot = robot }
    }
}
