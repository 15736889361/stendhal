/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2005 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
/*
 * SettingsPanel.java
 *
 * Created on 26. Oktober 2005, 20:12
 */

package games.stendhal.client.gui;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.wt.ButtonCommandList;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * The panel where you can adjust your settings.
 */
public class SettingsPanel extends JPanel {
	/**
	 * serial version uid
	 */
	private static final long serialVersionUID = -8601268317610622278L;

	/**
	 * The button insets width.
	 */
	private static final int BUTTON_PADDING = 3;
	/**
	 * Space between buttons.
	 */
	private static final int SPACING = 2;
	
	/**
	 * Stores the commands available for each group label 
	 */
	private static Map<String, String[]> groupsAndCommands;
	/** Tooltip texts for each menu button indexed by the group label */
	private static final Map<String, String> tooltips = new HashMap<String, String>();
	
	/**
	 * Set the commands available for each group label 
	 */
	private static void initialize() {
		groupsAndCommands = new HashMap<String, String[]>();
		groupsAndCommands.put("help", new String[] {"Commands", "Manual", "FAQ", "Rules", "Atlas"});
		groupsAndCommands.put("accountcontrol", new String[] {"Change Password", "Merge", "Login History"});
		groupsAndCommands.put("settings", new String[] {"Mute", "Settings"});
		groupsAndCommands.put("rp", new String[] {"Who", "Hall Of Fame", "Travel Log"});
	//	groupsAndCommands.put("contribute", new String[] {"Report Bug", "Request Feature", "Chat"});
		
		tooltips.put("help", "Help topics");
		tooltips.put("accountcontrol", "Manage accounts");
		tooltips.put("settings", "Settings");
		tooltips.put("rp", "Game commands");
	}
	
	static {
		initialize();
	}

	/**
	 * Creates a new instance of SettingsPanel.
	 */
	public SettingsPanel() {
		setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL, SPACING));
		setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING));
	}

	/**
	 * Add a window entry.
	 * 
	 * @param label
	 *            The menu label, which also defines the image to look up, and the group of commands if relevant.
	 */
	public void add(final String label) {
		ImageIcon icon = new ImageIcon(SettingsPanel.class.getClassLoader().getResource("data/gui/"+label+".png"));
		final JButton button = new JButton(icon);
		button.setMargin(new Insets(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING));
		/*
		 * Don't let the buttons take focus. Keyboard focus anywhere else but
		 * in the game screen/chat entry is extremely confusing.
		 */
		button.setFocusable(false);
		
		/*
		 * Add the popup menu
		 */
		ButtonCommandList commands = new ButtonCommandList(label, groupsAndCommands.get(label));
		button.setComponentPopupMenu(commands);
		
		button.addActionListener(new ButtonListener(button));
		button.setToolTipText(tooltips.get(label));
		add(button);
	}

	/**
	 * Listener for button presses. Show the associated popup menu below
	 * the button.
	 */
	private static class ButtonListener implements ActionListener {
		JButton button;
		
		public ButtonListener(JButton button) {
			this.button = button;
		}
		
		public void actionPerformed(ActionEvent e) {
			JPopupMenu menu = button.getComponentPopupMenu();
			menu.show(button, 0, button.getHeight());
		}
	}
}
