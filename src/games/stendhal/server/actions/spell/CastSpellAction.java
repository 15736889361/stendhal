/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.spell;
import static games.stendhal.common.constants.Actions.CASTSPELL;
import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.Spell;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;
/**
 * Casts a spell for a player at the given target
 * 
 * @author madmetzger
 */
public class CastSpellAction implements ActionListener {
	
	public static void register() {
		CommandCenter.register(CASTSPELL, new CastSpellAction());
	}

	public void onAction(Player player, RPAction action) {
		//base object is always the player sending the action
		action.put("baseobject", player.getID().getObjectID());
		Entity target = EntityHelper.entityFromTargetName(action.get(TARGET), player);
		Spell spell = (Spell) EntityHelper.entityFromSlot(player, action);
		spell.cast(player, target);
	}

}
