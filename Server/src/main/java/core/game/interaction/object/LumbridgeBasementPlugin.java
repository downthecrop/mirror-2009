package core.game.interaction.object;

import core.cache.def.impl.ObjectDefinition;
import core.game.content.dialogue.DialoguePlugin;
import core.game.interaction.NodeUsageEvent;
import core.game.interaction.OptionHandler;
import core.game.interaction.UseWithHandler;
import core.game.node.Node;
import core.game.node.entity.impl.ForceMovement;
import core.game.node.entity.npc.AbstractNPC;
import core.game.node.entity.npc.NPC;
import core.game.node.entity.player.Player;
import core.game.node.object.Scenery;
import core.game.node.object.SceneryBuilder;
import core.game.system.task.Pulse;
import rs09.game.world.GameWorld;
import core.game.world.map.Direction;
import core.game.world.map.Location;
import core.game.world.map.path.Pathfinder;
import core.game.world.update.flag.context.Animation;
import core.plugin.Plugin;
import rs09.plugin.PluginManager;
import core.plugin.Initializable;
import core.tools.RandomFunction;

/**
 * Handles the lumbridge basement.
 * @author Vexia
 *
 */
@Initializable
public class LumbridgeBasementPlugin extends OptionHandler {

	/**
	 * The animation to use for the shortcut.
	 */
	private static final Animation ANIMATION = new Animation(2240);

	/**
	 * The jumping animation for stepping stones.
	 */
	private static final Animation JUMP_ANIMATION = new Animation(741);

	@Override
	public Plugin<Object> newInstance(Object arg) throws Throwable {
		ObjectDefinition.forId(6899).getHandlers().put("option:squeeze-through", this);
		ObjectDefinition.forId(6898).getHandlers().put("option:squeeze-through", this);
		ObjectDefinition.forId(6905).getHandlers().put("option:squeeze-through", this);
		ObjectDefinition.forId(6912).getHandlers().put("option:squeeze-through", this);
		ObjectDefinition.forId(5949).getHandlers().put("option:jump-across", this);
		ObjectDefinition.forId(6658).getHandlers().put("option:enter", this);
		ObjectDefinition.forId(32944).getHandlers().put("option:enter", this);
		ObjectDefinition.forId(40261).getHandlers().put("option:climb-up", this);
		ObjectDefinition.forId(40262).getHandlers().put("option:climb-up", this);
		ObjectDefinition.forId(40849).getHandlers().put("option:jump-down", this);
		ObjectDefinition.forId(40260).getHandlers().put("option:climb-through", this);
		ObjectDefinition.forId(41077).getHandlers().put("option:crawl-through", this);
		PluginManager.definePlugins(new LightCreatureNPC(), new LightCreatureHandler(), new FishMongerDialogue());
		SceneryBuilder.add(new Scenery(40260, Location.create(2526, 5828, 2), 2));
		return this;
	}

	@Override
	public boolean handle(final Player player, Node node, String option) {
		switch (option) {
		case "squeeze-through":
			Direction dir = null;
			Location to = null;
			switch (node.getId()) {
			case 6912:
				to = node.getLocation().getY() == 9603 ? Location.create(3224, 9601, 0) : Location.create(3224, 9603, 0);
				dir = node.getLocation().getY() == 9603 ? Direction.SOUTH : Direction.NORTH;
				break;
			default:
				to = player.getLocation().getX() >= 3221 ? Location.create(3219, 9618, 0) : Location.create(3222, 9618, 0);
				dir = player.getLocation().getX() >= 3221 ? Direction.WEST : Direction.EAST;
				break;
			}
			player.sendMessage("You squeeze through the hole.");
			ForceMovement.run(player, player.getLocation(), to, ANIMATION, ANIMATION, dir, 20).setEndAnimation(Animation.RESET);
			return true;
		case "jump-across":
			Location f = null;
			Location s = null;
			switch (node.getId()) {
			case 5949:
				f = Location.create(3221, 9554, 0);
				s = player.getLocation().getY() >= 9556 ?  Location.create(3221, 9552, 0) : Location.create(3221, 9556, 0);
				break;
			}
			final Location first = f;
			final Location second = s;
			player.lock();
			GameWorld.getPulser().submit(new Pulse(2, player) {
				int counter = 1;

				@Override
				public boolean pulse() {
					if (counter == 3) {
						player.unlock();
						ForceMovement.run(player, player.getLocation(), second, JUMP_ANIMATION, 20);
						player.sendMessage("You leap across with a mighty leap!");
						return true;
					} else if (counter == 1){
						ForceMovement.run(player, player.getLocation(), first, JUMP_ANIMATION, 20);
					}
					counter++;
					return false;
				}
			});
			break;
		case "enter":
			switch (node.getId()) {
			case 32944:
				//Location.create(3353,3951,0)
				player.teleport(Location.create(3219, 9532, 2));
				break;
			case 6658:
				//Location.create(3226, 9542, 0)
				player.teleport(Location.create(3353, 3950, 0));
				break;
			}
			break;
		case "climb-up":
			switch (node.getId()) {
			case 40261:
				player.teleport(player.getLocation().transform(0, -1, 1));
				break;
			case 40262:
				player.teleport(player.getLocation().transform(0, -1, 1));
				break;
			}
			break;
		case "jump-down":
			switch (node.getId()) {
			case 40849:
				player.teleport(player.getLocation().transform(0, 1, -1));
				break;
			}
			break;
		case "climb-through":
			switch (node.getId()) {
			case 40260:
				player.teleport(Location.create(2525, 5810, 0));
				break;
			}
			break;
		case "crawl-through":
			switch (node.getId()) {
			case 41077:
				player.teleport(Location.create(2527, 5830, 2));
				break;
			}
			break;
		}
		return true;
	}

