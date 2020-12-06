package apoy2k.robby.model

import java.util.*

data class Field(val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null
}
