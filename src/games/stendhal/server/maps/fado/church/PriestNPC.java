package games.stendhal.server.maps.fado.church;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;

import java.util.Map;


/**
 * Creates a priest NPC who can celebrate marriages between two
 * players.
 *
 * The marriage itself is done in a separate quest file
 *
 * @author daniel/kymara
 *
 */
public class PriestNPC implements ZoneConfigurator {
	private SpeakerNPC priest;

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
		priest = new SpeakerNPC("Priest") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Welcome to the church!");
				new HealerAdder().addHealer(this, 1000);
				addJob("I am the priest, and I will #marry those who have gold rings to exchange and are engaged.");
				addHelp("I can help you #marry your loved one. But you must be engaged under the supervision of Sister Benedicta, and have a #ring to give your partner.");
				addQuest("I will #marry people who were engaged in the proper manner. Speak to Sister Benedicta if you are not engaged yet. And remember each to bring a wedding #ring!");
				addGoodbye("Go well, and safely.");
				addReply("ring", "Once you are engaged, you can go to Ognir who works here in Fado to get your wedding rings made. I believe he also sells engagement rings, but they are purely for decoration. How wanton!");
			}
		};

		priest.setDescription("You see the holy Priest of Fado Church");
		priest.setEntityClass("priestnpc");
		priest.setPosition(11, 5);
		priest.setDirection(Direction.DOWN);
		priest.initHP(100);
		zone.add(priest);
	}
}
