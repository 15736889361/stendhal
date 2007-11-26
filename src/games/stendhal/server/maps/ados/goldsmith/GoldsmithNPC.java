package games.stendhal.server.maps.ados.goldsmith;

import games.stendhal.common.Direction;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ados Goldsmith (Inside / Level 0)
 *
 * @author dine
 */
public class GoldsmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildGoldsmith(zone, attributes);
	}

	private void buildGoldsmith(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC goldsmith = new SpeakerNPC("Joshua") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				// to the oven
				nodes.add(new Node(5, 3));
				// to a water
				nodes.add(new Node(5, 9));
				nodes.add(new Node(4, 9));
				// to the table
				nodes.add(new Node(4, 12));
				nodes.add(new Node(3, 12));
				nodes.add(new Node(3, 13));
				// to the bar
				nodes.add(new Node(8, 13));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(14, 10));
				// towards the shields
				nodes.add(new Node(14, 5));
				nodes.add(new Node(18, 5));
				// to the starting point
				nodes.add(new Node(18, 3));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi!");
				addJob("I'm the goldsmith of this city.");
				addHelp("My brother Xoderos is a blacksmith in Semos. Currently he is selling tools. Perhaps he can make a #gold_pan for you.");
				addGoodbye("Bye");

				// Joshua makes gold if you bring him gold_nugget and wood
				Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	// use sorted TreeMap instead of HashMap
				requiredResources.put("wood", 2);
				requiredResources.put("gold_nugget", 1);

				ProducerBehaviour behaviour = new ProducerBehaviour("joshua_cast_gold",
						"cast", "gold_bar", requiredResources, 15 * 60);

				addProducer(behaviour,
				        "Hi! I'm the local goldsmith. If you require me to #cast you a #gold #bar just tell me!");
				addReply("wood",
		        		"I need some wood to keep my furnace lit. You can find any amount of it just lying around in the forest.");
				addReply(Arrays.asList("ore", "gold_ore", "gold_nugget"),
				        "I think there are places in the water where you can find gold ore. But you need a special tool to prospect for gold.");
				addReply(Arrays.asList("gold_bar", "gold", "bar"),
				        "After I've casted the gold for you keep it save. I've heard rumours that Fado city will be safe to travel to again soon. There you can sell or trade gold.");
				addReply("gold_pan",
				        "If you had a gold pan, you would be able to prospect for gold at certain places.");

			}
		};

		goldsmith.setEntityClass("goldsmithnpc");
		goldsmith.setDirection(Direction.DOWN);
		goldsmith.setPosition(18, 3);
		goldsmith.initHP(100);
		zone.add(goldsmith);
	}
}
