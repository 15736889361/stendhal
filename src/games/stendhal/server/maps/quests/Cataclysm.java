package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AlwaysTrueCondition;

import java.util.Arrays;


/**
 * Tell the player that Cataclysm is ahead.
 *
 * @author kymara
 */
public class Cataclysm extends AbstractQuest {

	/**
	 * Makes Carmen tell you that she can sense big changes.
	 */
	private void carmen() {
		SpeakerNPC npc = npcs.get("Carmen");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AlwaysTrueCondition(),
				ConversationStates.ATTENDING,
				"Hello. I can #heal you in these #troubled #times.", null);

		npc.addReply(Arrays.asList("troubled", "times"),
				"I sense many changes approaching. I believe that a #Cataclysm is coming.");
		npc.addReply("Cataclysm",
				"Yes, some upheaval, maybe a rebirth of old spirits. The lands could change and new ways begin.");

	}

	/**
	 * Makes Diogenes tell you to ask Carmen what's happening.
	 */
	private void diogenes() {
		SpeakerNPC npc = npcs.get("Diogenes");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING,
			"Greetings. I expect you are wondering what strange things are happening here?",
			null);

		npc.addReply("yes",
						"So am I, my friend. I expect young Carmen will tell you something.");
		npc.addReply(
						"no",
						"Ah, the folly of youth! You do not look around you with open eyes until it is too late.");

	}

	/**
	 * Makes Hayunn Naratha refer to the Cataclysm.
	 */
	private void hayunn() {
		SpeakerNPC npc = npcs.get("Hayunn Naratha");
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING,
			"Greetings. I'm ashamed to address you while I look #unwell. It's not fitting for my post.",
			null);

		npc.addReply(
			"unwell",
			"I imagine it is from the smoke. I hope it's nothing more ominous. In any case, let me know if I can help you at all.");
	}

	/**
	 * Makes Monogenes speak of the fire and Cataclysm.
	 */
	private void monogenes() {
		SpeakerNPC npc = npcs.get("Monogenes");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING,
			"Hi. *cough* *splutter* The smoke is getting into my lungs. The #fire is spreading.",
			null);

		npc.addReply(
			"fire",
			"It started overnight and now Semos is lit up like a torch. They say a #Cataclysm is coming.");

		npc.addReply(
			"Cataclysm",
			"I've never seen the like, but my great grandfather spoke of such a thing. Some see it as a disaster. Others say that the rebuilding after such an event allows for new life and new ways.");

	}

	/**
	 * Makes Nomyr Ahba tell you rumours of the Cataclysm.
	 */
	private void nomyr() {
		SpeakerNPC npc = npcs.get("Nomyr Ahba");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING,
			"Hi. I'm guessing you knew to come to an old gossip, for #information.",
			null);

		npc.addReply(
			"information",
			"Well my friend, fire is spreading through Semos and we're all getting sick. People say that it's the start of a #Cataclysm...");

		npc.addReply(
			"Cataclysm",
			"Don't ask me why, but I think the world will look very different in the near future. Lucky I haven't got a home to lose, really.");
	}

	/**
	 * Makes Sato tell you that Carmen can sense big changes.
	 */
	private void sato() {
		SpeakerNPC npc = npcs.get("Sato");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING,
			"Hi. We've fallen on hard #times.", null);

		npc.addReply("times",
			"All I know is, my sheep are getting sick. Maybe #Carmen can sense what is happening here.");

		npc.addReply(
			"Carmen",
			"She's a summon healer, she can sense anything strange with her powers. Me, I'm just a simple sheep dealer.");
	}


	@Override
	public void addToWorld() {
		super.addToWorld();

		carmen();
		diogenes();
		hayunn();
		monogenes();
		nomyr();
		sato();
	}

}
