package games.stendhal.server.maps.semos.tavern;

import games.stendhal.server.StendhalRPZone;
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

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class BarMaidNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildMargaret(zone);
	}

	private void buildMargaret(StendhalRPZone zone) {
		SpeakerNPC margaret = new SpeakerNPC("Margaret") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 13));
				nodes.add(new Node(17, 14));
				nodes.add(new Node(16, 9));
				nodes.add(new Node(13, 9));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 11));
				nodes.add(new Node(23, 11));
				nodes.add(new Node(23, 11));
				nodes.add(new Node(23, 14));
				nodes.add(new Node(23, 11));
				nodes.add(new Node(17, 11));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("I am the bar maid for this fair tavern. We sell both imported and local beers, and fine food.");
				addHelp("This tavern is a great place to take a break and meet new people! Just ask if you want me to #offer you a drink.");
				addSeller(new SellerBehaviour(shops.get("food&drinks")));
				addGoodbye();
			}
		};
		npcs.add(margaret);
		zone.assignRPObjectID(margaret);
		margaret.put("class", "tavernbarmaidnpc");
		margaret.setPosition(17, 13);
		margaret.initHP(100);
		zone.add(margaret);
	}
}
