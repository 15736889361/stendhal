package utilities;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.PlayerSlot;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public abstract class PlayerTestHelper {

	/**
	 * Create a named player.
	 * 
	 * @param name
	 * @return player
	 */
	public static Player createPlayer(String name) {
		generatePlayerRPClasses();

		Player pl = new Player(new RPObject());
		pl.setName(name);

		addEmptySlots(pl);

		return pl;
	}

	/**
	 * Create an named mock player object.
	 * @param name of the player
	 * 
	 * @return mock player object
	 */
	public static PrivateTextMockingTestPlayer createPrivateTextMockingTestPlayer(String name) {
		generatePlayerRPClasses();

		PrivateTextMockingTestPlayer pl = new PrivateTextMockingTestPlayer(new RPObject(), name);
		addEmptySlots(pl);

		return pl;
	}

	/**
	 * Register a player in rule processor, world and zone.
	 * 
	 * @param player
	 * @param zoneName
	 */
	public static void registerPlayer(Player player, String zoneName) {
		registerPlayer(player, SingletonRepository.getRPWorld().getZone(zoneName));
	}

	/**
	 * Register a player in rule processor, world and zone.
	 * 
	 * @param player
	 * @param zone
	 */
	public static void registerPlayer(Player player, StendhalRPZone zone) {
		registerPlayer(player);

		zone.add(player);
	}

	/**
	 * Register a player in rule processor and world.
	 * 
	 * @param player
	 * @param zone
	 */
	public static void registerPlayer(Player player) {
		MockStendhalRPRuleProcessor.get().addPlayer(player);

		MockStendlRPWorld.get().add(player);
	}

	public static Player createPlayerWithOutFit(String name) {
		Player player = createPlayer(name);

		player.put("outfit", "01010101");

		return player;
	}

	/**
	 * Remove a player from rule processor, world and zone.
	 * 
	 * @param player
	 * @param zone
	 */
	public static void unregisterPlayer(Player player, StendhalRPZone zone) {
		zone.remove(player);
		removePlayer(player);
	}

	/**
	 * Remove a player from rule processor, world and zone.
	 * 
	 * @param playerName
	 * @param zone
	 */
	public static void removePlayer(String playerName, StendhalRPZone zone) {
		Player player = MockStendhalRPRuleProcessor.get().getPlayer(playerName);

		if (player != null) {
			unregisterPlayer(player, zone);
		}
	}

	/**
	 * Remove a player from rule processor, world and zone.
	 * 
	 * @param playerName
	 * @param zoneName
	 */
	public static void removePlayer(String playerName, String zoneName) {
		removePlayer(playerName, MockStendlRPWorld.get().getZone(zoneName));
	}

	/**
	 * Remove a player from world and rule processor.
	 * 
	 * @param playerName
	 */
	public static void removePlayer(String playerName) {
		Player player = MockStendhalRPRuleProcessor.get().getPlayer(playerName);

		if (player != null) {
			removePlayer(player);
		}
	}

	/**
	 * Remove a player from world and rule processor.
	 * 
	 * @param player
	 */
	public static void removePlayer(Player player) {
		if (player != null) {
			MockStendlRPWorld.get().remove(player.getID());
			MockStendhalRPRuleProcessor.get().getOnlinePlayers().remove(player);
		}
	}

	/**
	 * Remove all players from world and rule processor.
	 */
	public static void removeAllPlayers() {
		MockStendlRPWorld.reset();
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	/**
	 * Equip the player with the given amount of money.
	 * 
	 * @param player
	 * @param amount
	 * @return success flag
	 */
	public static boolean equipWithMoney(Player player, int amount) {
		return equipWithStackableItem(player, "money", amount);
	}

	/**
	 * Equip the player with the given items.
	 * 
	 * @param player
	 * @param clazz
	 * @return success flag
	 */
	public static boolean equipWithItem(Player player, String clazz) {
		Item item = SingletonRepository.getEntityManager().getItem(clazz);

		return player.equip(item);
	}

	/**
	 * Equip the player with the given amount of items.
	 * 
	 * @param player
	 * @param clazz
	 * @param amount
	 * @return success flag
	 */
	public static boolean equipWithStackableItem(Player player, String clazz, int amount) {
		StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(clazz);
		item.setQuantity(amount);

		return player.equip(item);
	}

	/**
	 * Reset the conversation state of the named NPC.
	 * 
	 * @param npcName
	 */
	public static void resetNPC(String npcName) {
		SpeakerNPC npc = SingletonRepository.getNPCList().get(npcName);

		if (npc != null) {
			npc.setCurrentState(ConversationStates.IDLE);
		}
	}

	/**
	 * Remove the named NPC.
	 * 
	 * @param npcName
	 */
	public static void removeNPC(String npcName) {
		SingletonRepository.getNPCList().remove(npcName);
	}

	/**
	 * Remove a zone from the world.
	 * 
	 * @param zoneName
	 */
	public static void removeZone(String zoneName) {
		// TODO implement removeZone()
	}

	public static void addEmptySlots(Player player) {
//		"bag", "rhand", "lhand", "head", "armor",
//		"legs", "feet", "finger", "cloak", "keyring"
		player.addSlot(new PlayerSlot("bag"));
		player.addSlot(new PlayerSlot("lhand"));
		player.addSlot(new PlayerSlot("rhand"));
		player.addSlot(new PlayerSlot("armor"));
		player.addSlot(new PlayerSlot("head"));
		player.addSlot(new PlayerSlot("legs"));
		player.addSlot(new PlayerSlot("feet"));
		player.addSlot(new PlayerSlot("finger"));
		player.addSlot(new PlayerSlot("cloak"));
		player.addSlot(new PlayerSlot("keyring"));
		player.addSlot(new RPSlot("!buddy"));
		player.getSlot("!buddy").add(new RPObject());
		player.addSlot(new RPSlot("!quests"));
		player.getSlot("!quests").add(new RPObject());
		player.addSlot(new RPSlot("!kills"));
		player.getSlot("!kills").add(new RPObject());
		player.addSlot(new RPSlot("!tutorial"));
		player.getSlot("!tutorial").add(new RPObject());
		player.addSlot(new RPSlot("!visited"));
		player.getSlot("!visited").add(new RPObject());
	}

	public static void generateEntityRPClasses() {
		if (!RPClass.hasRPClass("entity")) {
			Entity.generateRPClass();
		}

		if (!RPClass.hasRPClass("active_entity")) {
			ActiveEntity.generateRPClass();
		}

		if (!RPClass.hasRPClass("rpentity")) {
			RPEntity.generateRPClass();
		}
	}

	public static void generateNPCRPClasses() {
		generateEntityRPClasses();

		if (!RPClass.hasRPClass("npc")) {
			NPC.generateRPClass();
		}
	}

	public static void generatePlayerRPClasses() {
		generateEntityRPClasses();

		if (!RPClass.hasRPClass("player")) {
			Player.generateRPClass();
		}
	}

	public static void generateCreatureRPClasses() {
		generateNPCRPClasses();

		if (!RPClass.hasRPClass("creature")) {
			Creature.generateRPClass();
		}
	}
}
