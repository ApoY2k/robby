package apoy2k.robby

// Keep in sync with main.js

const val ATTR_ACTION = "data-action"
const val ATTR_BIND = "data-bind"

enum class CommandLabel {
    SWITCH_FIELD,
    REFRESH_BOARD,
    RESET_BOARD,
}

const val VIEW_BOARD = "/views/board"
