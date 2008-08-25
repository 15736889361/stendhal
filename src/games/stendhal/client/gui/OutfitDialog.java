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

import games.stendhal.common.Outfits;
import games.stendhal.client.OutfitStore;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

public class OutfitDialog extends JDialog {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(OutfitDialog.class);

	private static final long serialVersionUID = 4628210176721975735L;

	private static final int PLAYER_WIDTH = 48;

	private static final int PLAYER_HEIGHT = 64;

	// to keep the sprites to show
	private final Sprite[] hairs;

	private final Sprite[] heads;

	private final Sprite[] bodies;

	private final Sprite[] clothes;

	// current selected parts index
	private int hairs_index = 1;

	private int heads_index;

	private int bodies_index;

	private int clothes_index;

	// to handle the draws update
	private final Timer timer;

	// 0 for direction UP, 1 RIGHT, 2 DOWN and 3 LEFT
	private int direction = 2;

	private final StendhalClient client;

	private final SpriteStore store = SpriteStore.get();

	private final OutfitStore ostore = OutfitStore.get();

	public OutfitDialog(final Frame parent, final String title, final int outfit) {
		this(parent, title, outfit, Outfits.HAIR_OUTFITS, Outfits.HEAD_OUTFITS, Outfits.BODY_OUTFITS,
				Outfits.CLOTHES_OUTFITS);
	}

	/**
	 * Creates new form SetOutfitGameDialog.
	 * @param parent
	 *
	 * @param title
	 *            a String with the title for the dialog
	 * @param outfit
	 *            the current outfit
	 * @param total_hairs
	 *            an integer with the total of sprites with hairs
	 * @param total_heads
	 *            an integer with the total of sprites with heads
	 * @param total_bodies
	 *            an integer with the total of sprites with bodies
	 * @param total_clothes
	 *            an integer with the total of sprites with clothes
	 */
	private OutfitDialog(final Frame parent, final String title, int outfit,
			final int total_hairs, final int total_heads, final int total_bodies,
			final int total_clothes) {
		super(parent, false);
		initComponents();
		setTitle(title);

		client = StendhalClient.get();

		// initializes the arrays
		// Plus 1 to add the sprite_empty.png that is always at 0
		hairs = new Sprite[total_hairs];
		heads = new Sprite[total_heads];
		bodies = new Sprite[total_bodies];
		// Plus 1 to add the sprite_empty.png that is always at 0
		clothes = new Sprite[total_clothes];

		// updates the draws every 2500 milliseconds
		timer = new Timer();
		timer.schedule(new AnimationTask(), 1000, 2500);

		// analyse current outfit
		bodies_index = outfit % 100;
		outfit = outfit / 100;
		clothes_index = outfit % 100;
		outfit = outfit / 100;
		heads_index = outfit % 100;
		outfit = outfit / 100;
		hairs_index = outfit % 100;

		// reset special outfits
		if (hairs_index >= hairs.length) {
			hairs_index = 0;
		}
		if (heads_index >= heads.length) {
			heads_index = 0;
		}
		if (bodies_index >= bodies.length) {
			bodies_index = 0;
		}
		if (clothes_index >= clothes.length) {
			clothes_index = 0;
		}
	}

