/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import games.stendhal.common.Rand;
import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.impl.CreatureLogic;
import games.stendhal.server.entity.creature.impl.DropItem;
import games.stendhal.server.entity.creature.impl.EquipItem;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import games.stendhal.server.entity.spawner.CreatureRespawnPoint;
import games.stendhal.server.pathfinder.Path;
import games.stendhal.server.pathfinder.Path.Node;
import games.stendhal.server.rule.EntityManager;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SyntaxException;
import marauroa.common.game.Definition.Type;

/**
 * Serverside representation of a creature.
 * <p>
 * A creature is defined as an entity which can move with certain speed, has
 * life points (HP) and can die.
 * <p>
 * Not all creatures have to be hostile, but at the moment the default behavior
 * is to attack the player.
 * <p>
 * The ai
 */
public class Creature extends NPC {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Creature.class);


	/**
	 * The higher the number the less items are dropped.
	 * To use numbers determined at creatures.xml, just make it 1.
	 */
	private static final double SERVER_DROP_GENEROSITY = 1;

	private CreatureRespawnPoint point;

	/** the speed of this creature */
	private double speed;

	/** size in width of a tile */
	private int width;

	private int height;

	/** Ths list of item names this creature may drop 
	 *  Note; per default this list is shared with all creatures
	 *  of that class*/
	protected List<DropItem> dropsItems;

	/** Ths list of item instances this creature may drop for
	 *  use in quests. This is always creature specific */
	protected List<Item> dropItemInstances;

	/**
	 * List of things this creature should say
	 */
	protected List<String> noises;

	private int respawnTime;

	private Map<String, String> aiProfiles;
	
	private CreatureLogic creatureLogic = null;

	public static void generateRPClass() {
		try {
			RPClass npc = new RPClass("creature");
			npc.isA("npc");
			npc.addAttribute("debug", Type.VERY_LONG_STRING, Definition.VOLATILE);
			npc.addAttribute("metamorphosis", Type.STRING, Definition.VOLATILE);
			npc.addAttribute("width", Type.FLOAT, Definition.VOLATILE);
			npc.addAttribute("height", Type.FLOAT, Definition.VOLATILE);
		} catch (SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	public Creature(RPObject object) {
		super(object);
		creatureLogic = new CreatureLogic(this);

		setRPClass("creature");
		put("type", "creature");
		put("title_type", "enemy");
		if (object.has("title_type")) {
			put("title_type", object.get("title_type"));
		}
		creatureLogic.createPath();

		dropsItems = new ArrayList<DropItem>();
		dropItemInstances = new ArrayList<Item>();
		aiProfiles = new HashMap<String, String>();
	}

	public Creature(Creature copy) {
		this();

		this.speed = copy.speed;
		this.width = copy.width;
		this.height = copy.height;

		/** Creatures created with this function will share their
		 *  dropsItems with any other creature of that kind. If you want
		 *  individual dropsItems, use clearDropItemList first!
		 */
		if (copy.dropsItems != null) {
			this.dropsItems = copy.dropsItems;
		}
		// this.dropItemInstances is ignored;

		this.aiProfiles = copy.aiProfiles;
		this.noises = copy.noises;

		this.respawnTime = copy.respawnTime;

		put("class", copy.get("class"));
		put("subclass", copy.get("subclass"));
		put("name", copy.get("name"));

		put("x", 0);
		put("y", 0);
		put("width", copy.get("width"));
		put("height", copy.get("height"));
		setDescription(copy.getDescription());
		setATK(copy.getATK());
		setDEF(copy.getDEF());
		setXP(copy.getXP());
		initHP(copy.getBaseHP());
		setName(copy.getName());

		setLevel(copy.getLevel());

		update();

		stop();
		if (logger.isDebugEnabled()) {
			logger.debug(getIDforDebug() + " Created " + get("class") + ":" + this);
		}
	}

	public Creature getInstance() {
		return new Creature(this);
	}

	/**
	 * creates a new creature without properties. These must be set in the
	 * deriving class
	 */
	public Creature() {
		super();		
		creatureLogic = new CreatureLogic(this);
		setRPClass("creature");
		put("type", "creature");
		put("title_type", "enemy");

		creatureLogic.createPath();

		dropsItems = new ArrayList<DropItem>();
		dropItemInstances = new ArrayList<Item>();
		aiProfiles = new HashMap<String, String>();
	}

	/**
	 * Creates a new creature with the given properties.
	 * @param clazz The creature's class, e.g. "golem"
	 * @param subclass The creature's subclass, e.g. "wooden_golem"
	 * @param name Typically the same as clazz, except for NPCs
	 * @param hp The creature's maximum health points
	 * @param attack The creature's attack strength
	 * @param defense The creature's attack strength
	 * @param level The creature's level
	 * @param xp The creature's experience
	 * @param width The creature's width, in squares
	 * @param height The creature's height, in squares
	 * @param speed
	 * @param dropItems
	 * @param aiProfiles
	 * @param noises
	 * @param respawnTime
	 * @param description
	 * @throws AttributeNotFoundException
	 */
	public Creature(String clazz, String subclass, String name, int hp, int attack, int defense, int level, int xp,
	        int width, int height, double speed, List<DropItem> dropItems, Map<String, String> aiProfiles,
	        List<String> noises, int respawnTime, String description) {
		this();

		this.speed = speed;
		this.width = width;
		this.height = height;

		/** Creatures created with this function will share their
		 *  dropItems with any other creature of that kind. If you want
		 *  individual dropItems, use clearDropItemList first!
		 */
		if (dropItems != null) {
			this.dropsItems = dropItems;
		}
		// this.dropItemInstances is ignored;

		this.aiProfiles = aiProfiles;
		this.noises = noises;

		this.respawnTime = respawnTime;

		put("class", clazz);
		put("subclass", subclass);
		put("name", name);

		put("x", 0);
		put("y", 0);
		put("width", width);
		put("height", height);		
		setDescription(description);
		setATK(attack);
		setDEF(defense);
		setXP(xp);
		setBaseHP(hp);
		setHP(hp);

		setLevel(level);

		update();

		stop();
	}

	public RPObject.ID getIDforDebug() {
		return null;
	}

	public void setRespawnPoint(CreatureRespawnPoint point) {
		this.point = point;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public CreatureRespawnPoint getRespawnPoint() {
		return point;
	}

	/**
	 *  clears the list of predefined dropItems and creates
	 *  an empty list specific to this creature
	 *
	 */
	public void clearDropItemList() {
		dropsItems = new ArrayList<DropItem>();
		dropItemInstances.clear();
	}

	/**
	 *  adds a named item to the List of Items that will be dropped on dead
	 *  if clearDropItemList hasn't been called first, this will change
	 *  all creatures of this kind
	 */
	public void addDropItem(String name, double probability, int min, int max) {
		dropsItems.add(new DropItem(name, probability, min, max));
	}

	/**
	 *  adds a named item to the List of Items that will be dropped on dead
	 *  if clearDropItemList hasn't been called first, this will change
	 *  all creatures of this kind
	 */
	public void addDropItem(String name, double probability, int amount) {
		dropsItems.add(new DropItem(name, probability, amount));
	}

	/**
	 * adds a specific item to the List of Items that will be dropped on dead
	 * with 100 % probability. this is always for that specific creature only.
	 * @param item
	 */
	public void addDropItem(Item item) {
		dropItemInstances.add(item);
	}

	/**
	 * Returns true if this RPEntity is attackable
	 */
	@Override
	public boolean isAttackable() {
		return true;
	}
	
	@Override
	public void onDead(Entity killer) {
		if (point != null) {
			point.notifyDead(this);
		} else {
			// Perhaps a summoned creature
			StendhalRPRuleProcessor.get().removeNPC(this);
		}

		super.onDead(killer);
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {
		for (Item item : dropItemInstances) {
			corpse.add(item);
			if (corpse.isFull()) {
				break;
			}
		}

		for (Item item : createDroppedItems(StendhalRPWorld.get().getRuleManager().getEntityManager())) {
			corpse.add(item);

			if (corpse.isFull()) {
				break;
			}
		}
	}

	@Override
	public void getArea(Rectangle2D rect, double x, double y) {
		if ((width == 1) && (height == 2)) {
			// The size 1,2 is a bit special... :)
			rect.setRect(x, y + 1, 1, 1);
		} else {
			rect.setRect(x, y, width, height);
		}
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	/**
	 * Returns a list of enemies. One of it will be attacked.
	 *
	 * @return list of enemies
	 */
	protected List<RPEntity> getEnemyList() {
		StendhalRPZone zone = getZone();
		if (aiProfiles.keySet().contains("offensive")) {
			return zone.getPlayerAndFirends();
		} else {
			return getAttackingRPEntities();
		}
	}

	/**
	 * returns the nearest enemy, which is reachable
	 *
	 * @param range attack radius
	 * @return chosen enemy or null if no enemy was found.
	 */
	public RPEntity getNearestEnemy(double range) {

		// where are we?
		Rectangle2D entityArea = getArea(getX(), getY());
		int x = (int) entityArea.getCenterX();
		int y = (int) entityArea.getCenterY();

		// create list of enemies
		List<RPEntity> enemyList = getEnemyList();

		// calculate the distance of all possible enemies
		Map<RPEntity, Double> distances = new HashMap<RPEntity, Double>();
		for (RPEntity enemy : enemyList) {
			if (enemy == this) {
				continue;
			}

			if (enemy.has("invisible")) {
				continue;
			}

			if (enemy.get("zoneid").equals(get("zoneid"))) {
				java.awt.geom.Rectangle2D rect = enemy.getArea(enemy.getX(), enemy.getY());
				int fx = (int) rect.getCenterX();
				int fy = (int) rect.getCenterY();

				if ((Math.abs(fx - x) < range) && (Math.abs(fy - y) < range)) {
					distances.put(enemy, squaredDistance(enemy));
				}
			}
		}

		// now choose the nearest enemy for which there is a path
		RPEntity chosen = null;
		while ((chosen == null) && (distances.size() > 0)) {
			double shortestDistance = Double.MAX_VALUE;
			for (RPEntity enemy : distances.keySet()) {
				double distance = distances.get(enemy).doubleValue();
				if (distance < shortestDistance) {
					chosen = enemy;
					shortestDistance = distance;
				}
			}

			// calculate the destArea
			Rectangle2D targetArea = chosen.getArea(chosen.getX(), chosen.getY());
			Rectangle destArea = new Rectangle((int) (targetArea.getX() - entityArea.getWidth()), (int) (targetArea
			        .getY() - entityArea.getHeight()), (int) (entityArea.getWidth() + targetArea.getWidth() + 1),
			        (int) (entityArea.getHeight() + targetArea.getHeight() + 1));

			// for 1x2 size creatures the destArea, needs bo be one up
			destArea.translate(0, (int) (this.getY() - entityArea.getY()));

			// is there a path to this enemy?
			// List<Node> path = Path.searchPath(this, chosen, 20.0);
			List<Node> path = Path.searchPath(this, getX(), getY(), destArea, 20.0);
			if ((path == null) || (path.size() == 0)) {
				distances.remove(chosen);
				chosen = null;
			} else {
				// set the path. if not setMovement() will search a new one
				setPath(path, false);
			}
		}
		// return the chosen enemy or null if we could not find one in reach
		return chosen;
	}

	public boolean isEnemyNear(double range) {
		int x = getX();
		int y = getY();

		double distance = range * range; // We save this way several sqrt
		// operations

		List<RPEntity> enemyList = getEnemyList();
		if (enemyList.size() == 0) {
			StendhalRPZone zone = getZone();
			enemyList = zone.getPlayerAndFirends();
		}

		for (RPEntity playerOrFriend : enemyList) {
			if (playerOrFriend == this) {
				continue;
			}

			if (playerOrFriend.has("invisible")) {
				continue;
			}

			if (playerOrFriend.get("zoneid").equals(get("zoneid"))) {
				int fx = playerOrFriend.getX();
				int fy = playerOrFriend.getY();

				if ((Math.abs(fx - x) < range) && (Math.abs(fy - y) < range)) {
					if (squaredDistance(playerOrFriend) < distance) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/** returns a string-repesentation of the path */
	public String pathToString() {
		int pos = getPathPosition();
		List<Path.Node> thePath = getPath();
		List<Path.Node> nodeList = thePath.subList(pos, thePath.size());

		return nodeList.toString();
	}

	/** need to recalculate the ai when we stop the attack */
	@Override
	public void stopAttack() {
		creatureLogic.resetAIState();
		super.stopAttack();
	}


	public void tryToPoison() {
		if ((getAttackTarget() != null) && nextTo(getAttackTarget()) && aiProfiles.containsKey("poisonous")) {
			// probability of poisoning is 1 %
			int roll = Rand.roll1D100();
			String[] poison = aiProfiles.get("poisonous").split(",");
			int prob = Integer.parseInt(poison[0]);
			String poisonType = poison[1];

			if (roll <= prob) {
				ConsumableItem item = (ConsumableItem) StendhalRPWorld.get().getRuleManager().getEntityManager()
				        .getItem(poisonType);
				if (item == null) {
					logger.error("Creature unable to poisoning with " + poisonType);
				} else {
					RPEntity entity = getAttackTarget();

					if (entity instanceof Player) {
						Player player = (Player) entity;

						if (!player.isPoisoned() && player.poison(item)) {
							StendhalRPRuleProcessor.get().addGameEvent(getName(), "poison", player.getName());

							player.sendPrivateText("You have been poisoned by a " + getName());
						}
					}
				}
			}
		}
	}

	/**
	 * This method should be called every turn if the animal is supposed to
	 * heal itself on its own. If it is used, an injured animal will heal
	 * itself by up to <i>amount</i> hitpoints every <i>frequency</i> turns.
	 * @param amount The number of hitpoints that can be restored at a time
	 * @param frequency The number of turns between healings  
	 */
	public void healSelf(int amount, int frequency) {
		if ((StendhalRPRuleProcessor.get().getTurn() % frequency == 0) && (getHP() > 0)) {
			if (getHP() + amount < getBaseHP()) {
				setHP(getHP() + amount);
				put("heal", amount);
			} else {
				setHP(getBaseHP());
				put("heal", getHP() + amount - getBaseHP());
			}
		}
	}

	public void equip(List<EquipItem> items) {
		for (EquipItem equipedItem : items) {
			if (!hasSlot(equipedItem.slot)) {
				addSlot(new EntitySlot(equipedItem.slot));
			}

			RPSlot slot = getSlot(equipedItem.slot);
			EntityManager manager = StendhalRPWorld.get().getRuleManager().getEntityManager();

			Item item = manager.getItem(equipedItem.name);

			if (item instanceof StackableItem) {
				((StackableItem) item).setQuantity(equipedItem.quantity);
			}

			slot.add(item);
		}
	}

	private List<Item> createDroppedItems(EntityManager manager) {
		List<Item> list = new LinkedList<Item>();

		for (DropItem dropped : dropsItems) {
			double probability = Rand.rand(1000000)/10000.0;

			if (probability <= (dropped.probability/SERVER_DROP_GENEROSITY)) {
				Item item = manager.getItem(dropped.name);
				if (item == null) {
					logger.error("Unable to create item: " + dropped.name);
					continue;
				}

				if (dropped.min == dropped.max) {
					list.add(item);
				} else {
					StackableItem stackItem = (StackableItem) item;
					stackItem.setQuantity(Rand.rand(dropped.max - dropped.min) + dropped.min);
					list.add(stackItem);
				}
			}
		}
		return list;
	}

	@Override
	public boolean canDoRangeAttack(RPEntity target) {
		if (aiProfiles.containsKey("archer")) {
			// The creature can shoot, but only if the target is at most
			// 7 tiles away.
			// TODO: make the max distance configurable via creatures.xml.
			return squaredDistance(target) <= 7 * 7;
		}
		return super.canDoRangeAttack(target);
	}

	/**
	 * returns the value of an ai profile
	 *
	 * @param key as defined in creatures.xml
	 * @return value or null if undefined
	 */
	public String getAIProfile(String key) {
		return aiProfiles.get(key);
	}

	/**
	 * is called after the Creature is added to the zone
	 */
	public void init() {
		// do nothing
	}

	@Override
	public void logic() {
		creatureLogic.logic();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Random sound noises.
	 */
	public void makeNoice() {
		if (noises.size() > 0) {
			int pos = Rand.rand(noises.size());
			say(noises.get(pos));
		}
	}
}
