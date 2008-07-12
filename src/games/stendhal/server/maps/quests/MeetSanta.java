package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Meet Santa anywhere around the World.
 *<p>
 * PARTICIPANTS: <ul><li> Santa Claus</ul>
 *
 * STEPS: <ul><li> Find Santa <li>Say hi <li> Get reward <li> Get hat</ul>
 *
 * REWARD: <ul><li> a stocking which can be opened to obtain a random good reward: food,
 * money, potions, items, etc... </ul>
 *
 * REPETITIONS:None
 */
public class MeetSanta extends AbstractQuest implements LoginListener {
	private static final String QUEST_SLOT = "meet_santa_08";

	/** the Santa NPC. */
	protected SpeakerNPC santa;

	private StendhalRPZone zone;

	@Override
	public void init(final String name) {
		super.init(name, QUEST_SLOT);
	}

	@SuppressWarnings("unused")
	private SpeakerNPC createSanta() {
		santa = new SpeakerNPC("Santa") {
			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new QuestCompletedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"Hi again!",
				    new SpeakerNPC.ChatAction() {
					    @Override
					    public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) { 
						addHat(player);	    
					    }
					}
				    );

				final List<SpeakerNPC.ChatAction> reward = new LinkedList<SpeakerNPC.ChatAction>();
				reward.add(new EquipItemAction("stocking"));
				reward.add(new SetQuestAction(QUEST_SLOT, "done"));
				reward.add(new ChatAction() {
				      	@Override
				        public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					    addHat(player);
					}
				    }
				);
				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new QuestNotCompletedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"Merry Christmas! I have a present and a hat for you.",
					new MultipleActions(reward));

				addJob("I am Santa Claus! Where have you been in these years?");
				addGoodbye("Good bye, and remember to behave if you want a present next year!");
			}
		};
		santa.setEntityClass("santaclausnpc");
		santa.initHP(100);

		// start in int_admin_playground
		zone = SingletonRepository.getRPWorld().getZone("int_admin_playground");
		santa.setPosition(17, 13);
		zone.add(santa);

		return santa;
	}

	private void addHat(final Player player) {
		// fetch old outfit as we want to know the current hair
		final Outfit oldoutfit = player.getOutfit();
		// all santa hat sprites are at 50 + current hair
		if (oldoutfit.getHair() < 50) {
			final int hatnumber = oldoutfit.getHair() + 50;
			// the new outfit only changes the hair, rest is null
			final Outfit newOutfit = new Outfit(hatnumber, null, null, null);
			//put it on, and store old outfit.
			player.setOutfit(newOutfit.putOver(oldoutfit), true);
		}
	}


	public void onLoggedIn(final Player player) {
		// is it Christmas?
		final Outfit outfit = player.getOutfit();
		final int hairnumber = outfit.getHair();
		if ((hairnumber >= 50) && (hairnumber < 90)) {
			final Date now = new Date();
			final GregorianCalendar notXmas = new GregorianCalendar(2008, Calendar.JANUARY, 6);
			final Date dateNotXmas = notXmas.getTime();
			if (now.after(dateNotXmas)) {
				final int newhair = hairnumber - 50;
				final Outfit newOutfit = new Outfit(newhair, null, null, null);
				player.setOutfit(newOutfit.putOver(outfit), false);
			}
		}
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		SingletonRepository.getLoginNotifier().addListener(this);
		/* activate santa here in 2008
		createSanta();
		new TeleporterBehaviour(santa, "Ho, ho, ho! Merry Christmas!", false);
		*/
	}
}
