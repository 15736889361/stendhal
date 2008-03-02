package games.stendhal.server.maps.quests;

import games.stendhal.common.Grammar;
import games.stendhal.common.Level;
import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * QUEST: Weekly Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <ul><li> Hazel, Museum Curator of Kirdneh
 * <li> some items
 * </ul>
 * STEPS:<ul>
 * <li> talk to Museum Curator to get a quest to fetch a rare item
 * <li> bring the item to the Museum Curator
 * <li> if you cannot bring it in 6 weeks she offers you the chance to fetch
 * 
 * another instead </ul>
 * 
 * REWARD:
 * <ul><li> xp
 * <li> between 100 and 600 money
 * <li> can buy kirdneh house if other eligibilities met
 * </ul>
 * REPETITIONS:
 * <ul><li> once a week</ul>
 */
public class WeeklyItemQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "weekly_item";

	private static final long expireDelay = 6L * MathHelper.MILLISECONDS_IN_ONE_WEEK; 

	class WeeklyQuestAction extends SpeakerNPC.ChatAction {

		/**
		 * All items which are hard enough to find but not tooo hard and not in Daily quest. If you want to do
		 * it better, go ahead. *
		 */
		private final List<String> listeditems = Arrays.asList("mega potion", "lucky charm", "ice sword", "fire sword",
				"great sword", "immortal sword", "dark dagger", "assassin dagger", "night dagger", "hell dagger",
				"golden cloak", "shadow cloak", "chaos cloak", "mainio cloak", "obsidian", "diamond", "golden legs",
				"shadow legs", "golden armor", "shadow armor", "golden shield", "shadow shield", "skull staff",
				"steel boots", "golden boots", "shadow boots", "stone boots", "chaos boots", "golden helmet",
				"shadow helmet", "horned golden helmet", "chaos helmet", "golden twoside axe", "drow sword",
				"chaos legs", "chaos sword", "chaos shield", "chaos armor", "green dragon shield", "egg",
				"golden arrow", "power arrow", "mainio legs", "mainio boots", "mainio shield", "mainio armor");

		@Override
		public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
			String questInfo = player.getQuest("weekly_item");
			String questKill = null;
			String questCount = null;
			String questLast = null;
			final long delay = MathHelper.MILLISECONDS_IN_ONE_WEEK; 

			if (questInfo != null) {
				String[] tokens = (questInfo + ";0;0;0").split(";");
				questKill = tokens[0];
				questLast = tokens[1];
				questCount = tokens[2];
			}
			if ((questKill != null) && !"done".equals(questKill)) {
				String sayText = "You're already on a quest to bring the museum "
						+ Grammar.a_noun(questKill)
						+ ". Please say #complete if you have it with you.";
				if (questLast != null) {
					long timeRemaining = (Long.parseLong(questLast) + expireDelay)
							- System.currentTimeMillis();

					if (timeRemaining < 0) {
						engine.say(sayText
								+ " But, perhaps that is now too rare an item. I can give you #another task, or you can return with what I first asked you.");
						return;
					}
				}
				engine.say(sayText);
				return;
			}

			if (questLast != null) {
				long timeRemaining = (Long.parseLong(questLast) + delay)
						- System.currentTimeMillis();

				if (timeRemaining > 0) {
					engine.say("The museum can only afford to send you to fetch an item once a week. Please check back in "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
					return;
				}
			}
			String itemName = Rand.rand(listeditems);
			engine.say("I want Kirdneh's museum to be the greatest in the land! Please fetch "
					+ Grammar.a_noun(itemName)
					+ " and say #complete, once you've brought it.");
			questLast = "" + (new Date()).getTime();
			player.setQuest("weekly_item", itemName + ";" + questLast + ";"
					+ questCount);
		}
	}

	class WeeklyQuestCompleteAction extends SpeakerNPC.ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
			String questInfo = player.getQuest("weekly_item");
			String questKill = null;
			String questCount = null;
			String questLast = null;

			if (questInfo == null) {
				engine.say("I don't remember giving you any #task yet.");
				return;
			}
			String[] tokens = (questInfo + ";0;0").split(";");
			questKill = tokens[0];
			questLast = tokens[1];
			questCount = tokens[2];
			if (questCount.equals("null")) {
				questCount = "0";
			}
			if ("done".equals(questKill)) {
				engine.say("You already completed the last quest I had given to you.");
				return;
			}
			if (player.drop(questKill)) {
				int start = Level.getXP(player.getLevel());
				int next = Level.getXP(player.getLevel() + 1);
				int reward = 3 * (next - start) / 5;
				if (player.getLevel() >= Level.maxLevel()) {
					reward = 0;
				}
				int goldamount;
				StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 100 * Rand.roll1D6();
				money.setQuantity(goldamount);
				player.equip(money, true);
				engine.say("Wonderful! Here is " + Integer.toString(goldamount) + " money to cover your expenses.");
				player.addXP(reward);
				questCount = "" + (Integer.valueOf(questCount) + 1);
				questLast = "" + (new Date()).getTime();
				player.setQuest("weekly_item", "done" + ";" + questLast + ";"
						+ questCount);
			} else {
				engine.say("You don't seem to have "
						+ Grammar.a_noun(questKill)
						+ " with you. Please get it and say #complete only then.");
			}
		}
	}

	class WeeklyQuestAbortAction extends SpeakerNPC.ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
			String questInfo = player.getQuest("weekly_item");
			String questKill = null;
			String questCount = null;
			String questLast = null;

			if (questInfo != null) {
				String[] tokens = (questInfo + ";0;0;0").split(";");
				questKill = tokens[0];
				questLast = tokens[1];
				questCount = tokens[2];
			}

			if ((questKill != null) && !"done".equals(questKill)) {
				if (questLast != null) {
					long timeRemaining = (Long.parseLong(questLast) + expireDelay)
							- System.currentTimeMillis();

					if (timeRemaining < 0) {
						engine.say("I see. Please, ask me for another #quest when you think you can help Kirdneh museum again.");
						// Don't make the player wait any longer and don't
						// credit the player with a count increase?
						// questCount = "" + (Integer.valueOf(questCount) + 1 );
						// questLast = "" + (new Date()).getTime();
						player.setQuest("weekly_item", "done" + ";" + questLast
								+ ";" + questCount);
						return;
					}
				}
				engine.say("It hasn't been long since you've started your quest, you shouldn't give up so soon.");
				return;
			}
			engine.say("I'm afraid I didn't send you on a #quest yet.");
		}
	}

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	private void step_1() {
		SpeakerNPC npc = npcs.get("Hazel");
		npc.add(ConversationStates.ATTENDING, Arrays.asList("quest", "task", "exhibits"),
				null, ConversationStates.ATTENDING, null,
				new WeeklyQuestAction());
	}

	private void step_2() {
		// get the item
	}

	private void step_3() {
		SpeakerNPC npc = npcs.get("Hazel");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("complete", "done"), null,
				ConversationStates.ATTENDING, null,
				new WeeklyQuestCompleteAction());
	}

	private void step_4() {
		SpeakerNPC npc = npcs.get("Hazel");
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("another", "abort"), null,
				ConversationStates.ATTENDING, null, new WeeklyQuestAbortAction());
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
		step_3();
		step_4();
	}

}
