package games.stendhal.server.maps.fado.forest;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an albino elf NPC 
 *
 * @author kymara
 */
public class GreeterNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Orchiwald") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 11));
				nodes.add(new Node(40, 11));
				nodes.add(new Node(40, 28));
				nodes.add(new Node(58, 28));
				nodes.add(new Node(58, 91));
				nodes.add(new Node(99, 91));
				nodes.add(new Node(99, 76));
				nodes.add(new Node(36, 76));
				nodes.add(new Node(36, 37));
				nodes.add(new Node(3, 37));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			        addGreeting("Welcome to the humble dwellings of the albino elves.");
				addJob("I just wander around. In fact, albino elves wander around a lot. We're #nomadic, you know.");
				addReply("nomadic","We don't have a permanent home, we travel instead between forests and glens. When we find a clearing we like, we settle. We liked this one because of the ancient #stones near by.");
				addReply("stones","They have some mystical quality. We like to be by them for the changing of the seasons.");
				addHelp("I would sell you enchanted scrolls to return to Fado City. I have a source of cheap ones.");
				addSeller(new SellerBehaviour(shops.get("fadoscrolls")) {

					@Override
					public int getUnitPrice(String item) {
						// Player gets 20 % rebate
						return (int) (0.80f * priceList.get(item));
					}
				});
				addQuest("A generous offer, but I require nothing, thank you.");
 				addGoodbye("Bye then.");
			}
		};
		npc.setDescription("You see Orchiwald, an albino elf.");
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		npc.setEntityClass("albinoelf2npc");
		npc.setPosition(3, 11);
		npc.initHP(100);
		zone.add(npc);
	}
}
