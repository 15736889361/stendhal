package games.stendhal.server.maps.nalwor.tower;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Princess NPC who lives in a tower.
 *
 * @author kymara
 */
public class PrincessNPC implements ZoneConfigurator {
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
		SpeakerNPC npc = new SpeakerNPC("Tywysoga") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(3, 3));
				nodes.add(new Node(7, 3));
				nodes.add(new Node(7, 9));
				nodes.add(new Node(12, 9));
				nodes.add(new Node(12, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hail to thee, human.");
				addJob("I'm a princess. What can I do?");
				addHelp("A persistent person could do a #task for me.");
				addOffer("I don't trade. My parents would have considered it beneath me.");
 				addGoodbye("Goodbye, strange one.");
			}
		};

		npc.setDescription("You see a beautiful but forlorn High Elf.");
		npc.setEntityClass("elfprincessnpc");
		npc.setPosition(17, 13);
		npc.initHP(100);
		zone.add(npc);
	}
}
