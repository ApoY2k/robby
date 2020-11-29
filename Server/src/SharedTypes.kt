package apoy2k.robby

// Keep in sync with main.js

const val ATTR_ACTION = "data-action"
const val ATTR_BIND = "data-bind"

enum class CommandLabel {
    JOIN_GAME,
    LEAVE_GAME,
    PLACE_ROBOT,
    DRAW_CARDS,
    SELECT_CARD,
    REMOVE_CARD,
    CONFIRM_CARDS,
    RESET_BOARD,
    REFRESH_VIEW,
}

enum class CommandField {
    LABEL,
    PLAYER_NAME,
    FIELD_ID,
    CARD_ID,
    MODEL,
    VIEW_NAME,
}

const val VIEW_GAME = "/views/game"
const val VIEW_BOARD = "/views/board"
const val VIEW_PLAYERS = "/views/players"
const val VIEW_JOIN_FORM = "/views/join_form"
const val VIEW_CARDS = "/views/cards"
