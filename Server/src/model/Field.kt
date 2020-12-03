package apoy2k.robby.model

import java.util.*

enum class FieldType {
    BLANK,
    BLOCKED,
}

data class Field(var type: FieldType = FieldType.BLANK,
                 val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null
}
