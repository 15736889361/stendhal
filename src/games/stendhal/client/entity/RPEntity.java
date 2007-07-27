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
package games.stendhal.client.entity;

import games.stendhal.client.GameObjects;
import games.stendhal.client.GameScreen;
import games.stendhal.client.NotificationType;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.StendhalUI;
import games.stendhal.client.stendhal;
import games.stendhal.client.soundreview.SoundMaster;
import games.stendhal.common.Grammar;
import games.stendhal.common.Rand;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.RPObject;

/**
 * This class is a link between client graphical objects and server attributes
 * objects.<br>
 * You need to extend this object in order to add new elements to the game.
 */
public abstract class RPEntity extends ActiveEntity {
	/**
	 * Admin Level property.
	 */
	public final static Object	PROP_ADMIN_LEVEL	= new Object();

	/**
	 * Ghostmode property.
	 */
	public final static Object	PROP_GHOSTMODE		= new Object();

	/**
	 * Indicator text property.
	 */
	public final static Object	PROP_TEXT_INDICATORS	= new Object();

	/**
	 * Outfit property.
	 */
	public final static Object	PROP_OUTFIT	= new Object();

	/**
	 * Title Type property.
	 */
	public final static Object	PROP_TITLE_TYPE	= new Object();

	/**
	 * The value of an outfit that isn't set.
	 */
	public static final int	OUTFIT_UNSET	= -1;

	private boolean showBladeStrike;

	String[] attackSounds={
		"punch-1.wav","punch-2.wav","punch-3.wav"
		,"punch-4.wav","punch-5.wav","punch-6.wav",
		"swingaxe-1.wav","slap-1.wav","arrow-1.wav"};

	public enum Resolution {
		HIT(0), BLOCKED(1), MISSED(2);

		private final int val;

		Resolution(final int val) {
			this.val = val;
		}

		public int get() {
			return val;
		}
	};

	private int atk;

	private int def;

	private int xp;

	private int hp;

	private int adminlevel;

	/**
	 * The outfit code.
	 */
	private int	outfit;

	private int base_hp;

	private float hp_base_hp;

	private int level;

	private boolean eating;

	private boolean poisoned;

	private long combatIconTime;

	private List<TextIndicator> textIndicators;

	private RPObject.ID attacking;

	private int mana;

	private int base_mana;

	private boolean ghostmode;

	private String guild;

	private String titleType;

	/**
	 * Entity we are attacking.
	 * (need to reconsile this with 'attacking')
	 */
	protected RPEntity attackTarget;

	/**
	 * The last entity to attack this entity.
	 */
	protected Entity lastAttacker;

	/**
	 * The type of effect to show.
	 *
	 * These are NOT mutually exclusive
	 * - Maybe use bitmask and apply in priority order.
	 */
	private Resolution resolution;

	private int atkXp;

	private int defXp;

	private int atkItem = -1;

	private int defItem = -1;



	/** Create a new game entity */
	RPEntity()  {
		textIndicators = new LinkedList<TextIndicator>();
		attackTarget = null;
		lastAttacker = null;
	}


	//
	// RPEntity
	//

	/**
	 * Create/add a text indicator message.
	 *
	 * @param	text		The text message.
	 * @param	type		The indicator type.
	 */
	protected void addTextIndicator(final String text, final NotificationType type) {
		textIndicators.add(new TextIndicator(text, type));
		fireChange(PROP_TEXT_INDICATORS);
	}


	/**
	 * Get the admin level.
	 *
	 * @return	The admin level.
	 */
	public int getAdminLevel() {
		return adminlevel;
	}

	/**
	 * @return Returns the atk.
	 */
	public int getAtk() {
		return atk;
	}

	/**
	 * @return Returns the atk of items
	 */
	public int getAtkItem() {
		return atkItem;
	}

	/**
	 * @return the attack xp
	 */
	public int getAtkXp() {
		return atkXp;
	}

	/**
	 * @return Returns the base_hp.
	 */
	public int getBase_hp() {
		return base_hp;
	}

	/**
	 * @return Returns the base mana value
	 */
	public int getBaseMana() {
		return base_mana;
	}

	/**
	 * @return Returns the def.
	 */
	public int getDef() {
		return def;
	}

