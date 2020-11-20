package apoy2k.robby.engine

enum class CommandLabel {
    SWITCHFIELD
}

class Command(val label: CommandLabel, vararg val parameters: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (label != other.label) return false
        if (!parameters.contentEquals(other.parameters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + parameters.contentHashCode()
        return result
    }

    override fun toString(): String {
        return listOf(label, parameters.joinToString(";")).joinToString(":")
    }
}

fun parse(command: String) {

}
