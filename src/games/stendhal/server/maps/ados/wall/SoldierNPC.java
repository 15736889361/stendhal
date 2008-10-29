package games.stendhal.server.maps.ados.wall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ados Wall North population.
 *
 * @author hendrik
 * @author kymara
 */
public class SoldierNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAdosGreetingSoldier(zone);
	}

	/**
	 * Creatures a soldier telling people a story, why Ados is so empty.
	 *
	 * @param zone StendhalRPZone
	 */
	private void buildAdosGreetingSoldier(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Julius") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(84, 109));
				path.add(new Node(84, 116));
				setPath(new FixedPath(path, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi, welcome to Ados City!");
				addReply("map", "Caption\n"
					+ "1 Bank,   2 Goldsmith,   3 Bakery,   4 Haunted House,\n"
					+ "5 Castle,   6 Felina's House,   7 Barracks \n"
					+ "8 Bar,   9 Sewing Rooms, ida \n"
					+ "10 Meat and Fish Huts,   11 Town Hall",
					new ExamineChatAction("map-ados-city.png", "Ados City", "Map of Ados City"));
				addJob("I guard Ados against attacks, and #help visitors.");
				addHelp("If you need a #map to guide you around Ados, just ask.");
				addGoodbye("I hope you will enjoy your visit to Ados.");
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(84, 109);
		npc.initHP(100);
		zone.add(npc);
	}
}
