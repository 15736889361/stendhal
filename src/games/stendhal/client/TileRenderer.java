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

import games.stendhal.tools.tiled.LayerDefinition;

import java.awt.Graphics;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.net.InputSerializer;

import marauroa.common.Logger;

/**
 * This is a helper class to render coherent tiles based on the tileset. This
 * should be replaced by independent tiles as soon as possible .
 */
public class TileRenderer extends LayerRenderer {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(TileRenderer.class);

	private static final Sprite	emptySprite = new EmptySprite();

	private TileStore tiles;

	private int[] map;

	private Sprite [] spriteMap;

	private int frame;

	private long delta;

	public TileRenderer() {
		tiles = null;
		map = null;
		spriteMap = null;
		frame = 0;
		animatedTiles = new HashMap<Integer, List<Integer>>();
		delta = System.currentTimeMillis();

		//createAnimateTiles();
	}

	/** Sets the data that will be rendered 
	 * @throws ClassNotFoundException */
	public void setMapData(InputSerializer in) throws IOException, ClassNotFoundException {
		LayerDefinition layer=LayerDefinition.decode(in);
		width=layer.getWidth();
		height=layer.getHeight();

		logger.debug("Layer("+layer.getName()+"): " +width+"x"+height);
		
		map=layer.expose();
	}
	
	@Override
	public void setTileset(TileStore tileset) {
		tiles=tileset;

		/*
		 * Cache normal sprites
		 */
		spriteMap = new Sprite[map.length];

		if(tileset != null) {
			int i = spriteMap.length;

			while(i-- != 0) {
				int value = map[i];

				if(value > 0) {
					spriteMap[i] = tileset.getTile(value);
				} else {
					spriteMap[i] = emptySprite;
				}
			}
		} else {
			Arrays.fill(spriteMap, emptySprite);
		}
	}

	private int get(int x, int y) {
		return map[y * width + x];
	}

	private Sprite getTile(int x, int y) {
		return spriteMap[(y * width) + x];
	}

	private Map<Integer, List<Integer>> animatedTiles;

	private void addAnimatedTile(int tile, int[] tiles) {
		List<Integer> list = new LinkedList<Integer>();
		for (int num : tiles) {
			list.add(num);
		}

		animatedTiles.put(tile, list);
	}

