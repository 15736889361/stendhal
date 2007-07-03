package games.stendhal.server.maps.quests;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Speak with Io
 * PARTICIPANTS:
 * - Io
 *
 * STEPS:
 * - Talk to Io to activate the quest and keep speaking with Io.
 *
 * REWARD:
 * - 10 XP
 * - 5 gold coins
 *
 * REPETITIONS:
 * - As much as wanted, but you only get the reward once.
 */
public class MeetIo extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_io";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	@Override
	public List<String> getHistory(Player player) {
		List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("FIRST_CHAT");
		if (isCompleted(player)) {
			res.add("DONE");
		}
		return res;
	}

	private void prepareIO() {

		SpeakerNPC npc = npcs.get("Io Flotto");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.HELP_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						if (player.isQuestCompleted(QUEST_SLOT)) {
							npc.say("Do you want to repeat the six basic elements of telepathy? I already know the answer but I'm being polite...");
						} else {
							npc.say("I'm a telepath and a telekinetic; I can help you by sharing my mental skills with you. Do you want me to teach you the six basic elements of telepathy? I already know the answer but I'm being polite...");
						}
					}
				});
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_1,
				"Type #/who to ascertain the names of those adventurers who are currently present in the world of Stendhal. Do you want to learn the second basic element of telepathy?",
				null);

		npc.add(ConversationStates.INFORMATION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_2,
				"Type #/where #username to discern where in Stendhal that person is currently roaming; you can use #/where #sheep to keep track of any sheep you might own. To understand the system used for defining positions in Stendhal, try asking #Zynn; he knows more about it that I do. Ready for the third lesson?",
				null);

		npc.add(ConversationStates.INFORMATION_2,
				"Zynn",
				null,
				ConversationStates.INFORMATION_2,
				"His full name is Zynn Iwuhos. He spends most of his time in the library, making maps and writing historical record books. Ready for the next lesson?",
				null);

		npc.add(ConversationStates.INFORMATION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_3,
				"Type #/tell #username #message or #/msg #username #message to talk to anybody you wish, no matter where in Stendhal that person is.  You can type #// #response to continue talking to the last person you send a message to. Ready to learn my fourth tip?",
				null);

		npc.add(ConversationStates.INFORMATION_3,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_4,
				"Press #Shift+Up at the same time to recall things you previously said, in case you need to repeat yourself. You can also use #Ctrl+L if you are having trouble. Okay, shall we move on to the fifth lesson?",
				null);

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_5,
				"Type #/support #<message> to report a problem to any administrators who happen to be online at that moment. You can also try IRC, if you are still having problems; start up any IRC client and join channel ##arianne on the server #irc.freenode.net\nOkay, time for your last lesson in mental manipulation!",
				null);

		npc.add(ConversationStates.INFORMATION_5,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_6,
				"You can travel to the astral plane at any time, thereby saving and closing your game. Just type #/quit, or press the #Esc key, or even simply close the window. Okay! Hmm, I think you want to learn how to float in the air like I do.",
				null);

		/** Give the reward to the patient newcomer user */
		npc.add(ConversationStates.INFORMATION_6,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						String answer;
						if (player.isQuestCompleted(QUEST_SLOT)) {
							answer = "Hey! I know what you're thinking, and I don't like it!";
						} else {
							// give the reward
							StackableItem money = (StackableItem) StendhalRPWorld.get()
							.getRuleManager().getEntityManager().getItem(
									"money");

							money.setQuantity(10);
							player.equip(money);
		
							player.addXP(10);
							player.setQuest(QUEST_SLOT, "done");
							player.notifyWorldAboutChanges();
		
							answer = "Remember, don't let anything disturb your concentration.";
						}
		
						npc.say("*yawns* Maybe I'll show you later... I don't want to overload you with too much information at once. You can get a summary of all those lessons at any time, incidentally, just by typing #/help.\n"
								+ answer);
					}
				});

		npc.add(ConversationStates.ANY,
				"no",
				null,
				ConversationStates.IDLE,
				"If you ever decide to widen the frontiers of your mind a bit more, drop by and say hello. Farewell for now!",
				null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		prepareIO();
	}
}
