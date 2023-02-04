package content.region.kandarin.ardougne.quest.fightarena.dialogues.prisoners

import core.api.isEquipped
import core.game.dialogue.DialoguePlugin
import core.game.dialogue.FacialExpression
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import org.rs09.consts.Items
import org.rs09.consts.NPCs

@Initializable
class FightslaveDialogue(player: Player? = null) : DialoguePlugin(player) {

    // Fightslave - NPC inside prison cell.
    // Source: https://runescape.wiki/w/Slave_fighter?oldid=2632974
    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (isEquipped(player!!, Items.KHAZARD_HELMET_74) && isEquipped(player!!, Items.KHAZARD_ARMOUR_75)) {
            playerl(FacialExpression.FRIENDLY, "Do you know of a Justin or Jeremy in this arena?").also { stage = 0 }
        } else {
            playerl(FacialExpression.FRIENDLY, "Do you know of a Justin or Jeremy in this arena?").also { stage = 1 }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npcl(FacialExpression.FRIENDLY, "Please leave me alone.").also { stage = END_DIALOGUE }
        }

        when (stage) {
            1 -> npcl(FacialExpression.AFRAID, "I've not met anybody in here by that name.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): DialoguePlugin {
        return FightslaveDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.FIGHTSLAVE_262)
    }
}