	private void createAnimateTiles() {
		// TODO: Broken. Animated tiles don't work now in this way.
		// Outside_0 = 0 - 479
		// Outside_1 = 480 - 959
		// Dungeon_0 = 960 - 1439
		// Dungeon_1 = 1440 - 1819
		// Interior_0 = 1820 - 2389
		// Navigation = 2390 - 2400
		// Objects = 2400 - 2600
		// Collision = 2600 - 2602
		// Buildings_0 = 2602 - 2982
		// Outside_2 = 2982 - 3562
		// Interior_1 = 3562 - 3942

		// Double white daisy
		addAnimatedTile(22, new int[] { 22, 52, 82, 112, 112, 112, 112, 112, 112, 112 });
		addAnimatedTile(52, new int[] { 52, 82, 112, 22, 22, 22, 22, 22, 22, 22 });
		addAnimatedTile(82, new int[] { 82, 112, 22, 52, 52, 52, 52, 52, 52, 52 });
		addAnimatedTile(112, new int[] { 112, 22, 52, 82, 82, 82, 82, 82, 82, 82 });

		// Single white daisy
		addAnimatedTile(23, new int[] { 23, 53, 83, 113, 113, 113, 113, 113, 113, 113 });
		addAnimatedTile(53, new int[] { 53, 83, 113, 23, 23, 23, 23, 23, 23, 23 });
		addAnimatedTile(83, new int[] { 83, 113, 23, 53, 53, 53, 53, 53, 53, 53 });
		addAnimatedTile(113, new int[] { 113, 23, 53, 83, 83, 83, 83, 83, 83, 83 });

		// Double yellow daisy
		addAnimatedTile(24, new int[] { 24, 54, 84, 114, 114, 114, 114, 114, 114, 114 });
		addAnimatedTile(54, new int[] { 54, 84, 114, 24, 24, 24, 24, 24, 24, 24 });
		addAnimatedTile(84, new int[] { 84, 114, 24, 54, 54, 54, 54, 54, 54, 54 });
		addAnimatedTile(114, new int[] { 114, 24, 54, 84, 84, 84, 84, 84, 84, 84 });

		// Single yellow daisy
		addAnimatedTile(25, new int[] { 25, 55, 85, 115, 115, 115, 115, 115, 115, 115 });
		addAnimatedTile(55, new int[] { 55, 85, 115, 25, 25, 25, 25, 25, 25, 25 });
		addAnimatedTile(85, new int[] { 85, 115, 25, 55, 55, 55, 55, 55, 55, 55 });
		addAnimatedTile(115, new int[] { 115, 25, 55, 85, 85, 85, 85, 85, 85, 85 });

		// Double red daisy
		addAnimatedTile(26, new int[] { 26, 56, 86, 116, 116, 116, 116, 116, 116, 116 });
		addAnimatedTile(56, new int[] { 56, 86, 116, 26, 26, 26, 26, 26, 26, 26, 26, 26 });
		addAnimatedTile(86, new int[] { 86, 116, 26, 56, 56, 56, 56, 56, 56, 56 });
		addAnimatedTile(116, new int[] { 116, 26, 56, 86, 86, 86, 86, 86, 86, 86 });

		// Single red daisy
		addAnimatedTile(27, new int[] { 27, 57, 87, 117, 117, 117, 117, 117, 117, 117 });
		addAnimatedTile(57, new int[] { 57, 87, 117, 27, 27, 27, 27, 27, 27, 27 });
		addAnimatedTile(87, new int[] { 87, 117, 27, 57, 57, 57, 57, 57, 57, 57 });
		addAnimatedTile(117, new int[] { 117, 27, 57, 87, 87, 87, 87, 87, 87, 87 });

		// Double blue daisy
		addAnimatedTile(28, new int[] { 28, 58, 88, 118, 118, 118, 118, 118, 118, 118 });
		addAnimatedTile(58, new int[] { 58, 88, 118, 28, 28, 28, 28, 28, 28, 28 });
		addAnimatedTile(88, new int[] { 88, 118, 28, 58, 58, 58, 58, 58, 58, 58 });
		addAnimatedTile(118, new int[] { 118, 28, 58, 88, 88, 88, 88, 88, 88, 88 });

		// Single blue daisy
		addAnimatedTile(29, new int[] { 29, 59, 89, 119, 119, 119, 119, 119, 119, 119 });
		addAnimatedTile(59, new int[] { 59, 89, 119, 29, 29, 29, 29, 29, 29, 29 });
		addAnimatedTile(89, new int[] { 89, 119, 29, 59, 59, 59, 59, 59, 59, 59 });
		addAnimatedTile(119, new int[] { 119, 29, 59, 89, 89, 89, 89, 89, 89, 89 });

		// Fire
		addAnimatedTile(1167, new int[] { 1167, 1258 });
		addAnimatedTile(1258, new int[] { 1167, 1258 });

		// Green Water, Top Left
		addAnimatedTile(3083, new int[] { 3083, 3086, 3089, 3086 });
		addAnimatedTile(3086, new int[] { 3083, 3086, 3089, 3086 });
		addAnimatedTile(3089, new int[] { 3083, 3086, 3089, 3086 });

		// Green Water, Top
		addAnimatedTile(3084, new int[] { 3084, 3087, 3090, 3087 });
		addAnimatedTile(3087, new int[] { 3084, 3087, 3090, 3087 });
		addAnimatedTile(3090, new int[] { 3084, 3087, 3090, 3087 });

		// Green Water, Top Right
		addAnimatedTile(3085, new int[] { 3085, 3088, 3091, 3088 });
		addAnimatedTile(3088, new int[] { 3085, 3088, 3091, 3088 });
		addAnimatedTile(3091, new int[] { 3085, 3088, 3091, 3088 });

		// Green Water, Left
		addAnimatedTile(3113, new int[] { 3113, 3116, 3119, 3116 });
		addAnimatedTile(3116, new int[] { 3113, 3116, 3119, 3116 });
		addAnimatedTile(3119, new int[] { 3113, 3116, 3119, 3116 });

		// Green Water, pond
		addAnimatedTile(3114, new int[] { 3114, 3117, 3120, 3117 });
		addAnimatedTile(3117, new int[] { 3114, 3117, 3120, 3117 });
		addAnimatedTile(3120, new int[] { 3114, 3117, 3120, 3117 });

		// Green Water, Right
		addAnimatedTile(3115, new int[] { 3115, 3118, 3121, 3118 });
		addAnimatedTile(3118, new int[] { 3115, 3118, 3121, 3118 });
		addAnimatedTile(3121, new int[] { 3115, 3118, 3121, 3118 });

		// Green Water, Bottom Left
		addAnimatedTile(3143, new int[] { 3143, 3146, 3149, 3146 });
		addAnimatedTile(3146, new int[] { 3143, 3146, 3149, 3146 });
		addAnimatedTile(3149, new int[] { 3143, 3146, 3149, 3146 });

		// Green Water, Bottom
		addAnimatedTile(3144, new int[] { 3144, 3147, 3150, 3147 });
		addAnimatedTile(3147, new int[] { 3144, 3147, 3150, 3147 });
		addAnimatedTile(3150, new int[] { 3144, 3147, 3150, 3147 });

		// Green Water, Bottom Right
		addAnimatedTile(3145, new int[] { 3145, 3148, 3151, 3148 });
		addAnimatedTile(3148, new int[] { 3145, 3148, 3151, 3148 });
		addAnimatedTile(3151, new int[] { 3145, 3148, 3151, 3148 });

		// Green Water, Top Left Corner
		addAnimatedTile(3263, new int[] { 3263, 3265, 3267, 3265 });
		addAnimatedTile(3265, new int[] { 3263, 3265, 3267, 3265 });
		addAnimatedTile(3267, new int[] { 3263, 3265, 3267, 3265 });

		// Green Water, Top Right Corner
		addAnimatedTile(3264, new int[] { 3264, 3266, 3268, 3266 });
		addAnimatedTile(3266, new int[] { 3264, 3266, 3268, 3266 });
		addAnimatedTile(3268, new int[] { 3264, 3266, 3268, 3266 });

		// Green Water, Bottom Left Corner
		addAnimatedTile(3293, new int[] { 3293, 3295, 3297, 3295 });
		addAnimatedTile(3295, new int[] { 3293, 3295, 3297, 3295 });
		addAnimatedTile(3297, new int[] { 3293, 3295, 3297, 3295 });

		// Green Water, Bottom Right Corner
		addAnimatedTile(3294, new int[] { 3294, 3296, 3298, 3296 });
		addAnimatedTile(3296, new int[] { 3294, 3296, 3298, 3296 });
		addAnimatedTile(3298, new int[] { 3294, 3296, 3298, 3296 });

		// Green Water, Vertical Canal
		addAnimatedTile(3323, new int[] { 3323, 3324, 3325, 3324 });
		addAnimatedTile(3324, new int[] { 3323, 3324, 3325, 3324 });
		addAnimatedTile(3325, new int[] { 3323, 3324, 3325, 3324 });

		// Green Water, Horizontal Canal
		addAnimatedTile(3353, new int[] { 3353, 3354, 3355, 3354 });
		addAnimatedTile(3354, new int[] { 3353, 3354, 3355, 3354 });
		addAnimatedTile(3355, new int[] { 3353, 3354, 3355, 3354 });

		// Light Water, Center
		addAnimatedTile(3269, new int[] { 3269, 3270, 3271, 3270 });
		addAnimatedTile(3270, new int[] { 3269, 3270, 3271, 3270 });
		addAnimatedTile(3271, new int[] { 3269, 3270, 3271, 3270 });

		// Golden water
		addAnimatedTile(965, new int[] { 965, 995, 1025, 995 });
		addAnimatedTile(995, new int[] { 965, 995, 1025, 995 });
		addAnimatedTile(1025, new int[] { 965, 995, 1025, 995 });

		// Waterfall start
		addAnimatedTile(145, new int[] { 145, 175, 205 });
		addAnimatedTile(175, new int[] { 175, 205, 235 });
		addAnimatedTile(205, new int[] { 205, 235, 145 });

		// Waterfall middle
		addAnimatedTile(146, new int[] { 146, 176, 206 });
		addAnimatedTile(176, new int[] { 176, 206, 236 });
		addAnimatedTile(206, new int[] { 206, 236, 146 });

		// Waterfall end
		addAnimatedTile(147, new int[] { 147, 177, 207 });
		addAnimatedTile(177, new int[] { 177, 207, 237 });
		addAnimatedTile(207, new int[] { 207, 237, 147 });

		// Waterfall end left
		addAnimatedTile(148, new int[] { 148, 178, 208 });
		addAnimatedTile(178, new int[] { 178, 208, 238 });
		addAnimatedTile(208, new int[] { 208, 238, 148 });

		// Waterfall end right
		addAnimatedTile(149, new int[] { 149, 179, 209 });
		addAnimatedTile(179, new int[] { 179, 209, 239 });
		addAnimatedTile(209, new int[] { 209, 239, 149 });

		// Waterfall golden start
		addAnimatedTile(265, new int[] { 265, 295, 325 });
		addAnimatedTile(295, new int[] { 295, 325, 355 });
		addAnimatedTile(325, new int[] { 325, 355, 265 });

		// Waterfall golden middle
		addAnimatedTile(266, new int[] { 266, 296, 326 });
		addAnimatedTile(296, new int[] { 296, 326, 356 });
		addAnimatedTile(326, new int[] { 326, 356, 266 });

		// Waterfall golden end
		addAnimatedTile(267, new int[] { 267, 297, 327 });
		addAnimatedTile(297, new int[] { 297, 327, 357 });
		addAnimatedTile(327, new int[] { 327, 357, 267 });

		// Waterfall golden end left
		addAnimatedTile(268, new int[] { 268, 298, 328 });
		addAnimatedTile(298, new int[] { 298, 328, 358 });
		addAnimatedTile(328, new int[] { 328, 358, 268 });

		// Waterfall golden end right
		addAnimatedTile(269, new int[] { 269, 299, 329 });
		addAnimatedTile(299, new int[] { 299, 329, 359 });
		addAnimatedTile(329, new int[] { 329, 359, 269 });

		// Golden Teleport
		addAnimatedTile(1443, new int[] { 1443, 1444, 1445 });
		addAnimatedTile(1444, new int[] { 1444, 1445, 1443 });
		addAnimatedTile(1445, new int[] { 1445, 1443, 1444 });

		// White Teleport
		addAnimatedTile(1473, new int[] { 1473, 1474, 1475 });
		addAnimatedTile(1474, new int[] { 1474, 1475, 1473 });
		addAnimatedTile(1475, new int[] { 1475, 1473, 1474 });

		// Gray Teleport
		addAnimatedTile(1503, new int[] { 1503, 1504, 1505 });
		addAnimatedTile(1504, new int[] { 1504, 1505, 1503 });
		addAnimatedTile(1505, new int[] { 1505, 1503, 1504 });

		// Red Teleport
		addAnimatedTile(1533, new int[] { 1533, 1534, 1535 });
		addAnimatedTile(1534, new int[] { 1534, 1535, 1533 });
		addAnimatedTile(1535, new int[] { 1535, 1533, 1534 });

		// Green Teleport
		addAnimatedTile(1563, new int[] { 1563, 1564, 1565 });
		addAnimatedTile(1564, new int[] { 1564, 1565, 1563 });
		addAnimatedTile(1565, new int[] { 1565, 1563, 1564 });

		// Blue Teleport
		addAnimatedTile(1593, new int[] { 1593, 1594, 1595 });
		addAnimatedTile(1594, new int[] { 1594, 1595, 1593 });
		addAnimatedTile(1595, new int[] { 1595, 1593, 1594 });

		// Interior_1 = 3562 - 394

		// Blacksmith fire (small), Top
		addAnimatedTile(3660, new int[] { 3660, 3660, 3661, 3661, 3662, 3662, 3661, 3661 });
		addAnimatedTile(3661, new int[] { 3660, 3660, 3661, 3661, 3662, 3662, 3661, 3661 });
		addAnimatedTile(3662, new int[] { 3660, 3660, 3661, 3661, 3662, 3662, 3661, 3661 });

		// Blacksmith fire (small), Bottom
		addAnimatedTile(3690, new int[] { 3690, 3690, 3691, 3691, 3692, 3692, 3691, 3691 });
		addAnimatedTile(3691, new int[] { 3690, 3690, 3691, 3691, 3692, 3692, 3691, 3691 });
		addAnimatedTile(3692, new int[] { 3690, 3690, 3691, 3691, 3692, 3692, 3691, 3691 });

		// Blacksmith fire (large), Top Left
		addAnimatedTile(3657, new int[] { 3657, 3657, 3747, 3747, 3837, 3837, 3747, 3747 });
		addAnimatedTile(3747, new int[] { 3657, 3657, 3747, 3747, 3837, 3837, 3747, 3747 });
		addAnimatedTile(3837, new int[] { 3657, 3657, 3747, 3747, 3837, 3837, 3747, 3747 });

		// Blacksmith fire (large), Top
		addAnimatedTile(3658, new int[] { 3658, 3658, 3748, 3748, 3838, 3838, 3748, 3748 });
		addAnimatedTile(3748, new int[] { 3658, 3658, 3748, 3748, 3838, 3838, 3748, 3748 });
		addAnimatedTile(3838, new int[] { 3658, 3658, 3748, 3748, 3838, 3838, 3748, 3748 });

		// Blacksmith fire (large), Top Right
		addAnimatedTile(3659, new int[] { 3659, 3659, 3749, 3749, 3839, 3839, 3749, 3749 });
		addAnimatedTile(3749, new int[] { 3659, 3659, 3749, 3749, 3839, 3839, 3749, 3749 });
		addAnimatedTile(3839, new int[] { 3659, 3659, 3749, 3749, 3839, 3839, 3749, 3749 });

		// Blacksmith fire (large), Left
		addAnimatedTile(3687, new int[] { 3687, 3687, 3777, 3777, 3867, 3867, 3777, 3777 });
		addAnimatedTile(3777, new int[] { 3687, 3687, 3777, 3777, 3867, 3867, 3777, 3777 });
		addAnimatedTile(3867, new int[] { 3687, 3687, 3777, 3777, 3867, 3867, 3777, 3777 });

		// Blacksmith fire (large), Center
		addAnimatedTile(3688, new int[] { 3688, 3688, 3778, 3778, 3868, 3868, 3778, 3778 });
		addAnimatedTile(3778, new int[] { 3688, 3688, 3778, 3778, 3868, 3868, 3778, 3778 });
		addAnimatedTile(3868, new int[] { 3688, 3688, 3778, 3778, 3868, 3868, 3778, 3778 });

		// Blacksmith fire (large), Right
		addAnimatedTile(3689, new int[] { 3689, 3689, 3779, 3779, 3869, 3869, 3779, 3779 });
		addAnimatedTile(3779, new int[] { 3689, 3689, 3779, 3779, 3869, 3869, 3779, 3779 });
		addAnimatedTile(3869, new int[] { 3689, 3689, 3779, 3779, 3869, 3869, 3779, 3779 });

		// Blacksmith fire (large), Bottom Left
		addAnimatedTile(3717, new int[] { 3717, 3717, 3807, 3807, 3897, 3897, 3807, 3807 });
		addAnimatedTile(3807, new int[] { 3717, 3717, 3807, 3807, 3897, 3897, 3807, 3807 });
		addAnimatedTile(3897, new int[] { 3717, 3717, 3807, 3807, 3897, 3897, 3807, 3807 });

		// Blacksmith fire (large), Bottom
		addAnimatedTile(3718, new int[] { 3718, 3718, 3808, 3808, 3898, 3898, 3808, 3808 });
		addAnimatedTile(3808, new int[] { 3718, 3718, 3808, 3808, 3898, 3898, 3808, 3808 });
		addAnimatedTile(3898, new int[] { 3718, 3718, 3808, 3808, 3898, 3898, 3808, 3808 });

		// Blacksmith fire (large), Bottom Right
		addAnimatedTile(3719, new int[] { 3719, 3719, 3809, 3809, 3899, 3899, 3809, 3809 });
		addAnimatedTile(3809, new int[] { 3719, 3719, 3809, 3809, 3899, 3899, 3809, 3809 });
		addAnimatedTile(3899, new int[] { 3719, 3719, 3809, 3809, 3899, 3899, 3809, 3809 });

		// Flame Brazier
		addAnimatedTile(1567, new int[] { 1567, 1597, 1627, 1597 });
		addAnimatedTile(1597, new int[] { 1567, 1597, 1627, 1597 });
		addAnimatedTile(1527, new int[] { 1567, 1597, 1627, 1597 });
		
		// fire
		addAnimatedTile(4043, new int[] {4043, 4064 });
		addAnimatedTile(4044, new int[] {4044, 4065 });
		addAnimatedTile(4045, new int[] {4045, 4066 });
		
		addAnimatedTile(4046, new int[] {4046, 4067 });
		
		addAnimatedTile(4047, new int[] {4047, 4054 });
		addAnimatedTile(4048, new int[] {4048, 4055 });
		addAnimatedTile(4049, new int[] {4049, 4056 });

		// fire 2
		addAnimatedTile(4050, new int[] { 4050, 4071 });
		addAnimatedTile(4051, new int[] { 4051, 4072 });
		addAnimatedTile(4052, new int[] { 4052, 4073 });
		addAnimatedTile(4053, new int[] { 4053, 4074 });
		
		addAnimatedTile(4057, new int[] { 4057, 4078 });
		addAnimatedTile(4058, new int[] { 4058, 4079 });
		addAnimatedTile(4059, new int[] { 4059, 4080 });
		addAnimatedTile(4060, new int[] { 4060, 4081 });
	}


