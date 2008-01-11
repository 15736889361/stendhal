package games.stendhal.server.maps.semos.pad;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the NPC who deals in rainbow beans.
 * Other behaviour defined in maps/quests/RainbowBeans.java
 *
 * @author kymara
 */
public class DealerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	//
	// IL0_GreeterNPC
	//

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC dealerNPC = new SpeakerNPC("Pdiddi") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12));
				nodes.add(new Node(4, 10));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(8, 6));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(2, 4));
				nodes.add(new Node(12, 4));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(6, 6));
				nodes.add(new Node(6, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("I think you already know what I do.");
				addHelp("To be honest mate I can't help you with much, you're better off in the city for that.");
				addQuest("Haven't got anything for you, pal.");
				addOffer("Ha! The sign on the door's a cover! This is no inn. If you want a drink, you better go back into town.");				
				addGoodbye("Bye.");
			}
		};

		dealerNPC.setEntityClass("drugsdealernpc");
		dealerNPC.setPosition(4, 12);
		dealerNPC.initHP(100);
		zone.add(dealerNPC);
	}
}