	/**
	 * @return Returns the def of items
	 */
	public int getDefItem() {
		return defItem;
	}

	/**
	 * @return the defence xp
	 */
	public int getDefXp() {
		return defXp;
	}

	public String getGuild() {
	    return guild;
	}

	public int getHP() {
		return hp;
	}

	/**
	 * Get the ratio of HP to base HP.
	 *
	 * @return	The HP ratio (0.0 - 1.0).
	 */
	public float getHPRatio() {
		return hp_base_hp;
	}


	/**
	 * Get the list of text indicator elements.
	 *
	 * @return	An iterator of text indicators.
	 */
	public Iterator<TextIndicator> getTextIndicators() {
		return textIndicators.iterator();
	}


	public int getLevel() {
		return level;
	}

	/**
	 * @return Returns the total mana of a player
	 */
	public int getMana() {
		return mana;
	}

	/**
	 * Get the outfit code.
	 *
	 * @return	The outfit code.
	 */
	public int getOutfit() {
		return outfit;
	}

	public Resolution getResolution() {
		return resolution;
	}

	/**
	 * Get the nicely formatted entity title.
	 *
	 * This searches the follow attribute order:
	 *	title, name (w/o underscore), class (w/o underscore), type (w/o underscore).
	 *
	 * @return	The title, or <code>null</code> if unknown.
	 */
	@Override
	public String getTitle() {
		if(title != null) {
			return title;
		} else if(name != null) {
			return name.replace('_', ' ');
		} else if(clazz != null) {
			return clazz.replace('_', ' ');
		} else if(type != null) {
			return type.replace('_', ' ');
		} else {
			return null;
		}
	}


	/**
	 * Get title type.
	 *
	 * @return	The title type.
	 */
	public String getTitleType() {
		return titleType;
	}


	/**
	 * @return Returns the xp.
	 */
	public int getXp() {
		return xp;
	}


	public boolean isAttacking() {
		return (attacking != null);
	}

	public boolean isAttackingUser() {
		return ((attacking != null)
			&& attacking.equals(User.get().getID()));
	}

	public boolean isBeingAttacked() {
		return (lastAttacker != null);
	}

	public boolean isBeingStruck() {
		return showBladeStrike;
	}

	public void doneStriking() {
		showBladeStrike = false;
	}

	public boolean isDefending() {
                return (isBeingAttacked()
			&& (System.currentTimeMillis() - combatIconTime < 4 * 300));
	}

	public boolean isEating() {
		return eating;
	}

	/**
	 * Determine if in full ghostmode.
	 *
	 * @return	<code>true</code> is in full ghostmode.
	 */
	public boolean isGhostMode() {
		return ghostmode;
	}

	public boolean isPoisoned() {
		return poisoned;
	}



	// TODO: this is just an ugly workaround to avoid cyclic dependencies with
	// Creature
	protected void nonCreatureClientAddEventLine(final String text) {
		StendhalUI.get().addEventLine(getTitle(), text);
	}


	// When this entity attacks target.
	public void onAttack(final Entity target) {
		attacking = target.getID();
	}

	// When this entity's attack is blocked by the adversary
	public void onAttackBlocked(final Entity target) {
		showBladeStrike = true;
	}

	// When this entity causes damaged to adversary, with damage amount
	public void onAttackDamage(final Entity target, final int damage) {
		showBladeStrike = true;
	}

	// When this entity's attack is missing the adversary
	public void onAttackMissed(final Entity target) {
		showBladeStrike = true;
	}

	// When attacker attacks this entity.
	public void onAttacked(final Entity attacker) {
		/*
		 * Could keep track of all attackers, but right now we only
		 * need one of them for onDeath() sake
		 */
		lastAttacker = attacker;
	}

