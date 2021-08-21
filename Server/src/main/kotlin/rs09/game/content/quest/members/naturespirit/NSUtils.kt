package rs09.game.content.quest.members.naturespirit

import api.Container
import api.ContentAPI
import core.game.node.Node
import core.game.node.`object`.Scenery
import core.game.node.`object`.SceneryBuilder
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.map.RegionManager.forId
import core.game.world.map.RegionManager.lock
import core.game.world.update.flag.context.Graphics
import core.tools.RandomFunction
import org.rs09.consts.Items
import rs09.game.node.entity.npc.other.MortMyreGhastNPC

object NSUtils {
    fun flagFungusPlaced(player: Player) {
        ContentAPI.setAttribute(player, "/save:ns:fungus_placed", true)
    }

    fun flagCardPlaced(player: Player){
        ContentAPI.setAttribute(player, "/save:ns:card_placed", true)
    }

    fun hasPlacedFungus(player: Player): Boolean {
        return ContentAPI.getAttribute(player, "ns:fungus_placed", false)
    }

    fun hasPlacedCard(player: Player): Boolean {
        return ContentAPI.getAttribute(player, "ns:card_placed", false)
    }

    fun onStone(player: Player): Boolean {
        return player.location.equals(3440, 3335, 0)
    }

    fun getGhastKC(player: Player): Int {
        return ContentAPI.getAttribute(player,"ns:ghasts_killed", 0) as Int
    }

    fun incrementGhastKC(player: Player){
        ContentAPI.setAttribute(player, "/save:ns:ghasts_killed", getGhastKC(player) + 1)
        val msg = when(getGhastKC(player)) {
            1 -> "That's one down, two more to go."
            2 -> "Two down, only one more to go."
            3 -> "That's it! I've killed all 3 Ghasts!"
            else -> ""
        }

        if(!msg.isEmpty()){
            ContentAPI.sendMessage(player, msg)
        }
    }

    fun activatePouch(player: Player, attacker: MortMyreGhastNPC): Boolean {
        var shouldAddEmptyPouch = false
        val pouchAmt = ContentAPI.amountInInventory(player, Items.DRUID_POUCH_2958)
        if(pouchAmt == 1) shouldAddEmptyPouch = true
        if(pouchAmt > 0 && ContentAPI.removeItem(player, Items.DRUID_POUCH_2958, Container.INVENTORY)){
            if(shouldAddEmptyPouch){
                ContentAPI.addItem(player, Items.DRUID_POUCH_2957)
            }
            ContentAPI.spawnProjectile(player, attacker, 268)
            ContentAPI.submitWorldPulse(object : Pulse(){
                var ticks = 0
                override fun pulse(): Boolean {
                    when(ticks++){
                        2 -> ContentAPI.visualize(attacker, -1, Graphics(269, 125))
                        3 -> attacker.transform(attacker.id + 1).also { attacker.attack(player); attacker.setAttribute("woke", ContentAPI.getWorldTicks()); return true }
                    }
                    return false
                }
            })
            return true
        }
        return false
    }

    fun cleanupAttributes(player: Player){
        player.removeAttribute("ns:fungus_placed")
        player.removeAttribute("ns:card_placed")
    }

    @JvmStatic
    fun castBloom(player: Player): Boolean{
        var success = false
        val region = forId(player.location.regionId)
        if (player.skills.prayerPoints < 1) {
            player.packetDispatch.sendMessage("You don't have enough prayer points to do this.")
            return false
        }
        handleVisuals(player)
        val locs = player.location.surroundingTiles
        for (o in locs) {
            val obj = RegionManager.getObject(o)
            if (obj != null) {
                if (obj.name.equals("Rotting log", ignoreCase = true) && player.skills.prayerPoints >= 1) {
                    if (player.location.withinDistance(obj.location, 2)) {
                        SceneryBuilder.replace(obj, obj.transform(3509))
                        success = true
                    }
                }
            }
        }
        return success
    }

    /**
     * Handles the draining of prayer points and physical graphics and
     * animation.
     */
    private fun handleVisuals(player: Player) {
        player.skills.decrementPrayerPoints(RandomFunction.random(1, 3).toDouble())
        val AROUND_YOU = player.location.surroundingTiles
        for (location in AROUND_YOU) {
            // The graphic is meant to play on a 3x3 radius around you, but not
            // including the tile you are on.
            player.packetDispatch.sendGlobalPositionGraphic(263, location)
        }
    }
}