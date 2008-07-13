/** Stendhal Mana/Magic Extenstion
 *  @author timothyb89
 *  Adds a magic skills system to a Stendhal server.
 */

package games.stendhal.server.extension;

import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;

import org.apache.log4j.Logger;
import marauroa.common.game.RPAction;

/**
 *  # load StendhalServerExtension(s).
 * groovy=games.stendhal.server.scripting.StendhalGroovyRunner
 * http=games.stendhal.server.StendhalHttpServer
 * magic=games.stendhal.server.extension.MagicExtn
 * server_extension=groovy,http,magic
 */
public class MagicExtn extends StendhalServerExtension implements
		ActionListener {

	private static final Logger logger = Logger.getLogger(MagicExtn.class);

	/**
	 * 
	 */
	public MagicExtn() {
		super();
		logger.info("MagicExtn starting...");
		CommandCenter.register("spell", this);

		// StendhalRPRuleProcessor.register("listspells", this); //not ready yet
	}

	/*
	 * @see games.stendhal.server.StendhalServerExtension#init()
	 */
	@Override
	public void init() {
		// this extension has no specific init code, everything is
		// implemented as /commands that are handled onAction
	}

	public void onAction(final Player player, final RPAction action) {
		final String type = action.get("type");

		if (type.equals("spell")) {
			onSpell(player, action);
		}
	}

	private void onSpell(final Player player, final RPAction action) {
		final String usage = "Usage: #/spell <spellname>";
		String text = "";

		boolean canCastSpell = false;

		String castSpell = null;

		if (action.has("target")) {
			castSpell = action.get("target");
			if (castSpell == null) {
				player.sendPrivateText("You did not enter a spell to cast.");
				logger.error("User did not enter a spell.");
			}
			// if(player1 == null) {
			// text += "Player \"" + name1 + "\" not found. ";
			// }
			player.sendPrivateText("Trying to cast a spell...");
		} else {
			text = usage;
		}

		// the list of spells
		// TODO: Is this getQuest("spells") correct, shouldn't it be s.th. like
		// player.getSlot("spells")... ?
		final String availableSpells = player.getQuest("spells");
		
		if (availableSpells == null) {
			player.sendPrivateText("You can not cast this spell.");
			return;
		}
		
		// Checks to see if the list of spells available to the player contains
		// the spell they tried to cast
		if (availableSpells.contains(castSpell)) {
			canCastSpell = true;
		} else {
			player.sendPrivateText("You can not cast this spell.");
		}

		if (canCastSpell) {
			// put spells and actions here
			if (castSpell.contains("heal")) {
				if (player.getMana() > 15) {
					player.heal();

					final String mana = player.get("mana");
					final int mana_a = Integer.parseInt(mana);
					final int newmana = mana_a - 15;
					player.put("mana", newmana);
					player.sendPrivateText("You have been healed.");
					player.update();
					player.notifyWorldAboutChanges();
				} else {
					player.sendPrivateText("You do not have enough available mana to use this spell.");
				}

			} else if (castSpell.contains("raise stats")) {
				if (player.getMana() > 100) {
					/**
					 * Raises the level of a player along with the atk/def
					 */

					// gets old stats
					int oldLevel = player.getLevel();
					final int oldXP = player.getXP();
					final int oldDefXP = player.getDEFXP();
					int oldDef = player.getDEF();
					int oldAtk = player.getATK();
					final int oldAtkXP = player.getATKXP();

					// gets new stats
					final int newLevel = oldLevel++;
					final int newXP = oldXP + 44900;
					final int newDefXP = oldDefXP + 24700;
					final int newDef = oldDef++;
					final int newAtkXP = oldAtkXP + 24700;
					final int newAtk = oldAtk++;

					// sets new stats
					player.setXP(newXP);
					player.setLevel(newLevel);
					player.setDEFXP(newDefXP);
					player.setDEF(newDef);
					player.setATK(newAtk);
					player.setATKXP(newAtkXP);

					// saves changes
					player.update();
					player.notifyWorldAboutChanges();

					// takes away mana
					// player.put("mana", player.getMana() - 110);
					final String mana = player.get("mana");
					final int mana_a = Integer.parseInt(mana);
					final int newmana = mana_a - 110;
					player.put("mana", newmana);

					player.sendPrivateText("Your stats have been raised.");
				} else {
					player.sendPrivateText("You do not have enough mana to cast this spell.");
				}
			} else {
				player.sendPrivateText("The spell you tried to cast doesn't exist!");
			}
		}

		player.sendPrivateText(text);
	}

}
