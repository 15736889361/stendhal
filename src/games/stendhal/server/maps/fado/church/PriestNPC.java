package games.stendhal.server.maps.fado.church;

import games.stendhal.common.Direction;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;

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

	private NPCList npcs = NPCList.get();

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
				addJob("I am the priest, and I will #marry those who have gold rings to exchange and are engaged.");
				addHelp("I can help you #marry your loved one. But you must be engaged under the supervision of Sister Benedicta, and have a #ring to give your partner.");
				addQuest("I will #marry people who were engaged in the proper manner. Speak to Sister Benedicta if you are not engaged yet. And remember each to bring a wedding #ring!");
				addGoodbye("May the force be with you.");
				addReply("ring", "Once you are engaged, you can go to Ognir who works here in Fado to get your wedding rings made. I believe he also sells engagement rings, but they are purely for decoration. How wanton!");

			}
		};
		priest.setDescription("You see the holy Priest of Fado Church");
		npcs.add(priest);
		zone.assignRPObjectID(priest);
		priest.put("class", "priestnpc");
		priest.setPosition(11, 5);
		priest.setDirection(Direction.DOWN);
		priest.initHP(100);
		zone.add(priest);
	}
}
