package games.stendhal.server.maps.semos.jail;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Semos Jail - Level -2.
 * 
 * @author hendrik
 */
public class JailKeeperNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildJailKeeper(zone);
		disabledMagicScrolls(zone);
	}

	private void buildJailKeeper(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Sten Tanquilos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 17));
				nodes.add(new Node(27, 17));
				nodes.add(new Node(27, 18));
				nodes.add(new Node(4, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Greetings! How may I #help you?");
				addJob("I am the jail keeper. You have been confined here because of your bad behaviour.");
				addHelp("Please wait for an administrator to come here and decide what to do with you. In the meantime, there is no escape for you.");
				addGoodbye();
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(4, 17);
		npc.initHP(100);
		zone.add(npc);
	}

	private void disabledMagicScrolls(final StendhalRPZone zone) {
		zone.disAllowTeleport();
	}
}
