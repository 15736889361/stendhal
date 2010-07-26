package games.stendhal.server.maps.semos.plains;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A man hoeing the farm ground
 * 
 */
public class HoeingManNPC implements ZoneConfigurator {

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
		SpeakerNPC npc = new SpeakerNPC("Jingo Radish") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(48, 62));
				nodes.add(new Node(43, 76));
				nodes.add(new Node(43, 62));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Well met, wayfarer!");
				addJob("You see? I keep freeing the soil from weeds but those grow back every time...");
				addHelp("Take your time and check the area around... There's a mill somewhat north and a really nice farm to the east... Nice and rich country, you could go hunting for food!");
				addGoodbye("Goodbye and may your path be clear of weeds!");
			}
	
		};
		npc.setEntityClass("hoeingmannpc");
		npc.setDescription("You see a man with a hoe, he's busy weeding the soil.");
		npc.setPosition(48,62);
		npc.initHP(100);
		zone.add(npc);
	}

}
