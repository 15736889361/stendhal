/*
 * "Developed by Infosys 7, FH-Brandenburg, Germany
 *
 * This Software is provided as it is and is published under the GPL-License. No
 * warranty is provided. Building systems and portals based on this system is on
 * your own risk.
 *
 * @author Ken Werner modulname: Client contact: wernerk@fh-brandenburg.de"
 *
 * I asked the author for permission to use it in our project which links against
 * Apache Software License 2.0 code. He responded that he releases this class under
 * the Apache Software License 2.0 aswell.
 *
 * Slightly modified (by Zenix) to be used with the Stendhal MMORPG
 */
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * ScrollerPanel can be used for displaying a scrolling text (as in movie
 * credits).
 */
public class ScrollerPanel extends JPanel {

	private static final long serialVersionUID = -9047582023793318785L;

	private static final Logger logger = Logger.getLogger(ScrollerPanel.class);

	private String[] text;

	private Font font;

	private int textPos;

	private Timer t;

	private int lineSpacing;

	private Color textColor;

	private Color backgroundColor;

	private Dimension prefferedSize = null;

	private int lineHeight;

	/**
	 * creates an ScrollerPane wich scrolls the given text and using defaults
	 * for the other attributes.
	 * 
	 * @param text the text araay whis should be scrolled - one string per line
	 *             is scrolled
	 */
	public ScrollerPanel(String[] text) {
		this(text, new Font("SansSerif", Font.BOLD, 12), 0, Color.GRAY, Color.WHITE, 20);
	}

	/**
	 * creates an ScrollerPane wich scrolls the given text and uses the given
	 * attributes
	 * 
	 * @param text the text araay whis should be scrolled - one string per line is scrolled
	 * @param font the font which is rendered
	 * @param lineSpacing the gap between the lines
	 * @param textColor color of the text
	 * @param backgroundColor the background color of the panel
	 * @param scrollSpeed defines the scroller speed (pixel per second);
	 */
	public ScrollerPanel(String[] text, Font font, int lineSpacing, Color textColor, Color backgroundColor,
	        int scrollSpeed) {
		this.text = text;
		this.font = font;
		this.lineSpacing = lineSpacing;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		this.t = new Timer((int) (1.0 / scrollSpeed * 1000.0), new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				moveText();
			}
		});
		logger.debug("Created a new scrolling panel");
		calculateSizes();
		// seting up event handling
		eventHandling();
	}

	/**
	 * setting up the listeners an event handling in general
	 */
	private void eventHandling() {
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (!t.isRunning()) {
					resetTextPos();
					t.start();
					logger.debug("start scrolling");
				}
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		int width;
		// super.paint( g );
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setBackground(backgroundColor);
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
		GradientPaint gp = new GradientPaint(0f, 0f, backgroundColor, 0f, this.getHeight() / 2, textColor, true);
		g2d.setPaint(gp);
		g2d.setFont(font);
		FontMetrics metrics = g2d.getFontMetrics();
		for (int i = 0, n = text.length; i < n; i++) {
			width = metrics.stringWidth(text[i]);
			g2d.drawString(text[i], this.getWidth() / 2 - width / 2, textPos
			        + ((metrics.getHeight() + lineSpacing) * i));
		}
	}

	/**
	 * calculates the new textposition
	 */
	private void moveText() {
		if (textPos >= -((lineHeight + lineSpacing) * text.length)) {
			textPos--;
		} else {
			resetTextPos();
		}
		this.repaint(0, 0, this.getWidth(), this.getHeight());
	}

	/**
	 * reset the textposition
	 */
	private void resetTextPos() {
		textPos = this.getHeight() + font.getSize() + lineSpacing;
	}

	/**
	 * stops scrolling
	 */
	public void stop() {
		t.stop();
		logger.debug("stop scrolling");
	}

	/**
	 * Calculates the line height and preferred size of this component depending
	 * on the given font.
	 */
	private void calculateSizes() {
		this.prefferedSize = new Dimension();
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setFont(font);
		FontMetrics metrics = g2d.getFontMetrics();
		this.lineHeight = metrics.getHeight();
		this.prefferedSize.height = this.lineHeight * 8;
		for (int i = 0, n = text.length; i < n; i++) {
			prefferedSize.width = Math.max(prefferedSize.width, metrics.stringWidth(text[i]));
		}
		this.prefferedSize.width = prefferedSize.width + 6 * metrics.stringWidth(" ");
	}

	@Override
	public Dimension getPreferredSize() {
		return this.prefferedSize;
	}
}
