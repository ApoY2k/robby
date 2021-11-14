package apoy2k.robby.model

data class Session(val id: String, val name: String = "") {
    override fun toString() = "Session($id, $name)"
}
