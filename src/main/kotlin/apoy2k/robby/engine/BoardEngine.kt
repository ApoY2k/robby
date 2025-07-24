package apoy2k.robby.engine

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldElement
import apoy2k.robby.model.Movement
import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Robot
import apoy2k.robby.model.getTurnMovement
import apoy2k.robby.model.hasSteps
import org.slf4j.LoggerFactory

typealias Board = List<List<Field>>

enum class BoardType {
    SANDBOX,
    CHOPSHOP,
    DEMO,
}

private data class Position(val row: Int, val col: Int)

private val logger = LoggerFactory.getLogger("apoy2k.robby.BoardEngine")

/**
 * Converts a list of fields to a field matrix, using the x/y coordinates to insert the fields
 * into the matrix
 */
fun List<Field>.toBoard(): Board {
    val board = mutableListOf<MutableList<Field>>()
    forEach {
        val row = board.getOrNull(it.positionY)
        if (row == null) {
            board.add(it.positionY, mutableListOf())
        }
        board[it.positionY].add(it.positionX, it)
    }
    return board
}

/**
 * Generate IDs for all fields, only used for unit testing
 */
fun Board.assignIds() = flatten()
    .forEachIndexed { idx, field ->
        field.id = idx
    }

/**
 * Execute a single movement card with the associated robot.
 * Note that even if the MovementCard specifies a movement of 2 or 3, this method will only
 * ever move the robot one (or zero, depending on the card) step.
 * Repeating movements must be done by the calling operation
 *
 * **Modifies fields and robots!**
 */
fun Board.execute(card: MovementCard, robots: Collection<Robot>) {
    val robot = robots.find { it.id == card.robotId } ?: return
    logger.debug("Executing {} on {}", card, robot)
    robot.rotate(card.movement)

    if (card.hasSteps()) {
        val direction = robot.getMovementDirection(card.movement)
        val positions = calculateRobotMove(robot, direction, robots)
        return applyPositions(positions, robots)
    } else {
        // Even if no steps were executed, rotating the robot could still cause laser overlay updates
        updateLaserOverlays(robots)
    }
}

/**
 * Move all belts of the given type *one* tick.
 *
 * **Modifies fields and robots!**
 */
fun Board.moveBelts(beltType: FieldElement, robots: Collection<Robot>) {
    // Collect new positions that result of the belt moves as a list of "to execute" movements
    // Each movement contains the robot that moves and the row/col index of the new field after the movement
    val newPositions = mutableMapOf<Position, Robot>()

    // Collect new orientations for robots, if they get turned by a belt when moved to their new position
    val orientations = mutableMapOf<Robot, Direction>()

    flatten()
        .filter { it.elements.contains(beltType) && it.outgoingDirection != null }
        .forEach { field ->
            val robot = robots.firstOrNull { it.id == field.robotId } ?: return@forEach
            val movements = calculateRobotMove(robot, field.outgoingDirection!!, robots)

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
        val previousPos = positionOf(robot.id)
        val incomingDirectionError = Exception(
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
                else -> Direction.DOWN
            }

            else -> throw incomingDirectionError
        }

        val nextField = fieldAt(position)
        val nextFieldOutgoingDirection = nextField.outgoingDirection
        val rotationMovement = when (nextFieldOutgoingDirection != null) {
            true -> getTurnMovement(incomingDirection, nextFieldOutgoingDirection)
            else -> Movement.STAY
        }
        robot.rotate(rotationMovement)
    }

    applyPositions(newPositions, robots)
}

/**
 * Touch checkpoints for robots
 * **Modifies robots!**
 */
fun Board.touchCheckpoints(robots: Collection<Robot>) = flatten()
    .filter { field -> field.hasFlag() && robots.any { robot -> field.robotId == robot.id } }
    .forEach { field ->
        // Only count up the passed checkpoints if the flag is actually the next in line
        // as in: the number of the already passed points must be exaclty one lower than the flag
        // that is currently being touched
        val flagNumber = field.getFlagNumber()
        val robot = robots.first { it.id == field.robotId }
        if (flagNumber != robot.passedCheckpoints + 1) {
            return@forEach
        }
        robot.passedCheckpoints += 1
    }

/**
 * Touch repair points for robots
 * **Modifies robots!**
 */
fun Board.touchRepair(robots: Collection<Robot>) = flatten()
    .filter { field ->
        field.elements.contains(FieldElement.REPAIR)
                && robots.any { robot -> field.robotId == robot.id }
    }
    .forEach { field ->
        val robot = robots.first { it.id == field.robotId }
        robot.damage = Integer.max(0, robot.damage - 1)
    }

