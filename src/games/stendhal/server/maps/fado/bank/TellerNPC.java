package games.stendhal.server.maps.fado.bank;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the bank teller NPC.
 *
 * @author timothyb89
 */
public class TellerNPC implements ZoneConfigurator {

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
	// IL0_TellerNPC
	//

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC bankNPC = new SpeakerNPC("Yance") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 2));
				nodes.add(new Node(15, 15));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Welcome to the Fado Bank! Do you need #help?");
				addJob("I am the manager for the bank.");
				addHelp("Just to the right, you can see a few chests. Open one and you can store your belongings in it.");
				addGoodbye("Have a nice day.");
			}
		};

		npcs.add(bankNPC);
		zone.assignRPObjectID(bankNPC);
		bankNPC.put("class", "youngnpc");
		bankNPC.set(15, 1);
		bankNPC.initHP(1000);
		zone.add(bankNPC);
	}
}
