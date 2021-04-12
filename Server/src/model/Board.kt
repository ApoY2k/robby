package apoy2k.robby.model

import apoy2k.robby.exceptions.InvalidGameState
import org.slf4j.LoggerFactory

data class Board(val fields: List<List<Field>>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get the indices (row/col) of the provided field
     */
    fun getFieldIndices(field: Field): Pair<Int, Int> {
        val row = fields.indexOfFirst { it.contains(field) }
        return Pair(row, fields[row].indexOf(field))
    }

    /**
     * Execute a single movement card with the associated robot.
     * Note that even if the MovementCard specifies a movement of 2 or 3, this method will only
     * ever move the robot one (or zero, depending on the card) step.
     * Repeating movements must be done by the calling operation
     */
    fun execute(card: MovementCard) {
        val robot = card.player?.robot ?: throw InvalidGameState("Movement $card has no player or robot")
        logger.debug("Executing [$card] on [$robot]")
        robot.rotate(card.movement)

        if (card.hasSteps) {
            val direction = robot.getMovementDirection(card.movement)
            moveRobot(robot, direction)
        }
    }

    /**
     * Move a robot one step in the provided direction, regardless of its orientiation on the field
     */
    private fun moveRobot(robot: Robot, direction: Direction) {
        val sourceField = fields.flatten().firstOrNull { it.robot == robot }
            ?: throw InvalidGameState("Robot [$robot] could not be found on board cells")

        val targetField = getNeighbour(sourceField, direction)

        // Check for robot in the way (on the target field) and push them away, if possible
        targetField.robot?.let {
            // Move the other robot one step in the same direction as the robot currently execuging
            // its movement, regardless of the orientation of the robot being pushed
            val pushToField = getNeighbour(targetField, direction)

            // If the field that the robot would be pushed to also has a robot, it cannot be pushed
            // away by the original robot. Instead, the whole movement is halted and the original
            // robot should not move at all, as only one robot an be pushed away
            if (pushToField.robot != null) {
                return
            }

            pushToField.robot = targetField.robot

            // Free the targetField for the original robot that is actually moving currently
            targetField.robot = null
        }

        targetField.robot = sourceField.robot

        // Make sure to remove the robot from the old field!
        sourceField.robot = null
    }

    /**
     * Find the neighbouring field in a specific diection. If the source field is on the bounds of the board,
     * and the direction would take make the neighbour outside of it, the original field is returned
     */
    private fun getNeighbour(field: Field, direction: Direction): Field {
        val idx = getFieldIndices(field)

        val newRow = when (direction) {
            Direction.UP -> idx.first - 1
            Direction.DOWN -> idx.first + 1
            else -> idx.first
        }

        val newCol = when (direction) {
            Direction.LEFT -> idx.second - 1
            Direction.RIGHT -> idx.second + 1
            else -> idx.second
        }

        return fieldAt(newRow, newCol)
    }

    /**
     * Returns a field at the given row/col index. If either row or col is out of bounds,
     * the value is coerced into the constraints of the board and the field closes to the
     * given indices is returned
     */
    fun fieldAt(row: Int, col: Int): Field {
        val rowIdx = row.coerceIn(0..fields.lastIndex)
        return fields[rowIdx][col.coerceIn(0..fields[rowIdx].lastIndex)]
    }

    /**
     * Move all belts of the given type *one* tick
     */
    fun moveBelts(beltType: FieldType) {
        fields.flatten()
            .filter { it.type == beltType }
            .forEach { field ->
                val robot = field.robot ?: return@forEach

                // TODO First, turn robot depending on the relative direction the belt is running

                // Afterwards, move robot in the outgoing direction
                if (setOf(
                        Direction.LEFT,
                        Direction.UP,
                        Direction.RIGHT,
                        Direction.DOWN
                    ).contains(field.outgoingDirection)
                ) {
                    /**
                     * TODO This modifies the underlying fields
                     * which means the next iteration of the forEach alread operates on a mutated
                     * field-collection which might result in multiple moves being made
                     */
                    moveRobot(robot, field.outgoingDirection)
                }
            }
    }

    override fun toString(): String {
        return "Board [${fields.count()}x${fields[0].count()}]\r\n"
    }
}
