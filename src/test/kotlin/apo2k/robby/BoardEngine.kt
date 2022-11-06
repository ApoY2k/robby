package apoy2k.robby.kotlin.apo2k.robby

import apoy2k.robby.model.Field

/**
 * Generate IDs for all fields, only used for unit testing
 */
fun List<List<Field>>.assignIds() = this.flatten().forEachIndexed { idx, field ->
    field.id = idx
}
