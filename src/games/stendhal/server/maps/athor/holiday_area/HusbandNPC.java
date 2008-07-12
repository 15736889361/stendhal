package games.stendhal.server.maps.athor.holiday_area;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

public class HusbandNPC extends SpeakerNPCFactory {

	@Override
	protected SpeakerNPC instantiate(final String name) {
		final SpeakerNPC npc = new SpeakerNPC(name) {
			@Override
			public void say(final String text) {
				// He doesn't move around because he's "lying" on his towel.
				say(text, false);
			}
		};
		return npc;
	}
	
	@Override
	public void createDialog(final SpeakerNPC npc) {
		npc.addGreeting("Hi!");
		npc.addQuest("We have no tasks, we're here on holiday.");
		npc.addJob("I am a coachman, but on this island there are no carriages!");
		npc.addHelp("Don't try to talk to my wife, she is very shy.");
		npc.addGoodbye("Bye!");
	}
}
