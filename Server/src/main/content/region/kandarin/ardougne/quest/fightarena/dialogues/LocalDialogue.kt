package content.region.kandarin.ardougne.quest.fightarena.dialogues

import core.game.dialogue.DialoguePlugin
import core.game.dialogue.FacialExpression
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import org.rs09.consts.NPCs

@Initializable
class LocalDialogue(player: Player? = null) : DialoguePlugin(player) {

    // Local - NPC outside of fight arena.
    // Source: https://runescape.wiki/w/Local?oldid=572441
    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (player.questRepository.getStage("Fight Arena") >= 10) {
            playerl(FacialExpression.FRIENDLY, "Hello.").also { stage = 0 }
        } else {
            playerl(FacialExpression.FRIENDLY, "Hello.").also { stage = 7 }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npcl(FacialExpression.FRIENDLY, "I heard the Servil family are fighting soon. Should be very entertaining.").also { stage = 1 }
            1 -> npcl(FacialExpression.ASKING, "Hello stranger, are you new to these parts?").also { stage = 2 }
            2 -> playerl(FacialExpression.ASKING, "I suppose I am.").also { stage = 3 }
            3 -> npcl(FacialExpression.ASKING, "What's your business?").also { stage = 4 }
            4 -> playerl(FacialExpression.ASKING, "Just visiting friends in the cells.").also { stage = 5 }
            5 -> npcl(FacialExpression.LAUGH, "Visiting, that's funny.").also { stage = 6 }
            6 -> npcl(FacialExpression.FRIENDLY, "Only Khazard guards are allowed to see prisoners. Unless you know where to get some Khazard armour, you won't be visiting anyone.").also { stage = END_DIALOGUE }
        }
        when (stage) {
            7 -> npcl(FacialExpression.ASKING, "Hello stranger, are you new to these parts? You look lost.").also { stage = 8 }
            8 -> npcl(FacialExpression.HALF_THINKING, "I suppose you're here for the fight arena? There are some rich folk fighting tomorrow. Should be entertaining.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): DialoguePlugin {
        return LocalDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LOCAL_268)
    }
}