package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Jailed Dwarf
 * 
 * PARTICIPANTS: - Hunel, the guard of the Dwarf Kingdom's Prison
 * 
 * STEPS: - You see Hunel locked in the cell - You get the key by killing the
 * Duergar King - You speak to Hunel when you have the key. - Hunel wants to
 * stay in, he is afraid. - You can then sell chaos equipment to Hunel.
 * 
 * REWARD: - 2000 XP - everlasting place to sell chaos equipment
 * 
 * REPETITIONS: - None.
 */
public class JailedDwarf extends AbstractQuest {

	private static final String QUEST_SLOT = "jailed_dwarf";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	private void step_1() {
		SpeakerNPC npc = npcs.get("Hunel");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				null, ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
						if (!player.isQuestCompleted(QUEST_SLOT)) {
							if (player.isEquipped("kanmararn prison key")) {
								player.setQuest(QUEST_SLOT, "done");
								player.addXP(2000);
								engine.say("You got the key to unlock me! *mumble*  Errrr ... it doesn't look too safe out there for me ... I think I'll just stay here ... perhaps someone could #offer me some good equipment ... ");
							} else {
								engine.say("Help! The duergars have raided the prison and locked me up! I'm supposed to be the Guard! It's a shambles.");
								player.setQuest(QUEST_SLOT, "start");
								engine.setCurrentState(ConversationStates.IDLE);
							}
						} else {
							engine.say("Hi. As you see, I am still to nervous to leave ...");
						}
					}
				});

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		step_1();
	}
}
