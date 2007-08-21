package games.stendhal.server.maps.fado.city;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the city greeter NPC.
 *
 * @author timothyb89
 */
public class GreeterNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	private ShopList shops = ShopList.get();

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
	// OL0_GreeterNPC
	//

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC greeterNPC = new SpeakerNPC("Xhiphin Zohos") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(39, 29));
				nodes.add(new Node(23, 29));
				nodes.add(new Node(23, 21));
				nodes.add(new Node(40, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hello! Welcome to Fado City! You can #learn about Fado from me.");
				addReply("learn",
				        "Fado guards the bridge over Or'ril river which is vital for the commercial route between #Deniran and Ados. There's an active social life here, being the preferred city for celebrating marriages and tasting elaborate meals.");
				addReply("Deniran",
				        "Deniran is the jewel of the crown. Deniran is the center of Faiumoni and supports the army that tries to defeat enemies that wants to conquer Faiumoni.");
				addJob("I greet all of the new-comers to Fado. I can #offer you a scroll if you'd like to come back here again.");
				addHelp("You can head into the tavern to buy food and drinks. You can also visit the people in the houses, or visit the blacksmith or the city hotel.");
				addSeller(new SellerBehaviour(shops.get("fadoscrolls")));
				addGoodbye("Bye.");
			}
		};
		npcs.add(greeterNPC);
		zone.assignRPObjectID(greeterNPC);
		greeterNPC.setOutfit(new Outfit(05, 01, 06, 01));
		greeterNPC.set(39, 29);
		greeterNPC.initHP(1000);
		zone.add(greeterNPC);
	}
}
