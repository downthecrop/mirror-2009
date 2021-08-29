package rs09.game.system.command.rottenpotato

import core.game.node.Node
import core.game.node.scenery.Scenery
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item

/**
 * Rotten Potato use-with handling
 * @author Ceikry
 */
object RottenPotatoUseWithHandler{
    @JvmStatic
    fun handle(node: Node, player: Player){
        if(node is Scenery){
            val go = node.asScenery()
        }

        if(node is NPC){
            val npc = node.asNpc()
            player.dialogueInterpreter.open(RPUseWithNPCDialogue().ID,npc)
        }

        if(node is Item){
            val item = node.asItem()
        }

        if(node is Player){
            val p = node.asPlayer()
            player.dialogueInterpreter.open(RPUseWithPlayerDialogue().ID,p)
        }
    }
}