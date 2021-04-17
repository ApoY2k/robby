package apoy2k.robby.model

import apoy2k.robby.exceptions.InvalidGameState
import org.slf4j.LoggerFactory

data class Board(val fields: List<List<Field>>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get the indices (row/col) of the provided field
     */
    fun positionOf(field: Field): Position {
        val row = fields.indexOfFirst { it.contains(field) }
        return Position(row, fields[row].indexOf(field))
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
            val positions = calculateRobotMove(robot, direction)
            applyPositions(positions)
        }
    }

    /**
     * Calculates the new position of a robot (and others, if applicable via pushing) that result when a robot moves
     * one step in the provided direction **without taking the robots own orientation into account!**
     */
    private fun calculateRobotMove(robot: Robot, direction: Direction): Map<Position, Robot> {
        val sourceField = fields.flatten().firstOrNull { it.robot == robot }
            ?: throw InvalidGameState("Robot [$robot] could not be found on board cells")
        val targetField = getNeighbour(sourceField, direction)
        val result = mutableMapOf<Position, Robot>()

        // Check for robot in the way (on the target field) and push them away, if possible
        targetField.robot?.let {
            // Move the other robot one step in the same direction as the robot currently execuging
            // its movement, regardless of the orientation of the robot being pushed
            val pushToField = getNeighbour(targetField, direction)

            // If the field that the robot would be pushed to also has a robot, it cannot be pushed
            // away by the original robot. Instead, the whole movement is halted and the original
            // robot should not move at all, as only one robot an be pushed away
            if (pushToField.robot != null) {
                return emptyMap()
            }

            // Add the position of the pushed robot to the result of calculated movements
            result[positionOf(pushToField)] = it
        }

        sourceField.robot?.let {
            result[positionOf(targetField)] = it
        }

        return result
    }

    /**
     * Apply a map of position->robot entries to the board
     */
    private fun applyPositions(positions: Map<Position, Robot>) {
        fields.flatten().forEach { field ->
            // If a robot on a field is referenced in the position map, it must be removed from its "old" field
            // before it can be placed on the new one, defined in the positions map
            if (positions.containsValue(field.robot)) {
                field.robot = null
            }

            // If the field position is referenced in the provided position map, place the robot on it
            val position = positionOf(field)
            positions[position]?.let {
                field.robot = it
            }
        }
    }

    /**
     * Find the neighbouring field in a specific diection. If the source field is on the bounds of the board,
     * and the direction would take make the neighbour outside of it, the original field is returned
     */
    private fun getNeighbour(field: Field, direction: Direction): Field {
        val idx = positionOf(field)

        val newRow = when (direction) {
            Direction.UP -> idx.row - 1
            Direction.DOWN -> idx.row + 1
            else -> idx.row
        }

        val newCol = when (direction) {
            Direction.LEFT -> idx.col - 1
            Direction.RIGHT -> idx.col + 1
            else -> idx.col
        }

        return fieldAt(Position(newRow, newCol))
    }

    /**
     * Returns a field at the given row/col index. If either row or col is out of bounds,
     * the value is coerced into the constraints of the board and the field closes to the
     * given indices is returned
     */
    fun fieldAt(position: Position): Field {
        val rowIdx = position.row.coerceIn(0..fields.lastIndex)
        return fields[rowIdx][position.col.coerceIn(0..fields[rowIdx].lastIndex)]
    }

    /**
     * Move all belts of the given type *one* tick
     */
    fun moveBelts(beltType: FieldType) {
        // Collect new positions that result of the belt moves as a list of "to execute" movements
        // Each movement contains the robot that moves and the row/col index of the new field after the movement
        val newPositions = mutableMapOf<Position, Robot>()

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
                    newPositions.putAll(calculateRobotMove(robot, field.outgoingDirection))
                }
            }

        applyPositions(newPositions)
    }

    override fun toString(): String {
        return "Board[${fields.count()}x${fields[0].count()}]"
    }
}

data class Position(val row: Int, val col: Int)
