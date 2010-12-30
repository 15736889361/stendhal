/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

public class DecencyAndMannersWardenNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosVillageBench(zone, attributes);
	}

	private void buildSemosVillageBench(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Ketteh Wehoh") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addHelp("I am the town Decency and Manners Warden. I can advise you on how to conduct yourself in many ways; like not wandering around naked, for instance.");
				addJob("My job is to maintain a civilized level of behaviour in Semos. I know the protocol for every situation, AND all the ways of handling it wrong. Well, sometimes I get confused on whether to use a spoon or a fork; but then, nobody really uses cutlery in Semos anyway.");
				addQuest("The only task I have for you is to behave nicely towards others.");
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				super.onGoodbye(player);
				setDirection(Direction.DOWN);
			}
		};

		npc.setDirection(Direction.DOWN);
		npc.setDescription("Ketteh Wehoh is sitting on a bench while waiting for new adventurers.");
		npc.setEntityClass("elegantladynpc");
		npc.setPosition(13, 35);
		npc.initHP(100);
		zone.add(npc);
	}
}
