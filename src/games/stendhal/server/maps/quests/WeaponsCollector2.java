package games.stendhal.server.maps.quests;

import games.stendhal.common.Grammar;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: The Weapons Collector Part 2
 * 
 * PARTICIPANTS: - Balduin, a hermit living on a mountain between Semos and Ados
 * 
 * STEPS: - Balduin asks you for some new weapons. - You get one of the weapons
 * somehow, e.g. by killing a monster. - You bring the weapon up the mountain
 * and give it to Balduin. - Repeat until Balduin received all weapons. (Of
 * course you can bring up several weapons at the same time.) - Balduin gives
 * you a pair of swords in exchange.
 * 
 * REWARD: - rhand sword and lhand sword - 3000 XP
 * 
 * REPETITIONS: - None.
 */
public class WeaponsCollector2 extends AbstractQuest {

	private static final List<String> neededWeapons = Arrays.asList(
			"morning_star", // fairly rare from glow_monster in haunted house
			"staff", // rare from monk on mountain
			"great_sword" // rare from devil_queen on mountain
	);

	/**
	 * Returns a list of the names of all weapons that the given player still
	 * has to bring to fulfil the quest.
	 * 
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of weapon names
	 */
	private List<String> missingWeapons(Player player, boolean hash) {
		List<String> result = new LinkedList<String>();

		String doneText = player.getQuest("weapons_collector2");
		if (doneText == null) {
			doneText = "";
		}
		List<String> done = Arrays.asList(doneText.split(";"));
		for (String weapon : neededWeapons) {
			if (!done.contains(weapon)) {
				if (hash) {
					weapon = "#" + weapon;
				}
				result.add(weapon);
			}
		}
		return result;
	}

	private void step_1() {
		SpeakerNPC npc = npcs.get("Balduin");

		// player says hi before starting the quest
		npc
				.add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new SpeakerNPC.ChatCondition() {
							@Override
							public boolean fire(Player player, String text,
									SpeakerNPC engine) {
								return player
										.isQuestCompleted("weapons_collector")
										&& !player
												.hasQuest("weapons_collector2");
							}
						},
						ConversationStates.ATTENDING,
						"Greetings, old friend. If you are willing, I have another #quest for you.",
						null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text,
							SpeakerNPC engine) {
						return player.isQuestCompleted("weapons_collector")
								&& !player.hasQuest("weapons_collector2");
					}
				}, ConversationStates.QUEST_2_OFFERED, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						if (!player.isQuestCompleted("weapons_collector2")) {
							engine
									.say("Recent adventurers to these parts describe strange new creatures with weapons I have never seen. Would you fight these creatures and bring their weapons to me?");
						} else {
							engine
									.say("My collection is now complete! Thanks again.");
							engine
									.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		// player is willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						engine
								.say("Wonderful. Now, the #list is small but the risk may be great. If you return safely, I have another reward for you.");
						player.setQuest("weapons_collector2", "");
					}
				});

		// player is not willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED, "no", null,
				ConversationStates.ATTENDING,
				"Well, maybe someone else will happen by and help me.", null);

		// player asks what exactly is missing
		npc.add(
				ConversationStates.ATTENDING,
				"list",
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text,
							SpeakerNPC engine) {
						return player.hasQuest("weapons_collector2")
								&& !player
										.isQuestCompleted("weapons_collector2");
					}
				}, ConversationStates.QUESTION_2, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						List<String> needed = missingWeapons(player, true);
						engine.say("There "
								+ Grammar.isare(needed.size())
								+ " "
								+ Grammar.quantityplnoun(needed.size(),
										"weapon")
								+ " still missing from my newest collection: "
								+ Grammar.enumerateCollection(needed)
								+ ". Do you have anything like that with you?");
					}
				});

		// player says he doesn't have required weapons with him
		npc.add(ConversationStates.QUESTION_2, "no", null,
				ConversationStates.IDLE, null, new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						List<String> missing = missingWeapons(player, false);
						engine.say("Let me know as soon as you find "
								+ Grammar.itthem(missing.size())
								+ ". Farewell.");
					}
				});

		// player says he has a required weapon with him
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "What did you find?", null);

		for (String weapon : neededWeapons) {
			npc.add(ConversationStates.QUESTION_2, weapon, null,
					ConversationStates.QUESTION_2, null,
					new SpeakerNPC.ChatAction() {
						@Override
						public void fire(Player player, String text,
								SpeakerNPC engine) {
							List<String> missing = missingWeapons(player, false);
							if (missing.contains(text)) {
								if (player.drop(text)) {
									// register weapon as done
									String doneText = player
											.getQuest("weapons_collector2");
									player.setQuest("weapons_collector2",
											doneText + ";" + text);
									// check if the player has brought all
									// weapons
									missing = missingWeapons(player, true);
									if (missing.size() > 0) {
										engine
												.say("Thank you very much! Do you have anything more for me?");
									} else {
										Item lhandsword = StendhalRPWorld.get()
												.getRuleManager()
												.getEntityManager().getItem(
														"l_hand_sword");
										lhandsword.put("bound", player
												.getName());
										player.equip(lhandsword, true);
										Item rhandsword = StendhalRPWorld.get()
												.getRuleManager()
												.getEntityManager().getItem(
														"r_hand_sword");
										rhandsword.put("bound", player
												.getName());
										player.equip(rhandsword, true);
										player.addXP(3000);
										engine
												.say("At last, my collection is complete! Thank you very much; here, take this pair of swords in exchange!");
										player.setQuest("weapons_collector2",
												"done");
										player.notifyWorldAboutChanges();
									}
								} else {
									engine
											.say("I may be old, but I'm not senile, and you clearly don't have "
													+ Grammar.a_noun(text)
													+ ". What do you really have for me?");
								}
							} else {
								engine
										.say("I already have that one. Do you have any other weapon for me?");
							}
						}
					});
		}
	}

	private void step_2() {
		// Just find some of the weapons somewhere and bring them to Balduin.
	}

	private void step_3() {
		SpeakerNPC npc = npcs.get("Balduin");

		// player returns while quest is still active
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text,
							SpeakerNPC engine) {
						return player.hasQuest("weapons_collector2")
								&& !player
										.isQuestCompleted("weapons_collector2");
					}
				},
				ConversationStates.ATTENDING,
				"Welcome back. I hope you have come to help me with my latest #list of weapons.",
				null);

		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, String text,
							SpeakerNPC engine) {
						return player.isQuestCompleted("weapons_collector2");
					}
				}, ConversationStates.ATTENDING,
				"Welcome! Thanks again for extending my collection.", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		step_1();
		step_2();
		step_3();
	}
}
