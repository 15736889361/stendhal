package games.stendhal.server.maps.nalwor.hell;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Creates the elementals npcs in hell.
 *
 * @author kymara
 */
public class ElementalsNPCs implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPCs(zone);
	}

	private void buildNPCs(final StendhalRPZone zone) {
		final String[] names = {"Savanka", "Xeoilia", "Azira"};
		final Node[] start = new Node[] { new Node(115, 6), new Node(124, 10), new Node(116, 18) };
		for (int i = 0; i < 3; i++) {
			final SpeakerNPC npc = new SpeakerNPC(names[i]) {

				@Override
				protected void createPath() {
					final List<Node> nodes = new LinkedList<Node>();
					nodes.add(new Node(115, 6));
                    nodes.add(new Node(119, 6));
					nodes.add(new Node(119, 5));
                    nodes.add(new Node(122, 5));
                    nodes.add(new Node(122, 6));
                    nodes.add(new Node(125, 6));
                    nodes.add(new Node(125, 10));
                    nodes.add(new Node(124, 10));
                    nodes.add(new Node(124, 12));
					nodes.add(new Node(123, 12));
					nodes.add(new Node(123, 15));
                    nodes.add(new Node(124, 15));
					nodes.add(new Node(124, 17));
                    nodes.add(new Node(122, 17));
                    nodes.add(new Node(122, 18));
                    nodes.add(new Node(116, 18));
                    nodes.add(new Node(116, 16));
                    nodes.add(new Node(114, 16));
                    nodes.add(new Node(114, 15));
                    nodes.add(new Node(113, 15));
                    nodes.add(new Node(113, 13));
                    nodes.add(new Node(111, 13));
                    nodes.add(new Node(111, 8));
                    nodes.add(new Node(115, 8));
					setPath(new FixedPath(nodes, true));
				}

				@Override
				protected void createDialog() {
					add(
			     		ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						ConversationStates.IDLE,
						"Speak not to us, the harbingers of Hell!",
						null);
			
				}
			};
			npc.setEntityClass("fireelementalnpc");
			npc.setPosition(start[i].getX(), start[i].getY());
			npc.setDirection(Direction.DOWN);
			npc.initHP(100);
			zone.add(npc);
		}
	}
}
