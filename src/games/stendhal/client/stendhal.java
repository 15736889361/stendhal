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
package games.stendhal.client;

import games.stendhal.client.gui.StendhalFirstScreen;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.soundreview.SoundMaster;
import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.client.update.Version;
import games.stendhal.common.Debug;

import java.security.AccessControlException;

import javax.swing.JOptionPane;

import marauroa.common.Log4J;
import marauroa.common.Logger;

public class stendhal extends Thread {

	private static final Logger logger = Log4J.getLogger(stendhal.class);

	public static boolean doLogin;

	public static String STENDHAL_FOLDER;

	// detect web start sandbox and init STENDHAL_FOLDER otherwise
	static {
		try {
			System.getProperty("user.home");
		} catch (AccessControlException e) {
			Debug.WEB_START_SANDBOX = true;
		}

		/** We set the main game folder to the game name */
		String gameName = ClientGameConfiguration.get("GAME_NAME");
		STENDHAL_FOLDER = "/" + gameName.toLowerCase() + "/";
	}

	public static final String VERSION = Version.VERSION;

	public static String SCREEN_SIZE = "640x480";

	public static final boolean SHOW_COLLISION_DETECTION = false;

	public static final boolean SHOW_EVERYONE_ATTACK_INFO = false;

	public static final boolean FILTER_ATTACK_MESSAGES = true;

	public static final int FPS_LIMIT = 25;

	/**
	 * Parses command line arguments
	 * 
	 * @param args
	 *            command line arguments
	 */
	private static void parseCommandlineArguments(String[] args) {
		String size = null;
		int i = 0;

		while (i != args.length) {
			if (args[i].equals("-s")) {
				size = args[i + 1];
			} else if (args[i].equals("-wouterwens.bat")) {
				JOptionPane
						.showConfirmDialog(
								null,
								"Do not use command line parameters, that you do not understand",
								"Stendhal", JOptionPane.OK_OPTION,
								JOptionPane.QUESTION_MESSAGE);
			}
			i++;
		}

		if (size != null) {
			SCREEN_SIZE = size;
		}
	}

	/**
	 * Starts the LogSystem
	 */
	private static void startLogSystem() {
		Log4J.init("data/conf/log4j.properties");

		logger.info("Setting base at :" + STENDHAL_FOLDER);
		logger.info("Stendhal " + VERSION);
		logger.info("OS: " + System.getProperty("os.name") + " "
				+ System.getProperty("os.version"));
		logger.info("Java: " + System.getProperty("java.version"));
	}

	/**
	 * Try to use the system look and feel.
	 */
	private static void startSwingLookAndFeel() {
		try {
			// only enable SystemLookAndFeelClassName for MS Windows because of
			// bug
			// http://sourceforge.net/tracker/index.php?func=detail&aid=1601437&group_id=1111&atid=101111
			/*
			 * if (System.getProperty("os.name",
			 * "").toLowerCase().indexOf("windows") > -1) {
			 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
			 */
		} catch (Exception e) {
			logger
					.error(
							"Can't change Look&Feel to match your OS. Using the Cross-Platform look & feel",
							e);
		}
	}

	/**
	 * Starts the client and show the first screen
	 * 
	 * @return StendhalClient
	 */
	private static StendhalClient startClient() {
		StendhalClient client = StendhalClient.get();
		new StendhalFirstScreen(client);
		return client;
	}

	/**
	 * A loop which simply waits for the login to be completed.
	 */
	private static void waitForLogin() {
		while (!doLogin) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				// simply ignore it
			}
		}
	}

	/**
	 * Starts the real game gui
	 * 
	 * @param client
	 *            StendhalClient
	 */
	private static void startGameGUI(StendhalClient client) {

		new j2DClient(client);
	}

	/**
	 * Main Entry point.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		// get size string
		if (System.getProperty("stendhal.refactoringgui") != null) {
			SCREEN_SIZE = "1000x480";
		} else {
			SCREEN_SIZE = "640x480";
		}
		parseCommandlineArguments(args);
		startLogSystem();
		startSwingLookAndFeel();
		StendhalClient client = startClient();
		waitForLogin();
		startSoundMaster();
		startGameGUI(client);
	}

	private static void startSoundMaster() {
		SoundMaster sm = new SoundMaster();
		sm.init();
		Thread th = new Thread(sm);
		th.start();

	}
}
