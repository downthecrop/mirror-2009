package rs09.game.ai.general.scriptrepository

import core.game.ge.OfferState
import core.game.interaction.DestinationFlag
import core.game.interaction.MovementPulse
import core.game.node.entity.Entity
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.combat.InteractionType
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.entity.state.EntityState
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.impl.WildernessZone
import org.rs09.consts.Items
import rs09.game.ai.AIRepository
import rs09.game.ai.pvmbots.CombatBotAssembler
import rs09.game.node.entity.combat.CombatSwingHandler
import rs09.game.node.entity.combat.handlers.MagicSwingHandler
import rs09.game.node.entity.combat.handlers.MeleeSwingHandler
import rs09.game.node.entity.combat.handlers.RangeSwingHandler
import rs09.game.world.GameWorld
import kotlin.random.Random

/**
 * A bot script for killing green dragons in the wilderness.. Capable of banking, selling on ge, buying on ge, eating and more.
 * @param style The combat style the bot is going to use.
 * @param area (optional) What area the bot tries to kill dragons in.
 * @author Ceikry
 */
class GreenDragonKiller(val style: CombatStyle, area: ZoneBorders? = null) : Script() {
    var state = State.KILLING
    var handler: CombatSwingHandler? = null
    var lootDelay = 0
    var offerMade = false

    var food = Items.SWORDFISH_373


    var myBorders: ZoneBorders? = null
    val type = when(style){
        CombatStyle.MELEE -> CombatBotAssembler.Type.MELEE
        CombatStyle.MAGIC -> CombatBotAssembler.Type.MAGE
        CombatStyle.RANGE -> CombatBotAssembler.Type.RANGE
    }

    val westDragons = ZoneBorders(2971,3606,2991,3628)
    val wildernessLine = ZoneBorders(3078,3523,3096,3523)
    val edgevilleLine = ZoneBorders(3078,3520,3096,3520)
    val bankZone = ZoneBorders(3092,3489,3094,3493)
    override fun tick() {
        if(!bot.isActive){
            running = false
            return
        }
        if(bot.inventory.getAmount(food) < 3 && state == State.KILLING)
            state = State.TO_BANK
        scriptAPI.eat(food)
        when(state){



            State.KILLING -> {
                bot.properties.combatPulse.temporaryHandler = handler
                scriptAPI.attackNpcInRadius(bot,"Green dragon",20)
                state = State.LOOT_DELAYER
            }

            State.LOOT_DELAYER -> {
                if(lootDelay < 3)
                    lootDelay++
                else
                    state = State.LOOTING
            }


            State.RUNNING -> {
                val players = RegionManager.getLocalPlayers(bot.location)
                if(players.isEmpty()){
                    state = State.TO_DRAGONS
                } else {
                    if(bot.skullManager.level < 21 && bot.stateManager.get(EntityState.TELEBLOCK) != null){
                        scriptAPI.teleportToGE()
                        state = State.REFRESHING
                        return
                    }
                    scriptAPI.walkTo(WildernessZone.getInstance().borders.random().randomLoc)
                }
            }

            State.LOOTING -> {
                lootDelay = 0
                val items = AIRepository.groundItems[bot]
                if(items.isNullOrEmpty()) {state = State.KILLING; return}
                if(bot.inventory.isFull) {
                    if(bot.inventory.containsItem(Item(food))){
                        scriptAPI.forceEat(food)
                    } else {
                        state = State.TO_BANK
                    }
                    return
                }
                items.toTypedArray().forEach {it: Item -> scriptAPI.takeNearestGroundItem(it.id)}
            }

            State.TO_BANK -> {
                if(!wildernessLine.insideBorder(bot) && bot.location.y > 3521)
                    scriptAPI.walkTo(wildernessLine.randomLoc)
                if(wildernessLine.insideBorder(bot)){
                    val ditch = scriptAPI.getNearestNode("Wilderness Ditch",true)
                    ditch ?: return
                    ditch.interaction.handle(bot,ditch.interaction[0])
                }
                if(!bankZone.insideBorder(bot))
                    scriptAPI.walkTo(bankZone.randomLoc)
                if(bankZone.insideBorder(bot)){
                    val bank = scriptAPI.getNearestNode("Bank Booth",true)
                    bank ?: return
                    bot.pulseManager.run(object: MovementPulse(bot,bank, DestinationFlag.OBJECT){
                        override fun pulse(): Boolean {
                            bot.faceLocation(bank.location)
                            state = State.BANKING
                            return true
                        }
                    })
                }
            }

            State.BANKING -> {
                bot.pulseManager.run(object: Pulse(25){
                    override fun pulse(): Boolean {
                        for(item in bot.inventory.toArray()){
                            item ?: continue
                            if(item.name.toLowerCase().contains("lobster") || item.name.toLowerCase().contains("swordfish") || item.name.toLowerCase().contains("shark")) continue
                            if(item.id == 995) continue
                            bot.bank.add(item)
                        }
                        bot.inventory.clear()
                        state = if(bot.bank.getAmount(food) < 10)
                            State.TO_GE
                        else
                            State.TO_DRAGONS
                        for(item in inventory)
                            bot.inventory.add(item)
                        scriptAPI.withdraw(food,10)
                        bot.fullRestore()
                        return true
                    }
                })
            }

            State.TO_DRAGONS -> {
                offerMade = false
                if(bot.location.x >= 3143){
                    if(bot.location != Location.create(3144, 3514, 0))
                        scriptAPI.walkTo(Location.create(3144, 3514, 0))
                    else {
                        val shortcut = scriptAPI.getNearestNode("Underwall Tunnel",true)
                        shortcut ?: return
                        shortcut.interaction.handle(bot,shortcut.interaction[0])
                    }
                } else {
                    if (!edgevilleLine.insideBorder(bot) && bot.location.y < 3520) {
                        scriptAPI.walkTo(edgevilleLine.randomLoc)
                        return
                    }
                    if (edgevilleLine.insideBorder(bot)) {
                        val ditch = scriptAPI.getNearestNode("Wilderness Ditch", true)
                        ditch ?: return
                        ditch.interaction.handle(bot, ditch.interaction[0]).also { return }
                    }
                    if (bot.location.y > 3520 && !myBorders!!.insideBorder(bot))
                        scriptAPI.walkTo(myBorders!!.randomLoc).also { return }
                    if (myBorders!!.insideBorder(bot))
                        state = State.KILLING
                }
            }

            State.TO_GE -> {
                if(bot.location.x < 3143) {
                    if(bot.location == Location.create(3136, 3517, 0)){
                        val shortcut = scriptAPI.getNearestNode("Underwall Tunnel",true)
                        shortcut ?: return
                        shortcut.interaction.handle(bot,shortcut.interaction[0])
                    } else {
                        scriptAPI.walkTo(Location.create(3136, 3517, 0))
                    }
                    return
                }
                if(bot.location != Location.create(3165, 3487, 0)) {
                    scriptAPI.walkTo(Location.create(3165, 3487, 0))
                } else {
                    state = State.SELL_GE
                }
            }

            State.SELL_GE -> {
                scriptAPI.sellAllOnGe()
                GameWorld.Pulser.submit(object : Pulse() {
                    override fun pulse(): Boolean {
                        bot.bank.add(Item(Items.SWORDFISH_373, 100))
                        return true
                    }
                })
                state = State.BANKING
            }

            State.REFRESHING -> {
                running = false
                return
            }
        }
    }

