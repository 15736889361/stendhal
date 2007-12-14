package games.stendhal.server.maps.kotoch;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kymara
 */
public class OrcSamanNPC implements ZoneConfigurator {
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
		SpeakerNPC npc = new SpeakerNPC("Orc Saman") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 113));
				nodes.add(new Node(16, 113));
				nodes.add(new Node(16, 115));
				nodes.add(new Node(22, 115));
				nodes.add(new Node(22, 119));				
				nodes.add(new Node(8, 119));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Oof.");
				addJob("Me, Orc Saman.");
				addHelp("Orc Saman need help! Make #task.");
				addOffer("No trade.");
 				addGoodbye("see yoo.");
			}
		};

		npc.setDescription("You see an Orc Saman.");
		npc.setEntityClass("orcsamannpc");
		npc.setPosition(8, 113);
		npc.initHP(100);
		zone.add(npc);
	}
}
