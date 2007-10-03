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

import games.stendhal.common.Grammar;
import games.stendhal.server.GagManager;
import games.stendhal.server.Jail;
import games.stendhal.server.StendhalPlayerDatabase;
import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPAction;
import marauroa.server.game.db.Transaction;

/**
 * Processes /chat, /tell (/msg) and /support
 */
public class ChatAction implements ActionListener {

	private static final Logger logger = Log4J.getLogger(ChatAction.class);

	// HashMap <players_name, last_message_time>
	private Map<String, Long> lastMsg = new HashMap<String, Long>();

	/**
	 * Registers actions
	 */
	public static void register() {
		ChatAction chat = new ChatAction();
		StendhalRPRuleProcessor.register("answer", chat);
		StendhalRPRuleProcessor.register("chat", chat);
		StendhalRPRuleProcessor.register("tell", chat);
		StendhalRPRuleProcessor.register("support", chat);

		// start the logcleaner
		LogCleaner logCleaner = new LogCleaner();
		logCleaner.start();
	}

	public void onAction(Player player, RPAction action) {

		if (GagManager.isGagged(player)) {
			long timeRemaining = GagManager.get().getTimeRemaining(player);
			player.sendPrivateText("You are gagged, it will expire in "
					+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)));
			return;
		}

		if (action.get("type").equals("chat")) {
			onChat(player, action);
		} else if (action.get("type").equals("tell")) {
			onTell(player, action);
		} else if (action.get("type").equals("answer")) {
			onAnswer(player, action);
		} else {
			onSupport(player, action);
		}
	}

	private void onChat(Player player, RPAction action) {

		if (action.has("text")) {
			String text = action.get("text");
			player.put("text", text);
			StendhalRPRuleProcessor.get().addGameEvent(player.getName(),
					"chat", null, Integer.toString(text.length()),
					text.substring(0, Math.min(text.length(), 1000)));

			player.notifyWorldAboutChanges();
			StendhalRPRuleProcessor.get().removePlayerText(player);
		}

	}

	private void onAnswer(Player player, RPAction action) {
		if (action.has("text")) {
			if (player.getLastPrivateChatter() != null) {
				// convert the action to a /tell action
				action.put("type", "tell");
				action.put("target", player.getLastPrivateChatter());
				onTell(player, action);
			} else {
				player.sendPrivateText("Nobody has talked privately to you.");
			}
		}

	}

	private void onTell(Player player, RPAction action) {
		String away;
		String reply;

		// TODO: find a cleaner way to implement it
		if (Jail.isInJail(player)) {
			player
					.sendPrivateText("The strong anti telepathy aura prevents you from getting through. Use /support <text> to contact an admin!");
			return;
		}

		if (action.has("target") && action.has("text")) {
			String text = action.get("text").trim();
			String message;

			// find the target player
			String senderName = player.getTitle();
			String receiverName = action.get("target");

			Player receiver = StendhalRPRuleProcessor.get().getPlayer(
					receiverName);
			/*
			 * If the receiver is not logged or if it is a ghost and you don't
			 * have the level to see ghosts...
			 */
			if (receiver == null
					|| receiver.isGhost()
					&& player.getAdminLevel() < AdministrationAction
							.getLevelForCommand("ghostmode")) {
				player.sendPrivateText("No player named \""
						+ action.get("target") + "\" is currently active.");
				player.notifyWorldAboutChanges();
				return;
			}

			if (receiverName.equals("postman")) {
				// HACK: Don't risk breaking postman messages
				message = senderName + " tells you: " + text;
			} else if (senderName.equals(receiverName)) {
				message = "You mutter to yourself: " + text;
			} else {
				message = senderName + " tells " + receiverName + ": " + text;
			}

			// HACK: extract sender from postman messages
			StringTokenizer st = new StringTokenizer(text, " ");
			if (senderName.equals("postman") && (st.countTokens() > 2)) {
				String temp = st.nextToken();
				String command = st.nextToken();
				if (command.equals("asked")) {
					senderName = temp;
				}
			}

			// check ignore list
			reply = receiver.getIgnore(senderName);
			if (reply != null) {
				// sender is on ignore list
				// HACK: do not notify postman
				if (!senderName.equals("postman")) {
					if (reply.length() == 0) {
						player
								.sendPrivateText(Grammar.suffix_s(receiverName)
										+ " mind is not attuned to yours, so you cannot reach them.");
					} else {
						player.sendPrivateText(receiverName
								+ " is ignoring you: " + reply);
					}

					player.notifyWorldAboutChanges();
				}
				return;
			}

			// transmit the message
			receiver.sendPrivateText(message);

			if (!senderName.equals(receiverName)) {
				player.sendPrivateText("You tell " + receiverName + ": " + text);
			}

			/*
			 * Handle /away messages
			 */
			away = receiver.getAwayMessage();
			if (away != null) {
				if (receiver.isAwayNotifyNeeded(senderName)) {
					/*
					 * Send away response
					 */
					player.sendPrivateText(receiverName + " is away: " + away);

					player.notifyWorldAboutChanges();
				}
			}

			receiver.setLastPrivateChatter(senderName);
			StendhalRPRuleProcessor.get().addGameEvent(player.getName(),
					"chat", receiverName, Integer.toString(text.length()),
					text.substring(0, Math.min(text.length(), 1000)));
			receiver.notifyWorldAboutChanges();
			player.notifyWorldAboutChanges();
			return;
		}
	}

	private void onSupport(Player player, RPAction action) {

		if (action.has("text")) {

			if (action.get("text").trim().equals("")) {
				player.sendPrivateText("Usage /support <your message here>");
				return;
			}

			if (Jail.isInJail(player)) {
				// check if the player sent a support message before
				if (lastMsg.containsKey(player.getName())) {
					Long timeLastMsg = System.currentTimeMillis()
							- lastMsg.get(player.getName());

					// the player have to wait one minute since the last support
					// message was sent
					if (timeLastMsg < 60000) {
						player
								.sendPrivateText("We only allow inmates one support message per minute.");
						return;
					}
				}

				lastMsg.put(player.getName(), System.currentTimeMillis());
			}

			String message = action.get("text")
					+ "\r\nPlease use #/supportanswer #" + player.getTitle()
					+ " to answer.";

			StendhalRPRuleProcessor.get().addGameEvent(player.getName(),
					"support", action.get("text"));

			sendMessageToSupporters(player.getTitle(), message);

			player
					.sendPrivateText("You ask for support: "
							+ action.get("text")
							+ "\nIt may take a little time until your question is answered.");
			player.notifyWorldAboutChanges();
		}

	}

	/**
	 * sends a message to all supporters
	 *
	 * @param source
	 *            a player or script name
	 * @param message
	 *            Support message
	 */
	// TODO: try to clean up the dependencies, having other places in
	// the code call directly into an action does not seem to be a
	// good idea
	public static void sendMessageToSupporters(String source, String message) {
		String text = source + " asks for support to ADMIN: " + message;
		for (Player p : StendhalRPRuleProcessor.get().getPlayers()) {
			if (p.getAdminLevel() >= AdministrationAction.REQUIRED_ADMIN_LEVEL_FOR_SUPPORT) {
				p.sendPrivateText(text);
				p.notifyWorldAboutChanges();
			}
		}
	}

	/**
	 * Deletes the chatlog after a short delay. Note this runs inside a thread
	 * outside the normal turn based processing because the SQL command may take
	 * more then 100ms on MySQL.
	 */
	protected static class LogCleaner extends Thread {

		public LogCleaner() {
			super("ChatLogCleaner");
			super.setDaemon(true);
			super.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			while (true) {
				try {
					StendhalPlayerDatabase database = StendhalPlayerDatabase
							.getDatabase();
					Transaction transaction = database.getTransaction();
					database.cleanChatLog(transaction);
					transaction.commit();
					Thread.sleep(3600 * 1000);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
		}
	}
}
