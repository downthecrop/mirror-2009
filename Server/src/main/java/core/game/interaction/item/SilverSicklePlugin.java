package core.game.interaction.item;

import core.cache.def.impl.ItemDefinition;
import core.game.interaction.OptionHandler;
import core.game.node.Node;
import core.game.node.entity.player.Player;
import core.game.node.object.Scenery;
import core.game.node.object.SceneryBuilder;
import core.game.world.map.Location;
import core.game.world.map.Region;
import core.game.world.map.RegionManager;
import core.plugin.Initializable;
import core.plugin.Plugin;
import core.tools.RandomFunction;
import rs09.game.content.quest.members.naturespirit.NSUtils;

/**
 * Handles the Silver Sickle (b) to collect Mort Myre Fungus.
 * @author Splinter
 */
@Initializable
public final class SilverSicklePlugin extends OptionHandler {

	@Override
	public Plugin<Object> newInstance(Object arg) throws Throwable {
		ItemDefinition.forId(2963).getHandlers().put("option:operate", this);
		ItemDefinition.forId(2963).getHandlers().put("option:cast bloom", this);
		return this;
	}

	@Override
	public boolean handle(Player player, Node node, String option) {
		switch (option) {
		case "operate":
		case "cast bloom":
			player.getPacketDispatch().sendAnimation(9021);
			NSUtils.castBloom(player);
			return true;
		}
		return false;
	}

}
