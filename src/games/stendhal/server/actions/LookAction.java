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
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.LOOK;
import static games.stendhal.common.constants.Actions.NAME;
import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TYPE;
import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

public class LookAction implements ActionListener {

	

	public static void register() {
		CommandCenter.register(LOOK, new LookAction());
	}

	public void onAction(final Player player, final RPAction action) {
		Entity entity = EntityHelper.entityFromSlot(player, action);

		if (entity == null) {
			entity = EntityHelper.entityFromTargetName(action.get(TARGET), player);
		}

		if (entity != null) {
			if (entity instanceof Player) {
				if (((Player) entity).isGhost() 
						&& (player.getAdminLevel() < AdministrationAction.getLevelForCommand("ghostmode"))) {
					return;
				}
			}

			String name = entity.get(TYPE);
			if (entity.has(NAME)) {
				name = entity.get(NAME);
			}
			new GameEvent(player.getName(), LOOK, name).raise();
			final String text = entity.describe();

			if (entity instanceof Sign) {
				player.sendPrivateText(NotificationType.RESPONSE, text);
			} else {
				player.sendPrivateText(text);
			}

			player.notifyWorldAboutChanges();
		}
	}
}
