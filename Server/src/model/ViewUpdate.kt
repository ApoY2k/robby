package apoy2k.robby.model

enum class View(private val route: String) {
    GAME("/views/game"),
    BOARD("/views/board"),
    PLAYERS("/views/players"),
    JOIN_FORM( "/views/join_form"),
    CARDS("/views/cards");

    override fun toString(): String {
        return route
    }
}

data class ViewUpdate(val view: View, val recipients: Set<Session>) {
    constructor(view: View) : this(view, emptySet())
    constructor(view: View, player: Player) : this(view, setOf(player.session))

    override fun toString(): String {
        return view.toString()
    }
}
