package apoy2k.robby

enum class Environment {
    DATABASE_PATH,
}

fun envStr(env: Environment, fallback: String = "") = System.getenv(env.name) ?: fallback
fun envInt(env: Environment, fallback: Int) = envStr(env, fallback.toString()).toInt()
