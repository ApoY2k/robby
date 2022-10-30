package apoy2k.robby.model

data class Session(
    val id: String,
    val isLoggedIn: Boolean = false,
    val name: String = "",
)
