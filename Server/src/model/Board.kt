package apoy2k.robby.model

import apoy2k.robby.exceptions.InvalidGameState
import org.slf4j.LoggerFactory
import kotlin.math.atan2

data class Board(val fields: List<List<Field>>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get the position of a robot on the field
     */
    fun positionOf(robot: Robot): Position {
        val field = fields.flatten().first { it.robot == robot }
        return positionOf(field)
    }

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
        val robot = card.player?.robot ?: throw InvalidGameState("$card has no player or robot")
        logger.debug("Executing $card on $robot")
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
            // robot should not move at all, as only one robot and be pushed away
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
     * and the direction would make the neighbour out of bounds, the original field is returned
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

        // Collect new orientations for robots, if they get turned by a belt when moved to their new position
        val orientations = mutableMapOf<Robot, Direction>()

        fields.flatten()
            .filter { it.type == beltType }
            .forEach { field ->
                val robot = field.robot ?: return@forEach

                val movements = calculateRobotMove(robot, field.outgoingDirection)

                    // Check if two robots would be moved into the same space and if so, don't move either one
                    .filter { movement ->
                        // If the current result set alredy contains a new position, it means and the
                        // two movements are *not* for the same robot, they must both be removed
                        // as no two robots can be pushed into the same position
                        newPositions[movement.key]?.let {

                            // Movement is in the newPositions list and both movements apply to different
                            // robots -> must not be added (-> false for the filter return value)
                            if (it != movement.value) {
                                newPositions.remove(movement.key)

                                // Also make sure to remove the robot from the orientations map
                                // if it doesn't actually move after all
                                orientations.remove(it)

                                return@filter false
                            }
                        }

                        // Movement is not already in the newPositions map
                        true
                    }

                newPositions.putAll(movements)
            }

        // Rotate robots that have been moved by the belt movements
        // (as the newPositions map only contains positions that were the result from valid robot movements
        // from belts, all of these robots might require a rotation)
        newPositions.forEach { (position, robot) ->
            val previousPos = positionOf(robot)
            val incomingDirectionError = InvalidGameState(
                "Cannot determine incoming direction of [$robot] moving from" +
                        " [$previousPos] to [$position]"
            )

            // Determine the direction from which the robot moves onto the field
            val incomingDirection = when {
                previousPos.row == position.row -> when {
                    previousPos.col < position.col -> Direction.LEFT
                    previousPos.col > position.col -> Direction.RIGHT
                    else -> throw incomingDirectionError
                }
                previousPos.col == position.col -> when {
                    previousPos.row < position.row -> Direction.UP
                    previousPos.row > position.row -> Direction.DOWN
                    else -> throw incomingDirectionError
                }
                else -> throw incomingDirectionError
            }

            val nextField = fieldAt(position)
            val rotationMovement = getTurnDirection(incomingDirection, nextField.outgoingDirection)

            robot.rotate(rotationMovement)
        }

        applyPositions(newPositions)
    }

    /**
     * Determine the movement (turn) that translates one direction into another
     */
    private fun getTurnDirection(incomingDirection: Direction, outgoingDirection: Direction): Movement {
        val incVec = incomingDirection.toVec2()
        val outVec = outgoingDirection.toVec2()

        val angle = atan2(
            incVec.x * outVec.x - incVec.y * outVec.y,
            incVec.x * outVec.x + incVec.y * outVec.y
        )

        return when {
            angle < 0 -> Movement.TURN_RIGHT
            angle > 0 -> Movement.TURN_LEFT
            else -> Movement.STAY
        }
    }

    fun fireLasers(laserType: FieldType) {
        /**
         * For each laser:
         *  - determine orientation and find next wall (or end of board)
         *  - find way to determine if wall is blocking before or after the field it's attached to
         *      (near / far side of laser)
         *  - determine robots between laser and wall
         *  - damage each robot for 1 point
         */
        fields.flatten()
            .filter { it.type == laserType }
            .forEach { field ->
                val startPos = positionOf(field)
                val direction = field.outgoingDirection
                val nextWall = firstFieldByDirection(field, direction, FieldType.WALL)
                val wallPosition = positionOf(nextWall)

                // Depending on *where* the wall is on the field, the laser might still hit a target on the field,
                // or it is stopped on the edge to the field (that is, it will stop one
                // field *ahead* of the found field instead of the field itself)
                val lastHitField = when (direction) {
                    Direction.LEFT -> when (nextWall.hasDirection(Direction.RIGHT)) {
                        true -> fieldAt(Position(wallPosition.row, wallPosition.col + 1))
                        false -> nextWall
                    }
                    Direction.UP -> when (nextWall.hasDirection(Direction.DOWN)) {
                        true -> fieldAt(Position(wallPosition.row + 1, wallPosition.col))
                        false -> nextWall
                    }
                    Direction.DOWN -> when (nextWall.hasDirection(Direction.UP)) {
                        true -> fieldAt(Position(wallPosition.row - 1, wallPosition.col))
                        false -> nextWall
                    }
                    Direction.RIGHT -> when (nextWall.hasDirection(Direction.LEFT)) {
                        true -> fieldAt(Position(wallPosition.row, wallPosition.col - 1))
                        false -> nextWall
                    }
                    Direction.NONE -> nextWall
                }

                val endPos = positionOf(lastHitField)

                // With the start and endField finalized, iterate over all fields between
                // them in the direction of the laser, depending on the direciton of the laser and collect
                // all fields on the way to the map edge
                val fields = when (direction) {
                    Direction.LEFT -> (endPos.col..startPos.col).map { fieldAt(Position(startPos.row, it)) }
                    Direction.RIGHT -> (startPos.col..endPos.col).map { fieldAt(Position(startPos.row, it)) }
                    Direction.UP -> (endPos.row..startPos.row).map { fieldAt(Position(it, startPos.col)) }
                    Direction.DOWN -> (startPos.row..endPos.row).map { fieldAt(Position(it, startPos.col)) }
                    else -> emptyList()
                }

                applyLaserCondition(fields, laserType)
            }
    }

    /**
     * Aplys the `LASER` condition to all provided fields, until either:
     * - A robot is encountered. It is then damaged for 1 and the iteration stops
     * - The end of the list is reached
     */
    private fun applyLaserCondition(fields: Iterable<Field>, laserType: FieldType) {
        fields.forEach { field ->
            when (laserType) {
                FieldType.LASER -> field.conditions.add(FieldCondition.LASER)
                FieldType.LASER_2 -> field.conditions.add(FieldCondition.LASER_2)
                else -> Unit
            }

            field.robot?.let {
                it.damage += 1
                return
            }
        }
    }

    /**
     * Finds the first field, starting from a field and going in a direciton until a field of the
     * searched type is found. Returns the found field.
     */
    fun firstFieldByDirection(startField: Field, direction: Direction, fieldType: FieldType): Field {
        val startPos = positionOf(startField)

        return when (direction) {
            Direction.RIGHT -> {
                for (col in startPos.col + 1 until fields[startPos.row].size) {
                    val field = fieldAt(Position(startPos.row, col))
                    if (field.type == fieldType) {
                        return field
                    }
                }

                return fieldAt(Position(startPos.row, fields[startPos.row].size - 1))
            }
            Direction.LEFT -> {
                for (col in startPos.col - 1 downTo 0) {
                    val field = fieldAt(Position(startPos.row, col))
                    if (field.type == fieldType) {
                        return field
                    }
                }

                return fieldAt(Position(startPos.row, 0))
            }
            Direction.DOWN -> {
                for (row in startPos.row + 1 until fields.size) {
                    val field = fieldAt(Position(row, startPos.col))
                    if (field.type == fieldType) {
                        return field
                    }
                }

                return fieldAt(Position(fields.size - 1, startPos.col))
            }
            Direction.UP -> {
                for (row in startPos.row - 1 downTo 0) {
                    val field = fieldAt(Position(row, startPos.col))
                    if (field.type == fieldType) {
                        return field
                    }
                }

                return fieldAt(Position(0, startPos.col))
            }
            else -> startField
        }
    }

    override fun toString(): String {
        return "Board[${fields.count()}x${fields[0].count()}]"
    }
}

data class Position(val row: Int, val col: Int)
