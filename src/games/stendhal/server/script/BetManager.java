package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.core.scripting.ScriptingNPC;
import games.stendhal.server.core.scripting.ScriptingSandbox;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Expression;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Creates an NPC which manages bets.
 * 
 * <p>
 * A game master has to tell him on what the players can bet:
 * 
 * <pre>
 * /script BetManager.class accept fire water earth
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Then players can bet by saying something like
 * 
 * <pre>
 * bet 50 ham on fire
 * bet 5 cheese on water
 * </pre>
 * 
 * The NPC retrieves the items from the player and registers the bet.
 * </p>
 * 
 * <p>
 * The game master starts the action closing the betting time:
 * 
 * <pre>
 * /script BetManager.class action
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * After the game the game master has to tell the NPC who won:
 * </p>
 * 
 * <pre>
 * /script BetManager.class winner fire
 * </pre>.
 * 
 * <p>
 * The NPC will than tell all players the results and give it to winners:
 * 
 * <pre>
 * mort bet 50 ham on fire and won an additional 50 ham
 * hendrik lost 5 cheese betting on water
 * </pre>
 * 
 * </p>
 * 
 * Note: Betting is possible in "idle conversation state" to enable interaction
 * of a large number of players in a short time. (The last time i did a
 * show-fight i was losing count because there where more than 15 players)
 * 
 * @author hendrik
 */
public class BetManager extends ScriptImpl implements TurnListener {

	private static final int WAIT_TIME_BETWEEN_WINNER_ANNOUNCEMENTS = 10 * 3;

	private static Logger logger = Logger.getLogger(BetManager.class);

	/** The NPC. */
	protected ScriptingNPC npc;

	/** Holds the current state. */
	protected State state = State.IDLE;

	/** List of bets. */
	protected List<BetInfo> betInfos = new LinkedList<BetInfo>();

	/** Possible targets. */
	protected List<String> targets = new ArrayList<String>();

	/** Winner (in state State.PAYING_BETS). */
	protected String winner;

	/**
	 * Stores information about a bet.
	 */
	protected static class BetInfo {

		// use player name instead of player object
		// because player may reconnect during the show
		/** name of player. */
		private String playerName;

		/** target of bet. */
		private String target;

		/** name of item .*/
		private String itemName;

		
		private int amount;

		/**
		 * Converts the bet into a string.
		 * 
		 * @return String
		 */
		public String betToString() {
			StringBuilder sb = new StringBuilder();
			sb.append(amount);
			sb.append(" ");
			sb.append(itemName);
			sb.append(" on ");
			sb.append(target);
			return sb.toString();
		}

		@Override
		public String toString() {
			return playerName + " betted " + betToString();
		}
	}

	/**
	 * Current state.
	 */
	private enum State {
		/** I do nothing. */
		IDLE,
		/** I accept bets. */
		ACCEPTING_BETS,
		/** Bets are not accepted anymore; enjoy the show. */
		ACTION,
		/** Now we have a look at the result. */
		PAYING_BETS
	}

	/**
	 * Do we accept bets at the moment?
	 */
	protected class BetCondition extends SpeakerNPC.ChatCondition {

		@Override
		public boolean fire(Player player, Sentence sentence, SpeakerNPC engine) {
			return state == State.ACCEPTING_BETS;
		}
	}

	/**
	 * Do we NOT accept bets at the moment?
	 */
	protected class NoBetCondition extends SpeakerNPC.ChatCondition {

		@Override
		public boolean fire(Player player, Sentence sentence, SpeakerNPC engine) {
			return state != State.ACCEPTING_BETS;
		}
	}

	/**
	 * handles a bet.
	 */
	protected class BetAction extends SpeakerNPC.ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
			BetInfo betInfo = new BetInfo();
			betInfo.playerName = player.getName();

			// parse the command
			String errorMsg = null;

			if (sentence.hasError()) {
				errorMsg = sentence.getErrorString();
			} else {
				Expression object1 = sentence.getObject(0);
				Expression preposition = sentence.getPreposition(0);
				Expression object2 = sentence.getObject(1);

				if (object1 != null && object2 != null && preposition != null) {
    				if (preposition.equals("on")) {
        				betInfo.amount = object1.getAmount();
        				betInfo.itemName = object1.getNormalized(); // cheese
        				betInfo.target = object2.getNormalized();
    				} else {
    					errorMsg = "missing preposition 'on'";
    				}
    			} else {
    				errorMsg = "missing bet parameters";
    			}
			}

			// wrong syntax
			if (errorMsg != null) {
				engine.say("Sorry " + player.getTitle()
						+ ", I did not understand you. " + errorMsg);
				return;
			}

			// check that item is a ConsumableItem
			Item item = SingletonRepository.getEntityManager().getItem(
					betInfo.itemName);
			if (!(item instanceof ConsumableItem)) {
				engine.say("Sorry " + player.getTitle()
						+ ", I only accept food and drinks.");
				return;
			}

			// check target
			if (!targets.contains(betInfo.target)) {
				engine.say("Sorry " + player.getTitle()
						+ ", I only accept bets on " + targets);
				return;
			}

			// drop item
			if (!player.drop(betInfo.itemName, betInfo.amount)) {
				engine.say("Sorry " + player.getTitle() + ", you don't have "
						+ betInfo.amount + " " + betInfo.itemName);
				return;
			}

			// store bet in list and confirm it
			betInfos.add(betInfo);
			engine.say(player.getTitle() + ", your bet "
					+ betInfo.betToString() + " was accepted");

