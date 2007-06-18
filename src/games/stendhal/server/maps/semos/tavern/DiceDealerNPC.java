package games.stendhal.server.maps.semos.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.maps.ZoneConfigurator;
import java.awt.Rectangle;
import java.util.Map;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class DiceDealerNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildRicardo(zone);
	}

	private void buildRicardo(StendhalRPZone zone) {
		CroupierNPC ricardo = new CroupierNPC("Ricardo") {

			@Override
			protected void createPath() {
				// Ricardo doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				addGreeting("Welcome to the #gambling table, where dreams can come true.");
				addJob("I'm the only person in Semos who is licensed to offer gambling activities.");
				addReply(
				        "gambling",
				        "The rules are simple: just tell me if you want to #play, pay the stake, and throw the dice on the table. The higher the sum of the upper faces is, the nicer will be your prize. Take a look at the blackboards on the wall!");
				addHelp("If you are looking for Ouchit: he's upstairs.");
				addGoodbye();
			}
		};

		npcs.add(ricardo);

		zone.assignRPObjectID(ricardo);
		ricardo.put("class", "naughtyteen2npc");
		ricardo.setX(28);
		ricardo.setY(4);
		ricardo.setDirection(Direction.LEFT);
		ricardo.initHP(100);
		Rectangle tableArea = new Rectangle(25, 4, 2, 3);
		ricardo.setTableArea(tableArea);
		zone.add(ricardo);
	}
}