/**
 * Touch modification points for robots
 * **Modifies robots!**
 */
fun Board.touchModifications(robots: Collection<Robot>) = flatten()
    .filter { field ->
        field.elements.contains(FieldElement.REPAIR_MOD)
                && robots.any { robot -> field.robotId == robot.id }
    }
    .forEach { field ->
        val robot = robots.first { it.id == field.robotId }
        robot.damage = Integer.max(0, robot.damage - 1)
        // TODO: Implement modifications
    }

/**
 * Place new robot on the board
 * **Modifies fields!**
 */
fun Board.placeRobot(robotId: Int) {
    findNextStartField().robotId = robotId
}

/**
 * Refresh the laser the laser(_2)(_h/_v) overlays on all fields that are in the direction of any laser
 * on the board. This should be called every time a robot has or was moved, so they overlays correctly
 * reflect that robots block lasers as well as walls.
 * **Modifies fields!**
 */
fun Board.updateLaserOverlays(robots: Collection<Robot>) = flatten()
    .filter {
        // Clear all overlays on fields that have them and add them to the result, so they are updated
        // in case the overlay has to be removed
        if (it.hasLaserOverlay()) {
            it.clearLaserOverlays()
        }

        // Exexute the overlay algorithm for each field that has a laser-emitting element on it
        it.hasLaserEmitter()
    }
    .forEach { sourceField ->
        val robot = robots.firstOrNull { it.id == sourceField.robotId }
        if (robot != null) {
            val element = robot.getInLineLaserFieldElements() ?: return@forEach

            // For robots, the direction of firing lasers is reversed, as the algorithm for updating the laser
            // overlays works intended for wall-mounted lasers, whose outgoingDirection is the placement
            // of the wall they're attached to, which is **opposite** to the firing direction of the laser
            // So, to use the same algorithm for robot lasers, the facing-direction of robots
            // must be reversed when passing it to the laser overlay function

            addLaserOverlay(sourceField, robot.facing.toOpposite(), element)
        }

        val outgoingDirection = sourceField.outgoingDirection
        val element = sourceField.getInLineLaserFieldElements() ?: return@forEach
        if (outgoingDirection != null) {
            addLaserOverlay(sourceField, outgoingDirection, element)
        }
    }

/**
 * Fire all lasers of the given type, damaging the first robot in its line.
 * This only works when laser overlays where updated before!
 * **Modifies robots!**
 */
fun Board.fireLasers(type: FieldElement, robots: Collection<Robot>) = flatten()
    .forEach { field ->
        val robot = robots.firstOrNull { field.robotId == it.id } ?: return@forEach

        if (type == FieldElement.LASER) {
            field.elements.forEach {
                if (it == FieldElement.LASER || it == FieldElement.LASER_H || it == FieldElement.LASER_V) {
                    robot.damage = Integer.min(10, robot.damage + 1)
                }
            }
        }

        if (type == FieldElement.LASER_2) {
            field.elements.forEach {
                if (it == FieldElement.LASER_2 || it == FieldElement.LASER_2_H || it == FieldElement.LASER_2_V) {
                    robot.damage = Integer.min(10, robot.damage + 2)
                }
            }
        }
    }

/**
 * Fire robot lasers
 */
fun Board.fireRobotLasers(robots: Collection<Robot>) = flatten()
    .forEach { field ->
        val robot = robots.firstOrNull { field.robotId == it.id } ?: return@forEach
        field.elements.forEach {
            if (it == FieldElement.ROBOT_LASER_H || it == FieldElement.ROBOT_LASER_V) {
                robot.damage = Integer.min(10, robot.damage + 1)
            }
        }
    }

/**
 * Finds the last field a laser can hit, starting from a field and going in a direciton until any blocking element
 * is encountered.
 * **CAREFUL**: The **direction** provided to this method is interpreted as being the **position of the wall**
 * a laser originated from, **not** the cardinal direction the laser **travels**!
 * So, to determine the last field in a direction, the parameter for this functions must be passed as the **opposite**
 * of that direction!
 * @return The last field that is considered "hit" by the laser.
 */