	/**
	 * Cleans the previous draw.
	 *
	 * @param g
	 *            the Graphics where to clean
	 */
	private void clean(final Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(2, 2, PLAYER_WIDTH, PLAYER_HEIGHT);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawHair(final int code, final Graphics g) {
		clean(g);
		drawHair(code, g);
	}

	/**
	 * Draws a hair images from an outfit code.
	 * @param code
	 * @param g
	 */
	private void drawHair(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getHairSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the head image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawHead(final int code, final Graphics g) {
		clean(g);
		drawHead(code, g);
	}

	/**
	 * Draws a head from the outfit code.
	 * @param code
	 * @param g
	 */
	private void drawHead(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getHeadSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawDress(final int code, final Graphics g) {
		clean(g);
		drawDress(code, g);
	}

	/**
	 * Draws a dress from the outfit code.
	 * @param code
	 * @param g
	 */
	private void drawDress(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getDressSprite(code),
				PLAYER_WIDTH, direction * PLAYER_HEIGHT, PLAYER_WIDTH,
				PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawBase(final int code, final Graphics g) {
		clean(g);
		drawBase(code, g);
	}

	/**
	 * Draws a base from an outfit code.
	 * @param code
	 * @param g
	 */
	private void drawBase(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getBaseSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraw the final player.
	 *
	 * @param g
	 *            The graphics context.
	 */
	private void redrawFinalPlayer(final Graphics g) {
		clean(g);
		drawFinalPlayer(g);
	}

	/**
	 * Draws final player.
	 * @param g
	 */
	private void drawFinalPlayer(final Graphics g) {
		drawBase(bodies_index, g);
		drawDress(clothes_index, g);
		drawHead(heads_index, g);
		drawHair(hairs_index, g);
	}

	private void initComponents() {
		jpanel = new JPanel();
		jbtOK = new JButton();
		jbtLeftHairs = new JButton();
		jbtRightHairs = new JButton();
		jbtLeftHeads = new JButton();
		jbtRightHeads = new JButton();
		jbtLeftBodies = new JButton();
		jbtRightBodies = new JButton();
		jbtLeftClothes = new JButton();
		jbtRightClothes = new JButton();
		jlblHairs = new JLabel();
		jlblHeads = new JLabel();
		jlblBodies = new JLabel();
		jlblClothes = new JLabel();
		jlblFinalResult = new JLabel();
		jsliderDirection = new JSlider();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBackground(new Color(200, 200, 200));
		setResizable(false);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		jpanel.setLayout(null);

		jpanel.setBorder(new LineBorder(new Color(100, 100, 100), 2, true));
		jbtOK.setText("OK");
		jbtOK.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtOKActionPerformed(evt);
			}
		});

		jpanel.add(jbtOK);
		jbtOK.setBounds(190, 220, 80, 30);

		jbtLeftHairs.setFont(new Font("Dialog", 1, 14));
		jbtLeftHairs.setText("<");
		jbtLeftHairs.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftHairsActionPerformed(evt);
			}
		});

		jpanel.add(jbtLeftHairs);
		jbtLeftHairs.setBounds(10, 20, 45, 30);

