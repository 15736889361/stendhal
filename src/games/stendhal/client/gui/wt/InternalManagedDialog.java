/*
 * @(#) src/games/stendhal/client/gui/wt/InternalManagedDialog.java
 *
 * $Id$
 */

package games.stendhal.client.gui.wt;

//
//

import games.stendhal.client.gui.ManagedWindow;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.WoodStyle;
import games.stendhal.client.gui.styled.swing.StyledJPanel;
import games.stendhal.client.gui.wt.core.WtCloseListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * A base internal dialog in swing that implements ManagedWindow.
 *
 */
public class InternalManagedDialog implements ManagedWindow {
	/** size of the titlebar */
	private static final int TITLEBAR_HEIGHT	= 13;


	/**
	 * The close button.
	 */
	protected JButton		closeButton;

	/**
	 * The window content.
	 */
	protected JComponent		content;

	/**
	 * Listeners interesting in close notifications.
	 */
	protected List<WtCloseListener>	closeListeners;

	/**
	 * The simulated dialog.
	 */
	protected Panel			dialog;

	/**
	 * Start of a window drag.
	 */
	protected Point			dragStart;

	/**
	 * The content pane.
	 */
	protected StyledJPanel		contentPane;

	/**
	 * Whether the dialog is minimized.
	 */
	protected boolean		minimized;

	/**
	 * The minimize button.
	 */
	protected JButton		minimizeButton;

	/**
	 * If the window can be moved.
	 */
	protected boolean		movable;

	/**
	 * The window name.
	 */
	protected String		name;

	protected ContentSizeChangeCB	sizeChangeListener;

	/**
	 * The titlebar.
	 */
	protected JComponent		titlebar;

	/**
	 * The title label.
	 */
	protected JLabel		titleLabel;


	/**
	 * Create a managed dialog window.
	 *
	 * @param	name		The logical name.
	 * @param	title		The dialog window title.
	 */
	public InternalManagedDialog(String name, String title) {
		Style		style;
		Font		font;
		Color		color;


		this.name = name;

		style = WoodStyle.getInstance();

		minimized = false;
		movable = true;
		closeListeners = new LinkedList<WtCloseListener>();

		/*
		 * Heavy weight to work with AWT canvas
		 */
		dialog = new Panel();
		dialog.setLayout(null);

		contentPane = new StyledJPanel(style);
		contentPane.setLayout(null);

		dialog.add(contentPane);


		/*
		 * Titlebar
		 */
		titlebar = new StyledJPanel(style);
		titlebar.setMinimumSize(new Dimension(1, TITLEBAR_HEIGHT));
		titlebar.setLayout(new BoxLayout(titlebar, BoxLayout.X_AXIS));
		titlebar.addMouseListener(new TBDragClickCB());
		titlebar.addMouseMotionListener(new TBDragMoveCB());


		contentPane.add(titlebar);

		/*
		 * Title
		 */
		titleLabel = new JLabel(title, SwingConstants.LEFT);
		titleLabel.setOpaque(false);
		titleLabel.setBorder(BorderFactory.createEmptyBorder());

		if((font = style.getFont()) != null)
			font = titleLabel.getFont();

		titleLabel.setFont(font.deriveFont(Font.BOLD));

		if((color = style.getForeground()) != null)
			titleLabel.setForeground(color);

		titlebar.add(titleLabel);

		/*
		 * Spacing
		 */
		titlebar.add(Box.createHorizontalGlue());

		/*
		 * Minimize button
		 */
		minimizeButton = new JButton(new MinimizeIcon());
		minimizeButton.setDisabledIcon(new DisabledIcon());
		minimizeButton.setFocusable(false);
		minimizeButton.setMargin(new Insets(0, 0, 0, 0));
		minimizeButton.setBorder(BorderFactory.createEmptyBorder());
		minimizeButton.setContentAreaFilled(false);
		minimizeButton.addActionListener(new MinimizeCB());
		titlebar.add(minimizeButton);

		/*
		 * Spacer
		 */
		titlebar.add(Box.createHorizontalStrut(1));

		/*
		 * Close button
		 */
		closeButton = new JButton(new CloseIcon());
		closeButton.setDisabledIcon(new DisabledIcon());
		closeButton.setFocusable(false);
		closeButton.setMargin(new Insets(0, 0, 0, 0));
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		closeButton.addActionListener(new CloseCB());
		titlebar.add(closeButton);

		sizeChangeListener = new ContentSizeChangeCB();

		pack();
		WtWindowManager.getInstance().formatWindow(this);
	}


