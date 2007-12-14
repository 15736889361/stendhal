package games.stendhal.server.maps.quests;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.Sentence;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Quest to get a fishing rod PARTICIPANTS: - Perquod the fisherman
 * 
 * 
 * STEPS: - The fisherman asks you to go to the library to get him a quote of a
 * famous fisherman. - The player goes to the library where a book with some
 * quotes lies on the table and looks the correct one up. - The player goes back
 * to the fisherman and tells him the quote.
 * 
 * 
 * REWARD: - 750 XP - A fishing rod.
 * 
 * REPETITIONS: - no repetitions
 * 
 * @author dine
 */

public class LookUpQuote extends AbstractQuest {
	static final String QUEST_SLOT = "get_fishing_rod";

	static Map<String, String> quotes = new HashMap<String, String>();
	static {
		quotes.put("fisherman Bully", "Clownfish are always good for a laugh.");
		quotes.put("fisherman Jacky",
						"Don't mistake your trout for your old trout, she wouldn't taste so good.");
		quotes.put("fisherman Tommy",
						"I wouldn't trust a surgeonfish in a hospital, there's something fishy about them.");
		quotes.put("fisherman Sody",
				"Devout Crustaceans believe in the One True Cod.");
		quotes.put("fisherman Humphrey",
						"I don't understand why noone buys my fish. The sign says 'Biggest Roaches in town'.");
		quotes.put("fisherman Monty",
						"My parrot doesn't like to sit on a perch. He says it smells fishy.");
		quotes.put("fisherman Charby",
						"That fish restaurant really overcooks everything. It even advertises char fish.");
		quotes.put("fisherman Ally", "Holy mackerel! These chips are tasty.");
	}

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	@Override
	public List<String> getHistory(Player player) {
		List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add("FIRST_CHAT");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("GET_FISHING_ROD");
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add("DONE");
		}
		return res;
	}

	private void createFishingRod() {
		SpeakerNPC fisherman = npcs.get("Pequod");

		fisherman.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
					if (!player.hasQuest(QUEST_SLOT)) {
						npc.say("Hello newcomer! I can #help you on your way to become a real fisherman!");
					} else if (!player.isQuestCompleted(QUEST_SLOT)) {
						String name = player.getQuest(QUEST_SLOT);
						npc.say("Welcome back! Did you look up the famous quote by " + name + "?");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Welcome back!");
					}
				}
			});

		fisherman.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
					if (player.isQuestCompleted(QUEST_SLOT)) {
						npc.say("No, thanks. I have all I need.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else if (player.hasQuest(QUEST_SLOT)) {
						String name = player.getQuest(QUEST_SLOT);
						npc.say("I already asked you for a favor already! Have you already looked up the famous quote by " + name + "?");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Well, I once had a book with quotes of famous fishermen, but I lost it. And now I cannot remember a certain quote. Can you look it up for me?");
					}
				}
			});

		fisherman.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Then I don't do you a favour, either.", null);

		fisherman.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
					String name = Rand.rand(quotes.keySet());
					npc.say("Please look up the famous quote by " + name + ".");
					player.setQuest(QUEST_SLOT, name);
				}
			});

		fisherman.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_2, "So, what is it?", null);

		fisherman.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Too bad. I would have had a nice reward for you.", null);

		fisherman.add(ConversationStates.QUESTION_2, "", null,
			ConversationStates.ATTENDING, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
					String name = player.getQuest(QUEST_SLOT);
					String quote = quotes.get(name);
					if (sentence.toString().equalsIgnoreCase(quote)) {
						npc.say("Oh right, that's it! How could I forget this? Here, take this handy fishing rod as an acknowledgement of my gratitude!");
						Item fishingRod = StendhalRPWorld.get()
								.getRuleManager().getEntityManager()
								.getItem("fishing_rod");
						fishingRod.setBoundTo(player.getName());
						player.equip(fishingRod, true);
						player.addXP(750);
						player.setQuest(QUEST_SLOT, "done");
						player.notifyWorldAboutChanges();
					} else {
						npc.say("I think you made a mistake. Come back if you can tell me the correct quote.");
						npc.setCurrentState(ConversationStates.IDLE);
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		createFishingRod();
	}
}
