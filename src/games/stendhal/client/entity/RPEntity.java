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
import games.stendhal.client.Sprite;
import games.stendhal.client.SpriteStore;
import games.stendhal.client.StendhalUI;
import games.stendhal.client.stendhal;
import games.stendhal.common.Grammar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/**
 * This class is a link between client graphical objects and server attributes
 * objects.<br>
 * You need to extend this object in order to add new elements to the game.
 */
public abstract class RPEntity extends AnimatedEntity {


	private boolean showBladeStrike;
	
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

	private int base_hp;

	private float hp_base_hp;

	private int level;

	private boolean eating;

	private boolean poisoned;

	private Sprite nameImage;

	private long combatIconTime;

	private java.util.List<Sprite> damageSprites;

	private java.util.List<Long> damageSpritesTimes;

	private RPObject.ID attacking;

	private int mana;

	private int base_mana;

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
		super();
		damageSprites = new LinkedList<Sprite>();
		damageSpritesTimes = new LinkedList<Long>();
		attackTarget = null;
		lastAttacker = null;
	}

	/**
	 * Create/add a "floater" message.
	 *
	 * @param	text		The text message.
	 * @param	color		The color of the text.
	 */
	protected  void addFloater(final String text, final Color color) {
		damageSprites.add(GameScreen.get().createString(text, color));

		damageSpritesTimes.add(Long.valueOf(System.currentTimeMillis()));
	}

	public boolean isAttacking() {
		return (attacking != null);
	}

	protected void setName(final String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public int getHP() {
		return hp;
	}

	/**
	 * Gets the outfit sprite for the given object.
	 * 
	 * The outfit is described by the object's "outfit" attribute. It is an
	 * 8-digit integer of the form RRHHDDBB where RR is the number of the hair
	 * graphics, HH for the head, DD for the dress, and BB for the base.
	 * 
	 * @param store
	 *            The sprite store
	 * @param object
	 *            The object. The outfit attribute needs to be set.
	 * @return A sprite for the object
	 */
	protected Sprite getOutfitSprite(final SpriteStore store, final RPObject object) {
		int outfit = object.getInt("outfit");

		Sprite sprite = store.getSprite("data/sprites/outfit/player_base_" + outfit % 100 + ".png");
		sprite = sprite.copy();
		outfit /= 100;
		if (outfit % 100 != 0) {
			int dressIdx = outfit % 100;
			Sprite dress = store.getSprite("data/sprites/outfit/dress_" + dressIdx + ".png");
			dress.draw(sprite.getGraphics(), 0, 0);
		}
		outfit /= 100;

		Sprite head = store.getSprite("data/sprites/outfit/head_" + outfit % 100 + ".png");
		head.draw(sprite.getGraphics(), 0, 0);
		outfit /= 100;

		if (outfit % 100 != 0) {
			Sprite hair = store.getSprite("data/sprites/outfit/hair_" + outfit % 100 + ".png");
			hair.draw(sprite.getGraphics(), 0, 0);
		}

		return sprite;
	}

	@Override
	protected void buildAnimations(final RPObject object) {
		SpriteStore store = SpriteStore.get();

		sprites.put("move_up", store.getAnimatedSprite(translate(object.get("type")), 0, 4, 1.5, 2));
		sprites.put("move_right", store.getAnimatedSprite(translate(object.get("type")), 1, 4, 1.5, 2));
		sprites.put("move_down", store.getAnimatedSprite(translate(object.get("type")), 2, 4, 1.5, 2));
		sprites.put("move_left", store.getAnimatedSprite(translate(object.get("type")), 3, 4, 1.5, 2));

		sprites.get("move_up")[3] = sprites.get("move_up")[1];
		sprites.get("move_right")[3] = sprites.get("move_right")[1];
		sprites.get("move_down")[3] = sprites.get("move_down")[1];
		sprites.get("move_left")[3] = sprites.get("move_left")[1];
	}

	@Override
	protected Sprite defaultAnimation() {
		animation = "move_up";
		return sprites.get("move_up")[0];
	}

	// Called when entity says text
	public void onTalk(final String text) {
		if (!User.isNull() && (distance(User.get()) < 15 * 15)) {
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
			GameObjects.getInstance().addText(this, /* getName()+" says: "+ */
			line, Color.black, true);
		}
	}

	

	// TODO: this is just an ugly workaround to avoid cyclic dependencies with
	// Creature
	protected void nonCreatureClientAddEventLine(final String text) {
		StendhalUI.get().addEventLine(getName(), text);
	}

	// Called when entity listen to text from talker
	public  void onPrivateListen(final String text) {
		Color color = Color.darkGray;
		// TODO: replace this with its own RPEvent type after port to Marauroa 2.0
		if (text.startsWith("Tutorial: ")) {
			color = new Color(172, 0, 172);
		}
		StendhalUI.get().addEventLine(text, color);
		GameObjects.getInstance().addText(this, text.replace("|", ""), color, false);
	}

	// When entity gets healed
	public void onHealed(final int amount) {
		if (distance(User.get()) < 15 * 15) {
			addFloater("+" + amount, Color.green);

			
		}
	}

	// When entity eats food
	public void onEat(final int amount) {
		eating = true;
	}

	public void onEatEnd() {
		eating = false;
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

	public boolean isPoisoned() {
		return poisoned;
	}

	public boolean isAttackingUser() {
		return ((attacking != null)
			&& attacking.equals(User.get().getID()));

	}

	public boolean isBeingAttacked() {
		return (lastAttacker != null);
	}

	public Resolution getResolution() {
		return resolution;
	}


	// When entity is poisoned
	public final void onPoisoned(final int amount) {
		if ((distance(User.get()) < 15 * 15)) {
			poisoned = true;

			addFloater("-" + amount, Color.red);

			StendhalUI.get().addEventLine(
			        getName() + " is poisoned, losing " + Grammar.quantityplnoun(amount, "health point") + ".",
			        Color.red);
		}
	}

	public void onPoisonEnd() {
		poisoned = false;
	}

	// Called when entity kills another entity
	public void onKill(final Entity killed) {
	}

	// Called when entity is killed by killer
	public void onDeath(final Entity killer) {
		if (killer != null) {
			StendhalUI.get().addEventLine(getName() + " has been killed by " + killer.getName());
		}

		/*
		 * see
		 * http://sourceforge.net/tracker/index.php?func=detail&aid=1554077&group_id=1111&atid=101111
		 * if (getID().equals(client.getPlayer().getID())) {
		 * client.addEventLine(getName() + " has died. " +
		 * Grammar.suffix_s(getName()) + " new level is " + getLevel()); }
		 */
	}

	/**
	 * Called when object is added.
	 *
	 *
	 */
	@Override
	public void onAdded(final RPObject base) {
		super.onAdded(base);

		fireTalkEvent(base, null);
		fireHPEvent(base, null);
		fireKillEvent(base, null);
		fireAttackEvent(base, null);
	}

	/**
	 * Called when object is removed.
	 */
	@Override
	public void onRemoved() {
		super.onRemoved();

		fireTalkEvent(null, null);
		fireHPEvent(null, null);
		fireKillEvent(null, null);
		fireAttackEvent(null, null);
	}

	@Override
	public void onChangedAdded(final RPObject base, final RPObject diff) throws AttributeNotFoundException {
		super.onChangedAdded(base, diff);

		if (!inAdd) {
			fireTalkEvent(base, diff);
			fireHPEvent(base, diff);
			fireKillEvent(base, diff);
			fireAttackEvent(base, diff);
		}

		if (diff.has("base_hp")) {
			base_hp = diff.getInt("base_hp");
		}
		if (diff.has("hp")) {
			hp = diff.getInt("hp");
		}
		if (diff.has("hp/base_hp")) {
			hp_base_hp = (float) diff.getDouble("hp/base_hp");
		}
		if (diff.has("atk")) {
			atk = diff.getInt("atk");
		}
		if (diff.has("def")) {
			def = diff.getInt("def");
		}
		if (diff.has("xp")) {
			xp = diff.getInt("xp");
		}
		if (diff.has("level")) {
			level = diff.getInt("level");
		}
		if (diff.has("atk_xp")) {
			atkXp = diff.getInt("atk_xp");
		}
		if (diff.has("def_xp")) {
			defXp = diff.getInt("def_xp");
		}
		if (diff.has("atk_item")) {
			atkItem = diff.getInt("atk_item");
		}
		if (diff.has("def_item")) {
			defItem = diff.getInt("def_item");
		}
		if (diff.has("mana")) {
			mana = diff.getInt("mana");
		}
		if (diff.has("base_mana")) {
			base_mana = diff.getInt("base_mana");
		}

		Color nameColor = Color.white;

		if (diff.has("adminlevel")) {
			int adminlevel = diff.getInt("adminlevel");
			if (adminlevel >= 800) {
				nameColor = new Color(200, 200, 0);
			} else if (adminlevel >= 400) {
				nameColor = new Color(255, 255, 0);
			} else if (adminlevel > 0) {
				nameColor = new Color(255, 255, 172);
			}
		}

		String titleType = null;
		if (base.has("title_type")) {
			titleType = base.get("title_type");
		}
		if (diff.has("title_type")) {
			titleType = diff.get("title_type");
		}
		if (titleType != null) {
			if (titleType.equals("npc")) {
				nameColor = new Color(200, 200, 255);
			} else if (titleType.equals("enemy")) {
				nameColor = new Color(255, 200, 200);
			}
		}

		if (diff.has("name")) {
			name = diff.get("name");
			name = name.replace("_", " ");
			nameImage = GameScreen.get().createString(getName(), nameColor);
		} else if ((name == null) && diff.has("class")) {
			name = diff.get("class");
			name = name.replace("_", " ");
			nameImage = GameScreen.get().createString(getName(), nameColor);
		} else if ((name == null) && diff.has("type")) {
			name = diff.get("type");
			name = name.replace("_", " ");
			nameImage = GameScreen.get().createString(getName(), nameColor);
		}

		if (diff.has("xp") && base.has("xp")) {
			if (distance(User.get()) < 15 * 15) {
				addFloater("+" + (diff.getInt("xp") - base.getInt("xp")), Color.cyan);

				StendhalUI.get().addEventLine(
				        getName() + " earns "
				                + Grammar.quantityplnoun(diff.getInt("xp") - base.getInt("xp"), "experience point")
				                + ".", Color.blue);
			}
		}

		if (diff.has("level") && base.has("level")) {
			if (distance(User.get()) < 15 * 15) {
				String text = getName() + " reaches Level " + getLevel();

				GameObjects.getInstance().addText(this, GameScreen.get().createString(text, Color.green), 0);
				StendhalUI.get().addEventLine(text, Color.green);
			}
		}
	}

	public void onChangedRemoved(final RPObject base, final RPObject diff) {
		super.onChangedRemoved(base, diff);

		fireHPEventChangedRemoved(base, diff);
		fireAttackEventChangedRemoved(base, diff);
	}

	protected void fireAttackEvent(final RPObject base, final RPObject diff) {
		if ((diff == null) && (base == null)) {
			// Remove case

			onStopAttack();

			if (attackTarget != null) {
				attackTarget.onStopAttacked(this);
				attackTarget = null;
			}
		} else if (diff == null) {
			// Modified case
			if (base.has("target")) {
				int risk = (base.has("risk") ? base.getInt("risk") : 0);
				int damage = (base.has("damage") ? base.getInt("damage") : 0);
				int target = base.getInt("target");

				RPObject.ID targetEntityID = new RPObject.ID(target, base.get("zoneid"));

				RPEntity targetEntity = (RPEntity) GameObjects.getInstance().get(targetEntityID);

				if (targetEntity != null) {
					if (targetEntity != attackTarget) {
						onAttack(targetEntity);
						targetEntity.onAttacked(this);
					}

					if (risk == 0) {
						onAttackMissed(targetEntity);
						targetEntity.onMissed(this);
					}

					if ((risk > 0) && (damage == 0)) {
						onAttackBlocked(targetEntity);
						targetEntity.onBlocked(this);
					}

					if ((risk > 0) && (damage > 0)) {
						onAttackDamage(targetEntity, damage);
						targetEntity.onDamaged(this, damage);
					}

					// targetEntity.onAttack(this,risk,damage);
					attackTarget = targetEntity;
				}
				if (base.has("heal")) {
					onHealed(base.getInt("heal"));
				}
			}
		} else {
			// Modified case
			if (diff.has("target") && base.has("target") && !base.get("target").equals(diff.get("target"))) {
				System.out.println("Removing target: new target");
				onStopAttack();

				if (attackTarget != null) {
					attackTarget.onStopAttacked(this);
					attackTarget = null;
				}

			}

			if (diff.has("target") || base.has("target")) {
				boolean thereIsEvent = false;

				int risk = 0;
				if (diff.has("risk")) {
					thereIsEvent = true;
					risk = diff.getInt("risk");
				} else if (base.has("risk")) {
					risk = base.getInt("risk");
				} else {
					risk = 0;
				}

				int damage = 0;
				if (diff.has("damage")) {
					thereIsEvent = true;
					damage = diff.getInt("damage");
				} else if (base.has("damage")) {
					damage = base.getInt("damage");
				} else {
					damage = 0;
				}

				int target = -1;
				if (diff.has("target")) {
					target = diff.getInt("target");
				} else if (base.has("target")) {
					target = base.getInt("target");
				}

				RPObject.ID targetEntityID = new RPObject.ID(target, diff.get("zoneid"));

				RPEntity targetEntity = (RPEntity) GameObjects.getInstance().get(targetEntityID);

				if (targetEntity != null) {
					onAttack(targetEntity);
					targetEntity.onAttacked(this);

					if (thereIsEvent) {
						if (risk == 0) {
							onAttackMissed(targetEntity);
							targetEntity.onMissed(this);
						}

						if ((risk > 0) && (damage == 0)) {
							onAttackBlocked(targetEntity);
							targetEntity.onBlocked(this);
						}

						if ((risk > 0) && (damage > 0)) {
							onAttackDamage(targetEntity, damage);
							targetEntity.onDamaged(this, damage);
						}
					}

					attackTarget = targetEntity;
				}
			}
			if (diff.has("heal")) {
				onHealed(diff.getInt("heal"));
			}
		}
	}

	protected void fireAttackEventChangedRemoved(final RPObject base, final RPObject diff) {
		if (diff.has("target")) {
			onStopAttack();

			if (attackTarget != null) {
				attackTarget.onStopAttacked(this);
				attackTarget = null;
			}
		}
	}

	protected void fireKillEvent(final RPObject base,final  RPObject diff) {
		if (diff!=null){
				if (diff.has("hp/base_hp") && (diff.getDouble("hp/base_hp") == 0)) {
					onDeath(lastAttacker);
				}
		}
	}

	protected void fireTalkEvent(final RPObject base,final  RPObject diff) {
		if ((diff == null) && (base == null)) {
			// Remove case
		} else if (diff == null) {
			// First time case.
			if (base.has("text")) {
				String text = base.get("text");
				onTalk(text);
			}

			if (base.has("private_text")) {
				String text = base.get("private_text");
				onPrivateListen(text);
			}
		} else {
			if (diff.has("text")) {
				String text = diff.get("text");
				onTalk(text);
			}

			if (diff.has("private_text")) {
				String text = diff.get("private_text");
				onPrivateListen(text);
			}
		}
	}

	protected void fireHPEvent(final RPObject base, final RPObject diff) {
		if ((diff == null) && (base == null)) {
			// Remove case
		} else if (diff == null) {
			// First time case.
		} else {
			if (diff.has("hp") && base.has("hp")) {
				int healing = diff.getInt("hp") - base.getInt("hp");
				if (healing > 0) {
					onHealed(healing);
				}
			}

			if (diff.has("poisoned")) {
				// To remove the - sign on poison.
				onPoisoned(Math.abs(diff.getInt("poisoned")));
			}

			if (diff.has("eating")) {
				onEat(0);
			}
		}
	}

	private void fireHPEventChangedRemoved(final RPObject base, final RPObject diff) {
		if (diff.has("poisoned")) {
			onPoisonEnd();
		}

		if (diff.has("eating")) {
			onEatEnd();
		}
	}

	/** Draws only the Name and hp bar **/
	public void drawHPbar(final GameScreen screen) {
		if (nameImage != null) {
			screen.draw(nameImage, x, y - 0.5);

			Graphics g2d = screen.expose();

			Point2D p = new Point.Double(x, y);
			p = screen.invtranslate(p);

			if (hp_base_hp > 1) {
				hp_base_hp = 1;
			}

			if (hp_base_hp < 0) {
				hp_base_hp = 0;
			}

			float r = 1 - hp_base_hp;
			r *= 2.0;
			float g = hp_base_hp;
			g *= 2.0;

			g2d.setColor(Color.gray);
			g2d.fillRect((int) p.getX(), (int) p.getY() - 3, 32, 3);
			g2d.setColor(new Color(r > 1 ? 1 : r, g > 1 ? 1 : g, 0));
			g2d.fillRect((int) p.getX(), (int) p.getY() - 3, (int) (hp_base_hp * 32.0), 3);
			g2d.setColor(Color.black);
			g2d.drawRect((int) p.getX(), (int) p.getY() - 3, 32, 3);
		}
	}

	/** Draws this entity in the screen */
	@Override
	public void draw(final GameScreen screen) {
		super.draw(screen);

		if ((damageSprites != null) && (damageSprites.size() > 0)) 	{
			//			 Draw the damage done
			long current = System.currentTimeMillis();

			int i = 0;
			for (Sprite damageImage : damageSprites) {
				double tx = x + 0.6 - (damageImage.getWidth() / (GameScreen.SIZE_UNIT_PIXELS * 2.0f));
				double ty = y - ((current - damageSpritesTimes.get(i)) / (6.0 * 300.0));
				screen.draw(damageImage, tx, ty);
				i++;
			}

			if ((damageSpritesTimes.size() > 0) && (current - damageSpritesTimes.get(0) > 6 * 300)) {
				damageSprites.remove(0);
				damageSpritesTimes.remove(0);
			}
		}
	}

	@Override
	public ActionType defaultAction() {
		return ActionType.LOOK;
	}

	protected void buildOfferedActions(List<String> list) {
		super.buildOfferedActions(list);
		list.add(ActionType.ATTACK.getRepresentation());

		if (!User.isNull()) {
	        if (User.get().isAttacking()) {
				list.add(ActionType.STOP_ATTACK.getRepresentation());
			}
        }
	}

	@Override
	public void onAction(final ActionType at, final String... params) {
		// ActionType at = handleAction(action);
		RPAction rpaction;
		switch (at) {
			case ATTACK:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				int id = getID().getObjectID();
				rpaction.put("target", id);
				at.send(rpaction);
				break;
			case STOP_ATTACK:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				rpaction.put("attack", "");
				at.send(rpaction);
				break;
			default:
				super.onAction(at, params);
				break;
		}

	}

	@Override
	public int compareTo(final Entity entity) {
		if (!(entity instanceof RPEntity)) {
			return super.compareTo(entity);
		}

		double dx = getArea().getX() - entity.getArea().getX();
		double dy = getArea().getY() - entity.getArea().getY();

		if (dy < 0) {
			return -1;
		} else if (dy > 0) {
			return 1;
		} else if (dx != 0) {
			return (int) Math.signum(dx);
		} else {
			// Same tile...
			return 0;
		}
	}

	/**
	 * @return Returns the atk.
	 */
	public int getAtk() {
		return atk;
	}

	/**
	 * @return Returns the def.
	 */
	public int getDef() {
		return def;
	}

	/**
	 * @return Returns the xp.
	 */
	public int getXp() {
		return xp;
	}

	/**
	 * @return Returns the base_hp.
	 */
	public int getBase_hp() {
		return base_hp;
	}

	/**
	 * @return the attack xp
	 */
	public int getAtkXp() {
		return atkXp;
	}

	/**
	 * @return the defence xp
	 */
	public int getDefXp() {
		return defXp;
	}

	/**
	 * @return Returns the atk of items
	 */
	public int getAtkItem() {
		return atkItem;
	}

	/**
	 * @return Returns the def of items
	 */
	public int getDefItem() {
		return defItem;
	}

	/**
	 *@return Returns the total mana of a player
	 */
	public int getMana() {
		return mana;
	}

	/**
	 *@return Returns the base mana value
	 */
	public int getBaseMana() {
		return base_mana;
	}

	// When this entity attacks target.
	public void onAttack(final Entity target) {
		attacking = target.getID();
	}

	// When attacker attacks this entity.
	public void onAttacked(final Entity attacker) {
		/*
		 * Could keep track of all attackers, but right now we only
		 * need one of them for onDeath() sake
		 */
		lastAttacker = attacker;
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

	// When this entity causes damaged to adversary, with damage amount
	public void onAttackDamage(final Entity target, final int damage) {
		showBladeStrike = true;
	}

	// When this entity's attack is blocked by the adversary
	public void onAttackBlocked(final Entity target) {
		showBladeStrike = true;
	}

	// When this entity's attack is missing the adversary
	public void onAttackMissed(final Entity target) {
		showBladeStrike = true;
	}

	// When this entity is damaged by attacker with damage amount
	public void onDamaged(final Entity attacker, final int damage) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.HIT;

		playSound("punch-mix", 20, 60, 80);

		addFloater("-" + damage, Color.red);

		boolean showAttackInfoForPlayer = (!User.isNull())
		        && (this.equals(User.get()) || attacker.equals(User.get()));
		showAttackInfoForPlayer = showAttackInfoForPlayer & (!stendhal.FILTER_ATTACK_MESSAGES);

		if (stendhal.SHOW_EVERYONE_ATTACK_INFO || showAttackInfoForPlayer) {
			StendhalUI.get().addEventLine(
			        getName() + " suffers " + Grammar.quantityplnoun(damage, "point") + " of damage from "
			                + attacker.getName(), Color.RED);
		}
	}

	// When this entity blocks the attack by attacker
	public void onBlocked(final Entity attacker) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.BLOCKED;
	}

	// When this entity skip attacker's attack.
	public void onMissed(final Entity attacker) {
		combatIconTime = System.currentTimeMillis();
		resolution = Resolution.MISSED;
	}


	//
	// Entity
	//

	/**
	 * Transition method. Create the screen view for this entity.
	 *
	 * @return	The on-screen view of this entity.
	 */
	protected Entity2DView createView() {
		return new RPEntity2DView(this);
	}
}
