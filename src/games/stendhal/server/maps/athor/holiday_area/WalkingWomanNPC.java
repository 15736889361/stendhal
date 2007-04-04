package games.stendhal.server.maps.athor.holiday_area;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

public class WalkingWomanNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addGreeting("Hi!");
		npc.addQuest("I have no jobs for you, my friend");
		npc.addJob("I'm just walking along the coast!");
		npc.addHelp("I cannot help you...I'm just a girl...");
		npc.addGoodbye("Bye!");
	}
}
