package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.util.TimeUtil;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * QUEST: Fisherman's license Quiz
 * PARTICIPANTS:
 * - Santiago the fisherman
 *  
 *
 * STEPS:
 * - The fisherman puts all fish species onto the table and the player must 
 * 	 identify the names of the fish in the correct order.
 * - The player has one try per day. 
 *
 * REWARD:
 * - 500 XP
 * - The 2nd part of the exam will be unlocked.  
 *
 * REPETITIONS:
 * - If the player has failed the quiz, he can retry after 24 hours.
 * - After passing the quiz, no more repetitions are possible.
 * 
 * @author dine
 */

public class FishermansLicenseQuiz extends AbstractQuest {
	static final String QUEST_SLOT = "fishermans_license1";
	
	private List<String> speciesList = Arrays.asList(
		"trout", 
		"perch", 
		"mackerel", 
		"cod", 
		"roach",
		"char",
		"clownfish",
		"surgeonfish"
	);
	
	private int currentSpeciesNo;

	private static StendhalRPZone zone = (StendhalRPZone) StendhalRPWorld.get()
			.getRPZone("int_ados_fishermans_hut_west");
	
	private Item fishOnTable;

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
		res.add("FISHERMANS_LICENSE1");
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add("DONE");
		}
		return res;
	}
	
	public void cleanUpTable() {
		if (fishOnTable != null) {
			zone.remove(fishOnTable);
			fishOnTable = null;
		}
	}

	private void startQuiz() {
		Collections.shuffle(speciesList);
		currentSpeciesNo = -1;
		
		putNextFishOnTable();
	}
	
	private String getCurrentSpecies() {
		return speciesList.get(currentSpeciesNo);
	}
	
	private void putNextFishOnTable() {
		currentSpeciesNo++;
		cleanUpTable();
		fishOnTable = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem(getCurrentSpecies());
		fishOnTable.setDescription("You see a fish.");

		zone.assignRPObjectID(fishOnTable);
		zone.add(fishOnTable);
		fishOnTable.set(7, 4);
	}
	
	private long remainingTimeToWait(Player player) {
		if (! player.hasQuest(QUEST_SLOT)) {
			// The player has never tried the quiz before.
			return 0L;
		}
		long timeLastFailed = Long.parseLong(player.getQuest(QUEST_SLOT));
		long delay = 60 * 60 * 24 * 1000; // Miliseconds in a day
		long timeRemaining = timeLastFailed + delay - System.currentTimeMillis();

		return timeRemaining;
	}

	private void createQuizStep() {
		SpeakerNPC fisherman = npcs.get("Santiago");
		
		fisherman.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						if (player.isQuestCompleted(FishermansLicenseCollector.QUEST_SLOT)) {
							npc.say("I don't have a task for you, and you already have a fisherman's license.");
						} else {
							npc.say("I don't need anything from you, but if you like, you can do an #exam to get a fisherman's license.");
						}
					}
				});

		fisherman.add(ConversationStates.ATTENDING,
				"exam",
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						if (player.isQuestCompleted(FishermansLicenseCollector.QUEST_SLOT)) {
							npc.say("You have already got your fisherman's license.");
						} else if (player.isQuestCompleted(QUEST_SLOT)) {
							npc.say("Are you ready for the second part of your exam?");
							npc.setCurrentState(ConversationStates.QUEST_2_OFFERED);
						} else {
							long timeRemaining = remainingTimeToWait(player);
							if (timeRemaining > 0L) {
								npc.say("You can only do the quiz once a day. Come back in " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
							} else {
								npc.say("Are you ready for the first part of your exam?");
								npc.setCurrentState(ConversationStates.QUEST_OFFERED);
						    }
						}
					}
				});

		fisherman.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Come back when you're ready.",
				null);

		fisherman.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_1,
				"Fine. The first question is: What kind of fish is this?",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						startQuiz();
					}
				});

		fisherman.add(ConversationStates.QUESTION_1,
				"",
				null,
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC npc) {
						if (text.equals(getCurrentSpecies())) {
							if (currentSpeciesNo == speciesList.size() - 1) {
								npc.say("Correct! Congratulations, you have passed the first part of the #exam.");
								cleanUpTable();
								player.setQuest(QUEST_SLOT, "done");
								player.addXP(500);
							} else {
								npc.say("Correct! So, what kind of fish is this?");
								putNextFishOnTable();
								npc.setCurrentState(ConversationStates.QUESTION_1);
							}
						} else {
							npc.say("No, that's wrong. Unfortunatelly you have failed, but you can try again tomorrow.");
							cleanUpTable();
							// remember the current time, as you can't do the
							// quiz twice a day.
							player.setQuest(QUEST_SLOT, "" + System.currentTimeMillis());
						}
					}
				});
	}

	
	public void addToWorld() {
		super.addToWorld();
		createQuizStep();
	}
}