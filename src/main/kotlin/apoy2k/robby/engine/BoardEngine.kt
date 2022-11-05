package apoy2k.robby.engine

import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.slf4j.LoggerFactory
import kotlin.math.atan2

class BoardEngine(
    val board: List<List<Field>>,
    assignIds: Boolean = false
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    init {
        // Generate some IDs for all fields, only used for unit testing
        if (assignIds) {
            board.flatten().forEachIndexed { idx, field ->
                field.id = idx
            }
        }
    }

    companion object {

        /**
         * Build a board engine instance for a specific game
         */
        @JvmStatic
        fun buildFromGame(gameId: Int, database: Database): BoardEngine {
            val fields = database.fields.filter { it.gameId eq gameId }.map { it }
            return BoardEngine(fieldListToMatrix(fields))
        }

        /**
         * Converts a list of fields to a field matrix, using the x/y coordinates to insert the fields
         * into the matrix
         */
        @JvmStatic
        fun fieldListToMatrix(fields: List<Field>): List<List<Field>> {
            val board = mutableListOf<MutableList<Field>>()
            fields
                .forEach {
                    val row = board.getOrNull(it.positionY)
                    if (row == null) {
                        board.add(it.positionY, mutableListOf())
                    }
                    board[it.positionY].add(it.positionX, it)
                }
            return board
        }
    }

    /**
     * Get the position of a robot on the field
     */
    private fun positionOf(robotId: Int): Position {
        val field = board.flatten().first { it.robotId == robotId }
        return positionOf(field)
    }

    /**
     * Get the indices (row/col) of the provided field
     */
    private fun positionOf(field: Field): Position {
        // TODO For this to work reliably, the fields *must* have IDs - could there be a better way to handle this?
        val row = board.indexOfFirst { it.contains(field) }
        return Position(row, board[row].indexOf(field))
    }

    /**
     * Execute a single movement card with the associated robot.
     * Note that even if the MovementCard specifies a movement of 2 or 3, this method will only
     * ever move the robot one (or zero, depending on the card) step.
     * Repeating movements must be done by the calling operation
     *
     * @return Collection of fields updated during execution
     */
    fun execute(card: MovementCard, robot: Robot): Collection<Field> {
        logger.debug("Executing $card on $robot")
        robot.rotate(card.movement)

        if (card.hasSteps()) {
            val direction = robot.getMovementDirection(card.movement)
            val positions = calculateRobotMove(robot.id, direction)
            return applyPositions(positions)
        }

        return emptyList()
    }

    /**
     * Calculates the new position of a robot (and others, if applicable via pushing) that result when a robot moves
     * one step in the provided direction **without taking the robots own orientation into account!**
     */
    private fun calculateRobotMove(robotId: Int, direction: Direction): Map<Position, Int> {
        val sourceField = board.flatten().firstOrNull { it.robotId == robotId }
            ?: throw InvalidGameState("Robot [$robotId] could not be found on board cells")
        val targetField = getNeighbour(sourceField, direction)
        val result = mutableMapOf<Position, Int>()

        // Check for robot in the way (on the target field) and push them away, if possible
        targetField.robotId?.let {
            // Move the other robot one step in the same direction as the robot currently execuging
            // its movement, regardless of the orientation of the robot being pushed
            val pushToField = getNeighbour(targetField, direction)

            // If the field that the robot would be pushed to also has a robot, it cannot be pushed
            // away by the original robot. Instead, the whole movement is halted and the original
            // robot should not move at all, as only one robot and be pushed away
            if (pushToField.robotId != null) {
                return emptyMap()
            }

            // Add the position of the pushed robot to the result of calculated movements
            result[positionOf(pushToField)] = it
        }

        sourceField.robotId?.let {
            result[positionOf(targetField)] = it
        }

        return result
    }

    /**
     * Apply a map of position->robot entries to the board
     *
     * @return Collection of fields updated during execution
     */
    private fun applyPositions(positions: Map<Position, Int>): Collection<Field> {
        val result = mutableSetOf<Field>()
        board.flatten().forEach { field ->
            // If a robot on a field is referenced in the position map, it must be removed from its "old" field
            // before it can be placed on the new one, defined in the positions map
            if (positions.containsValue(field.robotId)) {
                field.robotId = null
                result.add(field)
            }

            // If the field position is referenced in the provided position map, place the robot on it
            val position = positionOf(field)
            positions[position]?.let {
                field.robotId = it
                result.add(field)
            }
        }

        // As robots have moved, the laser overlays must be recalculated, so they can reflect possible blocks or
        // lasers by the moved robots
        val overlayUpdates = updateLaserOverlays()
        result.addAll(overlayUpdates)

        return result
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
     * Returns a field at the given position. If either row or col is out of bounds,
     * the value is coerced into the constraints of the board and the field closes to the
     * given indices is returned
     */
    private fun fieldAt(position: Position): Field {
        val rowIdx = position.row.coerceIn(0..board.lastIndex)
        return board[rowIdx][position.col.coerceIn(0..board[rowIdx].lastIndex)]
    }

    /**
     * Returns a field at the given row/col index
     */
    fun fieldAt(row: Int, col: Int) = fieldAt(Position(row, col))

    /**
     * Move all belts of the given type *one* tick
     *
     * @return Collection of fields updated during execution
     */
    fun moveBelts(beltType: FieldElement, robots: List<Robot>): Collection<Field> {
        // Collect new positions that result of the belt moves as a list of "to execute" movements
        // Each movement contains the robot that moves and the row/col index of the new field after the movement
        val newPositions = mutableMapOf<Position, Int>()

        // Collect new orientations for robots, if they get turned by a belt when moved to their new position
        val orientations = mutableMapOf<Int, Direction>()

        board.flatten()
            .filter { it.elements.contains(beltType) && it.robotId != null }
            .forEach { field ->
                val movements = calculateRobotMove(field.robotId!!, field.outgoingDirection)

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
        newPositions.forEach { (position, robotId) ->
            val previousPos = positionOf(robotId)
            val incomingDirectionError = InvalidGameState(
                "Cannot determine incoming direction of [$robotId] moving from" +
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
            val rotationMovement = getTurnDirection(incomingDirection, nextField.outgoingDirection)
            val robot = robots.find { it.id == robotId } ?: return@forEach
            robot.rotate(rotationMovement)
        }

        return applyPositions(newPositions)
    }

    /**
     * Touch checkpoints for robots
     */
    fun touchCheckpoints(robots: List<Robot>) {
        // TODO Touching the same checkpoint multiple times should not increase the counter
        board.flatten()
            .filter { field ->
                field.elements.contains(FieldElement.FLAG)
                        && robots.any { robot -> field.robotId == robot.id }
            }
            .forEach { field ->
                val robot = robots.first { it.id == field.robotId }
                robot.passedCheckpoints += 1
            }
    }

    /**
     * Touch repair points for robots
     */
    fun touchRepair(robots: List<Robot>) {
        board.flatten()
            .filter { field ->
                field.elements.contains(FieldElement.REPAIR)
                        && robots.any { robot -> field.robotId == robot.id }
            }
            .forEach { field ->
                val robot = robots.first { it.id == field.robotId }
                robot.damage = Integer.max(0, robot.damage - 1)
            }
    }

    /**
     * Touch modification points for robots
     */
    fun touchModifications(robots: List<Robot>) {
        board.flatten()
            .filter { field ->
                field.elements.contains(FieldElement.REPAIR_MOD)
                        && robots.any { robot -> field.robotId == robot.id }
            }
            .forEach {
                // TODO: Implement modifications
            }
    }

    /**
     * Place new robot on the board
     */
    fun placeRobot(robotId: Int): Field {
        return board.flatten()
            .first { it.elements.contains(FieldElement.START) && it.robotId == null }
            .also { it.robotId = robotId }
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

    /**
     * Refresh the laser the laser(_2)(_h/_v) ovlerays on all fields that are in the direction of any laser
     * on the board. This should be called every time a robot has or was moved, so they overlays correctly
     * reflect that robots block lasers as well as walls.
     * @return Collection of all fields that were updated with overlays
     */
    fun updateLaserOverlays(): Collection<Field> {
        val result = mutableSetOf<Field>()

        /**
         * For each laser:
         *  - determine orientation and find next wall (or end of board)
         *  - find way to determine if wall is blocking before or after the field it's attached to
         *      (near / far side of laser)
         *  - determine robots between laser and wall
         *  - damage each robot for 1 point
         */
        board.flatten()
            .filter {
                // Reset existing overlays before
                it.elements.removeIf { el -> laserOverlays.contains(el) }

                // Exexute the overlay algorithm for each field that has a laser-emitting element on it
                // TODO Also calculate overlays for robot lasers
                it.elements.contains(FieldElement.LASER) || it.elements.contains(FieldElement.LASER_2)
            }
            .forEach { sourceField ->
                val startPos = positionOf(sourceField)
                val direction = sourceField.outgoingDirection
                val lastNonBlockedField = findLastLaserHitField(sourceField, direction)
                val lastNonBlockedFieldPos = positionOf(lastNonBlockedField)

                // Special case for walls, as depending on the positioning of the wall, the laser might
                // be able to pass the field, e.g. when the wall is parallel to the laser direction
                // or when the wall is on the far side of the field

                // As such, depending on *where* the wall is on the field, the laser might still hit a target on
                // the field, or it is stopped on the edge to the field (that is, it will stop one
                // field *ahead* of the found field instead of the field itself)
                val lastHitField = when (direction) {
                    Direction.LEFT -> when (lastNonBlockedField.hasDirection(Direction.RIGHT)) {
                        true -> fieldAt(Position(lastNonBlockedFieldPos.row, lastNonBlockedFieldPos.col + 1))
                        false -> lastNonBlockedField
                    }

                    Direction.UP -> when (lastNonBlockedField.hasDirection(Direction.DOWN)) {
                        true -> fieldAt(Position(lastNonBlockedFieldPos.row + 1, lastNonBlockedFieldPos.col))
                        false -> lastNonBlockedField
                    }

                    Direction.DOWN -> when (lastNonBlockedField.hasDirection(Direction.UP)) {
                        true -> fieldAt(Position(lastNonBlockedFieldPos.row - 1, lastNonBlockedFieldPos.col))
                        false -> lastNonBlockedField
                    }

                    Direction.RIGHT -> when (lastNonBlockedField.hasDirection(Direction.LEFT)) {
                        true -> fieldAt(Position(lastNonBlockedFieldPos.row, lastNonBlockedFieldPos.col - 1))
                        false -> lastNonBlockedField
                    }

                    Direction.NONE -> lastNonBlockedField
                }

                val endPos = positionOf(lastHitField)

                // With the start and endField finalized, iterate over all fields between
                // them in the direction of the laser, depending on the direciton of the laser and collect
                // all fields on the way to the map edge

                // Also, add an addtional step to the position so the laser conditions is not applied on the
                // lasers source field itself!

                val fields = when (direction) {
                    Direction.LEFT -> (endPos.col until startPos.col).map { fieldAt(Position(startPos.row, it)) }
                    Direction.RIGHT -> (startPos.col + 1..endPos.col).map { fieldAt(Position(startPos.row, it)) }
                    Direction.UP -> (endPos.row until startPos.row).map { fieldAt(Position(it, startPos.col)) }
                    Direction.DOWN -> (startPos.row + 1..endPos.row).map { fieldAt(Position(it, startPos.col)) }
                    else -> emptyList()
                }

                // Then apply the laser condition of the source laser field to all fields in the
                // direction of the source laser to build the "firing line" of the laser on the board
                fields.forEach apply@{ inLineField ->
                    val element = sourceField.getInLineLaserFieldElements() ?: return@apply
                    inLineField.elements.add(element)
                    result.add(inLineField)
                }
            }

        return result
    }

    /**
     * Fire all lasers of the given type, damaging the first robot in its line.
     * This only works when laser overlays where updated before!
     */
    fun fireLasers(type: FieldElement, robots: List<Robot>) {
        board.flatten()
            .forEach { field ->
                val robot = robots.firstOrNull { field.robotId == it.id } ?: return@forEach

                if (type == FieldElement.LASER
                    && (field.elements.contains(FieldElement.LASER)
                            || field.elements.contains(FieldElement.LASER_H)
                            || field.elements.contains(FieldElement.LASER_V))
                ) {
                    robot.damage = Integer.min(10, robot.damage + 1)
                }

                if (type == FieldElement.LASER_2
                    && (field.elements.contains(FieldElement.LASER_2)
                            || field.elements.contains(FieldElement.LASER_2_H)
                            || field.elements.contains(FieldElement.LASER_2_V))
                ) {
                    robot.damage = Integer.min(10, robot.damage + 2)
                }
            }
    }

    /**
     * Fire robot lasers
     */
    fun fireRobotLasers() {
        // TODO
    }

    /**
     * Finds the last field a laser can hit, starting from a field and going in a direciton until any blocking element
     * is encountered. Returns the last field that is considered "hit" by the laser.
     */
    fun findLastLaserHitField(startField: Field, direction: Direction): Field {
        val startPos = positionOf(startField)

        if (direction == Direction.RIGHT) {
            for (col in startPos.col + 1 until board[startPos.row].size) {
                val field = fieldAt(Position(startPos.row, col))
                if (!field.blocksHorizontalLaser()) {
                    continue
                }

                if (field.elements.contains(FieldElement.WALL)) {
                    return when (field.hasDirection(Direction.LEFT)) {
                        true -> fieldAt(Position(startPos.row, col - 1))
                        else -> field
                    }
                }

                return when (field.hasDirection(Direction.RIGHT)) {
                    true -> fieldAt(Position(startPos.row, col - 1))
                    else -> field
                }
            }

            return fieldAt(Position(startPos.row, board[startPos.row].size - 1))
        }

        if (direction == Direction.LEFT) {
            for (col in startPos.col - 1 downTo 0) {
                val field = fieldAt(Position(startPos.row, col))
                if (!field.blocksHorizontalLaser()) {
                    continue
                }

                if (field.elements.contains(FieldElement.WALL)) {
                    return when (field.hasDirection(Direction.RIGHT)) {
                        true -> fieldAt(Position(startPos.row, col + 1))
                        else -> field
                    }
                }

                return when (field.hasDirection(Direction.LEFT)) {
                    true -> fieldAt(Position(startPos.row, col + 1))
                    else -> field
                }
            }

            return fieldAt(Position(startPos.row, 0))
        }

        if (direction == Direction.DOWN) {
            for (row in startPos.row + 1 until board.size) {
                val field = fieldAt(Position(row, startPos.col))
                if (!field.blocksVerticalLaser()) {
                    continue
                }

                if (field.elements.contains(FieldElement.WALL)) {
                    return when (field.hasDirection(Direction.UP)) {
                        true -> fieldAt(Position(row - 1, startPos.col))
                        else -> field
                    }
                }

                return when (field.hasDirection(Direction.DOWN)) {
                    true -> fieldAt(Position(row - 1, startPos.col))
                    else -> field
                }
            }

            return fieldAt(Position(board.size - 1, startPos.col))
        }

        if (direction == Direction.UP) {
            for (row in startPos.row - 1 downTo 0) {
                val field = fieldAt(Position(row, startPos.col))
                if (!field.blocksVerticalLaser()) {
                    continue
                }

                if (field.elements.contains(FieldElement.WALL)) {
                    return when (field.hasDirection(Direction.DOWN)) {
                        true -> fieldAt(Position(row + 1, startPos.col))
                        else -> field
                    }
                }

                return when (field.hasDirection(Direction.UP)) {
                    true -> fieldAt(Position(row + 1, startPos.col))
                    else -> field
                }
            }

            return fieldAt(Position(0, startPos.col))
        }

        return startField
    }
}

data class Position(val row: Int, val col: Int)

enum class BoardType {
    SANDBOX,
    CHOPSHOP,
    DEMO,
}