	// When this entity blocks the attack by attacker
	public void onBlocked(final Entity attacker) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.BLOCKED;
	}

	// When this entity is damaged by attacker with damage amount
	public void onDamaged(final Entity attacker, final int damage) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.HIT;
		 try{

			    SoundMaster.play(attackSounds[Rand.rand(attackSounds.length)], x, y);
			}
			catch(NullPointerException e){

			}

		//playSound("punch-mix", 20, 60, 80);

		boolean showAttackInfoForPlayer = (!User.isNull())
		        && (this.equals(User.get()) || attacker.equals(User.get()));
		showAttackInfoForPlayer = showAttackInfoForPlayer & (!stendhal.FILTER_ATTACK_MESSAGES);

		if (stendhal.SHOW_EVERYONE_ATTACK_INFO || showAttackInfoForPlayer) {
			StendhalUI.get().addEventLine(
				getTitle() + " suffers "
				+ Grammar.quantityplnoun(damage, "point")
				+ " of damage from " + attacker.getTitle(),
				NotificationType.NEGATIVE);
		}
	}

	// Called when entity is killed by killer
	public void onDeath(final Entity killer) {
		if (killer != null) {
			StendhalUI.get().addEventLine(getTitle() + " has been killed by " + killer.getTitle());
		}

		/*
		 * see
		 * http://sourceforge.net/tracker/index.php?func=detail&aid=1554077&group_id=1111&atid=101111
		 * if (getID().equals(client.getPlayer().getID())) {
		 * client.addEventLine(getTitle() + " has died. " +
		 * Grammar.suffix_s(getTitle()) + " new level is " + getLevel()); }
		 */
	}

	// When entity eats food
	public void onEat(final int amount) {
		eating = true;
	}

	public void onEatEnd() {
		eating = false;
	}

	// When entity gets healed
	public void onHealed(final int amount) {
	}

	// When entity adjusts HP
	public void onHPChange(final int amount) {
		if (distanceToUser() < 15 * 15) {
			if(amount > 0) {
				addTextIndicator("+" + amount, NotificationType.POSITIVE);
			} else {
				addTextIndicator(String.valueOf(amount), NotificationType.NEGATIVE);
			}
		}
	}

	// Called when entity kills another entity
	public void onKill(final Entity killed) {
	}

	// When this entity skip attacker's attack.
	public void onMissed(final Entity attacker) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.MISSED;
	}

	// When entity is poisoned
	public final void onPoisoned(final int amount) {
		if ((distanceToUser() < 15 * 15)) {
			poisoned = true;

			StendhalUI.get().addEventLine(
			        getTitle() + " is poisoned, losing " + Grammar.quantityplnoun(amount, "health point") + ".",
				NotificationType.NEGATIVE);
		}
	}

	public void onPoisonEnd() {
		poisoned = false;
	}

	// Called when entity listen to text from talker
	public void onPrivateListen(final String text) {
		NotificationType type;


		// TODO: replace this with its own RPEvent type after port to Marauroa 2.0
		if (text.startsWith("Tutorial: ")) {
			type = NotificationType.TUTORIAL;
		} else {
			type = NotificationType.PRIVMSG;
		}

		StendhalClient.get().generateWhoPlayers(text);
		StendhalUI.get().addEventLine(text, type);
		GameScreen.get().addText(getX(), getY(), text.replace("|", ""), type, false);
	}

	// When this entity stops attacking
	public void onStopAttack() {
		attacking = null;
	}

	// When attacket stop attacking us
	public void onStopAttacked(final Entity attacker) {
		if (attacker == lastAttacker) {
			lastAttacker = null;
		}
	}

	// Called when entity says text
	public void onTalk(final String text) {
		if (User.isAdmin() || distanceToUser() < 15 * 15) {
			// TODO: Creature circle reference
			nonCreatureClientAddEventLine(text);

			String line = text.replace("|", "");

			// Allow for more characters and cut the text if possible at the
			// nearest space etc. intensifly@gmx.com
			if (line.length() > 84) {
				line = line.substring(0, 84);
				int l = line.lastIndexOf(" ");
				int ln = line.lastIndexOf("-");

				if (ln > l) {
					l = ln;
				}

				ln = line.lastIndexOf(".");

				if (ln > l) {
					l = ln;
				}

				ln = line.lastIndexOf(",");

				if (ln > l) {
					l = ln;
				}

				if (l > 0) {
					line = line.substring(0, l);
				}

				line = line + " ...";
			}

			GameScreen.get().addText(
				getX(), getY(), line, NotificationType.NORMAL, true);
		}
	}


	//
	// Entity
	//

	/**
	 * Initialize this entity for an object.
	 *
	 * @param	object		The object.
	 *
	 * @see-also	#release()
	 */
	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);

		/*
		 * Base HP
		 */
		if (object.has("base_hp")) {
			base_hp = object.getInt("base_hp");
		} else {
			base_hp = 0;
		}

		/*
		 * HP
		 */
		if (object.has("hp")) {
			hp = object.getInt("hp");
		} else {
			hp = 0;
		}

		/*
		 * HP ratio
		 */
		if (hp >= base_hp) {
			hp_base_hp = 1.0f;
		} else if (hp <= 0) {
			hp_base_hp = 0.0f;
		} else {
			hp_base_hp = hp / (float) base_hp;
		}

		/*
		 * Public chat
		 */
		if (object.has("text")) {
			onTalk(object.get("text"));
		}

		/*
		 * Private message
		 */
		if (object.has("private_text")) {
			onPrivateListen(object.get("private_text"));
		}

		/*
		 * Outfit
		 */
		if (object.has("outfit")) {
			outfit = object.getInt("outfit");
		} else {
			outfit = OUTFIT_UNSET;
		}

		/*
		 * Eating
		 */
		if (object.has("eating")) {
			onEat(0);
		}

		/*
		 * Poisoned
		 */
		if (object.has("poisoned")) {
			// To remove the - sign on poison.
			onPoisoned(Math.abs(object.getInt("poisoned")));
		}

		/*
		 * Ghost mode feature.
		 */
		if (object.has("ghostmode")) {
		    ghostmode = true;
		}

		/*
		 * Healed
		 */
		if (object.has("heal")) {
			onHealed(object.getInt("heal"));
		}

		/*
		 * Attack Target
		 */
		if (object.has("target")) {
			int target = object.getInt("target");

			RPObject.ID targetEntityID = new RPObject.ID(target, object.get("zoneid"));

			/*
			 * XXX - This is probably meaningless, as create order
			 * is unpredictable, and the target entity may not
			 * have been added yet
			 */
			attackTarget = (RPEntity) GameObjects.getInstance().get(targetEntityID);

			if (attackTarget != null) {
				onAttack(attackTarget);
				attackTarget.onAttacked(this);
				//attackTarget.onAttacked(this,risk,damage);
			}
		} else {
			attackTarget = null;
		}

		if(attackTarget != null) {
			int risk;
			int damage;

			if(object.has("risk")) {
				risk = object.getInt("risk");
			} else {
				risk = 0;
			}

			if(object.has("damage")) {
				damage = object.getInt("damage");
			} else {
				damage = 0;
			}

			if (risk == 0) {
				onAttackMissed(attackTarget);
				attackTarget.onMissed(this);
			} else if ((risk > 0) && (damage == 0)) {
				onAttackBlocked(attackTarget);
				attackTarget.onBlocked(this);
			} else if ((risk > 0) && (damage > 0)) {
				onAttackDamage(attackTarget, damage);
				attackTarget.onDamaged(this, damage);
			}
		}

		/*
		 * Admin level
		 */
		if (object.has("adminlevel")) {
			adminlevel = object.getInt("adminlevel");
		} else {
			adminlevel = 0;
		}

		/*
		 * Title type
		 */
		if (object.has("title_type")) {
			titleType = object.get("title_type");
		} else {
			titleType = null;
		}
	}


	/**
	 * Release this entity. This should clean anything that isn't
	 * automatically released (such as unregister callbacks, cancel
	 * external operations, etc).
	 *
	 * @see-also	#initialize(RPObject)
	 */
	@Override
	public void release() {
		onStopAttack();

		if (attackTarget != null) {
			attackTarget.onStopAttacked(this);
			attackTarget = null;
		}

		super.release();
	}


	/**
	 * Update cycle.
	 *
	 * @param	delta		The time (in ms) since last call.
	 */
	@Override
	public void update(final int delta) {
		super.update(delta);

		if(!textIndicators.isEmpty()) {
			Iterator<TextIndicator> iter = textIndicators.iterator();

			while(iter.hasNext()) {
				TextIndicator textIndicator = iter.next();

				if(textIndicator.addAge(delta) > 2000L) {
					iter.remove();
				}
			}

			fireChange(PROP_TEXT_INDICATORS);
		}
	}


	//
	// RPObjectChangeListener
	//

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (!inAdd) {
			/*
			 * Public chat
			 */
			if (changes.has("text")) {
				onTalk(changes.get("text"));
			}

			/*
			 * Private message
			 */
			if (changes.has("private_text")) {
				onPrivateListen(changes.get("private_text"));
			}

			/*
			 * Outfit
			 */
			if (changes.has("outfit")) {
				outfit = changes.getInt("outfit");
				fireChange(PROP_OUTFIT);
			}

			/*
			 * Eating
			 */
			if (changes.has("eating")) {
				onEat(0);
			}

			/*
			 * Poisoned
			 */
			if (changes.has("poisoned")) {
				// To remove the - sign on poison.
				onPoisoned(Math.abs(changes.getInt("poisoned")));
			}

			/*
			 * Healed
			 */
			if (changes.has("heal")) {
				onHealed(changes.getInt("heal"));
			}


			boolean hpRatioChange = false;

			/*
			 * Base HP
			 */
			if (changes.has("base_hp")) {
				base_hp = changes.getInt("base_hp");
				hpRatioChange = true;
			}

			/*
			 * HP
			 */
			if (changes.has("hp")) {
				int newHP = changes.getInt("hp");
				int change = newHP - hp;

				hp = newHP;

				// TODO: Remove has() check hack
				if (object.has("hp") && (change != 0)) {
					onHPChange(change);
				}

				hpRatioChange = true;
			}

			/*
			 * HP ratio
			 */
			if(hpRatioChange) {
				if (hp >= base_hp) {
					hp_base_hp = 1.0f;
				} else if (hp <= 0) {
					hp_base_hp = 0.0f;
				} else {
					hp_base_hp = hp / (float) base_hp;
				}

				if(hp == 0) {
					onDeath(lastAttacker);
				}
			}


			/*
			 * Attack Target
			 */
			if (changes.has("target")) {
				int target = changes.getInt("target");

				RPObject.ID targetEntityID = new RPObject.ID(target, changes.get("zoneid"));

				RPEntity targetEntity = (RPEntity) GameObjects.getInstance().get(targetEntityID);

				if (targetEntity != attackTarget) {
					onStopAttack();

					if (attackTarget != null) {
						attackTarget.onStopAttacked(this);
					}

					attackTarget = targetEntity;

					if(attackTarget != null) {
						onAttack(attackTarget);
						attackTarget.onAttacked(this);
						//attackTarget.onAttacked(this,risk,damage);
					}
				}
			}

			if(attackTarget != null) {
				int risk;
				int damage;

				boolean thereIsEvent = false;

				if(changes.has("risk")) {
					risk = changes.getInt("risk");
					thereIsEvent = true;
				} else if (object.has("risk")) {
					risk = object.getInt("risk");
				} else {
					risk = 0;
				}

				if(changes.has("damage")) {
					damage = changes.getInt("damage");
					thereIsEvent = true;
				} else if (object.has("damage")) {
					damage = object.getInt("damage");
				} else {
					damage = 0;
				}

				if(thereIsEvent) {
					if (risk == 0) {
						onAttackMissed(attackTarget);
						attackTarget.onMissed(this);
					} else if ((risk > 0) && (damage == 0)) {
						onAttackBlocked(attackTarget);
						attackTarget.onBlocked(this);
					} else if ((risk > 0) && (damage > 0)) {
						onAttackDamage(attackTarget, damage);
						attackTarget.onDamaged(this, damage);
					}
				}
			}

			/*
			 * Admin level
			 */
			if (changes.has("adminlevel")) {
				adminlevel = changes.getInt("adminlevel");
				fireChange(PROP_ADMIN_LEVEL);
			}

			/*
			 * Title type
			 */
			if (changes.has("title_type")) {
				titleType = changes.get("title_type");
				fireChange(PROP_TITLE_TYPE);
			}

			/*
			 * Title
			 */
			if (changes.has("class") || changes.has("name") || changes.has("title") || changes.has("type")) {
				fireChange(PROP_TITLE);
			}
		}

		if (changes.has("atk")) {
			atk = changes.getInt("atk");
		}

		if (changes.has("def")) {
			def = changes.getInt("def");
		}

		if (changes.has("xp")) {
			xp = changes.getInt("xp");
		}

		if (changes.has("level")) {
			level = changes.getInt("level");
		}

		if (changes.has("atk_xp")) {
			atkXp = changes.getInt("atk_xp");
		}

		if (changes.has("def_xp")) {
			defXp = changes.getInt("def_xp");
		}

		if (changes.has("atk_item")) {
			atkItem = changes.getInt("atk_item");
		}

		if (changes.has("def_item")) {
			defItem = changes.getInt("def_item");
		}

		if (changes.has("mana")) {
			mana = changes.getInt("mana");
		}

		if (changes.has("base_mana")) {
			base_mana = changes.getInt("base_mana");
		}

		if (changes.has("ghostmode")) {
			ghostmode = true;
			fireChange(PROP_GHOSTMODE);
		}

		if (changes.has("guild")) {
		    guild = changes.get("guild");
		}

		if (changes.has("xp") && object.has("xp")) {
			if (distanceToUser() < 15 * 15) {
				int amount=(changes.getInt("xp") - object.getInt("xp"));
				if(amount>0) {
					addTextIndicator("+" + amount, NotificationType.SIGNIFICANT_POSITIVE);

					StendhalUI.get().addEventLine( getTitle() + " earns "
							+ Grammar.quantityplnoun(amount, "experience point")
							+ ".", NotificationType.SIGNIFICANT_POSITIVE);
				} else if(amount<0) {
					addTextIndicator(""+amount, NotificationType.SIGNIFICANT_NEGATIVE);

					StendhalUI.get().addEventLine( getTitle() + " loses "
							+ Grammar.quantityplnoun(amount, "experience point")
							+ ".", NotificationType.SIGNIFICANT_NEGATIVE);
				}
			}
		}

		if (changes.has("level") && object.has("level")) {
			if (distanceToUser() < 15 * 15) {
				String text = getTitle() + " reaches Level " + getLevel();

				GameScreen.get().addText(getX(), getY(), GameScreen.get().createString(text, NotificationType.SIGNIFICANT_POSITIVE), 0);
				StendhalUI.get().addEventLine(text, NotificationType.SIGNIFICANT_POSITIVE);
			}
		}
	}


	/**
	 * The object removed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		super.onChangedRemoved(object, changes);

		/*
		 * Outfit
		 */
		if (changes.has("outfit")) {
			outfit = OUTFIT_UNSET;
			fireChange(PROP_OUTFIT);
		}

		/*
		 * No longer poisoned?
		 */
		if (changes.has("poisoned")) {
			onPoisonEnd();
		}

		/*
		 * No longer eating?
		 */
		if (changes.has("eating")) {
			onEatEnd();
		}

		if (changes.has("ghostmode")) {
			ghostmode = false;
			fireChange(PROP_GHOSTMODE);
		}

		/*
		 * Attack target gone?
		 */
		if (changes.has("target")) {
			onStopAttack();

			if (attackTarget != null) {
				attackTarget.onStopAttacked(this);
				attackTarget = null;
			}
		}
	}

	//
	//

	public static class TextIndicator {
		/**
		 * The age of the message (in ms).
		 */
		protected int		age;

		/**
		 * The message text.
		 */
		protected String	text;

		/**
		 * The indicator type.
		 */
		protected NotificationType	type;


		/**
		 * Create a floating message.
		 *
		 * @param	text		The text to drawn.
		 * @param	type		The indicator type.
		 */
		public TextIndicator(final String text, final NotificationType type) {
			this.text = text;
			this.type = type;

			age = 0;
		}


		//
		// TextIndicator
		//

		/**
		 * Add to the age of this message.
		 *
		 * @param	time		The amout to add.
		 *
		 * @return	The new age (in milliseconds).
		 */
		public int addAge(final int time) {
			age += time;

			return age;
		}


		/**
		 * Get the age of this message.
		 *
		 * @return	The age (in milliseconds).
		 */
		public int getAge() {
			return age;
		}


		/**
		 * Get the text message.
		 *
		 * @return	The text message.
		 */
		public String getText() {
			return text;
		}


		/**
		 * Get the indicator type.
		 *
		 * @return	The indicator type.
		 */
		public NotificationType getType() {
			return type;
		}
	}
}
