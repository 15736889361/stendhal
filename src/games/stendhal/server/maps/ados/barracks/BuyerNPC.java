package games.stendhal.server.maps.ados.barracks;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
	private ShopList shops = SingletonRepository.getShopList();

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
		SpeakerNPC npc = new SpeakerNPC("Mrotho") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(45, 49));
				nodes.add(new Node(29, 49));
				nodes.add(new Node(29, 57));
				nodes.add(new Node(45, 57));
				nodes.add(new Node(19, 57));
				nodes.add(new Node(19, 49));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Greetings. Have you come to enlist as a soldier?");
				addReply("yes", "Huh! Well I don't let your type enlist! Perhaps you want to #offer some of that armor instead...");
				addReply("no", "Good! You wouldn't have fit in here anyway.");
				addJob("I'm looking after the weaponry here. We're running low. I see you have some armor you might #offer though.");
				addHelp("I buy armor for the barracks here, make me an #offer.");
				addOffer("Please look at the blackboard by the shields rack to see what we are short of, and what we pay.");
				addQuest("Oh, thanks but no thanks. I don't need anything.");
				new BuyerAdder().add(this, new BuyerBehaviour(shops.get("buyrare3")), false);
				addGoodbye("Goodbye, comrade.");
			}
		};
		npc.setDescription("You see Mrotho, guarding over Ados Barracks.");
		npc.setEntityClass("barracksbuyernpc");
		npc.setPosition(45, 49);
		npc.initHP(500);
		zone.add(npc);
	}
}
