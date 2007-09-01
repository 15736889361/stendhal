package games.stendhal.server.maps.fado.dressingrooms;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.OutfitChangerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Dressing rooms at fado hotel
 *
 * @author kymara
 */
public class GroomAssistantNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildDressingRoom(zone, attributes);
	}

	private void buildDressingRoom(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Timothy") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(20, 10));
				nodes.add(new Node(20, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Good day! If you're a prospective groom I can #help you prepare for your wedding.");
				addJob("I assist grooms with getting suitably dressed for their wedding.");
				addHelp("Please tell me if you want to #wear #a #suit for your wedding.");
				addReply("suit", "If you want to look smart you must #wear #a #suit for your wedding. The hire charge is 50 money.");
				addQuest("You should probably be thinking about your wedding.");
				addGoodbye("Good bye, I hope everything goes well for you.");

				Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("suit", 50);
				OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList);
				addOutfitChanger(behaviour, "wear");
			}
		};
		NPCList.get().add(npc);
		zone.assignRPObjectID(npc);
		npc.setEntityClass("executivenpc");
		npc.setPosition(20, 10);
		npc.initHP(100);
		zone.add(npc);
	}
}