    override fun newInstance(): Script {
        val script = GreenDragonKiller(style)
        val tier = CombatBotAssembler.Tier.HIGH
        if(type == CombatBotAssembler.Type.RANGE)
            script.bot = CombatBotAssembler().assembleRangeDragonBot(tier,bot.startLocation)
        else
            script.bot = CombatBotAssembler().assembleMeleeDragonBot(tier, bot.startLocation)
        return script
    }

    enum class State {
        KILLING,
        RUNNING,
        LOOTING,
        LOOT_DELAYER,
        BANKING,
        TO_BANK,
        TO_DRAGONS,
        TO_GE,
        SELL_GE,
        REFRESHING,

    }

    init {
        handler = when(style){
            CombatStyle.MELEE -> MeleeSwinger(this)
            CombatStyle.MAGIC -> MageSwinger(this)
            CombatStyle.RANGE -> RangeSwinger(this)
        }
        equipment.add(Item(Items.ANTI_DRAGON_SHIELD_1540))
        myBorders = westDragons
        skills[Skills.AGILITY] = 99
        bankZone.addException(ZoneBorders(3094, 3492,3094, 3492))
        bankZone.addException(ZoneBorders(3094, 3490,3094, 3490))
    }

    internal class MeleeSwinger(val script: GreenDragonKiller) : MeleeSwingHandler() {
        override fun canSwing(entity: Entity, victim: Entity): InteractionType? {
            if(script.state == State.TO_BANK) {script.bot.pulseManager.current.stop()}
            if(victim is Player) {script.state = State.RUNNING; script.bot.pulseManager.current.stop()}
            return super.canSwing(entity, victim)
        }
    }

    internal class MageSwinger(val script: GreenDragonKiller) : MagicSwingHandler() {
        override fun canSwing(entity: Entity, victim: Entity): InteractionType? {
            if(victim is Player) {script.state = State.RUNNING; script.bot.pulseManager.current.stop()}
            return super.canSwing(entity, victim)
        }
    }

    internal class RangeSwinger(val script: GreenDragonKiller) : RangeSwingHandler() {
        override fun canSwing(entity: Entity, victim: Entity): InteractionType? {
            if(victim is Player) {script.state = State.RUNNING; script.bot.pulseManager.current.stop()}
            return super.canSwing(entity, victim)
        }
    }
}