package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/** 
 * QUEST: Take gold for Grafindle
 * PARTICIPANTS: 
 * - Grafindle
 * - Lorithien
 * 
 * STEPS: 
 * - Talk with Grafindle to activate the quest.
 * - Talk with Lorithien for the money.
 * - Return the gold bars to Grafindle
 *
 * REWARD: 
 * - 200 XP
 * - key to nalwor bank customer room
 *
 * REPETITIONS:
 * - None.
 */
public class TakeGoldforGrafindle extends AbstractQuest {
	private static final int GOLD_AMOUNT = 25;
	private static final String QUEST_SLOT = "grafindle_gold";

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
		String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("QUEST_REJECTED");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "lorithien", "done")) {
			res.add("QUEST_ACCEPTED");
		}
		if ((questState.equals("lorithien") && player.isEquipped("gold_bar", GOLD_AMOUNT)) || questState.equals("done")) {
			res.add("FOUND_ITEM");
		}
		if (questState.equals("lorithien") && !player.isEquipped("gold_bar", GOLD_AMOUNT)) {
			res.add("LOST_ITEM");
		}
		if (questState.equals("done")) {
			res.add("DONE");
		}
		return res;
	}

	private void step_1() {

		SpeakerNPC npc = npcs.get("Grafindle");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (player.isQuestCompleted(QUEST_SLOT)) {
							engine.say("I ask only that you are honest.");
						} else {
							engine.say("I need someone who can be trusted with #gold.");
						}
					}
				});

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING,
				"gold",
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return player.isQuestCompleted(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				"The bank has the gold safe now. Thank you!",
				null);

		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.ATTENDING,
				"gold",
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return !player.hasQuest(QUEST_SLOT);
					}
				},
				ConversationStates.QUEST_OFFERED,
				"One of our customers needs to bank their gold bars here for safety. It's #Lorithien, she cannot close the Post Office so she never has time.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Thank you. I hope to see you soon with the gold bars ... unless you are tempted to keep them.",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						player.setQuest(QUEST_SLOT, "start");
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				"no",
				null,
				ConversationStates.ATTENDING,
				"Well, at least you are honest and told me from the start.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"Lorithien",
				null,
				ConversationStates.QUEST_OFFERED,
				"She works in the post office here in Nalwor. It's a big responsibility, as those gold bars could be sold for a lot of money. Can you be trusted?",
				null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING,
				"gold",
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals("start");
					}
				},
				ConversationStates.ATTENDING,
				"#Lorithien will be getting so worried with all that gold not safe! Please fetch it!",
				null);

		npc.add(ConversationStates.ATTENDING,
				"lorithien",
				null,
				ConversationStates.ATTENDING,
				"She works in the post office here in Nalwor.",
				null);
	}

	private void step_2() {
		SpeakerNPC npc = npcs.get("Lorithien");

		/** If player has quest and is in the correct state, just give him the gold bars. */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT)
										.equals("start");
					}
				},
				ConversationStates.ATTENDING,
				"I'm so glad you're here! I'll be much happier when this gold is safely in the bank.",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						player.setQuest(QUEST_SLOT, "lorithien");

						StackableItem goldbars = (StackableItem) StendhalRPWorld.get().getRuleManager().getEntityManager()
								.getItem("gold_bar");
						goldbars.setQuantity(GOLD_AMOUNT);
						goldbars.put("bound", player.getName()); 
						player.equip(goldbars, true);
					}
				});

		/** If player keep asking for book, just tell him to hurry up */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(
										"lorithien");
					}
				},
				ConversationStates.ATTENDING,
				"Oh, please take that gold back to #Grafindle before it gets lost!",
				null);

		npc.add(ConversationStates.ATTENDING,
				"grafindle",
				null,
				ConversationStates.ATTENDING,
				"Grafindle is the senior banker here in Nalwor, of course!",
				null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(ConversationStates.ATTENDING,
				"gold",
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return !player.hasQuest(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				"Sorry, I have so many things to remember ... I didn't understand you.",
				null);
	}

	private void step_3() {
		SpeakerNPC npc = npcs.get("Grafindle");

		/** Complete the quest */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text, SpeakerNPC npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(
										"lorithien");
					}
				},
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (player.drop("gold_bar", GOLD_AMOUNT)) {
							engine.say("Oh, you brought the gold! Wonderful, I knew I could rely on you. Please, have this key to our customer room.");
							Item nalworkey = StendhalRPWorld.get()
									.getRuleManager().getEntityManager()
									.getItem("nalwor_bank_key");
							nalworkey.put("bound", player.getName());
							player.equip(nalworkey);
							player.addXP(200);

							player.notifyWorldAboutChanges();

							player.setQuest(QUEST_SLOT, "done");
						} else {
							engine.say("Haven't you got the gold bars from #Lorithien yet? Please go get them, quickly!");
						}
					}
				});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
		step_3();
	}
}
