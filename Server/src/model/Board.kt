package apoy2k.robby.model

data class Board(internal val cells: List<List<Field>>) {
    /**
     * Flip a field to its other state
     */
    fun flip(id: String) {
        cells.forEach { it.find { it.id == id }?.flip() }
    }
}
