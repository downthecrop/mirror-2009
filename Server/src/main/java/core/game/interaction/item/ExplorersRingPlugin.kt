package core.game.interaction.item

import api.ContentAPI
import core.Util
import core.cache.def.impl.ItemDefinition
import core.plugin.Initializable
import core.game.interaction.OptionHandler
import core.plugin.Plugin
import core.game.node.entity.skill.Skills
import core.game.interaction.item.ExplorersRingPlugin
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager.TeleportType
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import org.rs09.consts.Items
import rs09.game.interaction.InteractionListener

/**
 * Handles the explorers ring.
 *
 * @author Vexia
 */
class ExplorersRingPlugin : InteractionListener() {

    val RINGS = intArrayOf(Items.EXPLORERS_RING_1_13560, Items.EXPLORERS_RING_2_13561, Items.EXPLORERS_RING_3_13562)
    val CABBAGE_PORT = Location.create(3051, 3291, 0)


    override fun defineListeners() {
        on(RINGS, ITEM, "run-replenish"){player, node ->
            if (player.savedData.globalData.runReplenishDelay < System.currentTimeMillis()) {
                player.savedData.globalData.runReplenishCharges = 0
                player.savedData.globalData.runReplenishDelay = Util.nextMidnight(System.currentTimeMillis())
            }
            val charges = player.savedData.globalData.runReplenishCharges
            if (charges >= getRingLevel(node.id)) {
                ContentAPI.sendMessage(player,"You have used all the charges you can for one day.")
                return@on true
            }
            player.settings.updateRunEnergy(-50.0)
            player.savedData.globalData.runReplenishCharges = charges + 1
            ContentAPI.sendMessage(player,"You feel refreshed as the ring revitalises you and a charge is used up.")
            ContentAPI.visualize(player, 9988, 1733)
            return@on true
        }

        on(RINGS, ITEM, "low-alchemy"){player, _ ->
            if (!ContentAPI.hasLevelStat(player, Skills.MAGIC, 21)) {
                ContentAPI.sendMessage(player,"You need a Magic level of 21 in order to do that.")
                return@on true
            }
            if (player.savedData.globalData.lowAlchemyDelay < System.currentTimeMillis()) {
                player.savedData.globalData.lowAlchemyCharges = 0
                player.savedData.globalData.lowAlchemyDelay = Util.nextMidnight(System.currentTimeMillis())
            }
            if (player.savedData.globalData.lowAlchemyCharges <= 0 && player.savedData.globalData.lowAlchemyDelay > System.currentTimeMillis()) {
                ContentAPI.sendMessage(player,"You have used all the charges you can for one day.")
                return@on true
            }
            ContentAPI.sendMessage(player,"You grant yourself with 30 free low alchemy charges.") // todo this implementation is not correct, see https://www.youtube.com/watch?v=UbUIF2Kw_Dw
            player.savedData.globalData.lowAlchemyCharges = 30

            return@on true
        }

        on(RINGS, ITEM, "cabbage-port"){player, node ->
            teleport(player)
            return@on true
        }

        on(RINGS, ITEM, "operate", "rub"){player, node ->
            if(getRingLevel(node.id) < 3){
                ContentAPI.sendMessage(player, "This item can not be operated.")
                return@on true
            }

            teleport(player)
            return@on true
        }
    }

    fun teleport(player: Player){
        ContentAPI.teleport(player, CABBAGE_PORT, TeleportType.CABBAGE)
    }

    fun getRingLevel(id: Int): Int{
        return when(id){
            Items.EXPLORERS_RING_1_13560 -> 1
            Items.EXPLORERS_RING_2_13561 -> 2
            Items.EXPLORERS_RING_3_13562 -> 3
            else -> -1
        }
    }
}