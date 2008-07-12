/*
 * @(#) src/games/stendhal/client/gui/wt/core/WtPopupMenu.java
 *
 * $Id$
 */
package games.stendhal.client.gui.wt.core;

//
//

import games.stendhal.client.gui.styled.WoodStyle;
import games.stendhal.client.gui.styled.swing.StyledJPopupMenu;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.event.MenuKeyEvent;

/**
 * A popup-menu that will redirect most key events to it's invoker.
 */
public abstract class WtPopupMenu extends StyledJPopupMenu {

	public WtPopupMenu(String name) {
		super(WoodStyle.getInstance(), name);
	}

	//
	// WtPopupMenu
	//

	/**
	 * Create a menu item that will redirect it's key events.
	 * 
	 * @param label
	 * @param icon
	 * @return new Menuitem
	 * 
	 * 
	 */
	protected JMenuItem createItem(String label, Icon icon) {
		return new RedirectingMenuItem(label, icon);
	}

	/**
	 * Redirect key event to the menu's invoker.
	 * 
	 * @param ev
	 * 
	 */
	protected void redirectEvent(MenuKeyEvent ev) {
		Component invoker;
		invoker = getInvoker();
		if (invoker != null) {
			KeyEvent nev;
			KeyListener[] listeners;

			nev = new KeyEvent(invoker, ev.getID(), ev.getWhen(), ev
					.getModifiersEx(), ev.getKeyCode(), ev.getKeyChar(), ev
					.getKeyLocation());

			/*
			 * Call listeners directly to avoid modal redirect
			 */
			listeners = invoker.getKeyListeners();

			switch (nev.getID()) {
			case KeyEvent.KEY_PRESSED:
				for (KeyListener l : listeners) {
					l.keyPressed(nev);
				}
				break;

			case KeyEvent.KEY_RELEASED:
				for (KeyListener l : listeners) {
					l.keyReleased(nev);
				}
				break;

			case KeyEvent.KEY_TYPED:
				for (KeyListener l : listeners) {
					l.keyTyped(nev);
				}
				break;
			default:
				// do nothing
			}

			ev.consume();
		}
	}

	//
	//

	protected class RedirectingMenuItem extends JMenuItem {
		private static final long serialVersionUID = -1607102841664745919L;

		public RedirectingMenuItem(String label, Icon icon) {
			super(label, icon);
		}

		//
		// JMenuItem
		//

		@Override
		public void processMenuKeyEvent(MenuKeyEvent ev) {
			switch (ev.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				break;

			default:
				redirectEvent(ev);
			}

			if (!ev.isConsumed()) {
				super.processMenuKeyEvent(ev);
			}
		}
	}
}
