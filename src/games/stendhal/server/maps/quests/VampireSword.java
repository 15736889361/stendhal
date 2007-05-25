package games.stendhal.server.maps.quests;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ProducerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUEST: The Vampire Sword
 * 
 * PARTICIPANTS:
 * <ul>
 *  <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in Orril.</li>
 *  <li>Markovich, a sick vampire who will fill the goblet.</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 *  <li>Hogart tells you the story of the Vampire Lord.</li>
 *  <li>He offers to forge a Vampire Sword for you if you bring him what it needs.</li>
 *  <li>Go to the catacombs, kill 7 vampirettes to get to the 3rd level,
 *      kill 7 killer bats and the vampire lord to get the required
 *		items to fill the goblet.</li>
 *  <li>Fill the goblet and come back.</li>
 *  <li>You get some items from the Catacombs and kill the Vampire Lord.</li>
 *  <li>You get the iron needed in the usual way by collecting iron ore and casting in Semos.</li> 
 *  <li>Hogart forges the Vampire Sword for you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 *  <li>Vampire Sword</li>
 *  <li>5000 XP</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 *  <li>None</li>
 * </ul>
 */
public class VampireSword extends AbstractQuest {

	private static final int REQUIRED_IRON = 10;

	private static final int REQUIRED_MINUTES = 10;

