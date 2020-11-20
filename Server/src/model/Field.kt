package apoy2k.robby.model

import java.util.*

enum class FieldType {
    BLANK,
    NOT_BLANK
}

data class Field(var type: FieldType = FieldType.BLANK) {
    val id = UUID.randomUUID().toString()

    /**
     * Flip this fields state
     */
    fun flip() {
        type = when (type) {
            FieldType.BLANK -> FieldType.NOT_BLANK
            FieldType.NOT_BLANK -> FieldType.BLANK
        }
    }
}
