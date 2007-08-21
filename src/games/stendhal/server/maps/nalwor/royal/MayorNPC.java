package games.stendhal.server.maps.nalwor.royal;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the elf mayor NPC.
 *
 * @author kymara
 */
public class MayorNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

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
		SpeakerNPC npc = new SpeakerNPC("Maerion") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 23));
				nodes.add(new Node(13, 23));
				nodes.add(new Node(13, 25));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(17, 23));
				nodes.add(new Node(21, 23));
				nodes.add(new Node(21, 27));
				nodes.add(new Node(17, 27));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(13, 25));
				nodes.add(new Node(13, 23));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hello. You are brave, to stand before me.");
				addJob("You dare ask, little human?!");
				addHelp("Well, perhaps you can help me with a #problem I see brewing.");
				add(ConversationStates.ATTENDING, "problem", null, ConversationStates.ATTENDING,
				        "Here are no dark elves, believe me! Me?! no, no, no, I'm just well tanned...", null);
				addGoodbye("Farewell, human.");
			}
		};
		npc.setDescription("You see a regal elf. Something about him makes you uneasy.");
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		npc.put("class", "elfmayornpc");
		npc.set(9, 23);
		npc.initHP(100);
		zone.add(npc);
	}
}
