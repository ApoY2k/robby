package apoy2k.robby.model

import java.util.*

data class Board(internal val cells: List<List<Field>>) {
    /**
     * Places a robot on a field
     */
    fun place(fieldId: UUID, robot: Robot) {
        cells.forEach { it.find { it.id == fieldId }?.robot = robot }
    }
}
