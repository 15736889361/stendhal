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
package games.stendhal.client.gui;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.GameObjects;
import games.stendhal.client.GameScreen;
import games.stendhal.client.PerceptionListenerImpl;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
import games.stendhal.client.World;
import games.stendhal.client.WorldObjects;
import games.stendhal.client.stendhal;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.buddies.BuddyPanelController;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.gui.chattext.ChatCompletionHelper;
import games.stendhal.client.gui.chattext.ChatTextController;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.map.MapPanelController;
import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.listener.PositionChangeMulticaster;
import games.stendhal.client.sound.SoundGroup;
import games.stendhal.client.sound.SoundSystemFacade;
import games.stendhal.client.sound.manager.SoundFile.Type;
import games.stendhal.client.sound.nosound.NoSoundFacade;
import games.stendhal.common.CollisionDetection;
import games.stendhal.common.Debug;
import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.common.constants.SoundLayer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import marauroa.client.net.IPerceptionListener;
import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/** The main class that create the screen and starts the arianne client. */
public class j2DClient implements UserInterface {

	/**
	 * A shared [singleton] copy.
	 */
	private static j2DClient sharedUI;
	
	/**
	 * Get the default UI.
	 * @return  the instance
	 */
	public static j2DClient get() {
		return sharedUI;
	}

	/**
	 * Set the shared [singleton] value.
	 *
	 * @param sharedUI
	 *            The Stendhal UI.
	 */
	public static void setDefault(final j2DClient sharedUI) {
		j2DClient.sharedUI = sharedUI;
		ClientSingletonRepository.setUserInterface(sharedUI);
	}


	private static final long serialVersionUID = 3356310866399084117L;

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(j2DClient.class);

	private MainFrame mainFrame;
	private QuitDialog quitDialog;

	private GameScreen screen;

	private JLayeredPane pane;

	private KTextEdit gameLog;
	
	private ContainerPanel containerPanel;

	private boolean gameRunning;


	ChatTextController chatText = new ChatTextController();

	/** settings panel. */
	private SettingsPanel settings;

	/** the Character panel. */
	private Character character;

	/** the Key ring panel. */
	private KeyRing keyring;

	/** the minimap panel. */
	private MapPanelController minimap;

	/** the inventory.*/
	private SlotWindow inventory;
	
	private User lastuser;


	private final PositionChangeMulticaster positionChangeListener = new PositionChangeMulticaster();
	/**
	 * Delayed direction release holder.
	 */
	protected DelayedDirectionRelease directionRelease;

	private UserContext userContext;

	private final IPerceptionListener perceptionListener = new PerceptionListenerImpl() {
		int times;
		@Override
		public void onSynced() {
			setOffline(false);
			times = 0;
			logger.debug("Synced with server state.");
			addEventLine(new HeaderLessEventLine("Synchronized",
					NotificationType.CLIENT));
		}

		@Override
		public void onUnsynced() {
			times++;

			if (times > 3) {
				logger.debug("Request resync");
				addEventLine(new HeaderLessEventLine("Unsynced: Resynchronizing...",
						NotificationType.CLIENT));
			}
			
		}
	};

	/**
	 * The stendhal client.
	 */
	protected StendhalClient client;

	private SoundSystemFacade soundSystemFacade;


	/**
	 * A constructor for JUnit tests.
	 */
	public j2DClient() {
		setDefault(this);
	}

