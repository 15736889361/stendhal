package games.stendhal.server.maps.quests;

import games.stendhal.common.Grammar;
import games.stendhal.common.MathHelper;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.Player;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * QUEST: Cloaks for Bario
 * 
 * PARTICIPANTS:
 * - Bario, a guy living in an underground house deep under the Ados
 *   Wildlife Refuge
 * 
 * STEPS:
 * - Bario asks you for a number of blue elf cloaks.
 * - You get some of the cloaks somehow, e.g. by killing elves.
 * - You bring the cloaks to Bario and give them to him.
 * - Repeat until Bario received enough cloaks. (Of course you can
 *   bring up all required cloaks at the same time.)
 * - Bario gives you a golden shield in exchange.
 * 
 * REWARD:
 * - golden shield
 * - 1500 XP
 * 
 * REPETITIONS:
 * - None.
 */
public class CloaksForBario extends AbstractQuest {

	private static final int REQUIRED_CLOAKS = 10;
	private static final String QUEST_SLOT = "cloaks_for_bario";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	private void step_1() {
		SpeakerNPC npc = npcs.get("Bario");
		
		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE,
				SpeakerNPC.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return !player.hasQuest(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				"Hey! How did you get down here? You did what? Huh. Well, I'm Bario. I don't suppose you could do a #task for me?",
				null);

		npc.add(ConversationStates.ATTENDING,
				SpeakerNPC.QUEST_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return !player.hasQuest(QUEST_SLOT);
					}
				},
				ConversationStates.QUEST_OFFERED,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						engine.say("I don't dare go upstairs anymore because I stole a beer barrel from the dwarves. But it is so cold down here... Can you help me?");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				SpeakerNPC.QUEST_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return player.hasQuest(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (!player.isQuestCompleted(QUEST_SLOT)) {
							engine.say("You promised me to bring me ten blue elven cloaks. Remember?");
						} else {
							// player has already finished the quest
							engine.say("I don't have anything for you to do, really.");
						}
					}
				});

		npc.add(ConversationStates.ATTENDING,
				SpeakerNPC.QUEST_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (!player.isQuestCompleted(QUEST_SLOT)) {
							if (player.hasQuest(QUEST_SLOT)) {
								engine.say("You promised me to bring me ten blue elven cloaks. Remember?");
							} else {
								engine.say("I don't dare go upstairs anymore because I stole a beer barrel from the dwarves. But it is so cold down here... Can you help me?");
							}
						} else {
							// player has already finished the quest
							engine.say("I don't have anything else for you to do, really. Thanks for the offer.");
							engine.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				SpeakerNPC.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						engine.say("I need some blue elven cloaks if I'm to survive the winter. Bring me ten of them, and I will give you a reward.");
						player.setQuest(QUEST_SLOT, Integer.toString(REQUIRED_CLOAKS));
					}
				});
		
		
		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				"no",
				null,
				ConversationStates.ATTENDING,
				"Oh dear... I'm going to be in trouble...",
				null
				);
	}

	private void step_2() {
		// Just find some of the cloaks somewhere and bring them to Bario.
	}

	private void step_3() {
		SpeakerNPC npc = npcs.get("Bario");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE,
				SpeakerNPC.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return player.hasQuest(QUEST_SLOT)
								&& ! player.isQuestCompleted(QUEST_SLOT);
					}
				},
				ConversationStates.QUESTION_1,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						engine.say("Hi again! I still need "
								+ player.getQuest(QUEST_SLOT)
								+ " blue elven " + Grammar.plnoun(MathHelper.parseInt(player.getQuest(QUEST_SLOT)), "cloak") + ". Do you have any for me?");
					}
				});
		
		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE,
				SpeakerNPC.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return player.isQuestCompleted(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				"Welcome! Thanks again for those cloaks.",
				null);

	// player says he doesn't have any blue elf cloaks with him
	npc.add(ConversationStates.QUESTION_1,
			"no",
			null,
			ConversationStates.ATTENDING,
			"Too bad.",
			null);

	// player says he has a blue elf cloak with him
	npc.add(ConversationStates.QUESTION_1,
			SpeakerNPC.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text, SpeakerNPC engine) {
					if (player.drop("elf_cloak_+2")) {
						// find out how many cloaks the player still has to bring
						int toBring = Integer.parseInt(player.getQuest(QUEST_SLOT)) - 1;
						if (toBring > 0) {
							player.setQuest(QUEST_SLOT, Integer.toString(toBring));
							engine.say("Thank you very much! Do you have another one? I still need " + Grammar.quantityplnoun(toBring, "cloak") + ".");
							engine.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							Item goldenShield = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem("golden_shield");
							goldenShield.put("bound", player.getName());
							player.equip(goldenShield, true);
							player.addXP(1500);
							player.notifyWorldAboutChanges();
							player.setQuest(QUEST_SLOT, "done");
							engine.say("Thank you very much! Now I have enough cloaks to survive the winter. Here, take this golden shield as a reward.");
						}
					} else {
						engine.say("Really? I don't see any...");
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
