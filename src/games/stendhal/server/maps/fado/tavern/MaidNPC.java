package games.stendhal.server.maps.fado.tavern;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Builds the tavern maid NPC.
 *
 * @author timothyb89/kymara
 */
public class MaidNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();


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
	// MaidNPC
	//

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC tavernMaid = new SpeakerNPC("Old Mother Helena") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(28, 15));
				nodes.add(new Node(10, 15));
				nodes.add(new Node(10, 27));
				nodes.add(new Node(19, 27));
				nodes.add(new Node(19, 28));
				nodes.add(new Node(20, 28));
				nodes.add(new Node(21, 28));
				nodes.add(new Node(21, 27));
				nodes.add(new Node(28, 27));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//addGreeting();
				addJob("I am the bar maid for this fair tavern. We sell imported beers and fine food.");
				addHelp("Why not gather some friends and take a break together, you can put your food down and eat from that long table.");
				addQuest("Oh, I don't have time for anything like that.");

				Map<String, Integer> offers = new HashMap<String, Integer>();
				offers.put("beer", 10);
				offers.put("wine", 15);
				offers.put("cherry", 20);
				offers.put("cheese", 20);
				offers.put("bread", 50);
				offers.put("sandwich", 150);
	 
				addSeller(new SellerBehaviour(offers));
				addGoodbye("Goodbye, all you customers do work me hard ...");
			}
		};
		npcs.add(tavernMaid);
		zone.assignRPObjectID(tavernMaid);
		tavernMaid.setEntityClass("oldmaidnpc");
		tavernMaid.setPosition(10, 16);
		tavernMaid.initHP(100);
		zone.add(tavernMaid);
	}
}
