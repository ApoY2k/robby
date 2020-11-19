package apoy2k.robby.model

data class Board(
    internal val cells: Array<Array<Field>>
) {
    fun flip(id: String) {
        cells.forEach { it.find { it.id == id }?.flip() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return cells.contentDeepEquals(other.cells)
    }

    override fun hashCode(): Int {
        return cells.contentDeepHashCode()
    }
}