	// TODO: rename this to "vampire_sword" before server reset
	private static final String QUEST_SLOT = "vs_quest";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	private void prepareQuestOfferingStep() {
		SpeakerNPC npc = npcs.get("Hogart");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
		        ConversationStates.QUEST_OFFERED,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        if (!player.hasQuest(QUEST_SLOT)) {
					        npc.say("I can forge a powerful life stealing sword for you. You will need to go to the Catacombs below Semos Graveyard and fight the Vampire Lord. Are you interested?");
				        } else if (player.isQuestCompleted(QUEST_SLOT)) {
					        npc.say("What are you bothering me for now? You've got your sword, go and use it!");
				        } else {
					        npc.say("Why are you bothering me when you haven't completed your quest yet?");
				        }
			        }
		        });

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
		        ConversationStates.ATTENDING,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        npc.say("Then you need this #goblet. Take it to the Semos #Catacombs.");
				        Item emptygoblet = StendhalRPWorld.get().getRuleManager()
				                .getEntityManager().getItem("empty_goblet");
				        player.equip(emptygoblet, true);
				        player.setQuest(QUEST_SLOT, "start");
			        }
		        });
		npc.add(ConversationStates.QUEST_OFFERED,
                ConversationPhrases.NO_MESSAGES,
                null,
                ConversationStates.IDLE,
                "Oh, well forget it then. You must have a better sword than I can forge, huh? Bye.",
                null);

		npc.addReply("catacombs",
		        "The Catacombs of north Semos of the ancient #stories.");

		npc.addReply("goblet",
		        "Go fill it with the blood of the enemies you meet in the #Catacombs.");
	}

	private void prepareGobletFillingStep() {
		
		SpeakerNPC npc = npcs.get("Markovich");

		npc.addGoodbye("*cough* ... farewell ... *cough*");
		npc.addReply(Arrays.asList("blood", "vampirette_entrails", "bat_entrails"),
		        "I need blood. I can take it from the entrails of the alive and undead. I will mix the bloods together for you and #fill your #goblet, if you let me drink some too. But I'm afraid of the powerful #lord.");
		
		npc.addReply(Arrays.asList("lord", "vampire", "skull_ring"),
		        "The Vampire Lord rules these Catacombs! And I'm afraid of him. I can only help you if you kill him and bring me his skull ring with the #goblet.");

		npc.addReply(Arrays.asList("empty_goblet", "goblet"),
		        "Only a powerful talisman like this cauldron or a special goblet should contain blood.");

		Map<String, Integer> requiredResources = new HashMap<String, Integer>();
		requiredResources.put("vampirette_entrails", 7);
		requiredResources.put("bat_entrails", 7);
		requiredResources.put("skull_ring", 1);
		requiredResources.put("empty_goblet", 1);
		ProducerBehaviour behaviour = new ProducerBehaviour("sicky_fill_goblet",
				"fill", "goblet", requiredResources, 5 * 60, true);
		npc.addProducer(behaviour,
		        "Please don't try to kill me...I'm just a sick old #vampire. Do you have any #blood I could drink? If you have an #empty_goblet I will #fill it with blood for you in my cauldron.");

	}

	private void prepareForgingStep() {

		SpeakerNPC npc = npcs.get("Hogart");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
		        new SpeakerNPC.ChatCondition() {
			        @Override
			        public boolean fire(Player player, String text, SpeakerNPC npc) {
				        return player.hasQuest(QUEST_SLOT)
				                && player.getQuest(QUEST_SLOT).equals("start")
				                && player.isEquipped("goblet");
			        }
		        },
		        ConversationStates.QUEST_ITEM_BROUGHT,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        if (!player.isEquipped("iron", REQUIRED_IRON)) {
					        npc.say("You have battled hard to bring that goblet. I will use it to #forge the vampire sword");
				        } else {
					        player.drop("goblet");
					        player.drop("iron", REQUIRED_IRON);
					        npc.say("You've brought everything I need to make the vampire sword. Come back in "
					        		+ REQUIRED_MINUTES + " minutes and it will be ready");
				        	player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
				        	npc.setCurrentState(ConversationStates.IDLE);
				        }
			        }
		        });

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
		        new SpeakerNPC.ChatCondition() {
			        @Override
			        public boolean fire(Player player, String text, SpeakerNPC npc) {
				        return player.hasQuest(QUEST_SLOT)
				                && player.getQuest(QUEST_SLOT).equals("start")
				                && !player.isEquipped("goblet")
				                && player.isEquipped("empty_goblet");
			        }
		        },
		        ConversationStates.IDLE,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        npc.say("Did you lose your way? The Catacombs are in North Semos. Don't come back without a full goblet! Bye!");
			        }
		        });

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
		        new SpeakerNPC.ChatCondition() {
			        @Override
			        public boolean fire(Player player, String text, SpeakerNPC npc) {
				        return player.hasQuest(QUEST_SLOT)
				                && player.getQuest(QUEST_SLOT).equals("start")
				                && !player.isEquipped("goblet")
				                && !player.isEquipped("empty_goblet");
			        }
		        },
		        ConversationStates.QUESTION_1,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        npc.say("I hope you didn't lose your goblet! Do you need another?");
			        }
		        });

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
		        ConversationStates.IDLE,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        npc.say("You stupid ..... Be more careful next time. Bye!");
				        Item emptygoblet = StendhalRPWorld.get().getRuleManager()
				                .getEntityManager().getItem("empty_goblet");
				        player.equip(emptygoblet, true);
			        }
		        });

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
		        "Then why are you back here? Go slay some vampires! Bye!",
		        null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
		        new SpeakerNPC.ChatCondition() {
			        @Override
			        public boolean fire(Player player, String text, SpeakerNPC npc) {
				        return player.hasQuest(QUEST_SLOT)
				                && player.getQuest(QUEST_SLOT).startsWith("forging;");
			        }
		        },
		        ConversationStates.IDLE,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        String[] tokens = player.getQuest(QUEST_SLOT).split(";");
				        long delay = REQUIRED_MINUTES * 60 * 1000; // minutes -> milliseconds
				        long timeRemaining = (Long.parseLong(tokens[1]) + delay)
				                - System.currentTimeMillis();
				        if (timeRemaining > 0L) {
					        npc.say("I haven't finished forging the sword. Please check back in "
			                        + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
			                        + ".");
					        return;
				        }
				        npc.say("I have finished forging the mighty Vampire Sword. You deserve this. Now i'm going back to work, goodbye!");
				        player.addXP(5000);
				        Item vampireSword = StendhalRPWorld.get().getRuleManager()
				                .getEntityManager().getItem("vampire_sword");
				        vampireSword.put("bound", player.getName());
				        player.equip(vampireSword, true);
				        player.setQuest(QUEST_SLOT, "done");
				        player.notifyWorldAboutChanges();
			        }
		        });

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
                "forge",
                null,
                ConversationStates.QUEST_ITEM_BROUGHT,
                "Bring me " + REQUIRED_IRON + " #iron bars to forge the sword with. Don't forget to bring the goblet too.",
                null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				"iron",
				null,
				ConversationStates.IDLE,
		        "You know, collect the iron ore lying around and get it cast! Bye!",
		        null);

	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		prepareQuestOfferingStep();
		prepareGobletFillingStep();
		prepareForgingStep();
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
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("QUEST_ACCEPTED");
		}
		if ((questState.equals("start") && player.isEquipped("goblet"))
		        || questState.equals("done")) {
			res.add("FOUND_ITEM");
		}
		if (player.getQuest(QUEST_SLOT).startsWith("forging;")) {
			res.add("FORGING");
		}
		if (questState.equals("done")) {
			res.add("DONE");
		}
		return res;
	}
}
