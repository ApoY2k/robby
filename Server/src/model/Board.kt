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

        val direction = robot.getMovementDirection(card.movement)
        val steps = when (card.movement) {
            Movement.STRAIGHT, Movement.BACKWARDS -> 1
            Movement.STRAIGHT_2 -> 2
            Movement.STRAIGHT_3 -> 3
            else -> 0
        }

        robot.rotate(card.movement)
        moveRobot(robot, direction, steps)
    }

    /**
     * Move a robot an amount of steps in a defined direction
     */
    private fun moveRobot(robot: Robot, direction: Direction, steps: Int) {
        val sourceField = fields.flatten().firstOrNull { it.robot == robot }
            ?: throw InvalidGameState("Robot [$robot] could not be found on board cells")

        val targetField = findField(sourceField, direction, steps)

        if (targetField != sourceField) {
            targetField.robot = sourceField.robot

            if (steps > 0) {
                sourceField.robot = null
            }
        }
    }

    /**
     * Find a field oriented an amount of steps away from a start field in a given direction
     */
    private fun findField(field: Field, direction: Direction, steps: Int): Field {
        val row = fields.indexOfFirst { it.contains(field) }
        val col = fields[row].indexOf(field)

        var newRow = when (direction) {
            Direction.UP -> row - steps
            Direction.DOWN -> row + steps
            else -> row
        }

        var newCol = when (direction) {
            Direction.LEFT -> col - steps
            Direction.RIGHT -> col + steps
            else -> col
        }

        // Make sure robots can't move off grid by constraining them into the grid indexes
        newRow = newRow.coerceIn(0..fields.lastIndex)
        newCol = newCol.coerceIn(0..fields[newRow].lastIndex)

        return fields[newRow][newCol]
    }
}
