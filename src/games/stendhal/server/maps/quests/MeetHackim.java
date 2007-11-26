package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Speak with Hackim
 *
 * PARTICIPANTS: - Hackim Easso, the blacksmith's assistant
 *
 * STEPS: - Talk to Hackim to activate the quest and keep speaking with Hackim.
 *
 * REWARD: - 10 XP - 5 gold coins
 *
 * REPETITIONS: - As much as wanted, but you only get the reward once.
 */
public class MeetHackim extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_hackim";

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
		if (isCompleted(player)) {
			res.add("DONE");
		}
		return res;
	}

	private void prepareHackim() {

		SpeakerNPC npc = npcs.get("Hackim Easso");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"We aren't allowed to sell weapons to adventurers nowadays; we're working flat-out to produce equipment for the glorious Imperial Deniran Army as they fight against Blordrough's dark legions in the south. (Sssh... can you come here so I can whisper?)",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_2,
			"*whisper* Go to the tavern and talk to a man called #Xin #Blanca... he buys and sells equipment that might interest you. Do you want to hear more?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"Ask him what he has to #offer, and look at what he will let you #buy and #sell. For instance, if you had a studded shield which you didn't want, you could #sell #studded_shield.",
			null);

		String answer = "Guessed who supplies Xin Blanca with the weapons he sells? Well, it's me! I have to avoid raising suspicion, though, so I can only smuggle him small weapons. If you want something more powerful, you'll have to venture into the dungeons and kill some of the creatures there for items.\n";

		List<SpeakerNPC.ChatAction> reward = new LinkedList<SpeakerNPC.ChatAction>();
		reward.add(new EquipItemAction("money", 5));
		reward.add(new IncreaseXPAction(10));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		
		npc.add(ConversationStates.INFORMATION_3,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE, 
				answer + "If anybody asks, you don't know me!",
				new MultipleActions(reward));

		npc.add(ConversationStates.INFORMATION_3,
				ConversationPhrases.YES_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE, 
				answer + "Where did you get those weapons? A toy shop?",
				null);

		npc.add(
			new int[] { ConversationStates.ATTENDING,
					ConversationStates.INFORMATION_1,
					ConversationStates.INFORMATION_2,
					ConversationStates.INFORMATION_3 },
			"no",
			null,
			ConversationStates.ATTENDING,
			"Remember, all the weapons are counted; best to leave them alone.",
			null);

	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		prepareHackim();
	}
}