	@Override
	public Location getDestination(Node node, Node n) {
		if (n instanceof Scenery) {
			if (n.getId() == 5949) {
				return node.getLocation().getY() >= 9555 ? Location.create(3221, 9556, 0) : Location.create(3221, 9552, 0);
			} else if (n.getId() == 40262) {
				return n.getLocation().transform(0, 1, 0);
			} else if (n.getId() == 40261) {
				return n.getLocation().transform(0, 1, 0);
			}
		}
		return null;
	}

	/**
	 * Handles the sapphire lantern on a light creature.
	 * @author Vexia
	 *
	 */
	public class LightCreatureHandler extends UseWithHandler {

		/**
		 * Constructs the {@code LightCreatureHandler}
		 */
		public LightCreatureHandler() {
			super( 4700, 4701, 4702);
		}

		@Override
		public Plugin<Object> newInstance(Object arg) throws Throwable {
			addHandler(2021, NPC_TYPE, this);
			return this;
		}

		@Override
		public boolean handle(NodeUsageEvent event) {
			final Player player = event.getPlayer();
			player.lock(2);
			player.teleport(Location.create(2538, 5881, 0));
			return true;
		}
		
		@Override
		public Location getDestination(Player player, Node with) {
			if (player.getLocation().withinDistance(with.getLocation())) {
				return player.getLocation();
			}
			return null;
		}

	}


	/**
	 * Handles the light creature npc.
	 * @author Vexia
	 *
	 */
	public class LightCreatureNPC extends AbstractNPC {

		/**
		 * Constructs the {@code LightCreatureNPC}
		 */
		public LightCreatureNPC() {
			super(0, null);
			this.setWalks(true);
			this.setWalkRadius(10);
		}

		/**
		 * Constructs the {@code LightCreatureNPC}
		 */
		public LightCreatureNPC(int id, Location location) {
			super(id, location);
		}

		@Override
		public AbstractNPC construct(int id, Location location, Object... objects) {
			return new LightCreatureNPC(id, location);
		}

		@Override
		public void handleTickActions() {
			if (!getLocks().isMovementLocked()) {
				if (isWalks() && !getPulseManager().hasPulseRunning() && nextWalk < GameWorld.getTicks()) {
					setNextWalk();
					Location l = getLocation().transform(-5 + RandomFunction.random(getWalkRadius()), -5 + RandomFunction.random(getWalkRadius()), 0);
					if (canMove(l)) {
						Pathfinder.find(this, l, true, Pathfinder.PROJECTILE).walk(this);
					}
				}
			}
		}

		@Override
		public int[] getIds() {
			return new int[] {2021};
		}

	}
	
	/**
	 * Handles the fish monger dialogue.
	 * @author Vexia
	 *
	 */
	public class FishMongerDialogue extends DialoguePlugin {

		/**
		 * Constructs the {@code FishMongerDialogue}
		 */
		public FishMongerDialogue() {
			/*
			 * empty.
			 */
		}
		
		/**
		 * Constructs the {@code FishMongerDialogue}
		 */
		public FishMongerDialogue(Player player) {
			super(player);
		}
		
		@Override
		public DialoguePlugin newInstance(Player player) {
			return new FishMongerDialogue(player);
		}

		@Override
		public boolean open(Object... args) {
			npc = (NPC) args[0];
			npc("Hello, "  + player.getUsername() + " would you like to look at", "my wares?");
			stage = 0;
			return true;
		}

		@Override
		public boolean handle(int interfaceId, int buttonId) {
			switch (stage) {
			case 0:
				options("Yes, please.", "No, thanks.");
				stage++;
				break;
			case 1:
				switch (buttonId) {
				case 1:
					end();
					npc.openShop(player);
					break;
				case 2:
					end();
					break;
				}
				break;
			}
			return true;
		}

		@Override
		public int[] getIds() {
			return new int[] {1369};
		}
		
	}
}