	//
	// InternalManagedDialog
	//

	/**
	 * Close window.
	 */
	protected void closeCB() {
		setVisible(false);
	}


	/**
	 * Toggle minimization.
	 */
	protected void minimizeCB() {
		setMinimized(!isMinimized());
	}


	/**
	 * Do simulated dialog layout.
	 */
	protected void pack() {
		Dimension	tbSize;
		Dimension	cSize;
		int		width;


		tbSize = titlebar.getPreferredSize();

		if(content != null) {
			cSize = content.getPreferredSize();
		} else {
			cSize = new Dimension(0, 0);
		}

		width = Math.max(tbSize.width, cSize.width);

		titlebar.setBounds(0, 0, width, tbSize.height);
		titlebar.validate();

		if(content != null) {
			content.setBounds(
				0, tbSize.height, width, cSize.height);

			content.validate();
		}

		if(isMinimized()) {
			dialog.setBounds(0, 0, width, tbSize.height);
			contentPane.setBounds(0, 0, width, tbSize.height);
		} else {
			dialog.setSize(
				width, tbSize.height + cSize.height);

			contentPane.setSize(
				width, tbSize.height + cSize.height);
		}
	}


	protected void setTitle(String title) {
		titleLabel.setText(title);
	}


	//
	// ManagedDialog
	//

	/**
	 * Handle begining of titlebar drag.
	 *
	 * @param	x		The X coordinate.
	 * @param	y		The Y coordinate.
	 */
	protected void tbDragBegin(int x, int y) {
		if(isMovable()) {
			dragStart = SwingUtilities.convertPoint(
				titlebar, x, y, dialog);
		}
	}


	/**
	 * Handle end of titlebar drag.
	 *
	 * @param	x		The X coordinate.
	 * @param	y		The Y coordinate.
	 */
	protected void tbDragEnd(int x, int y) {
		tbDragMovement(x, y);
		dragStart = null;
		windowMoved();
	}


	/**
	 * Handle titlebar drag movement.
	 *
	 * @param	x		The X coordinate.
	 * @param	y		The Y coordinate.
	 */
	protected void tbDragMovement(int x, int y) {
		Point		p;
		Container	parent;


		if(dragStart != null) {
			parent = dialog.getParent();

			p = SwingUtilities.convertPoint(titlebar, x, y, parent);
			p.x -= dragStart.x;
			p.y -= dragStart.y;

			/*
			 * Keep in parent window
			 */
			if(p.x < 0) {
				p.x = 0;
			} else if((p.x + dialog.getWidth()) > parent.getWidth()) {
				p.x = parent.getWidth() - dialog.getWidth();
			}

			if(p.y < 0) {
				p.y = 0;
			} else if((p.y + dialog.getHeight()) > parent.getHeight()) {
				p.y = parent.getHeight() - dialog.getHeight();
			}


			dialog.setLocation(p);
		}
	}


	/**
	 * Get the actual dialog.
	 *
	 * @return	The dialog.
	 */
	public Container getDialog() {
		return dialog;
	}


	/**
	 * Call all registered close listeners.
	 */
	protected void fireCloseListeners() {
		WtCloseListener []	listeners;


		listeners = (WtCloseListener []) closeListeners.toArray(
			new WtCloseListener[closeListeners.size()]);

		for(WtCloseListener l : listeners)
			l.onClose(getName());
	}


