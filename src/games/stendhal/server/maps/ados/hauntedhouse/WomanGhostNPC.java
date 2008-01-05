package games.stendhal.server.maps.ados.hauntedhouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a female Ghost NPC.
 *
 * @author kymara
 */
public class WomanGhostNPC implements ZoneConfigurator {
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
	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC woman = new SpeakerNPC("Carena") {
			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 4));
				nodes.add(new Node(13, 4));
				nodes.add(new Node(13, 14));
				nodes.add(new Node(3, 14));
				nodes.add(new Node(3, 26));
				nodes.add(new Node(11, 26));
				nodes.add(new Node(11, 7));
				nodes.add(new Node(23, 7));
				nodes.add(new Node(23, 29));
				nodes.add(new Node(29, 29));
				nodes.add(new Node(29, 2));
				nodes.add(new Node(21, 2));
				nodes.add(new Node(21, 7));
				nodes.add(new Node(3, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//addGreeting("Wooouhhhhhh!");
				addJob("I can do nothing useful on this earthly world. I haunt this house now.");
				addHelp("Here is a warning: if you die, you will become a ghost like me, partially visible and intangible. But if you can find your way out of the afterlife, you will be reborn.");
				addGoodbye("Bye");
				// remaining behaviour defined in games.stendhal.server.maps.quests.FindGhosts
			}
		};
		woman.setDescription("You see a ghostly figure of a woman. She appears somehow sad.");
		woman.setResistance(0);

		woman.setEntityClass("woman_011_npc");
		// She is a ghost so she is see through
		woman.setVisibility(40);
		woman.setPosition(3, 4);
		// She has low HP
		woman.initHP(30);
		zone.add(woman);
	}
}
