package rs09.game.interaction.npc

import core.game.content.quest.miniquest.barcrawl.BarcrawlManager
import core.game.content.quest.miniquest.barcrawl.BarcrawlType
import core.game.node.entity.npc.NPC
import rs09.game.content.activity.gnomecooking.*
import rs09.game.content.ame.RandomEventManager
import rs09.game.content.ame.RandomEvents
import rs09.game.interaction.InteractionListener

/**
 * Handles the NPC talk-to option.
 * @author Ceikry
 */
class NPCTalkListener : InteractionListener {

    val barCrawlNPCs = intArrayOf(733,848,735,739,737,738,731,568,3217,736,734)

    override fun defineListeners() {
        on(barCrawlNPCs, NPC, "talk-to", "talk"){player, node ->
            val type = BarcrawlType.forId(node.id)
            val instance = BarcrawlManager.getInstance(player)
            if (instance.isFinished || !instance.isStarted || instance.isCompleted(type!!.ordinal)) {
                player.dialogueInterpreter.open(node.id, node)
            } else {
                player.dialogueInterpreter.open("barcrawl dialogue", node.id, type)
            }
            return@on true
        }

        on(NPC,"talk-to","talk","talk to"){player,node ->
            val npc = node.asNpc()
            if(RandomEvents.randomIDs.contains(node.id)){
                if(RandomEventManager.getInstance(player)!!.event == null || RandomEventManager.getInstance(player)!!.event!!.id != node.id){
                    player.sendMessage("They aren't interested in talking to you.")
                } else {
                    RandomEventManager.getInstance(player)!!.event!!.talkTo(node.asNpc())
                }
                return@on true
            }
            if (!npc.getAttribute("facing_booth", false)) {
                npc.faceLocation(player.location)
            }
            //I'm sorry for this but it was honestly the best way to do this
            if (player.getAttribute(GC_BASE_ATTRIBUTE + ":" + GC_JOB_ORDINAL, -1) != -1) {
                val job = GnomeCookingJob.values()[player.getAttribute(GC_BASE_ATTRIBUTE + ":" + GC_JOB_ORDINAL, -1)]
                if (node.getId() == job.npc_id && !player.getAttribute(GC_BASE_ATTRIBUTE + ":" + GC_JOB_COMPLETE, false)) {
                    player.dialogueInterpreter.open(GCCompletionDialogue(job))
                    return@on true
                }
            }
            return@on player.dialogueInterpreter.open(npc.id, npc)
        }
    }
}