	/**
	 * Set the content component.
	 * For now, if the content wishes to resize the dialog, it should
	 * set a client property named <code>size-change</code> on itself.
	 *
	 * @param	content		A component to implement the content.
	 */
	public void setContent(JComponent content) {
		if(this.content != null) {
			this.content.removePropertyChangeListener(
				sizeChangeListener);

			contentPane.remove(this.content);
		}

		this.content = content;

		contentPane.add(content);

		content.addPropertyChangeListener(
			"size-change", sizeChangeListener);

		pack();
	}


	/**
	 * Called when the window's position changes.
	 */
	protected void windowMoved() {
		/*
		 * Update saved state
		 */
		WtWindowManager.getInstance().moveTo(this, getX(), getY());
	}


	//
	// ManagedWindow
	//

	/**
	 * Get the managed window name.
	 *
	 * @return	The logical window name (not title).
	 */
	public String getName() {
		return name;
	}


	/**
	 * Get X coordinate of the window.
	 *
	 * @return	A value sutable for passing to <code>moveTo()</code>.
	 */
	public int getX() {
		return dialog.getX();
	}


	/**
	 * Get Y coordinate of the window.
	 *
	 * @return	A value sutable for passing to <code>moveTo()</code>.
	 */
	public int getY() {
		return dialog.getY();
	}


	/**
	 * Determine if the window is minimized.
	 *
	 * @return	<code>true</code> if the window is minimized.
	 */
	public boolean isMinimized() {
		return minimized;
	}


	/**
	 * Determine if the window is movable.
	 *
	 * @return	<code>true</code> if the window is movable.
	 */
	public boolean isMovable() {
		return movable;
	}


	/**
	 * Determine if the window is visible.
	 *
	 * @return	<code>true</code> if the window is visible.
	 */
	public boolean isVisible() {
		return dialog.isVisible();
	}


	/**
	 * Move to a location. This may be subject to internal representation,
	 * and should only use what was passed from <code>getX()</code> and
	 * <code>getY()</code>.
	 *
	 * @param	x		The X coordinate
	 * @param	y		The Y coordinate
	 *
	 * @return	<code>true</code> if the move was allowed.
	 */
	public boolean moveTo(int x, int y) {
		dialog.setLocation(x, y);
		return true;
	}


	/**
	 * Register a close listener.
	 *
	 * @param	listener	A close listener.
	 */
	public void registerCloseListener(WtCloseListener listener) {
		closeListeners.add(listener);
	}


	/**
	 * Unregister a close listener.
	 *
	 * @param	listener	A close listener.
	 */
	public void removeCloseListener(WtCloseListener listener) {
		closeListeners.remove(listener);
	}


	/**
	 * Set whether the window is minimizable.
	 *
	 * @param	minimizable	<code>true</code> if minimizable.
	 */
	public void setMinimizable(boolean minimizable) {
		minimizeButton.setEnabled(minimizable);
	}


	/**
	 * Set the window as minimized.
	 *
	 * @param	minimized	Whether the window should be minimized.
	 */
	public void setMinimized(boolean minimized) {
		int	cheight;


		this.minimized = minimized;

		if(minimized) {
			if(content != null) {
				content.setVisible(false);
			}

			dialog.setSize(
				titlebar.getWidth(),
				titlebar.getHeight());

			contentPane.setSize(
				titlebar.getWidth(),
				titlebar.getHeight());
		} else {
			if(content != null) {
				content.setVisible(true);
				cheight = content.getHeight();
			} else {
				cheight = 0;
			}

			dialog.setSize(
				titlebar.getWidth(),
				titlebar.getHeight() + cheight);

			contentPane.setSize(
				titlebar.getWidth(),
				titlebar.getHeight() + cheight);
		}
	}


	/**
	 * Set whether the window is movable.
	 *
	 * @param	movable		<code>true</code> if movable.
	 */
	public void setMovable(boolean movable) {
		this.movable = movable;
	}


	/**
	 * Set the window as visible (or hidden).
	 *
	 * @param	visible		Whether the window should be visible.
	 */
	public void setVisible(boolean visible) {
		dialog.setVisible(visible);

		/*
		 * Update saved state
		 */
		WtWindowManager.getInstance().setVisible(this, visible);

		/*
		 * Notify close listeners
		 */
		if(!visible)
			fireCloseListeners();
	}

