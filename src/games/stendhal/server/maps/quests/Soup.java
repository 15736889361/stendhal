package games.stendhal.server.maps.quests;

import games.stendhal.common.Grammar;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.IRPZone;

/**
 * QUEST: Special Soup
 * 
 * PARTICIPANTS: - Old Mother Helena in Fado tavern
 * 
 * STEPS: - Old Mother Helena tells you the ingredients of a special soup - You
 * collect the ingredients - You bring the ingredients to the tavern - The soup
 * is served at table - Eating the soup heals you fully and adds karma
 * 
 * 
 * REWARD: - heal - Karma bonus of 5 - 100 XP
 * 
 * REPETITIONS: - as many as desired - Only possible to repeat once every ten
 * minutes
 * 
 * @author kymara
 */
public class Soup extends AbstractQuest {

	private static final List<String> NEEDED_FOOD = Arrays.asList("carrot",
			"spinach", "courgette", "collard", "salad", "onion", "cauliflower",
			"broccoli", "leek");

	private static final String QUEST_SLOT = "soup_maker";

	private static final int REQUIRED_MINUTES = 10;

	/**
	 * Returns a list of the names of all food that the given player still has
	 * to bring to fulfil the quest.
	 * 
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of food item names
	 */
	private List<String> missingFood(Player player, boolean hash) {
		List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}
		List<String> done = Arrays.asList(doneText.split(";"));
		for (String ingredient : NEEDED_FOOD) {
			if (!done.contains(ingredient)) {
				if (hash) {
					ingredient = "#" + ingredient;
				}
				result.add(ingredient);
			}
		}
		return result;
	}

	/**
	 * Serves the soup as a reward for the given player.
	 */
	private void placeSoupFor(Player player) {
		Item soup = StendhalRPWorld.get().getRuleManager().getEntityManager()
				.getItem("soup");
		IRPZone zone = StendhalRPWorld.get().getZone("int_fado_tavern");
		// place on table. note: it's not equippable so must be eaten in tavern
		soup.setPosition(17, 23);
		// only allow player who made soup to eat the soup
		soup.setBoundTo(player.getName());
		// here the soup is altered to have the same heal value as the player's
		// base HP.
		soup.put("amount", player.getBaseHP());
		soup.put("regen", player.getBaseHP());
		zone.add(soup);
	}

	private void step_1() {
		SpeakerNPC npc = npcs.get("Old Mother Helena");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.INFORMATION_1,
			"Hello, stranger. You look weary from your travels. I know what would #revive you.",
			null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new SpeakerNPC.ChatCondition() {
				@Override
				public boolean fire(Player player, String text, SpeakerNPC npc) {
					// we don't set quest slot to done so we can't check
					// this
					// return player.isQuestCompleted(QUEST_SLOT);
					boolean questdone = player.hasQuest(QUEST_SLOT)
							&& player.getQuest(QUEST_SLOT).startsWith(
									"done");
					if (!questdone) {
						return false; // we haven't done the quest yet
					}

					String[] tokens = player.getQuest(QUEST_SLOT)
							.split(";");
					long delay = REQUIRED_MINUTES * 60 * 1000; // minutes
																// ->
																// milliseconds
					long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					return (timeRemaining <= 0L);
				}
			}, ConversationStates.QUEST_OFFERED,
			"Hello again. Have you returned for more of my special soup?",
			null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new SpeakerNPC.ChatCondition() {
				@Override
				public boolean fire(Player player, String text,
						SpeakerNPC npc) {
					// we don't set quest slot to done so we can't check
					// this
					// return player.isQuestCompleted(QUEST_SLOT);
					boolean questdone = player.hasQuest(QUEST_SLOT)
							&& player.getQuest(QUEST_SLOT).startsWith(
									"done");
					if (!questdone) {
						return false; // we haven't done the quest yet
					}

					String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					// minutes -> milliseconds
					long delay = REQUIRED_MINUTES * 60 * 1000; 
					long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					return (timeRemaining > 0L);
				}
			}, ConversationStates.ATTENDING, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text, SpeakerNPC npc) {
					String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					// minutes -> milliseconds
					long delay = REQUIRED_MINUTES * 60 * 1000;
					long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					npc.say("I hope you don't want more soup, because I haven't finished washing the dishes. Please check back in "
						+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
						+ " and I will have more time for you.");
					return;
				}
			});

		// player responds to word 'revive'
		npc.add(ConversationStates.INFORMATION_1, "revive",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text, SpeakerNPC npc) {
					if (!(player.hasQuest(QUEST_SLOT) && player.getQuest(
							QUEST_SLOT).startsWith("done"))) {
						npc.say("My special soup has a magic touch. "
								+ "I need you to bring me the #ingredients.");
					} else { // to be honest i don't understand when this
								// would be implemented. i put the text i
								// want down in stage 3 and it works fine.
						npc.say("I have everything for the recipe now.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		// player asks what exactly is missing
		npc.add(ConversationStates.QUEST_OFFERED, "ingredients", null,
			ConversationStates.QUEST_OFFERED, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text, SpeakerNPC npc) {
					List<String> needed = missingFood(player, true);
					npc.say("I need "
							+ Grammar.quantityplnoun(needed.size(),
									"ingredient")
							+ " before I make the soup: "
							+ Grammar.enumerateCollection(needed)
							+ ". Will you collect them?");
				}
			});

		// player is willing to collect
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1, 
			"You made a wise choice. Do you have anything I need already?",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Oh, never mind. It's your loss.", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("spinach", "courgette", "onion", "cauliflower", "broccoli", "leek"),
			null,
			ConversationStates.QUEST_OFFERED,
			"You will find that in allotments in Fado. So will you fetch the ingredients?",
			null);

		// players asks about the vegetables individually
		npc.add(ConversationStates.QUEST_OFFERED, "collard", null,
			ConversationStates.QUEST_OFFERED,
			"That grows indoors in pots. Someone like a witch or an elf might grow it. "
					+ "So will you fetch the ingredients?", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("salad", "carrot"),
			null,
			ConversationStates.QUEST_OFFERED,
			"I usually have to get them imported from Semos. So do you want the soup?",
			null);
	}

	private void step_2() {
		// Fetch the ingredients and bring them back to Helena.
	}

	private void step_3() {
		SpeakerNPC npc = npcs.get("Old Mother Helena");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1,
			"Welcome back! I hope you collected some #ingredients for the soup.",
			null);

		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_1, "ingredients",
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text, SpeakerNPC npc) {
					List<String> needed = missingFood(player, true);
					npc.say("I still need "
							+ Grammar.quantityplnoun(needed.size(),
									"ingredient") + ": "
							+ Grammar.enumerateCollection(needed)
							+ ". Did you bring anything I need?");
				}
			});

		// player says he has a required ingredient with him
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, "What did you bring?", null);

		npc.add(ConversationStates.QUESTION_1, NEEDED_FOOD, null,
			ConversationStates.QUESTION_1, null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(Player player, String text,
						SpeakerNPC npc) {
					if (text != null) {
						text = text.toLowerCase();
					}
					List<String> missing = missingFood(player, false);
					if (missing.contains(text)) {
						if (player.drop(text)) {
							// register ingredient as done
							String doneText = player
									.getQuest(QUEST_SLOT);
							player.setQuest(QUEST_SLOT, doneText + ";"
									+ text);
							// check if the player has brought all Food
							missing = missingFood(player, true);
							if (missing.size() > 0) {
								npc.say("Thank you very much! What else did you bring?");
							} else {
								// had to comment out mana as not sure
								// it's in game - added karma instead
								player.addKarma(5.0);
								// player.addBaseMana(10); // i don't
								// know what number to choose here as i
								// don't know about mana.
								player.addXP(100);
								/*
								 * place soup after XP added otherwise
								 * the XP change MIGHT change level and
								 * player MIGHT gain health points which
								 * changes the base HP, which is desired
								 * to be accurate for the place soup
								 * stage
								 */
								placeSoupFor(player);
								player.healPoison();
								npc.say("The soup's on the table for you. It will heal you. "
										+ "My magical method in making the soup had given you a little karma too.");
								player.setQuest(QUEST_SLOT, "done;"
										+ System.currentTimeMillis());
								player.notifyWorldAboutChanges();
								npc.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Don't take me for a fool, traveller. You don't have "
								+ Grammar.a_noun(text)
								+ " with you.");
						}
					} else {
						npc.say("You brought me that ingredient already.");
					}
				}
			});

		// player says something which isn't in the needed food list.
		npc.add(ConversationStates.QUESTION_1, "",
			new NotCondition(new TriggerInListCondition(NEEDED_FOOD)),
			ConversationStates.QUESTION_1,
			"I won't put that in your soup.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING,
			"I'm not sure what you want from me, then.", null);

		// player says he didn't bring any Food to different question
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING, "Okay then. Come back later.",
			null);

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		step_1();
		step_2();
		step_3();
	}
}
