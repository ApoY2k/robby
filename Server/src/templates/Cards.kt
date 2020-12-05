package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.VIEW_CARDS
import apoy2k.robby.engine.Game
import apoy2k.robby.model.ConfirmCardsCommand
import apoy2k.robby.model.DrawCardsCommand
import apoy2k.robby.model.SelectCardCommand
import apoy2k.robby.model.Session
import kotlinx.html.HtmlBlockTag
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.p


fun HtmlBlockTag.renderCards(game: Game, session: Session?) {
    val player = game.playerFor(session) ?: return

    div(classes = "row mt-3") {
        attributes[ATTR_BIND] = VIEW_CARDS

        if (player.hasDrawnCards()) {
            div(classes = "col") {
                button(classes = "btn btn-primary") {
                    attributes[ATTR_ACTION] = DrawCardsCommand().toString()
                    +"Draw new cards"
                }
            }
        }

        player.getDrawnCards().forEach { card ->
            val selectedOrder = player.getDrawnCards().indexOf(card) + 1

            div(classes = "col") {
                div(classes = "card") {
                    if (!player.hasCardsConfirmed()) {
                        attributes[ATTR_ACTION] = SelectCardCommand(card.id.toString()).toString()
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

        if (player.getSelectedCards().count() == 3) {
            div(classes = "col") {
                button(classes = "btn") {
                    attributes[ATTR_ACTION] = ConfirmCardsCommand().toString()

                    if (player.hasCardsConfirmed()) {
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
