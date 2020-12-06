package apoy2k.robby.model

import apoy2k.robby.exceptions.InvalidGameState
import org.slf4j.LoggerFactory

data class Board(val fields: List<List<Field>>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Execute a single movement card with the associated robot
     */
    fun execute(card: MovementCard) {
        val robot = card.player?.robot ?: throw InvalidGameState("Movement $card has no player or robot")

        logger.debug("Executing [$card] on [$robot]")

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
        val sourceField = fields.flatten().firstOrNull { it.robot == robot }
            ?: throw InvalidGameState("Robot [$robot] could not be found on board cells")

        val targetField = findField(sourceField, direction, steps)

        targetField.robot = sourceField.robot
        targetField.robot?.orientation = direction

        if (steps > 0) {
            sourceField.robot = null
        }
    }

    /**
     * Find a field oriented an amount of steps away from a start field in a given direction
     */
    private fun findField(field: Field, direction: Orientation, steps: Int): Field {
        val row = fields.indexOfFirst { it.contains(field) }
        val col = fields[row].indexOf(field)

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
        newRow = newRow.coerceIn(0..fields.lastIndex)
        newCol = newCol.coerceIn(0..fields[newRow].lastIndex)

        return fields[newRow][newCol]
    }
}
