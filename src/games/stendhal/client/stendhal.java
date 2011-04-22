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

import static java.io.File.separator;
import games.stendhal.client.gui.StendhalFirstScreen;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.login.LoginDialog;
import games.stendhal.client.gui.login.Profile;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.WoodStyle;
import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.client.update.Version;
import games.stendhal.common.Debug;
import games.stendhal.common.resource.ResourceManager;

import java.awt.Dimension;
import java.io.File;
import java.security.AccessControlException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import marauroa.common.Log4J;
import marauroa.common.MarauroaUncaughtExceptionHandler;

import org.apache.log4j.Logger;

public class stendhal {

	private static final String LOG_FOLDER = "log";
	private static final Logger logger = Logger.getLogger(stendhal.class);
	private static final ResourceManager RESOURCE_MANAGER = new ResourceManager();

	private static boolean doLogin;

	// Use getGameFolder() where you need the real game data location
	private static final String STENDHAL_FOLDER;
	public static final String GAME_NAME;
	/**
	 * Directory for storing the persistent game data.
	 */
	private static final String gameFolder;
	/**
	 * Just a try to get Webstart working without additional rights.
	 */
	static boolean WEB_START_SANDBOX = false;

	// detect web start sandbox and init STENDHAL_FOLDER otherwise
	static {
		try {
			System.getProperty("user.home");
		} catch (final AccessControlException e) {
			WEB_START_SANDBOX = true;
		}

		/** We set the main game folder to the game name */
		GAME_NAME = ClientGameConfiguration.get("GAME_NAME");
		STENDHAL_FOLDER = separator + GAME_NAME.toLowerCase(Locale.ENGLISH) + separator;
		gameFolder = System.getProperty("user.home") + STENDHAL_FOLDER;

		/** setup the search locations for the resource manager */
		RESOURCE_MANAGER.addScheme("sound" , "data/sounds");
		RESOURCE_MANAGER.addScheme("music" , "data/music");
		RESOURCE_MANAGER.addScheme("audio" , "data/sounds", "data/music");
	}

	public static final String VERSION = Version.getVersion();

	private static Dimension screenSize = new Dimension(640, 480);
	
	public static final boolean SHOW_COLLISION_DETECTION = false;

	public static final boolean SHOW_EVERYONE_ATTACK_INFO = false;

	public static final boolean FILTER_ATTACK_MESSAGES = true;

	public static final int FPS_LIMIT = 25;

	public static void setDoLogin()	{
		doLogin = true;
	}

	public static Dimension getScreenSize() {
		return screenSize;
	}

	/**
	 * Parses command line arguments.
	 * 
	 * @param args
	 *            command line arguments
	 */
	private static void parseCommandlineArguments(final String[] args) {
		String size = null;
		int i = 0;

		while (i != args.length) {
			if (args[i].equals("-s")) {
				size = args[i + 1];
			}
			i++;
		}

		if (size != null) {
			String[] tempsize = size.split("x");
			screenSize = new Dimension(Integer.parseInt(tempsize[0]), Integer.parseInt(tempsize[1]));
		}
	}

	/**
	 * Starts the LogSystem.
	 */
	private static void startLogSystem() {
		prepareLoggingSystemEnviroment();
		
		Log4J.init("data/conf/log4j.properties");

		logger.info("Setting base at :" + STENDHAL_FOLDER);
		logger.info("Stendhal " + VERSION);
		logger.info(Debug.PRE_RELEASE_VERSION);
		logger.info("Logging to directory: "+ getLogFolder());

		String patchLevel = System.getProperty("sun.os.patch.level");
		if ((patchLevel == null) || (patchLevel.equals("unknown"))) {
			patchLevel = "";
		}

		logger.info("OS: " + System.getProperty("os.name") + " " + patchLevel
				+ " " + System.getProperty("os.version") + " "
				+ System.getProperty("os.arch"));
		logger.info("Java-Runtime: " + System.getProperty("java.runtime.name")
				+ " " + System.getProperty("java.runtime.version") + " from "
				+ System.getProperty("java.home"));
		logger.info("Java-VM: " + System.getProperty("java.vm.vendor") + " "
				+ System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.vm.version"));
		LogUncaughtExceptionHandler.setup();
	}

	private static void prepareLoggingSystemEnviroment() {
		// property configuration relies on this parameter
		System.setProperty("log.directory", getLogFolder());
		//create the log directory if not yet existing:
		File logDir = new File(getLogFolder());
		if(!logDir.exists()) {
			logDir.mkdir();
		}
	}

	public static String getLogFolder() {
		return getGameFolder()+LOG_FOLDER;
	}

	/**
	 * A loop which simply waits for the login to be completed.
	 */
	private static void waitForLogin() {
		while (!doLogin) {
			try {
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				logger.warn(e, e);
			}
		}
	}

	/**
	 * Get the singleton instance for the resource manager
	 * 
	 * @return the current resource manager 
	 */
	public static ResourceManager getResourceManager() {
		return RESOURCE_MANAGER;
	}
	
	/**
	 * Get the location of persistent game client data.
	 * 
	 * @return game's home directory
	 */
	public static String getGameFolder() {
		return gameFolder;
	}

	/**
	 * Main Entry point.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		// get size string
		parseCommandlineArguments(args);
		startLogSystem();
		MarauroaUncaughtExceptionHandler.setup(false);
		final UserContext userContext = new UserContext();
		final PerceptionDispatcher perceptionDispatch = new PerceptionDispatcher();
		final StendhalClient client = new StendhalClient(userContext, perceptionDispatch);
		
		try {
			UIManager.setLookAndFeel(new StyledLookAndFeel(WoodStyle.getInstance()));
		} catch (UnsupportedLookAndFeelException e) {
			/*
			 * Should not happen as StyledLookAndFeel always returns true for
			 * isSupportedLookAndFeel()
			 */
			logger.error("Failed to set Look and Feel", e);
		}

		UIManager.getLookAndFeelDefaults().put("ClassLoader", stendhal.class.getClassLoader());
		
		final Profile profile = Profile.createFromCommandline(args);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (profile.isValid()) {
					new LoginDialog(null, client).connect(profile);
				} else {
					new StendhalFirstScreen(client);
				}
			}
		});
		
		waitForLogin();
		IDSend.send();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GameScreen gameScreen = GameScreen.get();
				j2DClient locclient = new j2DClient(client, gameScreen, userContext);
				perceptionDispatch.register(locclient.getPerceptionListener());
				locclient.startGameLoop(gameScreen);
			}
		});
	}
}
