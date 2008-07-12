package games.stendhal.server.maps.ados.entwives;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * entwife located in 0_ados_mountain_n2_w2.
 */
public class EntwifeNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildentwife(zone);
	}

	private void buildentwife(final StendhalRPZone zone) {
		final SpeakerNPC entwife = new SpeakerNPC("Tendertwig") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Welcome, fair wanderer.");
				addJob("I guard all i can see. It is a peaceful life.");
				addHelp("There is lots to see and harvest here. Just wander around.");
				addOffer("I have nothing to offer but fresh air and sunshine.");
				addGoodbye("May your travels be pleasant, my fine friend.");
				addQuest("There is something i wish. But I have no time at present to discuss it. Please come back again later.");
			}
		};

		entwife.setEntityClass("transparentnpc");
		entwife.setPosition(25, 35);
		entwife.initHP(100); 
		zone.add(entwife);
	}
}
