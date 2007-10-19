package games.stendhal.server.scripting;

import games.stendhal.server.StendhalRPAction;
import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.player.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPObject;

public abstract class ScriptingSandbox {

	// loadedNPCs and loadedRPObject are Sets. They are implemented using
	// maps because there are no WeakSets in Java. (In current Sun
	// Java HashSet is implemented using HashMap anyway).
	private Map<NPC, Object> loadedNPCs = new WeakHashMap<NPC, Object>();
	private Map<RPObject, Object> loadedRPObjects = new WeakHashMap<RPObject, Object>();

	private String exceptionMessage;

	private StendhalRPZone zone;

	private String filename;

	private static final Logger logger = Log4J.getLogger(ScriptingSandbox.class);

	public ScriptingSandbox(String filename) {
		this.filename = filename;
	}

	public StendhalRPZone getZone(RPObject rpobject) {
		return (StendhalRPZone) StendhalRPWorld.get().getRPZone(rpobject.getID());
	}

	public boolean setZone(String name) {
		zone = StendhalRPWorld.get().getZone(name);
		return (zone != null);
	}

	public boolean setZone(StendhalRPZone zone) {
		this.zone = zone;
		return (zone != null);
	}

	public StendhalRPZone addZone(String name, String content) {
		try {
			zone = StendhalRPWorld.get().addArea(name, content);
			logger.info(filename + " added area: " + name);
		} catch (Exception e) {
			logger.error("Exception while tyring to add area: " + e);
			zone = null;
		}
		return (zone);
	}

	/**
	 * @param player
	 * @param zoneName
	 * @param x
	 * @param y
	 * @return true in case of success, false otherwise
	 * @deprecated use Player.teleport() directly instead
	 */
	@Deprecated
	public boolean transferPlayer(Player player, String zoneName, int x, int y) {
		StendhalRPZone zoneTemp = StendhalRPWorld.get().getZone(zoneName);
		return player.teleport(zoneTemp, x, y, null, null);
	}

	public boolean playerIsInZone(Player player, String zoneName) {
		return player.getZone().getName().equals(zoneName);
	}

	public void add(NPC npc) {
		if (zone != null) {
			zone.add(npc);
			loadedNPCs.put(npc, null);
			logger.info(filename + " added NPC: " + npc);
		}
	}

	public void add(RPObject object) {
		if (zone != null) {
			zone.add(object);
			loadedRPObjects.put(object, null);
			logger.info(filename + " added object: " + object);
		}
	}

	public Creature[] getCreatures() {
		return (StendhalRPWorld.get().getRuleManager().getEntityManager().getCreatures().toArray(new Creature[1]));
	}

	public Creature getCreature(String clazz) {
		return StendhalRPWorld.get().getRuleManager().getEntityManager().getCreature(clazz);
	}

	public Item[] getItems() {
		return (StendhalRPWorld.get().getRuleManager().getEntityManager().getItems().toArray(new Item[1]));
	}

	public Item getItem(String name) {
		return StendhalRPWorld.get().getRuleManager().getEntityManager().getItem(name);
	}

	public Creature add(Creature template, int x, int y) {
		Creature creature = template.getInstance();
		if (zone != null) {
			if (StendhalRPAction.placeat(zone, creature, x, y)) {
				loadedNPCs.put(creature, null);
				logger.info(filename + " added creature: " + creature);
			} else {
				logger.info(filename + " could not add a creature: " + creature);
				creature = null;
			}
		}
		return (creature);
	}

	public void addGameEvent(String source, String event, List<String> params) {
		StendhalRPRuleProcessor.get().addGameEvent(source, event, params.toArray(new String[params.size()]));
	}

	public void modify(RPEntity entity) {
		entity.notifyWorldAboutChanges();
	}

	public void privateText(Player player, String text) {
		player.sendPrivateText(text);
	}

	// ------------------------------------------------------------------------

	public abstract boolean load(Player player, String[] args);

	public String getMessage() {
		return exceptionMessage;
	}

	protected void setMessage(String message) {
		this.exceptionMessage = message;
	}

	public void remove(NPC npc) {
		logger.info("Removing " + filename + " added NPC: " + npc);
		try {
			NPCList.get().remove(npc.getName());

			zone = npc.getZone();
			zone.remove(npc);
			loadedNPCs.remove(npc);
		} catch (Exception e) {
			logger.warn("Exception while removing " + filename + " added NPC: " + e);
		}
	}

	public void remove(RPObject object) {
		try {
			logger.info("Removing script added object: " + object);
			String id = object.getID().getZoneID();
			zone = StendhalRPWorld.get().getZone(id);
			zone.remove(object);
			loadedRPObjects.remove(object);
		} catch (Exception e) {
			logger.warn("Exception while removing " + filename + " added object: " + e);
		}
	}

	@SuppressWarnings("unchecked")
	public void unload(Player player, String[] args) {
		Set<NPC> setNPC = new HashSet<NPC>(loadedNPCs.keySet());

		for (NPC npc : setNPC) {
			remove(npc);
		}

		Set<RPObject> setRPObject = new HashSet<RPObject>(loadedRPObjects.keySet());
		for (RPObject object : setRPObject) {
			remove(object);
		}
	}

	public boolean execute(Player player, String[] args) {
		// do nothing
		return true;
	}
}
