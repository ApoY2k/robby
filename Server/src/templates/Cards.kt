package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderCards(game: Game, session: Session?) {
    val player = game.playerFor(session) ?: return
    val cardConfirmed = player.cardsConfirmed

    div(classes = "my-3") {
        attributes[ATTR_BIND] = View.CARDS.toString()

        div(classes = "row row-cols-5") {
            player.drawnCards.forEach { card ->
                val isSelected = player.selectedCards.contains(card)
                renderCard(card, cardConfirmed, isSelected)
            }
        }

        val selectedCards = player.selectedCards.count()

        div(classes = "row mt-3") {
            if (player.drawnCards.isNotEmpty()) {
                div(classes = "col") {
                    if (player.cardsConfirmed) {
                        button(classes = "btn btn-danger") {
                            attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                            +"Revoke confirmation of cards"
                        }
                    } else {
                        if (selectedCards == 3) {
                            button(classes = "btn btn-primary") {
                                attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                                +"Confirm selected cards"
                            }
                        } else {
                            p(classes = "alert alert-info") {
                                +"Select ${3 - selectedCards} more movements"
                            }
                        }
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderCard(card: MovementCard, playerConfirmedCards: Boolean, selected: Boolean? = false) {
    val title = when (card.movement) {
        Movement.STRAIGHT -> "Straight 1"
        Movement.STRAIGHT_2 -> "Straight 2"
        Movement.STRAIGHT_3 -> "Straight 3"
        Movement.TURN_LEFT -> "Turn Left"
        Movement.TURN_RIGHT -> "Turn Right"
        Movement.TURN_180 -> "Turn 180°"
        Movement.BACKWARDS -> "Backwards 1"
        Movement.BACKWARDS_2 -> "Backwards 2"
        Movement.BACKWARDS_3 -> "Backwards 3"
        Movement.HOLD -> "Hold"
    }

    val description = when (card.movement) {
        Movement.STRAIGHT -> "Move 1 field straight"
        Movement.STRAIGHT_2 -> "Move 2 fields straight"
        Movement.STRAIGHT_3 -> "Move 3 fields straight"
        Movement.TURN_LEFT -> "Turn Left"
        Movement.TURN_RIGHT -> "Turn Right"
        Movement.TURN_180 -> "Turn 180°"
        Movement.BACKWARDS -> "Move 1 field backwards"
        Movement.BACKWARDS_2 -> "Move 2 fields backwards"
        Movement.BACKWARDS_3 -> "Move 3 fields backwards"
        Movement.HOLD -> "Move 0 fields"
    }

    div(classes = "col") {
        div(classes = "card movement-card h-100") {
            if (selected == true) {
                attributes["class"] += " selected"
            }

            if (!playerConfirmedCards) {
                attributes[ATTR_ACTION] = SelectCardAction(card.id.toString()).toString()
            }

            p(classes = "card-header") {
                +title

                span(classes = "card-priority") {
                    +card.priority.toString()
                }
            }

            div(classes = "card-body") {
                div(classes = "card-text") {
                    +description
                }
            }
        }
    }
}
