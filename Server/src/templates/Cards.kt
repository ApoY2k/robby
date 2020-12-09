package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.*
import kotlinx.html.HtmlBlockTag
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.p

fun HtmlBlockTag.renderCards(game: Game, session: Session?) {
    val player = game.playerFor(session) ?: return

    div(classes = "row mt-3") {
        attributes[ATTR_BIND] = View.CARDS.toString()

        player.drawnCards.forEach { card ->
            val isSelected = player.selectedCards.contains(card)

            div(classes = "col-2") {
                div(classes = "card") {
                    if (!player.cardsConfirmed) {
                        attributes[ATTR_ACTION] = SelectCardAction(card.id.toString()).toString()
                    }

                    var classes = ""
                    if (isSelected) {
                        classes += " text-success"
                    }

                    div(classes = "card-body $classes") {
                        div(classes = "card-text") {
                            p {
                                +"${card.movement.name} (${card.priority})"
                            }
                        }
                    }
                }
            }
        }

        if (player.drawnCards.isNotEmpty()) {
            div(classes = "col") {
                button(classes = "btn") {
                    if (player.cardsConfirmed) {
                        attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                        attributes["class"] += " btn-danger"
                        +"Revoke confirm"
                    } else {
                        if (player.selectedCards.count() == 3) {
                            attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                            attributes["class"] += " btn-primary"
                            +"Confirm cards"
                        } else {
                            attributes["class"] += " btn-secondary"
                            +"Select 3 cards"
                        }
                    }
                }
            }
        }
    }
}