fun Board.findLastLaserHitField(startField: Field, direction: Direction): Field {
    val startPos = positionOf(startField)

    if (direction == Direction.LEFT) {
        if (startField.blocksHorizontalLaserExit(Direction.LEFT)) {
            return startField
        }

        for (col in startPos.col + 1 until this[startPos.row].size) {
            val field = fieldAt(Position(startPos.row, col))
            if (field.blocksHorizontalLaserEntry(Direction.LEFT)) {
                return fieldAt(Position(startPos.row, col - 1))
            }

            if (field.blocksHorizontalLaserExit(Direction.LEFT) || field.robotId != null) {
                return fieldAt(Position(startPos.row, col))
            }
        }

        return fieldAt(Position(startPos.row, this[startPos.row].size - 1))
    }

    if (direction == Direction.RIGHT) {
        if (startField.blocksHorizontalLaserExit(Direction.RIGHT)) {
            return startField
        }

        for (col in startPos.col - 1 downTo 0) {
            val field = fieldAt(Position(startPos.row, col))
            if (field.blocksHorizontalLaserEntry(Direction.RIGHT)) {
                return fieldAt(Position(startPos.row, col + 1))
            }

            if (field.blocksHorizontalLaserExit(Direction.RIGHT) || field.robotId != null) {
                return fieldAt(Position(startPos.row, col))
            }
        }

        return fieldAt(Position(startPos.row, 0))
    }

    if (direction == Direction.UP) {
        if (startField.blocksVerticalLaserExit(Direction.UP)) {
            return startField
        }

        for (row in startPos.row + 1 until size) {
            val field = fieldAt(Position(row, startPos.col))
            if (field.blocksVerticalLaserEntry(Direction.UP)) {
                return fieldAt(Position(row - 1, startPos.col))
            }

            if (field.blocksVerticalLaserExit(Direction.UP) || field.robotId != null) {
                return fieldAt(Position(row, startPos.col))
            }
        }

        return fieldAt(Position(size - 1, startPos.col))
    }

    if (direction == Direction.DOWN) {
        if (startField.blocksVerticalLaserExit(Direction.DOWN)) {
            return startField
        }

        for (row in startPos.row - 1 downTo 0) {
            val field = fieldAt(Position(row, startPos.col))
            if (field.blocksVerticalLaserEntry(Direction.DOWN)) {
                return fieldAt(Position(row + 1, startPos.col))
            }

            if (field.blocksVerticalLaserExit(Direction.DOWN) || field.robotId != null) {
                return fieldAt(Position(row, startPos.col))
            }
        }

        return fieldAt(Position(0, startPos.col))
    }

    return startField
}

/**
 * @return the field at the given row/col index
 */
fun Board.fieldAt(row: Int, col: Int) = fieldAt(Position(row, col))

/**
 * Find the next empty start field
 */
private fun Board.findNextStartField(): Field {
    val startFields = flatten().filter { it.hasStart() && it.robotId == null }
    if (startFields.isEmpty()) {
        throw Exception("No empty start fields available")
    }
    return startFields
        .minBy { it.getStartNumber() }
}

/**
 * @return the position of a robot on the board
 */
private fun Board.positionOf(robotId: Int): Position {
    val field = flatten().first { it.robotId == robotId }
    return positionOf(field)
}

/**
 * @return the indices (row/col) of the provided field
 */
private fun Board.positionOf(field: Field): Position {
    val row = indexOfFirst { it.contains(field) }
    return Position(row, this[row].indexOf(field))
}

/**
 * @return the field at the given position. If either row or col is out of bounds,
 * the value is coerced into the constraints of the board and the field closes to the
 * given indices is returned
 */
private fun Board.fieldAt(position: Position): Field {
    val rowIdx = position.row.coerceIn(0..lastIndex)
    return this[rowIdx][position.col.coerceIn(0..this[rowIdx].lastIndex)]
}

/**
 * Find the neighbouring field in a specific diection. If the source field is on the bounds of the board,
 * and the direction would make the neighbour out of bounds, null is returned.
 */
private fun Board.getNeighbour(field: Field, direction: Direction): Field? {
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

    if (newRow < 0 || newRow > lastIndex || newCol < 0 || newCol > this[newRow].lastIndex) {
        return null
    }

    return fieldAt(Position(newRow, newCol))
}

/**
 * Calculates the new position of a robot (and others, if applicable via pushing) that result when a robot moves
 * one step in the provided direction **without taking the robots own orientation into account!**
 */
