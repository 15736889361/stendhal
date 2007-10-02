package games.stendhal.server.maps.quests;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.TurnListener;
import games.stendhal.server.events.TurnNotifier;

import java.util.Arrays;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPObject;
import marauroa.common.game.SlotIsFullException;

/**
 * QUEST: Soldiers in Kanmararn
 * 
 * NOTE: It also starts a quest that needs NPC McPegleg that is created. It
 * doesn't harm if that script is missing, just that the IOU cannot be delivered
 * and hence the player can't get cash
 * 
 * PARTICIPANTS: - Henry - Sergeant James - corpse of Tom - corpse of Charles -
 * corpse of Peter
 * 
 * STEPS: - optional: speak to Sergeant James to get the task to find the map -
 * talk to Henry to get the task to find some proof that the other 3 soldiers
 * are dead. - collect the item in each of the corpses of the three other
 * soldiers - bring them back to Henry to get the map - bring the map to
 * Sergeant James
 * 
 * REWARD: - from Henry: - you can keep the IOU paper (for quest MCPeglegIOU) -
 * 2500 XP - from Sergeant James - steel boots
 * 
 * REPETITIONS: - None.
 * 
 * @see McPeglegIOU
 */
public class KanmararnSoldiers extends AbstractQuest {

	private static final Logger logger = Log4J
			.getLogger(KanmararnSoldiers.class);

	private static final String QUEST_SLOT = "soldier_henry";

	/**
	 * The maximum time (in seconds) until plundered corpses will be filled
	 * again, so that other players can do the quest as well.
	 */
	private static final int CORPSE_REFILL_SECONDS = 60;

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	/**
	 * A CorpseRefiller checks, in regular intervals, if the given corpse
	 * 
	 * @author daniel
	 * 
	 */
	private class CorpseRefiller implements TurnListener {
		private Corpse corpse;

		private String itemName;

		private String description;

		public CorpseRefiller(Corpse corpse, String itemName, String description) {
			this.corpse = corpse;
			this.itemName = itemName;
			this.description = description;
		}

		public void start() {
			TurnNotifier.get().notifyInTurns(1, this);
		}

		private boolean equalsExpectedItem(Item item) {
			if (!item.getName().equals(itemName)) {
				return false;
			}

			if (!item.getDescription().equals(description)) {
				return false;
			}

			return corpse.getName().equals(item.getInfoString());
		}

		public void onTurnReached(int currentTurn, String message) {
			boolean isStillFilled = false;
			// Check if the item is still in the corpse. Note that somebody
			// might have put other stuff into the corpse.
			for (RPObject object : corpse.getSlot("content")) {
				if (object instanceof Item) {
					Item item = (Item) object;
					if (equalsExpectedItem(item)) {
						isStillFilled = true;
					}
				}
			}
			try {
				if (!isStillFilled) {
					// recreate the item and fill the corpse
					Item item = StendhalRPWorld.get().getRuleManager()
							.getEntityManager().getItem(itemName);
					item.setInfoString(corpse.getName());
					item.setDescription(description);
					corpse.add(item);
					corpse.notifyWorldAboutChanges();
				}
			} catch (SlotIsFullException e) {
				// ignore, just don't refill the corpse until someone removes
				// the other items from the corpse
				logger.warn("Quest corpse is full: " + corpse.getName());
			}
			// continue the checking cycle
			TurnNotifier.get().notifyInSeconds(CORPSE_REFILL_SECONDS, this);
		}
	}

	private class HenryQuestAction extends SpeakerNPC.ChatAction {
		@Override
		public void fire(Player player, String text, SpeakerNPC npc) {
			if (!player.isQuestCompleted(QUEST_SLOT)
					&& !"map".equals(player.getQuest(QUEST_SLOT))) {
				npc
						.say("Find my #group, Peter, Tom, and Charles, prove it and I will reward you. Will you do it?");
			} else {
				npc.say("I'm so sad that most of my friends are dead.");
				npc.setCurrentState(ConversationStates.ATTENDING);
			}
		}
	}

	private class HenryQuestAcceptAction extends SpeakerNPC.ChatAction {
		@Override
		public void fire(Player player, String text, SpeakerNPC npc) {
			player.setQuest(QUEST_SLOT, "start");
		}
	}

	private class HenryQuestStartedCondition extends SpeakerNPC.ChatCondition {
		@Override
		public boolean fire(Player player, String text, SpeakerNPC npc) {
			return (player.hasQuest(QUEST_SLOT) && player.getQuest(QUEST_SLOT)
					.equals("start"));
		}
	}

