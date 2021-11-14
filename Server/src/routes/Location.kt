package apoy2k.robby.routes

enum class Location(val path: String) {
    ROOT("/"),
    SET_USERNAME("/set-username"),
    GAME_ROOT("/game"),
    GAME_VIEW("${GAME_ROOT.path}/{id}");

    fun build(params: Map<String, String> = mapOf()) = params.entries
        .fold(this.path) { acc, param ->
            acc.replace("{${param.key}}", param.value)
        }
}