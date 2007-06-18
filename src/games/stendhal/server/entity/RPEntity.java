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
package games.stendhal.server.entity;

import games.stendhal.common.Level;
import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.TutorialNotifier;
import games.stendhal.server.rule.ActionManager;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

public abstract class RPEntity extends GuidedEntity {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPEntity.class);

	protected static Statistics stats;

	private String name;
	
	private int atk;

	private int atk_xp;

	private int def;

	private int def_xp;

	private int base_hp;

	private int hp;

	private int xp;

	private int level;

	private int mana;

	private int base_mana;

	/**
	 * Maps each enemy which has recently damaged this RPEntity to the turn
	 * when the last damage has occured.
	 * 
	 * You only get ATK and DEF experience by fighting against a creature that
	 * is in this list.
	 */
	private Map<RPEntity, Integer> enemiesThatGiveFightXP;

	/** List of all enemies that are currently attacking of this entity. */
	private List<Entity> attackSources;

	/** current target */
	private RPEntity attackTarget;

	/**
	 * Maps each attacker to the sum of hitpoint loss it has caused to this
	 * RPEntity. 
	 */
	protected Map<Entity, Integer> damageReceived;

	/** list of players which are to reward with xp on killing this creature */
	protected Set<Player> playersToReward;

	protected int totalDamageReceived;

	/**
	 * To prevent players from gaining attack and defense experience by
	 * fighting against very weak creatures, they only gain atk and def xp
	 * for so many turns after they have actually been damaged by the enemy.
	 */
	private static int TURNS_WHILE_FIGHT_XP_INCREASES = StendhalRPWorld
			.get().getTurnsInSeconds(12);

	/**
	 * All the slots considered to be "with" the entity.
	 * Listed in priority order (i.e. bag first).
	 */
	public static final String[] CARRYING_SLOTS = { "bag", "head", "rhand", "lhand", "armor", "finger", "cloak", "legs", "feet" , "keyring" };

	public static void generateRPClass() {
		stats = Statistics.getStatistics();

		try {
			RPClass entity = new RPClass("rpentity");
			entity.isA("active_entity");
			entity.add("name", RPClass.STRING);
			entity.add("title", RPClass.STRING);
			entity.add("level", RPClass.SHORT);
			entity.add("xp", RPClass.INT);
			entity.add("mana", RPClass.INT);
			entity.add("base_mana", RPClass.INT);

			// TODO: Remove just prior to DB Reset
			entity.add("hp/base_hp", RPClass.FLOAT, RPClass.VOLATILE);
			entity.add("base_hp", RPClass.SHORT, RPClass.PRIVATE);
			entity.add("hp", RPClass.SHORT, RPClass.PRIVATE);

			entity.add("atk", RPClass.SHORT, RPClass.PRIVATE);
			entity.add("atk_xp", RPClass.INT, RPClass.PRIVATE);
			entity.add("def", RPClass.SHORT, RPClass.PRIVATE);
			entity.add("def_xp", RPClass.INT, RPClass.PRIVATE);
			entity.add("atk_item", RPClass.INT, (byte) (RPClass.PRIVATE | RPClass.VOLATILE));
			entity.add("def_item", RPClass.INT, (byte) (RPClass.PRIVATE | RPClass.VOLATILE));

			entity.add("risk", RPClass.BYTE, RPClass.VOLATILE);
			entity.add("damage", RPClass.INT, RPClass.VOLATILE);
			entity.add("heal", RPClass.INT, RPClass.VOLATILE);
			entity.add("target", RPClass.INT, RPClass.VOLATILE);
			entity.add("title_type", RPClass.STRING, RPClass.VOLATILE);

			entity.addRPSlot("head", 1, RPClass.PRIVATE);
			entity.addRPSlot("rhand", 1, RPClass.PRIVATE);
			entity.addRPSlot("lhand", 1, RPClass.PRIVATE);
			entity.addRPSlot("armor", 1, RPClass.PRIVATE);
			entity.addRPSlot("finger", 1, RPClass.PRIVATE);
			entity.addRPSlot("cloak", 1, RPClass.PRIVATE);
			entity.addRPSlot("legs", 1, RPClass.PRIVATE);
			entity.addRPSlot("feet", 1, RPClass.PRIVATE);
			entity.addRPSlot("bag", 12, RPClass.PRIVATE);
			entity.addRPSlot("keyring", 6, RPClass.PRIVATE);
		} catch (RPClass.SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public RPEntity(RPObject object) throws AttributeNotFoundException {
		super(object);
		attackSources = new ArrayList<Entity>();
		damageReceived = new HashMap<Entity, Integer>();
		playersToReward = new HashSet<Player>();
		enemiesThatGiveFightXP = new HashMap<RPEntity, Integer>();
		totalDamageReceived = 0;
	}

	public RPEntity() throws AttributeNotFoundException {
		super();
		attackSources = new ArrayList<Entity>();
		damageReceived = new HashMap<Entity, Integer>();
		playersToReward = new HashSet<Player>();
		enemiesThatGiveFightXP = new HashMap<RPEntity, Integer>();
		totalDamageReceived = 0;
	}

	/**
	 * Give the player some karma (good or bad).
	 *
	 * @param	karma		An amount of karma to add/subtract.
	 */
	public void addKarma(double karma) {
		// No nothing
	}

	/**
	 * Get the current amount of karma.
	 *
	 * @return	The current amount of karma.
	 *
	 * @see-also	#addKarma()
	 */
	public double getKarma() {
		// No karma (yet)
		return 0.0;
	}

	/**
	 * Get some of the player's karma. A positive value indicates
	 * good luck/energy. A negative value indicates bad luck/energy.
	 * A value of zero should cause no change on an action or outcome.
	 *
	 * @param	scale		A positive number.
	 *
	 * @return	A number between -scale and scale.
	 */
	public double useKarma(double scale) {
		// No impact
		return 0.0;
	}

	/**
	 * Get some of the player's karma. A positive value indicates
	 * good luck/energy. A negative value indicates bad luck/energy.
	 * A value of zero should cause no change on an action or outcome.
	 *
	 * @param	negLimit	The lowest negative value returned.
	 * @param	posLimit	The highest positive value returned.
	 *
	 * @return	A number within negLimit &lt;= 0 &lt;= posLimit.
	 */
	public double useKarma(double negLimit, double posLimit) {
		// No impact
		return 0.0;
	}

	/**
	 * Use some of the player's karma. A positive value indicates
	 * good luck/energy. A negative value indicates bad luck/energy.
	 * A value of zero should cause no change on an action or outcome.
	 *
	 * @param	negLimit	The lowest negative value returned.
	 * @param	posLimit	The highest positive value returned.
	 * @param	granularity	The amount that any extracted
	 *				karma is a multiple of.
	 *
	 * @return	A number within negLimit &lt;= 0 &lt;= posLimit.
	 */
	public double useKarma(double negLimit, double posLimit, double granularity) {
		// No impact
		return 0.0;
	}

	/**
	 * Determine if this is an obstacle for another entity.
	 *
	 * @param	entity		The entity to check against.
	 *
	 * @return	<code>true</code> if the other entity is an RPEntity.
	 */
	@Override
	public boolean isObstacle(Entity entity) {
		if (isGhost()) {
			return false;
		}

		return (entity instanceof RPEntity);
	}


	/**
	 * Heal this entity completely.
	 *
	 * @return	The amount actually healed.
	 */
	public int heal() {
		int baseHP = getBaseHP();
		int given = baseHP - getHP();

		if(given != 0) {
			put("heal", given);
			setHP(baseHP);
		}

		return given;
	}


	/**
	 * Heal this entity.
	 *
	 * @param	amount		The [maximum] amount to heal by.
	 *
	 * @return	The amount actually healed.
	 */
	public int heal(int amount) {
		return heal(amount, false);
	}


	/**
	 * Heal this entity.
	 *
	 * @param	amount		The [maximum] amount to heal by.
	 * @param	tell		Whether to tell the entity they've
	 *				been healed.
	 *
	 * @return	The amount actually healed.
	 */
	public int heal(int amount, boolean tell) {
		int hp = getHP();
		int given = Math.min(amount, getBaseHP() - hp);

		if(given != 0) {
			hp += given;

			if(tell) {
				put("heal", given);
			}

			setHP(hp);
		}

		return given;
	}


	@Override
	public void update() throws AttributeNotFoundException {
		super.update();

		if (has("name")) {
			name = get("name");
		}
		
		if (has("atk")) {
			atk = getInt("atk");
		}
		if (has("atk_xp")) {
			atk_xp = getInt("atk_xp");
			setATKXP(atk_xp);
		}

		if (has("def")) {
			def = getInt("def");
		}
		if (has("def_xp")) {
			def_xp = getInt("def_xp");
			setDEFXP(def_xp);
		}

		if (has("base_hp")) {
			base_hp = getInt("base_hp");
		}
		if (has("hp")) {
			hp = getInt("hp");
		}

		if (has("level")) {
			level = getInt("level");
		}
		if (has("xp")) {
			xp = getInt("xp");
		}
		if (has("mana")) {
			mana = getInt("mana");
		}
		if (has("base_mana")) {
			mana = getInt("base_mana");
		}
	}

	/**
	 * Set the entity's name.
	 *
	 * @param	name		The new name.
	 */
	public void setName(String name) {
		this.name = name;
		put("name", name);
	}

	/**
	 * Get the entity's name.
	 *
	 * @return	The entity's name.
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setLevel(int level) {
		this.level = level;
		put("level", level);
	}

	public int getLevel() {
		return level;
	}

	public void setATK(int atk) {
		this.atk = atk;
		put("atk", atk);
	}

	public int getATK() {
		return atk;
	}

	public void setATKXP(int atk) {
		this.atk_xp = atk;
		put("atk_xp", atk_xp);
		incATKXP();
	}

	public int getATKXP() {
		return atk_xp;
	}

	public int incATKXP() {
		this.atk_xp++;
		put("atk_xp", atk_xp);

		int newLevel = Level.getLevel(atk_xp);
		int levels = newLevel - (getATK() - 10);

		// In case we level up several levels at a single time.
		for (int i = 0; i < Math.abs(levels); i++) {
			setATK(this.atk + (int) Math.signum(levels) * 1);
			StendhalRPRuleProcessor.get().addGameEvent(getName(), "atk", Integer.toString(getATK()));
		}

		return atk_xp;
	}

	public void setDEF(int def) {
		this.def = def;
		put("def", def);
	}

	public int getDEF() {
		return def;
	}

	public void setDEFXP(int def) {
		this.def_xp = def;
		put("def_xp", def_xp);
		incDEFXP();
	}

	public int getDEFXP() {
		return def_xp;
	}

	public int incDEFXP() {
		this.def_xp++;
		put("def_xp", def_xp);

		int newLevel = Level.getLevel(def_xp);
		int levels = newLevel - (getDEF() - 10);

		// In case we level up several levels at a single time.
		for (int i = 0; i < Math.abs(levels); i++) {
			setDEF(this.def + (int) Math.signum(levels) * 1);
			StendhalRPRuleProcessor.get().addGameEvent(getName(), "def", Integer.toString(getDEF()));
		}

		return def_xp;
	}

	/**
	 * Set the base and current HP.
	 *
	 * @param	hp		The HP to set.
	 */
	public void initHP(int hp) {
		setBaseHP(hp);
		setHP(hp);
	}


	/**
	 * Set the base HP.
	 *
	 * @param	newhp		The base HP to set.
	 */
	public void setBaseHP(int newhp) {
		this.base_hp = newhp;
		put("base_hp", newhp);
	}


	/**
	 * Get the base HP.
	 *
	 * @return	The current HP.
	 */
	public int getBaseHP() {
		return base_hp;
	}


	/**
	 * Set the HP.
	 *
	 * @param	hp		The HP to set.
	 */
	public void setHP(int hp) {
		this.hp = hp;
		put("hp", hp);
	}


	/**
	 * Get the current HP.
	 *
	 * @return	The current HP.
	 */
	public int getHP() {
		return hp;
	}
	

	/**
	 * Gets the mana (magic)
	 *
	 * @return mana
	 */
	public int getMana() {
		return mana;
	}

	/** 
	 * Gets the base mana (like base_hp)
	 *
	 * @return base mana
	 */
	public int getBaseMana() {
		return base_mana;
	}

	/**
	 * sets the available mana
	 *
	 * @param newMana new amount of mana
	 */
	public void setMana(int newMana) {
		mana = newMana;
		put("mana", newMana);
	}

	/**
	 * Sets the base mana (like base_hp)
	 *
	 * @param newBaseMana new amount of base mana
	 */
	public void setBaseMana(int newBaseMana) {
		base_mana = newBaseMana;
		put("base_mana", newBaseMana);
	}
        /**                                                                                                                 
        * adds to base mana (like addXP)                                                                                 
        *                                                                                                                   
        * @param newBaseMana amount of base mana to be added                                                                        */
        public void addBaseMana(int newBaseMana) {
	        base_mana += newBaseMana;
	        put("base_mana", base_mana);
        }
    
	public void setXP(int newxp) {
		this.xp = newxp;
		put("xp", xp);
	}

	public void subXP(int newxp) {
		addXP(-newxp);
	}

	public void addXP(int newxp) {
		// Increment experience points
		this.xp += newxp;
		put("xp", xp);

		StendhalRPRuleProcessor.get().addGameEvent(getName(), "added xp", Integer.toString(newxp));
		StendhalRPRuleProcessor.get().addGameEvent(getName(), "xp", Integer.toString(xp));

		int newLevel = Level.getLevel(getXP());
		int levels = newLevel - getLevel();

		// In case we level up several levels at a single time.
		for (int i = 0; i < Math.abs(levels); i++) {
			setBaseHP(getBaseHP() + (int) Math.signum(levels) * 10);
			setHP(getHP() + (int) Math.signum(levels) * 10);

			setLevel(newLevel);
		}
	}

	public int getXP() {
		return xp;
	}

	/***************************************************************************
	 * * Attack handling code. * *
	 **************************************************************************/
	
	/**
	 * Returns true if this RPEntity is attackable
	 */
	public boolean isAttackable() {
		return true;
	}

	/** Modify the entity to order to attack the target entity */
	public void attack(RPEntity target) {
		put("target", target.getID().getObjectID());
		attackTarget = target;
	}

	/** Modify the entity to stop attacking */
	public void stopAttack() {
		if (has("risk")) {
			remove("risk");
		}
		if (has("damage")) {
			remove("damage");
		}
		if (has("heal")) {
			remove("heal");
		}
		if (has("target")) {
			remove("target");
		}

		if (attackTarget != null) {
			attackTarget.attackSources.remove(this);

			// XXX - Opponent could attack again, really remove?
			// Yes, because otherwise we would have a memory leak. When else
			// should dead creatures be removed from the hash map? --mort
			enemiesThatGiveFightXP.remove(attackTarget);

			attackTarget = null;
		}
	}

	public boolean getsFightXpFrom(RPEntity enemy) {
		Integer turnWhenLastDamaged = enemiesThatGiveFightXP.get(enemy);
		if (turnWhenLastDamaged == null) {
			return false;
		}
		int currentTurn = StendhalRPRuleProcessor.get().getTurn();
		if (currentTurn - turnWhenLastDamaged > TURNS_WHILE_FIGHT_XP_INCREASES) {
			enemiesThatGiveFightXP.remove(enemy);
			return false;
		}
		return true;
	}
	
	/**
	 * This method is called on each round when this entity has been attacked 
	 * by the given attacker.
	 * @param attacker The attacker.
	 * @param keepAttacking true means "keep attacking" and false means "stop
	 *        attacking".
	 */
	public void onAttacked(Entity attacker, boolean keepAttacking) {
		if (keepAttacking) {
			if (!attackSources.contains(attacker)) {
				attackSources.add(attacker);
			}
		} else {
			if (attacker.has("target")) {
				attacker.remove("target");
			}
			// attackSources.remove(attacker);
		}
	}

	/**
	 * Creates a blood pool on the ground under this entity, but only if there
	 * isn't a blood pool at that position already.
	 */
	private void bleedOnGround() {
		Rectangle2D rect = getArea();
		int bx = (int) rect.getX();
		int by = (int) rect.getY();
		StendhalRPZone zone = getZone();

		if(zone.getBlood(bx, by) == null) {
			Blood blood = new Blood();
			blood.set(bx, by);

			zone.assignRPObjectID(blood);
			zone.add(blood);
		}
	}

	/**
	 * This method is called when this entity has been attacked by Entity
	 * attacker and it has been damaged with damage points.
	 */
	public void onDamaged(Entity attacker, int damage) {
		logger.debug("Damaged " + damage + " points by " + attacker.getID());

		StendhalRPRuleProcessor.get().addGameEvent(attacker.getName(), "damaged", getName(), Integer.toString(damage));

		bleedOnGround();
		if (attacker instanceof RPEntity) {
			int currentTurn = StendhalRPRuleProcessor.get().getTurn();
			enemiesThatGiveFightXP.put((RPEntity) attacker, currentTurn);
		}
		
		int leftHP = getHP() - damage;

		totalDamageReceived += damage;

		// remember the damage done so that the attacker can later be rewarded
		// XP etc.
		if (damageReceived.containsKey(attacker)) {
			damageReceived.put(attacker, damage + damageReceived.get(attacker));
		} else {
			damageReceived.put(attacker, damage);
		}
		addPlayersToReward(attacker);

		if (leftHP > 0) {
			setHP(leftHP);
		} else {
			kill(attacker);
		}

		notifyWorldAboutChanges();
	}

	/**
	 * Manages a list of players to reward XP in case this creature is killed.
	 *
	 * @param player Player
	 */
	protected void addPlayersToReward(Entity player) {
		if (player instanceof Player) {
			playersToReward.add((Player) player);
		}
	}


	/**
	 * Apply damage to this entity. This is normally called from one of
	 * the other damage() methods to account for death.
	 *
	 * @param	amount		The HP to take.
	 *
	 * @return	The damage actually taken (in case HP was < amount).
	 */
	protected int damage(final int amount) {
		int hp = getHP();
		int taken = Math.min(amount, hp);

		hp -= taken;
		setHP(hp);

		return taken;
	}


	/**
	 * Apply damage to this entity, and call onDead() if HP reaches 0.
	 *
	 * @param	amount		The HP to take.
	 * @param	attacker	The attacking entity.
	 *
	 * @return	The damage actually taken (in case HP was < amount).
	 */
	public int damage(final int amount, final Entity attacker) {
		int taken = damage(amount);

		if(hp == 0) {
			onDead(attacker);
		}

		return taken;
	}


	/**
	 * Apply damage to this entity, and call onDead() if HP reaches 0.
	 *
	 * @param	amount		The HP to take.
	 * @param	attackerName	The name of the attacker (sutable
	 *				for use with <em>onDead()</em>.)
	 *
	 * @return	The damage actually taken (in case HP was < amount).
	 */
	public int damage(final int amount, final String attackerName) {
		int taken = damage(amount);

		if(hp == 0) {
			onDead(attackerName);
		}

		return taken;
	}


	/**
	 * Kills this RPEntity.
	 * @param killer The killer
	 */
	protected void kill(Entity killer) {
		setHP(0);
		StendhalRPRuleProcessor.get().killRPEntity(this, killer);
	}
	
	/**
	 * Gives XP to every player who has helped killing this RPEntity.
	 * @param oldXP The XP that this RPEntity had before being killed.
	 * @param oldLevel The level that this RPEntity had before being killed.
	 */
	protected void rewardKillers(int oldXP, int oldLevel) {
		int xpReward = (int) (oldXP * 0.05);

		for (Player killer: playersToReward) {

			TutorialNotifier.killedSomething(killer);
			
			Integer damageDone = damageReceived.get(killer);
			if (damageDone == null) {
				return;
			}
	
			if (logger.isDebugEnabled()) {
				String name = killer.has("name") ? killer.get("name") : killer.get("type");
	
				logger.debug(name + " did " + damageDone + " of " + totalDamageReceived + ". Reward was "
				        + xpReward);
			}
	
			int xpEarn = (int)(xpReward * ((float)damageDone / (float)totalDamageReceived));
	
			/** We limit xp gain for up to eight levels difference */
			/** XXX: Disabled. 
			double gainXpLimitation = 1 + ((oldLevel - killer.getLevel()) / (20.0));
			if (gainXpLimitation < 0.0) {
				gainXpLimitation = 0.0;
			} else if (gainXpLimitation > 1.0) {
				gainXpLimitation = 1.0;
			}*/
			
			double gainXpLimitation = 1;
	
			logger.debug("OnDead: " + xpReward + "\t" + damageDone + "\t" + totalDamageReceived + "\t"
			        + gainXpLimitation);
	
			int reward = (int) (xpEarn * gainXpLimitation);
	
			// We ensure that the player gets at least 1 experience
			// point, because getting nothing lowers motivation.
			if (reward == 0) {
				reward = 1;
			}
	
			killer.addXP(reward);
	
			// For some quests etc., it is required that the player kills a
			// certain creature without the help of others.
			// Find out if the player killed this RPEntity on his own, but
			// don't overwrite solo with shared.
			if (damageDone == totalDamageReceived) {
				killer.setKill(getName(), "solo");
			} else if (!killer.hasKilledSolo(getName())) {
				killer.setKill(getName(), "shared");
			}
			killer.notifyWorldAboutChanges();
		}
	}
	
	private void letAttackersStopAttack() {
		// a bit awkward, but we need to make sure there are no
		// concurrent modification exceptions.
		List<RPEntity> rpEntitiesThatAttacked = new LinkedList<RPEntity>();
		for (Entity attacker: attackSources) {
			if (attacker instanceof RPEntity) {
				rpEntitiesThatAttacked.add((RPEntity) attacker);
			}
		}
		for (RPEntity attacker: rpEntitiesThatAttacked) {
			 if (attacker.attackTarget == this) {
				attacker.stopAttack();
			}
		}
	}


	/**
	 * Get the normal movement speed.
	 *
	 * @return	The normal speed when moving.
	 */
	@Override
	public abstract double getBaseSpeed();


	/**
	 * This method is called when the entity has been killed ( hp==0 ).
	 * 
	 * @param killer
	 *            The entity who caused the death
	 */
	public void onDead(Entity killer) {
		onDead(killer, true);
	}


	/**
	 * This method is called when the entity has been killed ( hp==0 ).
	 * 
	 * @param	killerName	The killer's name (a phrase sutable
	 *				in the expression "<code>by</code>
	 *				<em>killerName</em>".
	 */
	public void onDead(String killerName) {
		onDead(killerName, true);
	}


	/**
	 * This method is called when this entity has been killed (hp == 0).
	 * 
	 * @param killer The entity who caused the death, i.e. who did the last hit.
	 * @param remove true iff this entity should be removed from the world. For
	 * 		  almost everything remove is true, but not for the players, who
	 *        are instead moved to afterlife ("reborn").
	 */
	public void onDead(Entity killer, boolean remove) {
		String killerName = killer.getTitle();

		if (killer instanceof RPEntity) {
			StendhalRPRuleProcessor.get().addGameEvent(killerName, "killed", getName());
		}

		onDead(killerName, remove);
	}


	/**
	 * This method is called when this entity has been killed (hp == 0).
	 * 
	 * @param	killerName	The killer's name (a phrase sutable
	 *				in the expression "<code>by</code>
	 *				<em>killerName</em>".
	 * @param	remove		<code>true</code> to remove entity
	 *				from world.
	 */
	protected void onDead(String killerName, boolean remove) {
		stopAttack();
		int oldLevel = this.getLevel();
		int oldXP = this.getXP();

		letAttackersStopAttack();

		// Establish how much xp points your are rewarded
		if (oldXP > 0) {
			// give XP to everyone who helped killing this RPEntity
			rewardKillers(oldXP, oldLevel);
		}

		damageReceived.clear();
		playersToReward.clear();
		totalDamageReceived = 0;

		// Stats about dead
		// TODO: Use getTitle() instead??
		if (has("name")) {
			stats.add("Killed " + get("name"), 1);
		} else {
			stats.add("Killed " + get("type"), 1);
		}

		// Add a corpse
		Corpse corpse = new Corpse(this, killerName);

		// Add some reward inside the corpse
		dropItemsOn(corpse);
		updateItemAtkDef();

		IRPZone zone = getZone();
		zone.assignRPObjectID(corpse);
		zone.add(corpse);

		if (remove) {
			StendhalRPWorld.get().remove(getID());
		}
	}

	abstract protected void dropItemsOn(Corpse corpse);

	/** Return true if this entity is attacked */
	public boolean isAttacked() {
		return !attackSources.isEmpty();
	}

	/** Return the Entities that are attacking this character */
	public List<Entity> getAttackSources() {
		return attackSources;
	}

	/** Return the RPEntities that are attacking this character */
	public List<RPEntity> getAttackingRPEntities() {
		List<RPEntity> list = new ArrayList<RPEntity>();

		for (Entity entity : getAttackSources()) {
			if (entity instanceof RPEntity) {
				list.add((RPEntity) entity);
			}
		}
		return list;
	}

	/** Return true if this entity is attacking */
	public boolean isAttacking() {
		return attackTarget != null;
	}

	/** Return the RPEntity that this entity is attacking. */
	public RPEntity getAttackTarget() {
		return attackTarget;
	}


	/***************************************************************************
	 * * Equipment handling. * *
	 **************************************************************************/

	/**
	 * Tries to equip an item in the appropriate slot.
	 *
	 * @param item the item
	 * @return true if the item can be equipped, else false
	 */
	public boolean equip(Item item) {
		return equip(item, false);
	}

	/**
	 * Tries to equip an item in the appropriate slot.
	 *
	 * @param item the item
	 * @param putOnGroundIfItCannotEquiped put it on ground if it cannot equiped.
	 * @return true if the item can be equipped, else false
	 */
	public boolean equip(Item item, boolean putOnGroundIfItCannotEquiped) {
		ActionManager manager = StendhalRPWorld.get().getRuleManager().getActionManager();

		String slot = manager.getSlotNameToEquip(this, item);
		if (slot != null) {
			return manager.onEquip(this, slot, item);
		}

		if (putOnGroundIfItCannotEquiped) {
			StendhalRPZone zone = getZone();
			zone.assignRPObjectID(item);
			item.setX(getX());
			item.setY(getY() + 1);
			zone.add(item);
			return true;
		}

		// we cannot equip this item
		return false;
	}

	/**
	 * Tries to equip one unit of an item in the given slot.
	 * Note: This doesn't check if it is allowed to put the given item into
	 * the given slot, e.g. it is possible to wear your helmet at your feet
	 * using this method.
	 * 
	 * @param slotName the name of the slot
	 * @param item the item
	 * @return true if the item can be equipped, else false
	 */
	public boolean equip(String slotName, Item item) {
		if (hasSlot(slotName)) {
			ActionManager manager = StendhalRPWorld.get().getRuleManager().getActionManager();
			if (manager.onEquip(this, slotName, item)) {
				updateItemAtkDef();
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes a specific amount of an item from the RPEntity. The item can
	 * either be stackable or non-stackable. The units can be distributed
	 * over different slots. If the RPEntity doesn't have enough units of
	 * the item, doesn't remove anything. 
	 * @param name The name of the item
	 * @param amount The number of units that should be dropped
	 * @return true iff dropping the desired amount was successful.
	 */
	public boolean drop(String name, int amount) {
		if (!isEquipped(name, amount)) {
			return false;
		}

		int toDrop = amount;

		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			Iterator<RPObject> objectsIterator = slot.iterator();
			while (objectsIterator.hasNext()) {
				RPObject object = objectsIterator.next();
				if (!(object instanceof Item)) {
					continue;
				}

				Item item = (Item) object;

				if (!item.getName().equals(name)) {
					continue;
				}

				if (item instanceof StackableItem) {
					// The item is stackable, we try to remove
					// multiple ones.
					int quantity = item.getQuantity();
					if (toDrop >= quantity) {
						slot.remove(item.getID());
						toDrop -= quantity;
						// Recreate the iterator to prevent
						// ConcurrentModificationExceptions.
						// This inefficient, but simple.
						objectsIterator = slot.iterator();
					} else {
						((StackableItem) item).setQuantity(quantity - toDrop);
						toDrop = 0;
					}
				} else {
					// The item is not stackable, so we only remove a
					// single one.
					slot.remove(item.getID());
					toDrop--;
					// recreate the iterator to prevent
					// ConcurrentModificationExceptions.
					objectsIterator = slot.iterator();
				}

				if (toDrop == 0) {
					updateItemAtkDef();
					notifyWorldAboutChanges();
					return true;
				}
			}
		}
		// This will never happen because we ran isEquipped() earlier.
		return false;
	}

	/**
	 * Removes one unit of an item from the RPEntity. The item can
	 * either be stackable or non-stackable. If the RPEntity doesn't
	 * have enough the item, doesn't remove anything.
	 * @param name The name of the item
	 * @return true iff dropping the item was successful.
	 */
	public boolean drop(String name) {
		return drop(name, 1);
	}

	/**
	 * Removes the given item from the RPEntity. The item can
	 * either be stackable or non-stackable. If the RPEntity doesn't
	 * have the item, doesn't remove anything.
	 * @param item the item that should be removed
	 * @return true iff dropping the item was successful.
	 */
	public boolean drop(Item item) {
		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			Iterator<RPObject> objectsIterator = slot.iterator();
			while (objectsIterator.hasNext()) {
				RPObject object = objectsIterator.next();
				if (object instanceof Item) {
					if (object == item) {
						slot.remove(object.getID());
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determine if this entity is equiped with a minimum quantity of
	 * an item.
	 *
	 * @param	name		The item name.
	 * @param	amount		The minimum amount.
	 *
	 * @return	<code>true</code> if the item is equiped with the
	 *		minimum number.
	 */
	public boolean isEquipped(String name, int amount) {
		if (amount <= 0) {
			return false;
		}
		int found = 0;

		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			for (RPObject object : slot) {
				if (!(object instanceof Item)) {
					continue;
				}

				Item item = (Item) object;

				if (item.getName().equals(name)) {
					found += item.getQuantity();

					if (found >= amount) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determine if this entity is equiped with an item.
	 *
	 * @param	name		The item name.
	 *
	 * @return	<code>true</code> if the item is equiped.
	 */
	public boolean isEquipped(String name) {
		return isEquipped(name, 1);
	}

	/**
	 * Gets the number of items of the given name that are carried by the
	 * RPEntity. The item can either be stackable or non-stackable.
	 * @param name The item's name
	 * @return The number of carried items
	 */
	public int getNumberOfEquipped(String name) {
		int result = 0;

		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			for (RPObject object : slot) {
				if (object instanceof Item) {
					Item item = (Item) object;
					if (item.getName().equals(name)) {
						result += item.getQuantity();
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets an item that is carried by the RPEntity.
	 * If the item is stackable, gets all that are on the first
	 * stack that is found. 
	 * @param name The item's name
	 * @return The item, or a stack of stackable items, or null if nothing
	 *         was found
	 */
	public Item getFirstEquipped(String name) {
		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			for (RPObject object : slot) {
				if (object instanceof Item) {
					Item item = (Item) object;
					if (item.getName().equals(name)) {
						return item;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets an item that is carried by the RPEntity.
	 * If the item is stackable, gets all that are on the first
	 * stack that is found. 
	 * @param name The item's name
	 * @return The item, or a stack of stackable items, or null if nothing
	 *         was found
	 */
	public List<Item> getAllEquipped(String name) {
		List<Item> result = new LinkedList<Item>();

		for (String slotName : CARRYING_SLOTS) {
			RPSlot slot = getSlot(slotName);

			for (RPObject object : slot) {
				if (object instanceof Item) {
					Item item = (Item) object;
					if (item.getName().equals(name)) {
						result.add(item);
					}
				}
			}
		}
		return result;
	}

	public Item dropItemClass(String[] slots, String clazz) {
		for (String slotName : slots) {
			RPSlot slot = getSlot(slotName);

			for (RPObject object : slot) {
				if (object instanceof Item) {
					Item item = (Item) object;
					if (item.isOfClass(clazz)) {
						slot.remove(item.getID());
						updateItemAtkDef();
						return item;
					}
				}
			}
		}

		return null;
	}

	/**
	 * checks if an item of class <i>clazz</i> is equipped in slot <i>slot</i>
	 * returns true if it is, else false
	 */
	public boolean isEquippedItemClass(String slot, String clazz) {
		if (hasSlot(slot)) {
			// get slot if the this entity has one
			RPSlot rpslot = getSlot(slot);
			// traverse all slot items
			for (RPObject item : rpslot) {
				if ((item instanceof Item) && ((Item) item).isOfClass(clazz)) {
					return true;
				}
			}
		}
		// no slot, free slot or wrong item type
		return false;
	}

	/**
	 * returns the first item of class <i>clazz</i> from the slot or
	 * <code>null</code> if there is no item with the requested clazz returns
	 * the item or null
	 */
	public Item getEquippedItemClass(String slot, String clazz) {
		if (hasSlot(slot)) {
			// get slot if the this entity has one
			RPSlot rpslot = getSlot(slot);
			// traverse all slot items
			for (RPObject object : rpslot) {
				// is it the right type
				if (object instanceof Item) {
					Item item = (Item) object;
					if (item.isOfClass(clazz)) {
						return item;
					}
				}
			}
		}
		// no slot, free slot or wrong item type
		return null;
	}

	/**
	 * Returns true if this entity is holding a weapon equipped in its hands.
	 */
	public boolean hasWeapon() {
		return getWeapon() != null;
	}

	/**
	 * Gets the weapon that this entity is holding in its hands.
	 * @return The weapon, or null if this entity is not holding a weapon. If
	 *         the entity has a weapon in each hand, returns the weapon in its
	 *         left hand. 
	 */
	private Item getWeapon() {
		String[] weaponsClasses = { "club", "sword", "axe", "ranged", "missile" };

		for (String weaponClass : weaponsClasses) {
			String[] slots = { "lhand", "rhand" };
			for (String slot : slots) {
				Item item = getEquippedItemClass(slot, weaponClass);
				if (item != null) {
					return item;
				}
			}
		}
		return null;
	}

	public List<Item> getWeapons() {
		List<Item> weapons = new ArrayList<Item>();
		Item weaponItem = getWeapon();
		if (weaponItem != null) {
			weapons.add(weaponItem);

			// pair weapons
			if (weaponItem.getName().startsWith("l_hand_")) {
				// check if there is a matching right-hand weapon in
				// the other hand.
				String rpclass = weaponItem.getItemClass();
				weaponItem = getEquippedItemClass("rhand", rpclass);
				if ((weaponItem != null) && (weaponItem.getName().startsWith("r_hand_"))) {
					weapons.add(weaponItem);
				} else {
					// You can't use a left-hand weapon without the matching
					// right-hand weapon. Hmmm... but why not?
					weapons.clear();
				}
			} else {
				// You can't hold a right-hand weapon with your left hand, for
				// ergonomic reasons ;)
				if (weaponItem.getName().startsWith("r_hand_")) {
					weapons.clear();
				}
			}
		}
		return weapons;
	}

	/**
	 * Gets the range weapon (bow etc.) that this entity is holding in its hands.
	 * @return The range weapon, or null if this entity is not holding a range
	 *         weapon. If the entity has a range weapon in each hand, returns one
	 *         in its left hand. 
	 */
	public Item getRangeWeapon() {
		for (Item weapon: getWeapons()) {
			if (weapon.isOfClass("ranged")) {
				return weapon;
			}
		}
		return null;
	}

	/**
	 * Gets the stack of ammunition (arrows or similar) that this entity is
	 * holding in its hands.
	 * @return The ammunition, or null if this entity is not holding
	 *         ammunition. If the entity has ammunition in each hand, returns
	 *         the ammunition in its left hand. 
	 */
	public StackableItem getAmmunition() {
		String[] slots = { "lhand", "rhand" };

		for (String slot : slots) {
			StackableItem item = (StackableItem) getEquippedItemClass(slot, "ammunition");
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Gets the stack of missiles (spears or similar) that this entity is
	 * holding in its hands, but only if it is not holding another, non-missile
	 * weapon in the other hand.
	 * 
	 * You can only throw missiles while you're not holding another weapon.
	 * This restriction is a workaround because of the way attack strength
	 * is determined; otherwise, one could increase one's spear attack strength
	 * by holding an ice sword in the other hand.
	 * 
	 * @return The missiles, or null if this entity is not holding
	 *         missiles. If the entity has missiles in each hand, returns
	 *         the missiles in its left hand. 
	 */
	public StackableItem getMissileIfNotHoldingOtherWeapon() {
		StackableItem missileWeaponItem = null;
		boolean holdsOtherWeapon = false;
		
		for (Item weaponItem: getWeapons()) {
			if (weaponItem.isOfClass("missile")) {
				missileWeaponItem = (StackableItem) weaponItem;
			} else {
				holdsOtherWeapon = true;
			}
		}
		if (holdsOtherWeapon) {
			return null;
		} else {
			return missileWeaponItem;
		}
	}
	
	/** returns true if the entity has a shield equipped */
	public boolean hasShield() {
		return isEquippedItemClass("lhand", "shield") || isEquippedItemClass("rhand", "shield");
	}

	public Item getShield() {
		Item item = getEquippedItemClass("lhand", "shield");
		if (item != null) {
			return item;
		} else {
			return getEquippedItemClass("rhand", "shield");
		}
	}

	public boolean hasArmor() {
		return isEquippedItemClass("armor", "armor");
	}

	public Item getArmor() {
		return getEquippedItemClass("armor", "armor");
	}

	public boolean hasHelmet() {
		return isEquippedItemClass("head", "helmet");
	}

	public Item getHelmet() {
		return getEquippedItemClass("head", "helmet");
	}

	public boolean hasLegs() {
		return isEquippedItemClass("legs", "legs");
	}

	public Item getLegs() {
		return getEquippedItemClass("legs", "legs");
	}

	public boolean hasBoots() {
		return isEquippedItemClass("feet", "boots");
	}

	public Item getBoots() {
		return getEquippedItemClass("feet", "boots");
	}

	public boolean hasCloak() {
		return isEquippedItemClass("cloak", "cloak");
	}

	public Item getCloak() {
		return getEquippedItemClass("cloak", "cloak");
	}

	@Override
	public String describe() {
		String text = super.describe();
		if (getLevel() > 0) {
			text += " It is level " + getLevel() + ".";
		}
		return text;
	}
	
	/**
	 * Sends a message that only this RPEntity can read.
	 * In this default implementation, this method does nothing; it can
	 * be overridden in subclasses.
	 * @param text The message.
	 */
	public void sendPrivateText(String text) {
		// does nothing in this implementation.
	}

	public float getItemAtk() {
		int weapon = 0;
		List<Item> weapons = getWeapons();
		for (Item weaponItem : weapons) {
			weapon += weaponItem.getAttack();
		}

		// range weapons
		StackableItem ammunitionItem = null;
		if (weapons.size() > 0) {
			if (weapons.get(0).isOfClass("ranged")) {
				ammunitionItem = getAmmunition();

				if (ammunitionItem != null) {
					weapon += ammunitionItem.getAttack();
				} else {
					// If there is no ammunition...
					weapon = 0;
				}
			}
		}
		return 4.0f * weapon;
	}
	
	public float getItemDef() {
		int shield = 0;
		int armor = 0;
		int helmet = 0;
		int legs = 0;
		int boots = 0;
		int cloak = 0;
		int weapon = 0;

		if (hasShield()) {
			shield = getShield().getDefense();
		}

		if (hasArmor()) {
			armor = getArmor().getDefense();
		}

		if (hasHelmet()) {
			helmet = getHelmet().getDefense();
		}

		if (hasLegs()) {
			legs = getLegs().getDefense();
		}

		if (hasBoots()) {
			boots = getBoots().getDefense();
		}

		if (hasCloak()) {
			cloak = getCloak().getDefense();
		}

		List<Item> targetWeapons = getWeapons();
		for (Item weaponItem : targetWeapons) {
			weapon += weaponItem.getDefense();
		}

		return 4.0f * shield + 2.0f * armor + 1.5f * cloak + 1.0f * helmet + 1.0f * legs + 1.0f * boots + 4.0f * weapon;
	}

	/**
	 * Recalculates item based atk and def.
	 */
	public void updateItemAtkDef() {
		put("atk_item", ((int) getItemAtk()));
		put("def_item", ((int) getItemDef()));
		notifyWorldAboutChanges();
	}

	/**
	 * Can this entity do a distance attack on the given target?
	 *
	 * @return true if this entity is armed with a distance weapon and if the
	 *         target is in range. 
	 */
	public boolean canDoRangeAttack(RPEntity target) {
		Item rangeWeapon = getRangeWeapon();
		StackableItem ammunition = getAmmunition();
		StackableItem missiles = getMissileIfNotHoldingOtherWeapon();
		int maxRange;
		if (rangeWeapon != null && ammunition != null && ammunition.getQuantity() > 0) {
			maxRange = rangeWeapon.getInt("range") + ammunition.getInt("range");
		} else if (missiles != null && missiles.getQuantity() > 0) {
			maxRange = missiles.getInt("range");
		} else {
			// The entity doesn't hold the necessary distance weapons. 
			return false;
		}
		return squaredDistance(target) >= 2 * 2
				&& squaredDistance(target) <= maxRange * maxRange;
	}

	/**
	 * Gets this RPEntity's outfit.
	 * 
	 * Note: some RPEntities (e.g. sheep, many NPC's, all monsters) don't
	 * use the outfit system.
	 * 
	 * @return The outfit, or null if this RPEntity is represented as a single
	 *         sprite rather than an outfit combination.
	 */
	public Outfit getOutfit() {
		if (has("outfit")) {
			return new Outfit(getInt("outfit"));
		}
		return null;
	}

	/**
	 * Sets this RPEntity's outfit.
	 * 
	 * Note: some RPEntities (e.g. sheep, many NPC's, all monsters) don't
	 * use the outfit system.
	 * 
	 * @param outfit The new outfit.
	 */
	public void setOutfit(Outfit outfit) {
		put("outfit", outfit.getCode());
	}


	//
	// Entity
	//

	/**
	 * Get the nicely formatted entity title/name.
	 *
	 * @return	The title, or <code>null</code> if unknown.
	 */
	@Override
	public String getTitle() {
		if (has("title")) {
			return get("title");
		} else if (name != null) {
			return name.replace('_', ' ');
		} else if (has("class")) {
			return get("class").replace('_', ' ');
		} else if (has("type")) {
			return get("type").replace('_', ' ');
		} else {
			return null;
		}
	}


	/**
	 * Perform cycle logic.
	 * TODO: Move up to Entity class eventually.
	 */
	public abstract void logic();
}