	private class HenryQuestNotCompletedCondition extends
			SpeakerNPC.ChatCondition {
		@Override
		public boolean fire(Player player, String text, SpeakerNPC npc) {
			return (!player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT)
					.equals("start"));
		}
	}

	private class HenryQuestCompletedCondition extends SpeakerNPC.ChatCondition {
		@Override
		public boolean fire(Player player, String text, SpeakerNPC npc) {
			return (player.hasQuest(QUEST_SLOT) && !player.getQuest(QUEST_SLOT)
					.equals("start"));
		}
	}

	private class HenryQuestCompleteAction extends SpeakerNPC.ChatAction {
		@Override
		public void fire(Player player, String text, SpeakerNPC npc) {

			List<Item> allLeatherLegs = player.getAllEquipped("leather_legs");
			Item questLeatherLegs = null;
			for (Item leatherLegs : allLeatherLegs) {
				if ("tom".equalsIgnoreCase(leatherLegs.getInfoString())) {
					questLeatherLegs = leatherLegs;
					break;
				}
			}

			List<Item> allNotes = player.getAllEquipped("note");
			Item questNote = null;
			for (Item note : allNotes) {
				if ("charles".equalsIgnoreCase(note.getInfoString())) {
					questNote = note;
					break;
				}
			}

			List<Item> allScaleArmors = player.getAllEquipped("scale_armor");
			Item questScaleArmor = null;
			for (Item scaleArmor : allScaleArmors) {
				if ("peter".equalsIgnoreCase(scaleArmor.getInfoString())) {
					questScaleArmor = scaleArmor;
					break;
				}
			}

			if ((questLeatherLegs != null) && (questNote != null)
					&& (questScaleArmor != null)) {
				npc
						.say("Oh my! Peter, Tom, and Charles are all dead? *cries*. Anyway, here is your reward. And keep the IOU.");
				player.addXP(2500);
				player.drop(questLeatherLegs);
				player.drop(questScaleArmor);
				Item map = StendhalRPWorld.get().getRuleManager()
						.getEntityManager().getItem("map");
				map.setInfoString(npc.getName());
				map
						.setDescription("You see a hand drawn map, but no matter how you look at it, nothing on it looks familiar.");
				player.equip(map);
				player.setQuest(QUEST_SLOT, "map");
				npc.setCurrentState(ConversationStates.ATTENDING);
			} else {
				npc.say("You didn't prove that you have found them all!");
			}
		}
	}

	private class JamesQuestCompleteCondition extends SpeakerNPC.ChatCondition {
		@Override
		public boolean fire(Player player, String text, SpeakerNPC npc) {
			return (player.hasQuest(QUEST_SLOT) && player.getQuest(QUEST_SLOT)
					.equals("map"));
		}
	}

	private class JamesQuestCompletedCondition extends SpeakerNPC.ChatCondition {
		@Override
		public boolean fire(Player player, String text, SpeakerNPC npc) {
			return (player.isQuestCompleted(QUEST_SLOT));
		}
	}

	private class JamesQuestCompleteAction extends SpeakerNPC.ChatAction {
		@Override
		public void fire(Player player, String text, SpeakerNPC npc) {

			List<Item> allMaps = player.getAllEquipped("map");
			Item questMap = null;
			for (Item map : allMaps) {
				if ("henry".equalsIgnoreCase(map.getInfoString())) {
					questMap = map;
					break;
				}
			}
			if (questMap != null) {
				npc
						.say("The map! Wonderful! Thank you. And here is your reward.");
				player.addXP(5000);
				player.drop(questMap);

				Item item = StendhalRPWorld.get().getRuleManager()
						.getEntityManager().getItem("steel_boots");
				item.setBoundTo(player.getName());
				// Is this infostring really needed?
				item.setInfoString(npc.getName());
				player.equip(item);
				player.setQuest(QUEST_SLOT, "done");
				npc.setCurrentState(ConversationStates.ATTENDING);
			} else {
				npc.say("Well, where is the map?");
			}
		}
	}

	/**
	 * We create NPC Henry who will get us on the quest
	 */
	private void prepareCowardSoldier() {
		SpeakerNPC henry = npcs.get("Henry");

		henry.addGreeting("Ssshh! Silence or you will attract more #dwarves.");
		henry.addJob("I'm a soldier in the army.");
		henry.addGoodbye("Bye and be careful with all those dwarves around!");
		henry
				.addHelp("I need help myself. I got seperated from my #group. Now I'm all alone.");
		henry.addReply(Arrays.asList("dwarf", "dwarves"),
				"They are everywhere! Their #kingdom must be close.");
		henry.addReply(Arrays.asList("kingdom", "Kanmararn"),
				"Kanmararn, the legendary city of the #dwarves.");
		henry
				.addReply("group",
						"The General sent five of us to explore this area in search for #treasure.");
		henry.addReply("treasure",
				"A big treasure is rumored to be #somewhere in this dungeon.");
		henry.addReply("somewhere", "If you #help me I might give you a clue.");

		henry.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.QUEST_OFFERED, null, new HenryQuestAction());

		henry.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Thank you! I'll be waiting for your return.",
				new HenryQuestAcceptAction());

		henry
				.add(
						ConversationStates.QUEST_OFFERED,
						"group",
						null,
						ConversationStates.QUEST_OFFERED,
						"The General sent five of us to explore this area in search for #treasure. So, will you help me find them?",
						null);

		henry.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"OK. I understand. I'm scared of the #dwarves myself.", null);

		henry.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new HenryQuestStartedCondition(), ConversationStates.ATTENDING,
				null, new HenryQuestCompleteAction());

		henry.add(ConversationStates.ATTENDING, Arrays.asList("map", "group",
				"help"), new HenryQuestCompletedCondition(),
				ConversationStates.ATTENDING,
				"I'm so sad that most of my friends are dead.", null);

		henry.add(ConversationStates.ATTENDING, Arrays.asList("map"),
				new HenryQuestNotCompletedCondition(),
				ConversationStates.ATTENDING,
				"If you find my friends, i will give you the map", null);
	}

	/**
	 * add corpses of ex-NPCs.
	 */
	private void prepareCorpses() {
		StendhalRPZone zone = StendhalRPWorld.get()
				.getZone("-6_kanmararn_city");

		// Now we create the corpse of the second NPC
		Corpse tom = new Corpse("youngsoldiernpc", 5, 47);
		tom.setDegrading(false);
		tom.setStage(4); // he died first
		tom.setName("Tom");
		// TODO: Use a_noun() in Corpse?
		tom.setKiller("a Dwarven patrol");
		// Add our new Ex-NPC to the game world
		zone.add(tom);

		// Add a refiller to automatically fill the corpse of unlucky Tom
		CorpseRefiller tomRefiller = new CorpseRefiller(tom, "leather_legs",
				"You see torn leather legs that are heavily covered with blood.");
		tomRefiller.start();

		// Now we create the corpse of the third NPC
		Corpse charles = new Corpse("youngsoldiernpc", 94, 5);
		charles.setDegrading(false);
		charles.setStage(3); // he died second
		charles.setName("Charles");
		// TODO: Use a_noun() in Corpse?
		charles.setKiller("a Dwarven patrol");
		// Add our new Ex-NPC to the game world
		zone.add(charles);
		// Add a refiller to automatically fill the corpse of unlucky Charles
		CorpseRefiller charlesRefiller = new CorpseRefiller(charles, "note",
				"You read: \"IOU 250 money. (signed) McPegleg\"");
		charlesRefiller.start();

		// Now we create the corpse of the fourth NPC
		Corpse peter = new Corpse("youngsoldiernpc", 11, 63);
		peter.setDegrading(false);
		peter.setStage(2); // he died recently
		peter.setName("Peter");
		// TODO: Use a_noun() in Corpse?
		peter.setKiller("a Dwarven patrol");
		// Add our new Ex-NPC to the game world
		zone.add(peter);
		// Add a refiller to automatically fill the corpse of unlucky Peter
		CorpseRefiller peterRefiller = new CorpseRefiller(
				peter,
				"scale_armor",
				"You see a slightly rusty scale armor. It is heavily deformed by several strong hammer blows.");
		peterRefiller.start();
	}

	/**
	 * add James
	 */
	private void prepareSergeant() {
		SpeakerNPC james = npcs.get("Sergeant James");

		// quest related stuff
		james
				.addHelp("Think I need a little help myself. My #group got killed and #one of my men ran away. Too bad he had the #map.");
		james
				.addQuest("Find my fugitive soldier and bring him to me ... or at least the #map he's carrying.");
		james
				.addReply("group",
						"We were five, three of us died. You probably passed their corpses.");
		james.addReply(Arrays.asList("one", "henry"),
				"Yes, my youngest soldier. He ran away.");
		james
				.addReply("map",
						"The #treasure map that leads into the heart of the #dwarven #kingdom.");
		james.addReply("treasure",
				"A big treasure is rumored to be somewhere in this dungeon.");
		james.addReply(Arrays.asList("dwarf", "dwarves", "dwarven"),
				"They are strong enemies! We're in their #kingdom.");
		james.addReply(Arrays.asList("peter", "tom", "charles"),
				"He was a good soldier and fought bravely.");
		james.addReply(Arrays.asList("kingdom", "kanmararn"),
				"Kanmararn, the legendary kingdom of the #dwarves.");

		james.add(ConversationStates.ATTENDING, Arrays.asList("map", "henry"),
				new JamesQuestCompleteCondition(),
				ConversationStates.ATTENDING, null,
				new JamesQuestCompleteAction());

		james.add(ConversationStates.ATTENDING, Arrays.asList("map", "henry",
				"quest", "task", "help", "group", "one"),
				new JamesQuestCompletedCondition(),
				ConversationStates.ATTENDING,
				"Thanks again for bringing me the map!", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		prepareCowardSoldier();
		prepareCorpses();
		prepareSergeant();
	}
}