	public j2DClient(final StendhalClient client, final GameScreen gameScreen, final UserContext userContext) {
		this.client = client;
		this.userContext = userContext;
		setDefault(this);
				
		Dimension screenSize = stendhal.getScreenSize();

		/*
		 * Add a layered pane for the game area, so that we can have
		 * windows on top of it
		 */
		pane = new JLayeredPane();
		pane.setPreferredSize(screenSize);
		/*
		 *  Set the sizes strictly so that the layout manager
		 *  won't try to resize it
		 */
		pane.setMaximumSize(screenSize);
		pane.setMinimumSize(new Dimension(screenSize.width, 0));
	
		/*
		 * Create the main game screen
		 */
		screen = new GameScreen(client);
		GameScreen.setDefaultScreen(screen);
		screen.setMinimumSize(new Dimension(screenSize.width, 0));
		
		// ... and put it on the ground layer of the pane
		pane.add(screen, Component.LEFT_ALIGNMENT, JLayeredPane.DEFAULT_LAYER);

		client.setScreen(screen);
		positionChangeListener.add(screen);

				
		final KeyAdapter tabcompletion = new ChatCompletionHelper(chatText, World.get().getPlayerList().getNamesList());
		chatText.addKeyListener(tabcompletion);
		
		/*
		 * Always redirect focus to chat field
		 */
		screen.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent e) {
				chatText.getPlayerChatText().requestFocus();
			}