		jbtRightHairs.setFont(new Font("Dialog", 1, 14));
		jbtRightHairs.setText(">");
		jbtRightHairs.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightHairsActionPerformed(evt);
			}
		});

		jpanel.add(jbtRightHairs);
		jbtRightHairs.setBounds(120, 20, 45, 30);

		jbtLeftHeads.setFont(new Font("Dialog", 1, 14));
		jbtLeftHeads.setText("<");
		jbtLeftHeads.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftHeadsActionPerformed(evt);
			}
		});

		jpanel.add(jbtLeftHeads);
		jbtLeftHeads.setBounds(10, 100, 45, 30);

		jbtRightHeads.setFont(new Font("Dialog", 1, 14));
		jbtRightHeads.setText(">");
		jbtRightHeads.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightHeadsActionPerformed(evt);
			}
		});

		jpanel.add(jbtRightHeads);
		jbtRightHeads.setBounds(120, 100, 45, 30);

		jbtLeftBodies.setFont(new Font("Dialog", 1, 14));
		jbtLeftBodies.setText("<");
		jbtLeftBodies.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftBodiesActionPerformed(evt);
			}
		});

		jpanel.add(jbtLeftBodies);
		jbtLeftBodies.setBounds(10, 180, 45, 30);

		jbtRightBodies.setFont(new Font("Dialog", 1, 14));
		jbtRightBodies.setText(">");
		jbtRightBodies.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightBodiesActionPerformed(evt);
			}
		});

		jpanel.add(jbtRightBodies);
		jbtRightBodies.setBounds(120, 180, 45, 30);

		jbtLeftClothes.setFont(new Font("Dialog", 1, 14));
		jbtLeftClothes.setText("<");
		jbtLeftClothes.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftClothesActionPerformed(evt);
			}
		});

		jpanel.add(jbtLeftClothes);
		jbtLeftClothes.setBounds(10, 260, 45, 30);

		jbtRightClothes.setFont(new Font("Dialog", 1, 14));
		jbtRightClothes.setText(">");
		jbtRightClothes.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightClothesActionPerformed(evt);
			}
		});

		jpanel.add(jbtRightClothes);
		jbtRightClothes.setBounds(120, 260, 45, 30);

		jlblHairs.setBackground(new Color(255, 255, 255));
		jlblHairs.setFont(new Font("Dialog", 0, 10));
		jlblHairs.setHorizontalAlignment(SwingConstants.CENTER);
		jlblHairs.setText("loading...");
		jlblHairs.setBorder(new LineBorder(new Color(100, 100, 100), 1, true));
		jlblHairs.setOpaque(true);
		jpanel.add(jlblHairs);
		jlblHairs.setBounds(60, 10, 52, 68);

		jlblHeads.setBackground(new Color(255, 255, 255));
		jlblHeads.setFont(new Font("Dialog", 0, 10));
		jlblHeads.setHorizontalAlignment(SwingConstants.CENTER);
		jlblHeads.setText("loading...");
		jlblHeads.setBorder(new LineBorder(new Color(100, 100, 100), 1, true));
		jlblHeads.setOpaque(true);
		jpanel.add(jlblHeads);
		jlblHeads.setBounds(60, 90, 52, 68);

		jlblBodies.setBackground(new Color(255, 255, 255));
		jlblBodies.setFont(new Font("Dialog", 0, 10));
		jlblBodies.setHorizontalAlignment(SwingConstants.CENTER);
		jlblBodies.setText("loading...");
		jlblBodies.setBorder(new LineBorder(new Color(100, 100, 100), 1, true));
		jlblBodies.setOpaque(true);
		jpanel.add(jlblBodies);
		jlblBodies.setBounds(60, 170, 52, 68);

		jlblClothes.setBackground(new Color(255, 255, 255));
		jlblClothes.setFont(new Font("Dialog", 0, 10));
		jlblClothes.setHorizontalAlignment(SwingConstants.CENTER);
		jlblClothes.setText("loading...");
		jlblClothes.setBorder(new LineBorder(new Color(100, 100, 100), 1, true));
		jlblClothes.setOpaque(true);
		jpanel.add(jlblClothes);
		jlblClothes.setBounds(60, 250, 52, 68);

		jlblFinalResult.setBackground(new Color(255, 255, 255));
		jlblFinalResult.setFont(new Font("Dialog", 0, 10));
		jlblFinalResult.setHorizontalAlignment(SwingConstants.CENTER);
		jlblFinalResult.setText("loading...");
		jlblFinalResult.setBorder(new LineBorder(new Color(100, 100, 100), 1,
				true));
		jlblFinalResult.setOpaque(true);
		jpanel.add(jlblFinalResult);
		jlblFinalResult.setBounds(205, 90, 52, 68);

		jsliderDirection.setMaximum(3);
		jsliderDirection.setSnapToTicks(true);
		jsliderDirection.setValue(2);
		jsliderDirection.setInverted(true);
		jsliderDirection.addChangeListener(new ChangeListener() {

			public void stateChanged(final ChangeEvent evt) {
				jsliderDirectionStateChanged(evt);
			}
		});

		jpanel.add(jsliderDirection);
		jsliderDirection.setBounds(190, 170, 80, 27);

		getContentPane().add(jpanel, BorderLayout.CENTER);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 288) / 2, (screenSize.height - 361) / 2,
				288, 361);
	}



	/** this is called every time the user moves the slider.
	 * @param evt */
	private void jsliderDirectionStateChanged(final ChangeEvent evt) {
		direction = jsliderDirection.getValue();

		redrawFinalPlayer(jlblFinalResult.getGraphics());
		redrawHair(hairs_index, jlblHairs.getGraphics());
		redrawHead(heads_index, jlblHeads.getGraphics());
		redrawBase(bodies_index, jlblBodies.getGraphics());
		redrawDress(clothes_index, jlblClothes.getGraphics());
	}

	/** when user closes this window.
	 * @param evt */
	private void formWindowClosing(final WindowEvent evt) {
		timer.cancel();
		this.dispose();
	}

	/** Clothes Right button.
	 * @param evt */
	private void jbtRightClothesActionPerformed(final ActionEvent evt) {
		if (clothes_index < clothes.length - 1) {
			clothes_index++;
		} else {
			clothes_index = 0;
		}

		redrawDress(clothes_index, jlblClothes.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Clothes Left button.
	 * @param evt */
	private void jbtLeftClothesActionPerformed(final ActionEvent evt) {
		if (clothes_index > 0) {
			clothes_index--;
		} else {
			clothes_index = clothes.length - 1;
		}

		redrawDress(clothes_index, jlblClothes.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Bodies Right button.
	 * @param evt */
	private void jbtRightBodiesActionPerformed(final ActionEvent evt) {
		if (bodies_index < bodies.length - 1) {
			bodies_index++;
		} else {
			bodies_index = 0;
		}

		redrawBase(bodies_index, jlblBodies.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Bodies Left button.
	 * @param evt */
	private void jbtLeftBodiesActionPerformed(final ActionEvent evt) { 
		if (bodies_index > 0) {
			bodies_index--;
		} else {
			bodies_index = bodies.length - 1;
		}

		redrawBase(bodies_index, jlblBodies.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Heads Right button.
	 * @param evt */
	private void jbtRightHeadsActionPerformed(final ActionEvent evt) { 
		if (heads_index < heads.length - 1) {
			heads_index++;
		} else {
			heads_index = 0;
		}

		redrawHead(heads_index, jlblHeads.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Heads Left button.
	 * @param evt */
	private void jbtLeftHeadsActionPerformed(final ActionEvent evt) { 
		if (heads_index > 0) {
			heads_index--;
		} else {
			heads_index = heads.length - 1;
		}

		redrawHead(heads_index, jlblHeads.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	} 

	/** Hairs Right button.
	 * @param evt */
	private void jbtRightHairsActionPerformed(final ActionEvent evt) { 
		if (hairs_index < hairs.length - 1) {
			hairs_index++;
		} else {
			hairs_index = 0;
		}

		redrawHair(hairs_index, jlblHairs.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	} 

	/** Hairs Left button.
	 * @param evt */
	private void jbtLeftHairsActionPerformed(final ActionEvent evt) { 
		if (hairs_index > 0) {
			hairs_index--;
		} else {
			hairs_index = hairs.length - 1;
		}

		redrawHair(hairs_index, jlblHairs.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Button OK action.
	 * @param evt */
	private void jbtOKActionPerformed(final ActionEvent evt) { 
		sendAction();

		timer.cancel();
		this.dispose();
	} 

	private void sendAction() {
		if (client == null) {
			/** If running standalone, just print the outfit */
			System.out.println("OUTFIT is: "
					+ (bodies_index + clothes_index * 100 + heads_index * 100
							* 100 + hairs_index * 100 * 100 * 100));
			return;
		}

		final RPAction rpaction = new RPAction();
		rpaction.put("type", "outfit");
		rpaction.put("value", bodies_index + clothes_index * 100 + heads_index
				* 100 * 100 + hairs_index * 100 * 100 * 100);
		client.send(rpaction);
	}

	private JButton jbtLeftBodies;

	private JButton jbtLeftClothes;

	private JButton jbtLeftHairs;

	private JButton jbtLeftHeads;

	private JButton jbtOK;

	private JButton jbtRightBodies;

	private JButton jbtRightClothes;

	private JButton jbtRightHairs;

	private JButton jbtRightHeads;

	private JLabel jlblBodies;

	private JLabel jlblClothes;

	private JLabel jlblFinalResult;

	private JLabel jlblHairs;

	private JLabel jlblHeads;

	private JPanel jpanel;

	private JSlider jsliderDirection;


	/**
	 * Private class that handles the update (repaint) of jLabels
	 */
	private class AnimationTask extends TimerTask {

		@Override
		public void run() {
			// draws single parts
			redrawHair(hairs_index, jlblHairs.getGraphics());
			redrawHead(heads_index, jlblHeads.getGraphics());
			redrawBase(bodies_index, jlblBodies.getGraphics());
			redrawDress(clothes_index, jlblClothes.getGraphics());

			redrawFinalPlayer(jlblFinalResult.getGraphics());
		}
	}


	private void generateAllOutfits(final String baseDir) {
		/** TEST METHOD: DON'T NO USE */
		for (bodies_index = 0; bodies_index < bodies.length; bodies_index++) {
			for (clothes_index = 0; clothes_index < clothes.length; clothes_index++) {
				for (heads_index = 0; heads_index < heads.length; heads_index++) {
					for (hairs_index = 0; hairs_index < hairs.length; hairs_index++) {
						final String name = Integer.toString(bodies_index
								+ clothes_index * 100 + heads_index * 100 * 100
								+ hairs_index * 100 * 100 * 100);
						final File file = new File(baseDir + "outfits/" + name
								+ ".png");

						// for performance reasons only write new files.
						if (!file.exists()) {
							System.out.println("Creating " + name + ".png");
							final Image image = new BufferedImage(PLAYER_WIDTH,
									PLAYER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
							drawFinalPlayer(getGraphics());
							try {
								ImageIO.write((RenderedImage) image, "png",
										file);
							} catch (final Exception e) {
								logger.error(e, e);
							}
						}
					}
				}
			}
		}
	}

	public static void main(final String[] args) throws InterruptedException {
		String baseDir = "";
		if (args.length > 0) {
			baseDir = args[0] + "/";
		}

		final OutfitDialog f = new OutfitDialog(null, "Stendhal - Choose outfit", 0);
		// show is required now, because getGraphics() returns null otherwise
		f.setVisible(true);
		f.generateAllOutfits(baseDir);
	}
}