			// TODO: put items on ground
			// TODO: mark items on ground with: playername "betted" amount
			// itemname "on" target.

		}
	}

	public void onTurnReached(int currentTurn) {
		if (state != State.PAYING_BETS) {
			logger.error("onTurnReached invoked but state is not PAYING_BETS: "
					+ state);
			return;
		}

		if (!betInfos.isEmpty()) {
			BetInfo betInfo = betInfos.remove(0);

			Player player = SingletonRepository.getRuleProcessor().getPlayer(
					betInfo.playerName);
			if (player == null) {
				// player logged out
				if (winner.equals(betInfo.target)) {
					npc.say(betInfo.playerName
							+ " would have won but he or she went away.");
				} else {
					npc.say(betInfo.playerName
							+ " went away. But as he or she has lost anyway it makes no differents.");
				}

			} else {

				// create announcement
				StringBuilder sb = new StringBuilder();
				sb.append(betInfo.playerName);
				sb.append(" bet on ");
				sb.append(betInfo.target);
				sb.append(". So ");
				sb.append(betInfo.playerName);
				if (winner.equals(betInfo.target)) {
					sb.append(" gets ");
					sb.append(betInfo.amount);
					sb.append(" ");
					sb.append(betInfo.itemName);
					sb.append(" back and wins an additional ");
				} else {
					sb.append(" lost his ");
				}
				sb.append(betInfo.amount);
				sb.append(" ");
				sb.append(betInfo.itemName);
				sb.append(". ");
				npc.say(sb.toString());

				// return the bet and the win to the player
				if (winner.equals(betInfo.target)) {
					Item item = sandbox.getItem(betInfo.itemName);

					// it should always be a stackable items as we checked for
					// ConsumableItem when accepting the bet. But just in case
					// something is changed in the future, we will check it
					// again:
					if (item instanceof StackableItem) {
						StackableItem stackableItem = (StackableItem) item;
						stackableItem.setQuantity(2 * betInfo.amount); // bet +
						// win
					}
					player.equip(item, true);
				}

			}

			// TODO: remove item from ground, if it has been put there

			if (betInfos.isEmpty()) {
				winner = null;
				targets.clear();
				state = State.IDLE;
			} else {
				SingletonRepository.getTurnNotifier().notifyInTurns(
						WAIT_TIME_BETWEEN_WINNER_ANNOUNCEMENTS, this);
			}
		}
	}

	// ------------------------------------------------------------------------
	// scripting stuff and game master control
	// ------------------------------------------------------------------------

	@Override
	public void load(Player admin, List<String> args, ScriptingSandbox sandbox) {
		super.load(admin, args, sandbox);

		// Do not load on server startup
		if (admin == null) {
			return;
		}

		// create npc
		npc = new ScriptingNPC("Bob the Bookie");
		npc.setEntityClass("naughtyteen2npc");

		// place NPC next to admin
		sandbox.setZone(sandbox.getZone(admin));
		int x = admin.getX() + 1;
		int y = admin.getY();
		npc.setPosition(x, y);
		sandbox.add(npc);

		// Create Dialog
		npc.behave("greet", "Hi, do you want to bet?");
		npc.behave("job", "I am the Bet Dialer");
		npc.behave(
				"help",
				"Say \"bet 5 cheese on fire\" to get an additional 5 pieces of cheese if fire wins. If he loses, you will lose your 5 cheese.");
		npc.addGoodbye();
		npc.add(ConversationStates.IDLE, "bet", new BetCondition(),
				ConversationStates.IDLE, null, new BetAction());
		npc.add(ConversationStates.IDLE, "bet", new NoBetCondition(),
				ConversationStates.IDLE,
				"I am not accepting any bets at the moment.", null);

		// TODO: remove warning
		admin.sendPrivateText("BetManager is not fully coded yet");
	}

	@Override
	public void execute(Player admin, List<String> args) {

		// Help
		List<String> commands = Arrays.asList("accept", "action", "winner");
		if ((args.size() == 0) || (!commands.contains(args.get(0)))) {
			admin.sendPrivateText("Syntax: /script BetManager.class accept #fire #water\n"
					+ "/script BetManager.class action\n"
					+ "/script BetManager.class winner #fire\n");
			return;
		}

		int idx = commands.indexOf(args.get(0));
		switch (idx) {
		case 0: // accept #fire #water

			if (state != State.IDLE) {
				admin.sendPrivateText("accept command is only valid in state IDLE. But i am in "
						+ state + " now.\n");
				return;
			}
			for (int i = 1; i < args.size(); i++) {
				targets.add(args.get(i));
			}
			npc.say("Hi, I am accepting bets on " + targets
					+ ". If you want to bet simply say: \"bet 5 cheese on "
					+ targets.get(0)
					+ "\" to get an additional 5 pieces of cheese if "
					+ targets.get(0)
					+ " wins. If he loses, you will lose your 5 cheese.");
			state = State.ACCEPTING_BETS;
			break;

		case 1: // action

			if (state != State.ACCEPTING_BETS) {
				admin.sendPrivateText("action command is only valid in state ACCEPTING_BETS. But i am in "
						+ state + " now.\n");
				return;
			}
			npc.say("Ok, Let the fun begin! I will not accept bets anymore.");
			state = State.ACTION;
			break;

		case 2: // winner #fire

			if (state != State.ACTION) {
				admin.sendPrivateText("winner command is only valid in state ACTION. But i am in "
						+ state + " now.\n");
				return;
			}
			if (args.size() < 2) {
				admin.sendPrivateText("Usage: /script BetManager.class winner #fire\n");
			}
			winner = args.get(1);
			state = State.PAYING_BETS;
			npc.say("And the winner is ... " + winner + ".");
			SingletonRepository.getTurnNotifier().notifyInTurns(
					WAIT_TIME_BETWEEN_WINNER_ANNOUNCEMENTS, this);
			break;
			default:
				logger.warn("unknown switch case" + idx);
				break;
		}
	}
}