	/**
	 * Render the layer to screen. We assume that game screen will clip.
	 *
	 * @param	screen		The screen to draw on.
	 * @param	x		The view X world coordinate.
	 * @param	y		The view Y world coordinate.
	 * @param	w		The view world width.
	 * @param	h		The view world height.
	 */
	@Override
	public void draw(GameScreen screen, int x, int y, int w, int h) {
		if(tiles==null) {
			return;			
		}
		
		if (System.currentTimeMillis() - delta > 200) {
			delta = System.currentTimeMillis();
			frame++;
		}

		for (int j = y - 1; j < y + h + 1; j++) {
			for (int i = x - 1; i < x + w + 1; i++) {
				if ((j >= 0) && (j < getHeight()) && (i >= 0) && (i < getWidth())) {
					Sprite sprite = getTile(i, j);
					
					if(sprite==null) {
						logger.warn("Null sprite at ("+i+","+j+")");
						sprite=SpriteStore.get().getSprite("data/sprites/failsafe.png");
					}

// TODO: Apparently Broken [ 1708820 ], so safe to comment out until fixed:
//
//					if (animatedTiles.containsKey(value)) {
//						List<Integer> list = (animatedTiles.get(value));
//						value = list.get(frame % list.size());
//					}

					screen.draw(sprite, i, j);
				}
			}
		}
	}

