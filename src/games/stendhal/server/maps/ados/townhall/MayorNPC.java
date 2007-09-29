package games.stendhal.server.maps.ados.townhall;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds ados mayor NPC.
 * He may give an items quest later
 * Now he sells ados scrolls
 * @author kymara
 */
public class MayorNPC implements ZoneConfigurator {
	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildMayor(zone);
	}

	private void buildMayor(StendhalRPZone zone) {
		SpeakerNPC mayor = new SpeakerNPC("Mayor Chalmers") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 10));
				nodes.add(new Node(8, 10));	
				nodes.add(new Node(8, 16));	
				nodes.add(new Node(25, 16));
				nodes.add(new Node(25, 13));
				nodes.add(new Node(37, 13));
				nodes.add(new Node(25, 13));
				nodes.add(new Node(25, 16));
				nodes.add(new Node(8, 16));
				nodes.add(new Node(8, 10));	
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("On behalf of the citizens of Ados, welcome.");
				addJob("I'm the mayor of Ados. I can #offer you the chance to return here easily.");
				addHelp("Ask me about my #offer to return here.");
				//addQuest("I don't know you well yet. Perhaps later in the year I can trust you with something.");
				addSeller(new SellerBehaviour(shops.get("adosscrolls")));
				addGoodbye("Good day to you.");
			}
		};

		mayor.setDescription("You see the respected mayor of Ados");
		mayor.setEntityClass("badmayornpc");
		mayor.setPosition(3, 10);
		mayor.initHP(100);
		zone.add(mayor);
	}
}
