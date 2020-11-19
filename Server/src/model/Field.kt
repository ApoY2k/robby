package apoy2k.robby.model

import java.util.*

data class Field(internal var type: FieldType = FieldType.BLANK) {
    val id = UUID.randomUUID().toString()

    fun flip() {
        type = when (type) {
            FieldType.BLANK -> FieldType.NOT_BLANK
            FieldType.NOT_BLANK -> FieldType.BLANK
        }
    }
}
