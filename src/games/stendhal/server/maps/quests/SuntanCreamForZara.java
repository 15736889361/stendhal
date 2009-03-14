package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Suntan Cream for Zara
 * <p>
 * PARTICIPANTS:
 * <li> Zara, a woman at the Athos beach
 * <li> David or Pam, the lifeguards.
 * <p>
 * STEPS:
 * <li> Zara asks you to bring her some suntan cream from the lifeguards.
 * <li> Pam or David want to have some ingredients. After you brought it to them
 * they mix a cream.
 * <li> Zara sees your suntan cream and asks for it and then thanks you.
 * <p>
 * REWARD:
 * <li> 1000 XP
 * <li> The key for a house in Ados where a personal chest with new slots is
 * inside
 * <p>
 * REPETITIONS: - None.
 */
public class SuntanCreamForZara extends AbstractQuest {
	private static final String QUEST_SLOT = "suntan_cream_zara";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("FIRST_CHAT");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("QUEST_REJECTED");
		}
		if (questState.equals("start") ||  questState.equals("done")) {
			res.add("QUEST_ACCEPTED");
		}
		if ((player.isEquipped("suntan cream") && questState.equals("start"))
				|| questState.equals("done")) {
			res.add("FOUND_ITEM");
		}
		if (questState.equals("done")) {
			res.add("DONE");
		}
		return res;
	}

	private void createRequestingStep() {
		final SpeakerNPC zara = npcs.get("Zara");

		zara.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.hasQuest(QUEST_SLOT)) {
						if (player.isQuestCompleted(QUEST_SLOT)) {
							npc.say("I don't have a new task for you. But thank you for the suntan cream. I feel my skin is getting better already!");
							npc.setCurrentState(ConversationStates.ATTENDING);
						} else if ("rejected".equals(player.getQuest(QUEST_SLOT))) {
							npc.say("You refused to help me last time and my skin is getting worse. " 
								+ "Please can you bring me the magic #'suntan cream' that the #lifeguards produce?");
						} else {
							npc.say("Did you forget that you promised me to ask the #lifeguards for #'suntan cream'?");
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("I fell asleep in the sun and now my skin is burnt. Can you bring me the magic #'suntan cream' that the #lifeguards produce?");
					}
				}
			});

		zara.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Thank you very much. I'll be waiting here for your return!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		zara.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Ok, but I would have had a nice reward for you...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		zara.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("suntan cream", "suntan", "cream"),
			null,
			ConversationStates.QUEST_OFFERED,
			"The #lifeguards make a great cream to protect from the sun and to heal sunburns at the same time. Now, will you get it for me?",
			null);

		zara.add(
			ConversationStates.QUEST_OFFERED,
			"lifeguard",
			null,
			ConversationStates.QUEST_OFFERED,
			"The lifeguards are called Pam and David. I think they are in the dressing rooms. So, will you ask them for me?",
			null);

		zara.addReply(
			Arrays.asList("suntan cream", "suntan", "cream"),
			"The #lifeguards make a great cream to protect from the sun and to heal sunburns at the same time.");

		zara.addReply(
			"lifeguard",
			"The lifeguards are called Pam and David. I think they are in the dressing rooms.");

	}

	private void createBringingStep() {
		final SpeakerNPC zara = npcs.get("Zara");

		zara.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.QUEST_ITEM_BROUGHT, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.isEquipped("suntan cream")) {
						npc.say("Great! You got the suntan cream! Is it for me?");
					} else {
						npc.say("I know that the #'suntan cream' is hard to get, but I hope that you didn't forget my painful problem...");
						npc.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		zara.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the
			// helmet away and then saying "yes"
			new PlayerHasItemWithHimCondition("suntan cream"),
			ConversationStates.ATTENDING,
			"Thank you! I feel much better immediately! Here, take this key to my row house in Ados. Feel at home as long as I'm still here!",
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					player.drop("suntan cream");
					final Item zaraKey = SingletonRepository.getEntityManager()
							.getItem("small key");
					zaraKey.setBoundTo(player.getName());
					player.equipOrPutOnGround(zaraKey);
					player.addXP(1000);
					player.addKarma(15);
					player.setQuest(QUEST_SLOT, "done");
					player.notifyWorldAboutChanges();
				}
			});

		zara.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"No? Look at me! I cannot believe that you're so selfish!",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		createRequestingStep();
		createBringingStep();
	}
}
