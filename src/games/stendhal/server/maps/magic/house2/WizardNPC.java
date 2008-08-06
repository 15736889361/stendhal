package games.stendhal.server.maps.magic.house2;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Builds a wizard npc, an expert in textiles
 *
 * @author kymara 
 */
public class WizardNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Whiggins") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			    protected void createDialog() {
				addGreeting("Welcome, warmly");
				addHelp("If you need scrolls, Erodel Bmud sells a wide range.");
				addOffer("I don't sell anything here.");
				addJob("I keep this house nice and watch the fairies.");
				addGoodbye("Till next time.");
				// remaining behaviour defined in maps.quests.MithrilCloak
	 	     }
		    
		};

		npc.setDescription("You see Whiggins, looking tranquil and happy.");
		npc.setEntityClass("mithrilforgernpc");
		npc.setPosition(14, 14);
		npc.initHP(100);
		zone.add(npc);
	}
}
