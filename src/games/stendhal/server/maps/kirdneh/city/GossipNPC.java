package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Builds a information giving NPC in Kirdneh city. 
 *
 * @author kymara
 */
public class GossipNPC implements ZoneConfigurator {
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
		SpeakerNPC npc = new SpeakerNPC("Jef") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Heya!");
				addJob("Um, not sure what you mean. Right now I'm waiting for my mum to get back from the #shops.");
				addHelp("I have some #news about the bazaar over there.");
				
				addOffer("I don't sell stuff, I'm just waiting for my mum. But I have some #news if you wanna hear it.");
				addQuest("Huh? I don't get you.");
				addReply("news", "Some shopkeepers will be at the market soon! It'll be cool, it's kind of empty round here at the moment.");
				addReply("shops", "Yeah she's had to go out of town. But there's #news about our own bazaar.");
				addGoodbye("See you around.");
			}
		};

		npc.setEntityClass("kid6npc");
		npc.setPosition(114, 67);
		npc.initHP(100);
		zone.add(npc);
	}
}
