package apoy2k.robby

@JvmInline
value class SensorResult(private val result: Int) {
    init {
        require(result >= -10 && result <= 10) { "SensorResult must be in rage [-10,10]" }
    }
}

fun main() {
    val result = SensorResult(20) // Throws
    println(result)
}