			public void focusLost(final FocusEvent e) {
				// do nothing
			}
		});

		
		// On Screen windows
		/*
		 * Quit dialog
		 */
		quitDialog = new QuitDialog();
		pane.add(quitDialog.getQuitDialog(), JLayeredPane.MODAL_LAYER);

		/*
		 * Game log
		 */
		gameLog = new KTextEdit();
		gameLog.setPreferredSize(new Dimension(getWidth(), 171));

		final KeyListener keyListener = new GameKeyHandler();

		// add a key input system (defined below) to our canvas so we can
		// respond to key pressed
		chatText.addKeyListener(keyListener);
		screen.addKeyListener(keyListener);

		// Display a warning message in case the screen size was adjusted
		// This is a temporary solution until this issue is fixed server side.
		// I hope that it discourages its use without the risks of unupdateable
		// clients being distributed
		if (!screenSize.equals(new Dimension(640, 480))) {
			addEventLine(new HeaderLessEventLine("Using window size cheat: " + getWidth() + "x" + getHeight(), NotificationType.NEGATIVE));
		}

		// Display a hint if this is a debug client
		if (Debug.PRE_RELEASE_VERSION != null) {
			addEventLine(new HeaderLessEventLine("This is a pre release test client: " + Debug.VERSION + " - " + Debug.PRE_RELEASE_VERSION, NotificationType.CLIENT));
		}

		// set some default window positions
		final WtWindowManager windowManager = WtWindowManager.getInstance();
		windowManager.setDefaultProperties("corpse", false, 0, 190);
		windowManager.setDefaultProperties("chest", false, 100, 190);
		
		/*
		 * Finally create the window, and place all the components in it
		 */
		// Create the main window
		mainFrame = new MainFrame();
		mainFrame.getMainFrame().getContentPane().setBackground(Color.black);
		JComponent glassPane = DragLayer.get();
		mainFrame.getMainFrame().setGlassPane(glassPane);
		glassPane.setVisible(true);
		
		// *** Create the layout ***
		// left side panel
		JComponent leftColumn = createLeftPanel();
		
		// Chat entry and chat log
		final JComponent chatBox = SBoxLayout.createContainer(SBoxLayout.VERTICAL);
		// Set maximum size to prevent the entry requesting massive widths, but
		// force expand if there's extra space anyway 
		chatText.getPlayerChatText().setMaximumSize(new Dimension(screenSize.width, Integer.MAX_VALUE));
		chatBox.add(chatText.getPlayerChatText(), SBoxLayout.constraint(SLayout.EXPAND_X));
		
		chatBox.add(gameLog, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		chatBox.setMinimumSize(chatText.getPlayerChatText().getMinimumSize());
		chatBox.setMaximumSize(new Dimension(screenSize.width, Integer.MAX_VALUE));
		
		// Give the user the ability to make the the game area less tall
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane, chatBox);
		splitPane.setBorder(null);
		// Works for showing the resize, but is extremely flickery
		//splitPane.setContinuousLayout(true);
		pane.addComponentListener(new SplitPaneResizeListener(screen, splitPane));
		
		containerPanel = createContainerPanel();
		
		// Avoid panel drawing overhead
		final Container windowContent = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL);
		mainFrame.getMainFrame().setContentPane(windowContent);
		
		// Finally add the left pane, and the games screen + chat combo
		// Make the panel take any horizontal resize
		windowContent.add(leftColumn, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		leftColumn.setMinimumSize(new Dimension());
		
		/*
		 * Put the splitpane and the container panel to a subcontainer to make
		 * squeezing the window affect the left pane first rather than the right
		 */
		JComponent rightSide = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL);
		rightSide.add(splitPane, SBoxLayout.constraint(SLayout.EXPAND_Y));
		rightSide.add(containerPanel, SBoxLayout.constraint(SLayout.EXPAND_Y));
		rightSide.setMinimumSize(rightSide.getPreferredSize());
		windowContent.add(rightSide, SBoxLayout.constraint(SLayout.EXPAND_Y));
				
		/*
		 * Handle focus assertion and window closing
		 */
		mainFrame.getMainFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}

			@Override
			public void windowActivated(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}

			@Override
			public void windowGainedFocus(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}

			@Override
			public void windowClosing(final WindowEvent e) {
				requestQuit();
			}
		});
		
		mainFrame.getMainFrame().pack();
		setInitialWindowStates();

		/*
		 *  A bit roundabout way to calculate the desired minsize, but
		 *  different java versions seem to take the window decorations
		 *  in account in rather random ways.
		 */
		final int width = mainFrame.getMainFrame().getWidth() 
		- minimap.getComponent().getWidth() - containerPanel.getWidth();
		final int height = mainFrame.getMainFrame().getHeight() - gameLog.getHeight();

		mainFrame.getMainFrame().setMinimumSize(new Dimension(width, height));
		mainFrame.getMainFrame().setVisible(true);

		/*
		 * For small screens. Setting the maximum window size does
		 * not help - pack() happily ignores it.
		 */
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Dimension current = mainFrame.getMainFrame().getSize();
		mainFrame.getMainFrame().setSize(Math.min(current.width, maxBounds.width), 
				Math.min(current.height, maxBounds.height));

		/*
		 * Needed for small screens; Sometimes the divider is placed
		 * incorrectly unless we explicitly set it. Try to fit it on the
		 * screen and show a bit of the chat.
		 */
		splitPane.setDividerLocation(Math.min(stendhal.getScreenSize().height,
				maxBounds.height  - 80));

		directionRelease = null;
	
		// register the slash actions in the client side command line parser
		SlashActionRepository.register();

		checkAndComplainAboutJavaImplementation();
		WorldObjects.addWorldListener(getSoundSystemFacade());
	} // constructor
	
	/**
	 * Create the left side panel of the client.
	 * 
	 * @return A component containing the components left of the game screen
	 */
	private JComponent createLeftPanel() {
		minimap = new MapPanelController(client);
		final StatsPanelController stats = StatsPanelController.get();
		final BuddyPanelController buddies = new BuddyPanelController();
		final JComponent buddyPane = new ScrolledViewport((JComponent) buddies.getComponent()).getComponent();
		buddyPane.setBorder(null);
		
		final JComponent leftColumn = SBoxLayout.createContainer(SBoxLayout.VERTICAL);
		leftColumn.add(minimap.getComponent(), SBoxLayout.constraint(SLayout.EXPAND_X));
		leftColumn.add(stats.getComponent(), SBoxLayout.constraint(SLayout.EXPAND_X));
		
		// Add a background for the tabs. The column itself has none.
		JPanel tabBackground = new JPanel();
		tabBackground.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));
		JTabbedPane tabs = new JTabbedPane();
		tabs.setFocusable(false);
		tabs.add("Friends", buddyPane);
		tabBackground.add(tabs, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		leftColumn.add(tabBackground, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		
		return leftColumn;
	}
	
	/**
	 * Create the container panel (right side panel), and its child components.
	 * 
	 * @return container panel
	 */
	private ContainerPanel createContainerPanel() {
		ContainerPanel containerPanel = new ContainerPanel();
		containerPanel.setMinimumSize(new Dimension(0, 0));
		
		/*
		 * Contents of the containerPanel
		 */
		// The setting bar to the top
		settings = new SettingsPanel();
		settings.add("accountcontrol");
		settings.add("settings");
		settings.add("rp");
		settings.add("help");
		containerPanel.add(settings, SBoxLayout.constraint(SLayout.EXPAND_X));
		
		// Character window
		character = new Character();
		character.setAlignmentX(Component.LEFT_ALIGNMENT);
		containerPanel.addRepaintable(character);
		
		// Create the bag window
		inventory = new SlotWindow("bag", 3, 4);
		inventory.setCloseable(false);
		inventory.setInspector(containerPanel);
		containerPanel.addRepaintable(inventory);
		
		keyring = new KeyRing();
		keyring.setAlignmentX(Component.LEFT_ALIGNMENT);
		containerPanel.addRepaintable(keyring);
		client.addFeatureChangeListener(keyring);
		
		return containerPanel;
	}
	
	/**
	 * Modify the states of the on screen windows. The window manager normally
	 * restores the state of the window as it was on the previous session. For
	 * some windows this is not desirable.
	 * <p>
	 * <em>Note:</em> This need to be called from the event dispatch thread.
	 */
	private void setInitialWindowStates() {
		/*
		 * Window manager may try to restore the visibility of the dialog when
		 * it's added to the pane.
		 */
		quitDialog.getQuitDialog().setVisible(false);
		// Windows may have been closed in old clients
		character.setVisible(true);
		inventory.setVisible(true);
		/*
		 * Keyring, on the other hand, *should* be hidden until revealed
		 * by feature change 
		 */
		keyring.setVisible(false);
	}

	private void checkAndComplainAboutJavaImplementation() {
		final String vmName = System.getProperty("java.vm.name", "unknown").toLowerCase(Locale.ENGLISH);
		if ((vmName.indexOf("hotspot") < 0) && (vmName.indexOf("openjdk") < 0)) {
			final String text = "Stendhal is developed and tested on Sun Java and OpenJDK. You are using " 
				+ System.getProperty("java.vm.vendor", "unknown") + " " 
				+ System.getProperty("java.vm.name", "unknown") 
				+ " so there may be some problems like a black or grey screen.\n"
				+ " If you have coding experience with your JDK, we are looking for help.";
			addEventLine(new HeaderLessEventLine(text, NotificationType.ERROR));
		}
	}

	private void cleanup() {
		chatText.saveCache();
		logger.debug("Exit");
		System.exit(0);
	}

	/**
	 * Add a native in-window dialog to the screen.
	 *
	 * @param comp
	 *            The component to add.
	 */
	public void addDialog(final Component comp) {
		pane.add(comp, JLayeredPane.PALETTE_LAYER);
	}

	/**
	 * Start the game loop thread.
	 * 
	 * @param gameScreen
	 */
	public void startGameLoop(final GameScreen gameScreen) {
		Thread loop = new Thread(new Runnable() {
			public void run() {
				gameLoop(gameScreen);
				// gameLoop runs until the client quit 
				cleanup();
			}
		}, "Game loop");
		loop.start();
	}

	private void gameLoop(final GameScreen gameScreen) {
		final int frameLength = (int) (1000.0 / stendhal.FPS_LIMIT);
		int fps = 0;
		final GameObjects gameObjects = client.getGameObjects();
		final StaticGameLayers gameLayers = client.getStaticGameLayers();

		try {
			SoundGroup group = initSoundSystem();
			group.play("harp-1", 0, null, null, false, true);
		} catch (RuntimeException e) {
			logger.error(e, e);
		}
		
		// keep looping until the game ends
		long refreshTime = System.currentTimeMillis();
		long lastFpsTime = refreshTime;
		long lastMessageHandle = refreshTime;

		gameRunning = true;

		boolean canExit = false;
		while (!canExit) {
			try {
				fps++;
				// figure out what time it is right after the screen flip then
				// later we can figure out how long we have been doing redrawing
				// / networking, then we know how long we need to sleep to make
				// the next flip happen at the right time
				screen.nextFrame();
				final long now = System.currentTimeMillis();
				final int delta = (int) (now - refreshTime);
				refreshTime = now;
	
				logger.debug("Move objects");
				gameObjects.update(delta);
	
				if (gameLayers.isAreaChanged() && client.tryAcquireDrawingSemaphore()) {
					try {
						/*
						 * Update the screen
						 */
						screen.setMaxWorldSize(gameLayers.getWidth(), gameLayers.getHeight());
						screen.center();
	
						// [Re]create the map
		
						final CollisionDetection cd = gameLayers.getCollisionDetection();
						final CollisionDetection pd = gameLayers.getProtectionDetection();

						if (cd != null) {
							minimap.update(cd, pd,
									screen.getGraphicsConfiguration(),
									gameLayers.getArea());
						} 
						gameLayers.resetChangedArea();
					} finally {
						client.releaseDrawingSemaphore();
					}
				}

				final User user = User.get();

				if (user != null) {
					// check if the player object has changed.
					// Note: this is an exact object reference check
					if (user != lastuser) {
						character.setPlayer(user);
						keyring.setSlot(user, "keyring");
						inventory.setSlot(user, "bag");
						lastuser = user;
					}
				}

				triggerPainting();
	
				logger.debug("Query network");
	
				if (client.loop(0)) {
					lastMessageHandle = refreshTime;
				}
	
				/*
				 * Process delayed direction release
				 */
				if ((directionRelease != null) && directionRelease.hasExpired()) {
					client.removeDirection(directionRelease.getDirection(),
							directionRelease.isFacing());
	
					directionRelease = null;
				}

				if (logger.isDebugEnabled()) {
					if ((refreshTime - lastFpsTime) >= 1000L) {
						logger.debug("FPS: " + fps);
						final long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
						final long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
	
						logger.debug("Total/Used memory: " + totalMemory + "/"
								+ (totalMemory - freeMemory));
	
						fps = 0;
						lastFpsTime = refreshTime;
					}
				}

				// Shows a offline icon if no messages are received in 30 seconds.
				if ((refreshTime - lastMessageHandle > 30000L)
						|| !client.getConnectionState()) {
					setOffline(true);
				} else {
					setOffline(false);
				}

				logger.debug("Start sleeping");
				// we know how long we want per screen refresh (40ms) then
				// we add the refresh time and subtract the current time
				// leaving us with the amount we still need to sleep.
				long wait = frameLength + refreshTime - System.currentTimeMillis();

				if (wait > 0) {
					if (wait > 100L) {
						logger.info("Waiting " + wait + " ms");
						wait = 100L;
					}
	
					try {
						Thread.sleep(wait);
					} catch (final InterruptedException e) {
						logger.error(e, e);
					}
				}
	
				logger.debug("End sleeping");
	
				if (!gameRunning) {
					logger.info("Request logout");
					try {
						/*
						 * We request server permision to logout. Server can deny
						 * it, unless we are already offline.
						 */
						if (screen.getOffline() || client.logout()) {
							canExit = true;
						} else {
							logger.warn("You can't logout now.");
							gameRunning = true;
						}
					} catch (final Exception e) { // catch InvalidVersionException, TimeoutException and BannedAddressException
						/*
						 * If we get a timeout exception we accept exit request.
						 */
						canExit = true;
						logger.error(e, e);
					}
				}
			} catch (RuntimeException e) {
				logger.error(e, e);
			}
		}
	
		getSoundSystemFacade().exit();
	}

	private int paintCounter;
	private void triggerPainting() {
		if (mainFrame.getMainFrame().getState() != Frame.ICONIFIED) {
			paintCounter++;
			if (mainFrame.getMainFrame().isActive() || System.getProperty("stendhal.skip.inactive", "false").equals("false") || paintCounter >= 20) {
				paintCounter = 0;
				logger.debug("Draw screen");
				minimap.refresh();
				containerPanel.repaintChildren();
				screen.draw();
			}
		}
    }

	private SoundGroup initSoundSystem() {
		SoundGroup group = getSoundSystemFacade().getGroup(SoundLayer.USER_INTERFACE.groupName);
		group.loadSound("harp-1", "audio:/harp-1.ogg", Type.OGG, false);
		group.loadSound("click-4", "audio:/click-4.ogg", Type.OGG, false);
		group.loadSound("click-5", "audio:/click-5.ogg", Type.OGG, false);
		group.loadSound("click-6", "audio:/click-6.ogg", Type.OGG, false);
		group.loadSound("click-8", "audio:/click-8.ogg", Type.OGG, false);
		group.loadSound("click-10", "audio:/click-10.ogg", Type.OGG, false);
		return group;
	}

	/**
	 * Convert a keycode to the corresponding direction.
	 *
	 * @param keyCode
	 *            The keycode.
	 *
	 * @return The direction, or <code>null</code>.
	 */
	protected Direction keyCodeToDirection(final int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
			return Direction.LEFT;

		case KeyEvent.VK_RIGHT:
			return Direction.RIGHT;

		case KeyEvent.VK_UP:
			return Direction.UP;

		case KeyEvent.VK_DOWN:
			return Direction.DOWN;

		default:
			return null;
		}
	}

	protected void onKeyPressed(final KeyEvent e) {
		if (e.isShiftDown()) {
			/*
			 * We are going to use shift to move to previous/next line of text
			 * with arrows so we just ignore the keys if shift is pressed.
			 */
			return;
		}

		switch (e.getKeyCode()) {
		case KeyEvent.VK_L:
			if (e.isControlDown()) {
				/*
				 * Ctrl+L Make game log visible
				 */
				SwingUtilities.getRoot(gameLog).setVisible(true);
			}

			break;

		case KeyEvent.VK_R:
			if (e.isControlDown()) {
				/*
				 * Ctrl+R Remove text bubbles
				 */
				screen.clearTexts();
			}

			break;

		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			/*
			 * Ctrl means face, otherwise move
			 */
			final Direction direction = keyCodeToDirection(e.getKeyCode());

			if (e.isAltGraphDown()) {
				final User user = User.get();

				final EntityView view = screen.getEntityViewAt(user.getX()
						+ direction.getdx(), user.getY() + direction.getdy());

				if (view != null) {
					final IEntity entity = view.getEntity();
					if (!entity.equals(user)) {
						view.onAction();
					}
				}
			}

			processDirectionPress(direction, e.isControlDown());
		}
	}

	protected void onKeyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			/*
			 * Ctrl means face, otherwise move
			 */
			processDirectionRelease(keyCodeToDirection(e.getKeyCode()),
					e.isControlDown());
		}
	}

	/**
	 * Handle direction press actions.
	 *
	 * @param direction
	 *            The direction.
	 * @param facing
	 *            If facing only.
	 */
	protected void processDirectionPress(final Direction direction, final boolean facing) {
		if (directionRelease != null) {
			if (directionRelease.check(direction, facing)) {
				/*
				 * Cancel pending release
				 */
				logger.debug("Repeat suppressed");
				directionRelease = null;
				return;
			} else {
				/*
				 * Flush pending release
				 */
				client.removeDirection(directionRelease.getDirection(),
						directionRelease.isFacing());

				directionRelease = null;
			}
		}

		client.addDirection(direction, facing);
	}

	/**
	 * Handle direction release actions.
	 *
	 * @param direction
	 *            The direction.
	 * @param facing
	 *            If facing only.
	 */
	protected void processDirectionRelease(final Direction direction, final boolean facing) {
		if (directionRelease != null) {
			if (directionRelease.check(direction, facing)) {
				/*
				 * Ignore repeats
				 */
				return;
			} else {
				/*
				 * Flush previous release
				 */
				client.removeDirection(directionRelease.getDirection(),
						directionRelease.isFacing());
			}
		}

		directionRelease = new DelayedDirectionRelease(direction, facing);
	}

	/**
	 * Shutdown the client. Save state and tell the main loop to stop.
	 */
	public void shutdown() {
		gameRunning = false;

		// try to save the window configuration
		WtWindowManager.getInstance().save();
	}

	//
	// <StendhalGUI>
	//

	/**
	 * Add a new window.
	 *
	 * @param mw
	 *            A managed window.
	 *
	 * @throws IllegalArgumentException
	 *             If an unsupported ManagedWindow is given.
	 */
	public void addWindow(final ManagedWindow mw) {
		if (mw instanceof InternalManagedWindow) {
			addDialog((InternalManagedWindow) mw);
		} else {
			throw new IllegalArgumentException("Unsupport ManagedWindow type: "
					+ mw.getClass().getName());
		}
	}

	//
	// j2DClient
	//

	/**
	 * Add an event line.
	 *
	 */
	public void addEventLine(final EventLine line) {
		gameLog.addLine(line);
	}

	/**
	 * adds a text box on the screen
	 *
	 * @param x  x
	 * @param y  y
	 * @param text text to display
	 * @param type type of text
	 * @param isTalking chat?
	 */
	public void addGameScreenText(final double x, final double y, final String text, final NotificationType type,
			final boolean isTalking) {
		screen.addText(x, y, text, type, isTalking);
	}
	
	/**
	 * Display a box for a reached achievement
	 * 
	 * @param title the title of the achievement
	 * @param description the description of the achievement
	 * @param category the category of the achievement
	 */
	public void addAchievementBox(String title, String description, String category) {
		screen.addAchievementBox(title, description, category);
	}

	/**
	 * Initiate outfit selection by the user.
	 */
	public void chooseOutfit() {
		int outfit;

		final RPObject player = userContext.getPlayer();

		if (player.has("outfit_org")) {
			outfit = player.getInt("outfit_org");
		} else {
			outfit = player.getInt("outfit");
		}

		// Should really keep only one instance of this around
		final OutfitDialog dialog = new OutfitDialog(mainFrame.getMainFrame(), "Set outfit", outfit);
		dialog.setVisible(true);
	}
	
	/**
	 * Get the main window component.
	 * 
	 * @return main window
	 */
	public Frame getMainFrame() {
		return mainFrame.getMainFrame();
	}

	/**
	 * Get the current game screen height.
	 *
	 * @return The height.
	 */
	public int getHeight() {
		return screen.getHeight();
	}

	/**
	 * Get the current game screen width.
	 *
	 * @return The width.
	 */
	public int getWidth() {
		return screen.getWidth();
	}



	/**
	 * Set the input chat line text.
	 *
	 * @param text
	 *            The text.
	 */
	public void setChatLine(final String text) {
		chatText.setChatLine(text);
		
	}
	
	public void clearGameLog() {
		gameLog.clear();
	}

	/**
	 * Set the user's position.
	 *
	 * @param x
	 *            The user's X coordinate.
	 * @param y
	 *            The user's Y coordinate.
	 */
	public void setPosition(final double x, final double y) {
		positionChangeListener.positionChanged(x, y);
	}

	/**
	 * Sets the offline indication state.
	 *
	 * @param offline
	 *            <code>true</code> if offline.
	 */
	public void setOffline(final boolean offline) {
		screen.setOffline(offline);
	}

	//
	//

	protected class GameKeyHandler implements KeyListener {
		public void keyPressed(final KeyEvent e) {
			onKeyPressed(e);
		}

		public void keyReleased(final KeyEvent e) {
			onKeyReleased(e);
		}

		public void keyTyped(final KeyEvent e) {
			if (e.getKeyChar() == 27) {
				// Escape
				requestQuit();
			}
		}
	}


	protected static class DelayedDirectionRelease {
		/**
		 * The maximum delay between auto-repeat release-press.
		 */
		protected static final long DELAY = 50L;

		protected long expiration;

		protected Direction dir;

		protected boolean facing;

		public DelayedDirectionRelease(final Direction dir, final boolean facing) {
			this.dir = dir;
			this.facing = facing;

			expiration = System.currentTimeMillis() + DELAY;
		}

		//
		// DelayedDirectionRelease
		//

		/**
		 * Get the direction.
		 *
		 * @return The direction.
		 */
		public Direction getDirection() {
			return dir;
		}

		/**
		 * Determine if the delay point has been reached.
		 *
		 * @return <code>true</code> if the delay time has been reached.
		 */
		public boolean hasExpired() {
			return System.currentTimeMillis() >= expiration;
		}

		/**
		 * Determine if the facing only option was used.
		 *
		 * @return <code>true</code> if facing only.
		 */
		public boolean isFacing() {
			return facing;
		}

		/**
		 * Check if a new direction matches the existing one, and if so, reset
		 * the expiration point.
		 *
		 * @param dir
		 *            The direction.
		 * @param facing
		 *            The facing flag.
		 *
		 * @return <code>true</code> if this is a repeat.
		 */
		public boolean check(final Direction dir, final boolean facing) {
			if (!this.dir.equals(dir)) {
				return false;
			}

			if (this.facing != facing) {
				return false;
			}

			final long now = System.currentTimeMillis();

			if (now >= expiration) {
				return false;
			}

			expiration = now + DELAY;

			return true;
		}
	}


	public void requestQuit() {
		if (client.getConnectionState() || !screen.getOffline()) {
			quitDialog.requestQuit();
		} else {
			System.exit(0);
		}
	}

	public IPerceptionListener getPerceptionListener() {
		return perceptionListener;
	}

	/**
	 * Get the client.
	 *
	 * @return The client.
	 */
	public StendhalClient getClient() {
		return client;
	}
	
	/**
	 * The layered pane where the game screen is does not automatically resize 
	 * the game screen. This handler is needed to do that work.
	 */
	private static class SplitPaneResizeListener implements ComponentListener {
		private Component child;
		private JSplitPane splitPane;
		
		public SplitPaneResizeListener(Component child, JSplitPane splitPane) {
			this.child = child;
			this.splitPane = splitPane;
		}
		
		public void componentHidden(ComponentEvent e) {
			// do nothing
		}

		public void componentMoved(ComponentEvent e) {
			// do nothing
		}

		public void componentResized(ComponentEvent e) {
			Dimension newSize = e.getComponent().getSize();
			if (newSize.height > stendhal.getScreenSize().height) {
				/*
				 *  There is no proper limit setting for JSplitPane,
				 *  so return the divider to the maximum allowed height
				 *  by force.
				 */
				splitPane.setDividerLocation(stendhal.getScreenSize().height
						+ splitPane.getInsets().top);
			} else {
				child.setSize(newSize);
			}
		}

		public void componentShown(ComponentEvent e) {
			// do nothing
		}
	}

	/**
	 * sets the cursor
	 *
	 * @param cursor Cursor
	 */
	public void setCursor(Cursor cursor) {
		pane.setCursor(cursor);
	}

	/**
	 * gets the sound system
	 *
	 * @return SoundSystemFacade
	 */
	public SoundSystemFacade getSoundSystemFacade() {
		if (soundSystemFacade == null) {
			try {
				soundSystemFacade = new games.stendhal.client.sound.sound.SoundSystemFacadeImpl();
			} catch (RuntimeException e) {
				soundSystemFacade = new NoSoundFacade();
				logger.error(e, e);
				soundSystemFacade = new NoSoundFacade();
				logger.error(e, e);
			}
		}
		return soundSystemFacade;
	}
}
