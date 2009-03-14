package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.scroll.RainbowBeansScroll;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AlwaysTrueCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;

/**
 * QUEST: Rainbow Beans
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Pdiddi, a dealer in rainbow beans
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The NPC sells rainbow beans to players above level 30</li>
 * <li>When used, rainbow beans teleport you to a dreamworld full of strange
 * sights, hallucinations and the creatures of your nightmares</li>
 * <li>You can remain there for up to 30 minutes</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>The dream world is really cool!</li>
 * <li>XP from creatures you kill there</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>No more than once every 6 hours</li>
 * </ul>
 *
 * NOTES:
 * <ul>
 * <li>The area of the dreamworld will be a no teleport zone</li>
 * <li>You can exit via a portal if you want to exit before the 30 minutes is
 * up</li>
 * </ul>
 */
public class RainbowBeans extends AbstractQuest {

	private static final int REQUIRED_LEVEL = 30;

	private static final int REQUIRED_MONEY = 2000;

	private static final int REQUIRED_MINUTES = 6 * 60;

	private static final String QUEST_SLOT = "rainbow_beans";

	/** The RainbowBeansScroll for porting back. */
	private static RainbowBeansScroll scroll;

	@Override
	public void init(final String name) {
		super.init(name);
		if (scroll == null) {
			scroll = (RainbowBeansScroll) SingletonRepository.getEntityManager().getItem("rainbow beans");
		}

		/* login notifier to teleport away players logging into the dream world.
		 * there is a note in TimedTeleportScroll that it should be done there or its subclass.
		 */
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			public void onLoggedIn(final Player player) {
				scroll.teleportBack(player);
			}

		});
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Pdiddi");

		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.INFORMATION_1,
			"SHHH! Don't want all n' sundry knowin' wot I #deal in.", null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new ChatCondition() {
				public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.hasQuest(QUEST_SLOT)) {
						final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
						final long delayInMilliSeconds = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
						final long timeRemaining = (Long.parseLong(tokens[1]) + delayInMilliSeconds)
							- System.currentTimeMillis();
						return (timeRemaining <= 0L);
					} else {
						return false;
					}
				}
			}, ConversationStates.QUEST_OFFERED,
			"Oi, you. Back for more rainbow beans?", null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new ChatCondition() {
				public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.hasQuest(QUEST_SLOT)) {
						final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
						final long delayInMilliSeconds = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
						final long timeRemaining = (Long.parseLong(tokens[1]) + delayInMilliSeconds)
							- System.currentTimeMillis();
						return (timeRemaining > 0L);
					} else {
						return false;
					}
				}
			}, ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final long delayInMilliSeconds = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delayInMilliSeconds)
							- System.currentTimeMillis();
					npc.say("Alright? I hope you don't want more beans. You can't take more of that stuff for at least another "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
					return;
				}
			});

		// player responds to word 'deal'
		npc.add(ConversationStates.INFORMATION_1, "deal",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.getLevel() >= REQUIRED_LEVEL) {
						npc.say("Nosy, aint yer? I deal in rainbow beans. You take some, and who knows where the trip will take yer. It'll cost you "
								+ REQUIRED_MONEY
								+ " money. You want to buy some?");
					} else {
						npc.say("It's not stuff you're ready for, pal. Now get out of 'ere! An don't you come back till you've got more hairs on that chest!");
						npc.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		// player wants to take the beans
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.isEquipped("money", REQUIRED_MONEY)) {
						player.drop("money", REQUIRED_MONEY);
						npc.say("Alright, here's the beans. Once you take them, you come down in about 30 minutes. And if you get nervous up there, hit one of the green panic squares to take you back here.");
						if (player.hasQuest(QUEST_SLOT)) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							if (tokens.length == 4) {
								// we stored an old time taken or set it to -1 (never taken), either way, remember this.
								player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;" + tokens[3]);
							} else {
								// it must have started with "done" and they haven't taken beans since
								player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;-1");
								
							}
						} else {
							// first time they bought beans here
							player.setQuest(QUEST_SLOT, "bought;"
											+ System.currentTimeMillis() + ";taken;-1");
								
						}
						final Item rainbowBeans = SingletonRepository.getEntityManager().getItem(
																							 "rainbow beans");
						rainbowBeans.setBoundTo(player.getName());
						player.equipOrPutOnGround(rainbowBeans);
					} else {
						npc.say("Scammer! You don't have the cash.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		// player is not willing to experiment
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Aight, ain't for everyone. Anythin else you want, you say so.",
			null);

		// player says 'deal' or asks about beans when NPC is ATTENDING, not
		// just in information state (like if they said no then changed mind and
		// are trying to get him to deal again)
		// Use AlwaysTrueCondition to override deal as defined in addOffer().
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "beans", "rainbow beans", "yes"),
			new AlwaysTrueCondition(),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.getLevel() >= 30) {
						npc.say("We already talked about this, conversation's moved on now mate, keep up! Try another time.");
					} else {
						npc.say("That stuff's too strong for you. No chance mate!");
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		step_1();

	}
}
