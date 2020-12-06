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

        if (player.drawnCards.isEmpty()) {
            div(classes = "col") {
                button(classes = "btn btn-primary") {
                    attributes[ATTR_ACTION] = DrawCardsAction().toString()
                    +"Draw new cards"
                }
            }
        }

        player.drawnCards.forEach { card ->
            val selectedOrder = player.selectedCards.indexOf(card) + 1

            div(classes = "col") {
                div(classes = "card") {
                    if (!player.cardsConfirmed) {
                        attributes[ATTR_ACTION] = SelectCardAction(card.id.toString()).toString()
                    }

                    div(classes = "card-body") {
                        div(classes = "card-text") {
                            p {
                                +"${card.movement.name} (${card.priority})"
                            }

                            if (selectedOrder > 0) {
                                p {
                                    +"#$selectedOrder"
                                }
                            }
                        }
                    }
                }
            }
        }

        if (player.selectedCards.count() == 3) {
            div(classes = "col") {
                button(classes = "btn") {
                    attributes[ATTR_ACTION] = ConfirmCardsAction().toString()

                    if (player.cardsConfirmed) {
                        attributes["class"] += " btn-danger"
                        +"Revoke confirm"
                    } else {
                        attributes["class"] += " btn-primary"
                        +"Confirm cards"
                    }
                }
            }
        }
    }
}
