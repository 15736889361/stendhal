package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the flower seller in kirdneh.
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC sellernpc = new SpeakerNPC("Fleur") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi! Are you here to #trade?");
				addReply(ConversationPhrases.YES_MESSAGES, "Good! I can sell you a beautiful red rose. Not rhosyd mind you, they're rare. Only Rose Leigh knows where they grow, and noone ever knows where Rose Leigh is!");
				addReply(ConversationPhrases.NO_MESSAGES, "Very well, if I can help you just say.");
				addJob("I sell roses in this here market.");
				addHelp("If you need to access your funds, there is a branch of Fado bank right here in Kirdneh. It's the small building north of the museum, on the east of the city.");
				Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("rose", 50);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye("Come back soon!");
			}
		};

		sellernpc.setEntityClass("woman_001_npc");
		sellernpc.setPosition(64, 82);
		sellernpc.initHP(100);
		zone.add(sellernpc);
	}
}