private fun Board.calculateRobotMove(
    robot: Robot,
    direction: Direction,
    robots: Collection<Robot>
): Map<Position, Robot> {
    val sourceField = flatten().firstOrNull { it.robotId == robot.id }
        ?: throw Exception("Robot [$robot] could not be found on board cells")
    val result = mutableMapOf<Position, Robot>()
    val targetField = getNeighbour(sourceField, direction)

    // If the target field is not on the board or is a hole, reset the robot to an empty start field
    if (targetField == null || targetField.elements.contains(FieldElement.HOLE)) {
        val startField = findNextStartField()
        robot.damage = 10
        result[positionOf(startField)] = robot
        return result
    }

    // Check if a wall is in front of the robot's movement for the next step. This could either
    // be a wall on the current field on the same direction of travel, or on the neighboring field
    // on the opposite side of travel direction:
    // If traveling to the right, a blocking wall is either on the right of the source field
    // or the left of the target field
    if (sourceField.hasWallOn(direction) || targetField.hasWallOn(direction.toOpposite())) {
        return emptyMap()
    }

    // TODO Repeat wall check for wall-mounted devices (or include in "hasWallOn"?)

    // Check for robot in the way (on the target field) and push them away, if possible
    val pushedRobot = robots.find { it.id == targetField.robotId }
    if (pushedRobot != null) {
        // Move the other robot one step in the same direction as the robot currently execuging
        // its movement, regardless of the orientation of the robot being pushed
        val pushToField = getNeighbour(targetField, direction)

        // If the field the other robot is pushed to is not on the board, reset that robot
        // to an empty start field
        if (pushToField == null || pushToField.elements.contains(FieldElement.HOLE)) {
            val startField = findNextStartField()
            pushedRobot.damage = 10
            result[positionOf(startField)] = pushedRobot
            return result
        }

        // If there are walls the pushed robot would crash into, it cannot be pushed. This check is the same
        // as for the original moving robot, but from the perspective of the pushed robot, so the
        // source field (for the pushed robot) is the original target field, and the target field is the
        // field it would be pushed to. If this is true, no movements can be done on any robot
        if (targetField.hasWallOn(direction) || pushToField.hasWallOn(direction.toOpposite())) {
            return emptyMap()
        }

        // If the field that the robot would be pushed to also has a robot, it cannot be pushed
        // away by the original robot. Instead, the whole movement is halted and the original
        // robot should not move at all, as only one robot can be pushed away
        if (pushToField.robotId != null) {
            return emptyMap()
        }

        // Add the position of the pushed robot to the result of calculated movements
        result[positionOf(pushToField)] = pushedRobot
    }

    // Set the robot on the target field
    result[positionOf(targetField)] = robot

    return result
}

/**
 * Apply a map of position->robot entries to the board.
 *
 * **Modifies fields!**
 */
private fun Board.applyPositions(positions: Map<Position, Robot>, robots: Collection<Robot>) {
    flatten().forEach { field ->
        // If a robot on a field is referenced in the position map, it must be removed from its "old" field
        // before it can be placed on the new one, defined in the positions map
        if (positions.values.any { it.id == field.robotId }) {
            field.robotId = null
        }

        // If the field position is referenced in the provided position map, place the robot on it
        val position = positionOf(field)
        positions[position]?.let {
            field.robotId = it.id
        }
    }

    // As robots have moved, the laser overlays must be recalculated, so they can reflect possible blocks or
    // lasers by the moved robots
    updateLaserOverlays(robots)
}

/**
 * Add laser overlays based on a given source field and direction.
 * **Modifies fields!**
 */
private fun Board.addLaserOverlay(sourceField: Field, direction: Direction, element: FieldElement) {
    val lastHitField = findLastLaserHitField(sourceField, direction)

    val startPos = positionOf(sourceField)
    val endPos = positionOf(lastHitField)

    // With the start and endField finalized, iterate over all fields between
    // them in the direction of the laser, depending on the direction of the laser and collect
    // all fields on the way to the map edge

    val fields = when (direction) {
        Direction.RIGHT -> (endPos.col until startPos.col).map { fieldAt(Position(startPos.row, it)) }
        Direction.LEFT -> (startPos.col + 1..endPos.col).map { fieldAt(Position(startPos.row, it)) }
        Direction.DOWN -> (endPos.row until startPos.row).map { fieldAt(Position(it, startPos.col)) }
        Direction.UP -> (startPos.row + 1..endPos.row).map { fieldAt(Position(it, startPos.col)) }
    }

    // Then apply the laser condition of the source laser field to all fields in the
    // direction of the source laser to build the "firing line" of the laser on the board
    fields.forEach apply@{ inLineField ->
        inLineField.elements.add(element)
    }
}

/**
 * Find the highset flag number on a board
 */
fun Board.highestFlagNumber() = flatten()
    .filter { it.hasFlag() }
    .maxBy { it.getFlagNumber() }
    .getFlagNumber()
