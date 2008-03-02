package games.stendhal.server.maps.kalavan.castle;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the princess in Kalavan castle.
 *
 * @author kymara
 */
public class PrincessNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC princessNPC = new SpeakerNPC("Princess Ylflia") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 21));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(14, 41));
				nodes.add(new Node(14, 48));
				nodes.add(new Node(18, 48));
				nodes.add(new Node(19, 48));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(20, 41));
				nodes.add(new Node(20, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("How do you do?");
				addReply(Arrays.asList("good", "fine"), "Good! Can I help you?");
				addReply("bad", "Oh dear ... Can I help you?");
				addReply("well", "Wonderful! Can I help you?");
				addJob("I am the princess of this kingdom. To become one of my citizens, speak to Barrett Holmes in the city. He may be able to sell you a house. But first, I have a #favour to ask of you...");
				addHelp("Watch out for mad scientists. My father allowed them liberty to do some work in the basement and I am afraid things have got rather out of hand.");
				addOffer("Sorry, but I do not have anything to offer you. You could do me a #favour, though...");
				addGoodbye("Goodbye, and good luck.");
			}
		};

		princessNPC.setEntityClass("princess2npc");
		princessNPC.setPosition(19, 21);
		princessNPC.initHP(100);
		zone.add(princessNPC);
	}
}
