package apoy2k.robby

// Keep in sync with main.js

const val ATTR_ACTION = "data-action"
const val ATTR_BIND = "data-bind"

enum class CommandLabel {
    JOIN_GAME,
    LEAVE_GAME,
    PLACE_ROBOT,
    SELECT_CARD,
    REMOVE_CARD,
    CONFIRM_CARDS,
    REFRESH_CARDS,
    REFRESH_BOARD,
    REFRESH_PLAYERS,
    RESET_BOARD,
}

const val VIEW_BOARD = "/views/board"