	//
	//

	/**
	 * An empty (non-drawing) sprite. Worth making non-inner?
	 */
	protected static class EmptySprite implements Sprite {
		/**
		 * A unique reference object.
		 */
		private static final Object	REF	= new Object();

		/**
		 * Copy the sprite.
		 *
		 * @return	A new copy of the sprite.
		 */
		public Sprite copy() {
			return this;
		}

		/**
		 * Draw the sprite onto the graphics context provided
		 * 
		 * @param g
		 *            The graphics context on which to draw the sprite
		 * @param x
		 *            The x location at which to draw the sprite
		 * @param y
		 *            The y location at which to draw the sprite
		 */
		public void draw(Graphics g, int x, int y) {
		}

		/**
		 * Draws the image
		 * 
		 * @param g
		 *            the graphics context where to draw to
		 * @param destx
		 *            destination x
		 * @param desty
		 *            destination y
		 * @param x
		 *            the source x
		 * @param y
		 *            the source y
		 * @param w
		 *            the width
		 * @param h
		 *            the height
		 */
		public void draw(Graphics g, int destx, int desty, int x, int y, int w, int h) {
		}

		/**
		 * Get the height of the drawn sprite
		 * 
		 * @return The height in pixels of this sprite
		 */
		public int getHeight() {
			return GameScreen.SIZE_UNIT_PIXELS;
		}

		/**
		 * Get the sprite reference. This identifier is an externally
		 * opaque object that implements equals() and hashCode() to
		 * uniquely/repeatably reference a keyed sprite.
		 *
		 * @return	The reference identifier, or <code>null</code>
		 *		if not referencable.
		 */
		public Object getReference() {
			return REF;
		}

		/**
		 * Get the width of the drawn sprite
		 * 
		 * @return The width in pixels of this sprite
		 */
		public int getWidth() {
			return GameScreen.SIZE_UNIT_PIXELS;
		}
	}
}
