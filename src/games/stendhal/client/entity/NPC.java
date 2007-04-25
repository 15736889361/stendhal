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
package games.stendhal.client.entity;

import games.stendhal.client.soundreview.SoundMaster;
import games.stendhal.common.Rand;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPObject;

public class NPC extends RPEntity {
	/**
	 * Initialize this entity for an object.
	 *
	 * @param	object		The object.
	 *
	 * @see-also	#release()
	 */
	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);

		String type = getType();

		if (type.startsWith("npc")) {
			setAudibleRange(3);
			if (name.equals("Diogenes")) {
				moveSounds = new String[2];
				moveSounds[0]="laugh-1.wav";
				moveSounds[1]="laugh-2.wav";
			//	SoundSystem.startSoundCycle(this, "Diogenes-patrol", 10000, 20, 50, 100);
			} else if (name.equals("Carmen")) {
				moveSounds = new String[2];
				moveSounds[0]="giggle-1.wav";
				moveSounds[1]="giggle-2.wav";
			
				//SoundSystem.startSoundCycle(this, "Carmen-patrol", 60000, 20, 50, 75);
			} else if (name.equals("Nishiya")) {
				moveSounds = new String[3];
				moveSounds[0]="cough-11.wav";
				moveSounds[1]="cough-2.wav";
				moveSounds[2]="cough-3.wav";
			//	SoundSystem.startSoundCycle(this, "Nishiya-patrol", 40000, 20, 50, 80);
			} else if (name.equals("Margaret")) {
				moveSounds = new String[3];
				moveSounds[0]="hiccup-1.aiff";
				moveSounds[1]="hiccup-2.wav";
				moveSounds[2]="hiccup-3.wav";
				
				//SoundSystem.startSoundCycle(this, "Margaret-patrol", 30000, 10, 30, 70);
			} else if (name.equals("Sato")) {
				moveSounds= new String[1];
				moveSounds[0]="sneeze-1.wav";
				//SoundSystem.startSoundCycle(this, "Sato-patrol", 60000, 30, 50, 70);
			}
		}
	}


	//
	// Entity
	//

	/** 
	 * Transition method. Create the screen view for this entity.
	 *
	 * @return	The on-screen view of this entity.
	 */
	protected Entity2DView createView() {
		return new NPC2DView(this);
	}

	/**
	 * Get the ground area this entity occupies.
	 *
	 * @return	The physical area.
	 */
	@Override
	public Rectangle2D getArea() {
		return new Rectangle.Double(x, y + 1, 1, 1);
	}

	private long soundWait = 0L;

	/**
	 * When the entity's position changed.
	 *
	 * @param	x		The new X coordinate.
	 * @param	y		The new Y coordinate.
	 */
	@Override
	protected void onPosition(double x, double y) {
		super.onPosition(x, y);

		if(soundWait < System.currentTimeMillis()&&Rand.rand(1000)<5) {
			
			try {
				SoundMaster.play(moveSounds[Rand.rand(moveSounds.length)], x, y);
			} catch(NullPointerException e){
			}

			soundWait = System.currentTimeMillis() + 2000L;
		}
	}
}
