package apoy2k.robby.model

data class Session(
    val id: String,
    val name: String = ""
) {
    val isLoggedIn = name.isNotBlank()
}
