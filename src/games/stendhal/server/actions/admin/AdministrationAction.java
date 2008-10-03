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
package games.stendhal.server.actions.admin;

import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.WellKnownActionConstants;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.EntityHelper;

import java.util.HashMap;
import java.util.Map;

import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * Most /commands for admins are handled here.
 */
public abstract class AdministrationAction implements ActionListener {

	protected static final Logger logger = Logger.getLogger(AdministrationAction.class);

	public static final int REQUIRED_ADMIN_LEVEL_FOR_SUPPORT = 100;

	public static final int REQUIRED_ADMIN_LEVEL_FOR_SUPER = 5000;

	protected static final Map<String, Integer> REQUIRED_ADMIN_LEVELS = new HashMap<String, Integer>();

	public static void register() {
		InspectAction.register();
		DestroyAction.register();
		SupportAnswerAction.register();
		TellAllAction.register();
		TeleportAction.register();
		TeleportToAction.register();
		AdminLevelAction.register();
		AlterAction.register();
		AlterCreatureAction.register();
		SummonAction.register();
		SummonAtAction.register();
		InvisibleAction.register();
		GhostModeAction.register();
		TeleClickModeAction.register();
		JailAction.register();
		JailReportAction.register();
		GagAction.register();
		AlterQuestAction.register();
		WrapAction.register();
		REQUIRED_ADMIN_LEVELS.put("super", 5000);
	}

	public static void registerCommandLevel(final String command, final int minLevel) {
		REQUIRED_ADMIN_LEVELS.put(command, minLevel);
	}

	public static Integer getLevelForCommand(final String command) {
		final Integer val = REQUIRED_ADMIN_LEVELS.get(command);
		if (val == null) {
			return -1;
		}

		return val;
	}

	public static boolean isPlayerAllowedToExecuteAdminCommand(final Player player,
			final String command, final boolean verbose) {
		// get adminlevel of player and required adminlevel for this command
		final int adminlevel = player.getAdminLevel();
		final Integer required = REQUIRED_ADMIN_LEVELS.get(command);

		// check that we know this command
		if (required == null) {
		return true;
		}

		if (adminlevel < required.intValue()) {
			// not allowed
			logger.warn("Player " + player.getName() + " with admin level "
					+ adminlevel + " tried to run admin command " + command
					+ " which requires level " + required + ".");

			// Notify the player if verbose is set.
			if (verbose) {

				// is this player an admin at all?
				if (adminlevel == 0) {
					player.sendPrivateText("Sorry, you need to be an admin to run \""
							+ command + "\".");
				} else {
					player.sendPrivateText("Your admin level is only "
							+ adminlevel + ", but a level of " + required
							+ " is required to run \"" + command + "\".");
				}
			}
			return false;
		}

		// OK
		return true;
	}

	public final void onAction(final Player player, final RPAction action) {

	

		perform(player, action);
	}

	protected abstract void perform(Player player, RPAction action);
protected final Entity getTargetAnyZone(final Player player, final RPAction action) {

		
		Entity entity = EntityHelper.entityFromSlot(player, action);

		if (entity == null) {
			entity = EntityHelper.entityFromTargetNameAnyZone(action.get(WellKnownActionConstants.TARGET), player);
		}
		
		return entity;
}
	/**
	 * get the Entity-object of the specified target.
	 * 
	 * @param player
	 * @param action
	 * @return the Entity or null if it does not exist
	 */
	protected final Entity getTarget(final Player player, final RPAction action) {
		Entity entity = EntityHelper.entityFromSlot(player, action);

		if (entity == null) {
			entity = EntityHelper.entityFromTargetName(action.get(WellKnownActionConstants.TARGET), player);
		}
		
		return entity;
	}
}
