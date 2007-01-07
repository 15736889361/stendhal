package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: News from Hackim
 * PARTICIPANTS:
 * - Hackim
 * - Xin Blanca
 *
 * STEPS:
 * - Hackim asks you to give a message to Xin Blanca.
 * - Xin Blanca thanks you with a pair of leather_legs.
 *
 * REWARD:
 * - 10 XP
 * - a pair of leather_legs
 *
 * REPETITIONS:
 * - None.
 */
public class NewsFromHackim extends AbstractQuest {
	private static final String QUEST_SLOT = "campfire";

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
			return res;
		}
		res.add("QUEST_ACCEPTED");
		if (isCompleted(player)) {
			res.add("DONE");
		}
		return res;
	}

	
	private void step_1() {
		SpeakerNPC npc = npcs.get("Hackim Easso");

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
							engine.say("Pssst! C'mere... do me a favour and tell #Xin #Blanca that the new supply of weapons is ready, will you?");
						} else {
							engine.say("Thanks, but I don't have any messages to pass on to #Xin. I can't smuggle so often now... I think Xoderos is beginning to suspect something. Anyway, let me know if there's anything else I can do.");
							engine.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				SpeakerNPC.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Thanks! I'm sure that #Xin will reward you generously. Let me know if you need anything else.",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						player.setQuest(QUEST_SLOT, "start");
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				"no",
				null,
				ConversationStates.ATTENDING,
				"Yes, now that I think about it, it probably isn't wise to involve too many people in this... Just forget we spoke, okay? You never heard anything, if you know what I mean.",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						player.setQuest(QUEST_SLOT, "rejected");
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				"Xin",
				null,
				ConversationStates.QUEST_OFFERED,
				"You don't know who Xin is? Everybody at the tavern knows Xin. He's the guy who owes beer money to most of the people in Semos! So, will you do it?",
				null);
	}

	private void step_2() {

		SpeakerNPC npc = npcs.get("Xin Blanca");

		npc.add(ConversationStates.IDLE,
				SpeakerNPC.GREETING_MESSAGES,
				new SpeakerNPC.ChatCondition() {
					@Override
					public boolean fire(Player player, SpeakerNPC engine) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals("start");
					}
				},
				ConversationStates.ATTENDING,
				null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text, SpeakerNPC engine) {
						String answer;
						if (!player.isEquipped("leather_legs")) {
							answer = "Take this set of brand new leather leg armour! Let me know if you want anything else.";
						} else {
							answer = "Take this set of brand new... oh, you already have leather leg armour. Well, maybe you can sell them off or something.";
						}
						//player.say("Well, to make a long story short; I know your business with Hackim and I'm here to tell you that the next shipment is ready.");
						engine.say("Ah, it's ready at last! That is very good news indeed! Here, let me give you a little something for your help... " + answer);
						player.setQuest(QUEST_SLOT, "done");
		
						Item item = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem(
								"leather_legs");
						player.equip(item, true);
						player.addXP(10);
		
						player.notifyWorldAboutChanges();
					}
				});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
	}
  }
