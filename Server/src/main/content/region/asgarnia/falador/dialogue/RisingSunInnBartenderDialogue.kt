package content.region.asgarnia.falador.dialogue

import core.api.*
import core.game.dialogue.DialoguePlugin
import core.game.dialogue.FacialExpression
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import org.rs09.consts.Items
import org.rs09.consts.NPCs
import core.game.dialogue.Topic
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE

/**
 * Handles dialogue for Kaylee and Emily
 * of the Rising Sun Inn
 *
 * @author vddCore
 */
@Initializable
class RisingSunInnBartenderDialogue(player: Player? = null) : core.game.dialogue.DialoguePlugin(player) {
    override fun newInstance(player: Player): core.game.dialogue.DialoguePlugin
        = RisingSunInnBartenderDialogue(player)

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        npcl(
            core.game.dialogue.FacialExpression.HAPPY,
            "Hi! What can I get you?"
        )

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            START_DIALOGUE -> if (hasAnyBeerGlasses()) {
                showTopics(
                    Topic(core.game.dialogue.FacialExpression.ASKING, "What ales are you serving?", 10),
                    Topic(core.game.dialogue.FacialExpression.HAPPY, "I've got some beer glasses...", 20)
                )
            } else {
                playerl(core.game.dialogue.FacialExpression.ASKING, "What ales are you serving?")
                .also { stage = 10 }
            }

            10 -> npcl(
                core.game.dialogue.FacialExpression.FRIENDLY,
                "Well, we've got Asgarnian Ale, Wizard's Mind Bomb and Dwarven Stout. "
                + "Each for only 3 coins."
            ).also { stage++ }

            11 -> showTopics(
                Topic(core.game.dialogue.FacialExpression.HAPPY, "One Asgarnian Ale, please.", 12),
                Topic(core.game.dialogue.FacialExpression.HAPPY, "I'll try the Mind Bomb.", 13),
                Topic(core.game.dialogue.FacialExpression.ASKING, "Can I have a Dwarven Stout?", 14),
                Topic(core.game.dialogue.FacialExpression.NEUTRAL, "I don't feel like any of those.", END_DIALOGUE)
            )

            12 -> if (ensureHasMoney()) {
                purchaseBrew(Items.ASGARNIAN_ALE_1905)
            }

            13 -> if (ensureHasMoney()) {
                purchaseBrew(Items.WIZARDS_MIND_BOMB_1907)
            }

            14 -> if (ensureHasMoney()) {
                purchaseBrew(Items.DWARVEN_STOUT_1913)
            }

            20 -> {
                npcl(
                    core.game.dialogue.FacialExpression.HALF_GUILTY,
                    "Oh, we will buy those from you if you're interested. We offer 2 coins for each glass."
                ).also { stage ++ }
            }

            21 -> showTopics(
                Topic(core.game.dialogue.FacialExpression.HAPPY, "Yes, please!", 22),
                Topic(core.game.dialogue.FacialExpression.NEUTRAL, "No thanks, I like my empty beer glasses.", END_DIALOGUE)
            )

            22 -> {
                trySellAllBeerGlasses()

                npcl(
                    core.game.dialogue.FacialExpression.FRIENDLY,
                    "There you go!"
                ).also { stage = END_DIALOGUE }
            }

            30 -> {
                playerl(core.game.dialogue.FacialExpression.FRIENDLY, "Thanks, ${npc.name}.")
                .also { stage = END_DIALOGUE }
            }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.EMILY_736,
        NPCs.KAYLEE_3217
    )

    private fun ensureHasMoney(): Boolean {
        if (!inInventory(player, Items.COINS_995, 3)) {
            npcl(core.game.dialogue.FacialExpression.ANGRY, "No freeloaders!")
            .also {
                sendMessage(player, "You don't have enough money to buy that.")
                stage = END_DIALOGUE
            }
            return false
        }

        return true
    }

    private fun purchaseBrew(brewId: Int) {
        if (removeItem(player, Item(Items.COINS_995, 3))) {
            addItemOrDrop(player, brewId)

            sendItemDialogue(
                player,
                Item(brewId),
                "You hand 3 coins over to ${npc.name}. She gives you ${when (brewId) {
                    Items.WIZARDS_MIND_BOMB_1907 -> "a Wizard's Mind Bomb"
                    Items.DWARVEN_STOUT_1913 -> "a Dwarven Stout"    
                    Items.ASGARNIAN_ALE_1905 -> "an Asgarnian Ale"
                    else -> "a broken fucking dialogue"
                }}."
            ).also { stage = 30 }
        }
    }

    private fun hasAnyBeerGlasses()
        = inInventory(player, Items.BEER_GLASS_1919)
          || inInventory(player, Items.BEER_GLASS_1920)

    private fun trySellAllBeerGlasses() {
        val regularGlassAmount = amountInInventory(player, Items.BEER_GLASS_1919)
        val notedGlassAmount = amountInInventory(player, Items.BEER_GLASS_1920)

        if (removeItem(player, Item(Items.BEER_GLASS_1919, regularGlassAmount))) {
            addItem(player, Items.COINS_995, regularGlassAmount * 2)
        }

        if (removeItem(player, Item(Items.BEER_GLASS_1920, notedGlassAmount))) {
            addItem(player, Items.COINS_995, notedGlassAmount * 2)
        }
    }
}