	//
	//

	/**
	 * Handle content resize required property change.
	 */
	protected class ContentSizeChangeCB implements PropertyChangeListener {

		//
		// PropertyChangeListener
		//

		public void propertyChange(PropertyChangeEvent ev) {
			pack();
		}
	}


	/**
	 * Handle close button.
	 */
	protected class CloseCB implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			closeCB();
		}
	}


	/**
	 * Handle minimzation button.
	 */
	protected class MinimizeCB implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			minimizeCB();
		}
	}


	/**
	 * Disabled button icon.
	 */
	protected static class DisabledIcon implements Icon {
		//
		// Icon
		//

		public int getIconHeight() {
			return TITLEBAR_HEIGHT;
		}


		public int getIconWidth() {
			return TITLEBAR_HEIGHT;
		}


		public void paintIcon(Component c, Graphics g, int x, int y) {
		}
	}


	/**
	 * Close button icon.
	 */
	protected static class CloseIcon implements Icon {
		//
		// Icon
		//

		public int getIconHeight() {
			return TITLEBAR_HEIGHT;
		}


		public int getIconWidth() {
			return TITLEBAR_HEIGHT;
		}


		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color	oldColor;
			int	height;
			int	width;


			oldColor = g.getColor();

			height = getIconHeight();
			width = getIconWidth();

			g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
			g.fillRect(x, y, width, height);

			g.setColor(Color.BLACK);


			/*
			 * \\
			 * \\\
			 *  \\
			 */
			g.drawLine(
				x + 1, y + 2,
				x + width - 3, y + height - 2);

			g.drawLine(
				x + 1, y + 1,
				x + width - 2, y + height - 2);

			g.drawLine(
				x + 2, y + 1,
				x + width - 2, y + height - 3);

			/*
			 *  //
			 * ///
			 * //
			 */
			g.drawLine(
				x + width - 3, y + 1,
				x + 1, y + height - 3);

			g.drawLine(
				x + width - 2, y + 1,
				x + 1, y + height - 2);

			g.drawLine(
				x + width - 2, y + 2,
				x + 2, y + height - 2);


			g.setColor(oldColor);
		}
	}


	/**
	 * Minmization button icon.
	 */
	protected static class MinimizeIcon implements Icon {
		//
		// Icon
		//

		public int getIconHeight() {
			return TITLEBAR_HEIGHT;
		}


		public int getIconWidth() {
			return TITLEBAR_HEIGHT;
		}


		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color	oldColor;
			int	height;
			int	width;


			oldColor = g.getColor();

			height = getIconHeight();
			width = getIconWidth();

			g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
			g.fillRect(x, y, width, height);

			g.setColor(Color.BLACK);
			g.fillRect(x + 1, y + height - 3, width - 2, 2);

			g.setColor(oldColor);
		}
	}


	/**
	 * Mouse drag event handler for the titlebar.
	 */
	protected class TBDragMoveCB extends MouseMotionAdapter {
		/**
		 * Handle mouse drag event.
		 *
		 * @param	ev		The mouse event.
		 */
		public void mouseDragged(MouseEvent ev) {
			tbDragMovement(ev.getX(), ev.getY());
		}
	}


	/**
	 * Mouse click event handler for the titlebar.
	 */
	protected class TBDragClickCB extends MouseAdapter {
		/**
		 * Handle mouse pressed event.
		 *
		 * @param	ev		The mouse event.
		 */
		public void mousePressed(MouseEvent ev) {
			if(ev.getButton() == MouseEvent.BUTTON1) {
				tbDragBegin(ev.getX(), ev.getY());
			}
		}


		/**
		 * Handle mouse released event.
		 *
		 * @param	ev		The mouse event.
		 */
		public void mouseReleased(MouseEvent ev) {
			if(ev.getButton() == MouseEvent.BUTTON1) {
				tbDragEnd(ev.getX(), ev.getY());
			}
		}
	}
}
