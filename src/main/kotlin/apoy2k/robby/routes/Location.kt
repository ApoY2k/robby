package apoy2k.robby.routes

enum class Location(val path: String) {
    ROOT("/"),
    AUTH("/auth"),
    GAME_ROOT("/game"),
    GAME_VIEW("${GAME_ROOT.path}/{id}"),
    GAME_WEBSOCKET("${GAME_ROOT.path}/{id}/ws"),
    GAME_IMAGE("${GAME_ROOT.path}/{id}/image"),
    BOARDS_ROOT("/boards"),
    BOARDS_VIEW("${BOARDS_ROOT.path}/{id}"),
    BOARD_EDITOR("/editor");

    fun build(params: Map<String, String> = mapOf()) = params.entries
        .fold(this.path) { acc, param ->
            acc.replace("{${param.key}}", param.value)
        }
}
