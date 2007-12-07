package games.stendhal.server.entity.npc;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.fsm.PostTransitionAction;
import games.stendhal.server.entity.npc.fsm.PreTransitionCondition;
import games.stendhal.server.entity.npc.fsm.Transition;
import games.stendhal.server.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This is a finite state machine that implements a chat system. See:
 * http://en.wikipedia.org/wiki/Finite_state_machine In fact, it is a
 * transducer. * States are denoted by integers. Some constants are defined in
 * ConversationStates for often-used states. * Input is the text that the player
 * says to the SpeakerNPC. * Output is the text that the SpeakerNPC answers.
 *
 * See examples to understand how it works. RULES: * State 0 (IDLE) is both the
 * start state and the state that will end the conversation between the player
 * and the SpeakerNPC. * State 1 (ATTENDING) is the state where only one player
 * can talk to NPC and where the prior talk doesn't matter. * State -1 (ANY) is
 * a wildcard and is used to jump from any state whenever the trigger is active. *
 * States from 2 to 100 are reserved for special behaviours and quests.
 *
 * Example how it works: First we need to create a message to greet the player
 * and attend it. We add a hi event:
 *
 * add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
 * ConversationStates.ATTENDING, "Welcome, player!", null)
 *
 * Once the NPC is in the IDLE state and hears the word "hi", it will say
 * "Welcome player!" and move to ATTENDING.
 *
 * Now let's add some options when player is in ATTENDING_STATE, like job,
 * offer, buy, sell, etc.
 *
 * add(ConversationStates.ATTENDING, ConversationPhrases.JOB_MESSAGES,
 * ConversationStates.ATTENDING, "I work as a part time example showman", null)
 *
 * add(ConversationStates.ATTENDING_STATE, "offer",
 * ConversationStates.ATTENDING_STATE, "I sell best quality swords", null)
 *
 * Ok, two new events: job and offer, they go from ATTENDING state to ATTENDING
 * state, because after reacting to "job" or "offer", the NPC can directly react
 * to one of these again.
 *
 * add(ConversationStates.ATTENDING, "buy",
 * ConversationStates.BUY_PRICE_OFFERED, null, new ChatAction() { public void
 * fire(Player player, String text, SpeakerNPC npc) { int i=text.indexOf(" ");
 * String item=text.substring(i+1); if(item.equals("sword")) { npc.say(item+"
 * costs 10 coins. Do you want to buy?"); } else { npc.say("Sorry, I don't sell " +
 * item); npc.setActualState(ConversationStates.ATTENDING); } } });
 *
 * Now the hard part. We listen to "buy", so we need to process the text, and
 * for that we use the ChatAction class, we create a new class that will handle
 * the event. Also see that we move to a new state, BUY_PRICE_OFFERED (20). The
 * player is then replying to a question, so we only expect two possible
 * replies: yes or no.
 *
 * add(ConversationStates.BUY_PRICE_OFFERED, ConversationPhrases.YES_MESSAGES,
 * ConversationStates.ATTENDING, "Sorry, I changed my mind. I won't sell
 * anything.", null); // See SellerBehaviour.java for a working example.
 *
 * Whatever the reply is, return to ATTENDING state so we can listen to new
 * things.
 *
 * Finally we want to finish the conversation, so whatever state we are, we want
 * to finish a conversation with "Bye!".
 *
 * add(ConversationStates.ANY, ConversationPhrases.GOODBYE_MESSAGES,
 * ConversationStates.IDLE, "Bye!", null);
 *
 * We use the state ANY (-1) as a wildcard, so if the input text is "bye" the
 * transition happens, no matter in which state the FSM really is, with the
 * exception of the IDLE state.
 */
public class SpeakerNPC extends NPC {
	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(SpeakerNPC.class);

	private Engine engine = new Engine(this);

	/**
	 * Determines how long a conversation can be paused before it will
	 * terminated by the NPC. Defaults to 30 seconds at 300 ms / turn.
	 */
	private long playerChatTimeout = 90;

	// Default wait message when NPC is busy
	private String waitMessage;

	// Default wait action when NPC is busy
	private ChatAction waitAction;

	// Default bye message when NPC stops chatting with the player
	private String goodbyeMessage;

	private ChatCondition initChatCondition;

	// Default initChat action when NPC stop chatting with the player
	private ChatAction initChatAction;

	/**
	 * Stores which turn was the last one at which a player spoke to this NPC.
	 * This is important to determine conversation timeout.
	 */
	private long lastMessageTurn;

	/**
	 * The player who is currently talking to the NPC, or null if the NPC is
	 * currently not taking part in a conversation.
	 */
	private Player attending;

	/**
	 * Creates a new SpeakerNPC.
	 *
	 * @param name
	 *            The NPC's name. Please note that names should be unique.
	 */
	public SpeakerNPC(String name) {
		baseSpeed = 0.2;
		createPath();

		lastMessageTurn = 0;

		setName(name);
		createDialog();
		put("title_type", "npc");

		setSize(1, 1);
	}

	protected void createPath() {
		// sub classes can implement this method
	}

	protected void createDialog() {
		// sub classes can implement this method
	}

	/**
	 * Is called when the NPC stops chatting with a player. Override it if
	 * needed.
	 */
	protected void onGoodbye(Player player) {
		// do nothing
	}


	/**
	 * Gets all players that have recently (this turn?) talked and are standing
	 * nearby the NPC. Nearby means that they are standing less than <i>range</i>
	 * squares away horizontally and less than <i>range</i> squares away
	 * vertically.
	 *
	 * Why is range a double, not an int? Maybe someone wanted to implement a
	 * circle instead of the rectangle we're having now. -- mort
	 * (DHerding@gmx.de)
	 *
	 * @param npc
	 * @param range
	 * @return A list of nearby players who have recently talked.
	 */
	private List<Player> getNearbyPlayersThatHaveSpoken(NPC npc, double range) {
		int x = npc.getX();
		int y = npc.getY();

		List<Player> players = new LinkedList<Player>();

		for (Player player : getZone().getPlayers()) {
			int px = player.getX();
			int py = player.getY();

			if (player.has("text") && (Math.abs(px - x) < range)
					&& (Math.abs(py - y) < range)) {
				players.add(player);
			}
		}
		return players;
	}

	/**
	 * Gets the player who is standing nearest to the NPC. Returns null if no
	 * player is standing nearby. Nearby means that they are standing less than
	 * <i>range</i> squares away horizontally and less than <i>range</i>
	 * squares away vertically. Note, however, that the Euclidian distance is
	 * used to compare which player is standing closest.
	 *
	 * @param range
	 * @return The nearest player, or null if no player is standing on the same
	 *         map.
	 */
	private Player getNearestPlayer(double range) {
		int x = getX();
		int y = getY();

		Player nearest = null;

		int squaredDistanceOfNearestPlayer = Integer.MAX_VALUE;

		for (Player player : getZone().getPlayers()) {
			int px = player.getX();
			int py = player.getY();

			if ((Math.abs(px - x) < range) && (Math.abs(py - y) < range)) {
				int squaredDistanceOfThisPlayer = (px - x) * (px - x)
						+ (py - y) * (py - y);
				if (squaredDistanceOfThisPlayer < squaredDistanceOfNearestPlayer) {
					squaredDistanceOfNearestPlayer = squaredDistanceOfThisPlayer;
					nearest = player;
				}
			}
		}
		return nearest;
	}

	/**
	 * The player who is currently talking to the NPC, or null if the NPC is
	 * currently not taking part in a conversation.
	 *
	 * @return Player
	 */
	public Player getAttending() {
		return attending;
	}

	/**
	 * Sets the player to whom the NPC is currently listening. Note: You don't
	 * need to use this for most NPCs.
	 *
	 * @param player
	 *            the player with whom the NPC should be talking.
	 */
	public void setAttending(Player player) {
		attending = player;

		if (player != null) {
			stop();
		} else {
			if (hasPath()) {
				setSpeed(getBaseSpeed());
			}
		}
	}

	@Override
	public void onDead(Entity who) {
		heal();
		notifyWorldAboutChanges();
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {
		// They can't die
	}

	/**
	 * Sets the time a conversation can be paused before it will be terminated
	 * by the NPC.
	 *
	 * @param playerChatTimeout
	 *            the time, in turns
	 */
	public void setPlayerChatTimeout(long playerChatTimeout) {
		this.playerChatTimeout = playerChatTimeout;
	}

	@Override
	public void logic() {
		if (has("text")) {
			remove("text");
		}

		// if no player is talking to the NPC, the NPC can move around.
		if (!isTalking()) {
			// TODO: Reset this on FSM engine state change
			if (getAttending() != null) {
				setAttending(null);
			}
			if (hasPath()) {
				setSpeed(baseSpeed);
			}
			applyMovement();
		} else if (attending != null) {
			// If the player is too far away
			if ((attending.squaredDistance(this) > 8 * 8)
			// or if the player fell asleep ;)
					|| (StendhalRPRuleProcessor.get().getTurn()
							- lastMessageTurn > playerChatTimeout)) {
				// we force him to say bye to NPC :)
				if (goodbyeMessage != null) {
					say(goodbyeMessage);
				}
				onGoodbye(attending);
				engine.setCurrentState(ConversationStates.IDLE);
				setAttending(null);
			}
		}

		// now look for nearest player only if there's an initChatAction
		if (!isTalking() && (initChatAction != null)) {
			Player nearest = getNearestPlayer(7);
			if (nearest != null) {
				if (initChatCondition == null ||
					initChatCondition.fire(nearest, null, this)) {	// Note: The sentence parameter is left as null, so be carefull not to use it in the fire() handler.
					initChatAction.fire(nearest, null, this);
				}
			}
		}

		// and finally react on anybody talking to us
		List<Player> speakers = getNearbyPlayersThatHaveSpoken(this, 5);
		for (Player speaker : speakers) {
			tell(speaker, speaker.get("text"));
		}

		notifyWorldAboutChanges();
	}

	public boolean isTalking() {
		return engine.getCurrentState() != ConversationStates.IDLE;
	}

	public abstract static class ChatAction implements PostTransitionAction {
		public abstract void fire(Player player, Sentence sentence, SpeakerNPC npc);
	}

	public abstract static class ChatCondition implements PreTransitionCondition {
		public abstract boolean fire(Player player, Sentence sentence, SpeakerNPC npc);
	}

	@Override
	// you can override this if you don't want your NPC to turn around
	// in certain situations.
	public void say(String text) {
		// turn towards player if necessary, then say it.
		say(text, true);
	}

	protected void say(String text, boolean turnToPlayer) {
		// be polite and face the player we are talking to
		if (turnToPlayer && (attending != null)) {
			faceToward(attending);
		}
		super.say(text);

	}

	/** Message when NPC is attending another player. */
	public void addWaitMessage(String text, ChatAction action) {
		waitMessage = text;
		waitAction = action;
	}

	/** Message when NPC is attending another player. */
	public void addInitChatMessage(ChatCondition condition, ChatAction action) {
		initChatCondition = condition;
		initChatAction = action;
	}

	/** Add a new transition to FSM */
	public void add(int state, String trigger, ChatCondition condition,
			int nextState, String reply, ChatAction action) {
		engine.add(state, trigger, condition, nextState, reply, action);
	}

	/**
	 * Adds a new set of transitions to the FSM
	 *
	 * @param state
	 *            the starting state of the FSM
	 * @param triggers
	 *            a list of inputs for this transition
	 * @param condition
	 *            null or condition that has to return true for this transition
	 *            to be considered
	 * @param nextState
	 *            the new state of the FSM
	 * @param reply
	 *            a simple text reply (may be null for no reply)
	 * @param action
	 *            a special action to be taken (may be null)
	 */
	public void add(int state, List<String> triggers, ChatCondition condition,
			int nextState, String reply, ChatAction action) {
		engine.add(state, triggers, condition, nextState, reply, action);
	}

	/**
	 * Adds a new set of transitions to the FSM
	 *
	 * @param states
	 *            the starting states of the FSM
	 * @param trigger
	 *            input for this transition
	 * @param condition
	 *            null or condition that has to return true for this transition
	 *            to be considered
	 * @param nextState
	 *            the new state of the FSM
	 * @param reply
	 *            a simple text reply (may be null for no reply)
	 * @param action
	 *            a special action to be taken (may be null)
	 */
	public void add(int[] states, String trigger, ChatCondition condition,
			int nextState, String reply, ChatAction action) {
		for (int state : states) {
			add(state, trigger, condition, nextState, reply, action);
		}
	}

	/**
	 * Adds a new set of transitions to the FSM
	 *
	 * @param states
	 *            the starting states of the FSM
	 * @param triggers
	 *            a list of inputs for this transition
	 * @param condition
	 *            null or condition that has to return true for this transition
	 *            to be considered
	 * @param nextState
	 *            the new state of the FSM
	 * @param reply
	 *            a simple text reply (may be null for no reply)
	 * @param action
	 *            a special action to be taken (may be null)
	 */
	public void add(int[] states, List<String> triggers,
			ChatCondition condition, int nextState, String reply,
			ChatAction action) {
		for (int state : states) {
			add(state, triggers, condition, nextState, reply, action);
		}
	}

	/**
	 *
	 */
	public void add(int state, List<String> triggers, int nextState,
			String reply, ChatAction action) {
		for (String trigger : triggers) {
			add(state, trigger, null, nextState, reply, action);
		}
	}

	public void listenTo(Player player, String text) {
		tell(player, text);
	}

	/**
	 * If the given player says something to this NPC, and the NPC is already
	 * speaking to another player, tells the given player to wait.
	 *
	 * @param player
	 *            The player who spoke to the player
	 * @param text
	 *            The text that the given player has said
	 * @return true iff the NPC had to get rid of the player
	 */
	private boolean getRidOfPlayerIfAlreadySpeaking(Player player, String text) {
		// If we are attending another player make this one wait.
		if (!player.equals(attending)) {
			if (ConversationPhrases.GREETING_MESSAGES.contains(text)) {

				logger.debug("Already attending a player");
				if (waitMessage != null) {
					say(waitMessage);
				}

				if (waitAction != null) {
					Sentence sentence = ConversationParser.parse(text);
					waitAction.fire(player, sentence, this);	// Note: sentence is currently not yet used in the called handler functions.
				}
			}
			return true;
		}
		return false;
	}

	/** This function evolves the FSM */
	private boolean tell(Player player, String text) {
		// If we are not attending a player, attend this one.
		if (engine.getCurrentState() == ConversationStates.IDLE) {
			logger.debug("Attending player " + player.getName());
			setAttending(player);
		}

		if (getRidOfPlayerIfAlreadySpeaking(player, text)) {
			return true;
		}

		lastMessageTurn = StendhalRPRuleProcessor.get().getTurn();

		return engine.step(player, text);
	}

	public void setCurrentState(int state) {
		engine.setCurrentState(state);
	}

	public void addGreeting() {
		addGreeting("Greetings! How may I help you?", null);
	}

	public void addGreeting(String text) {
		addGreeting(text, null);
	}

	public void addGreeting(String text, SpeakerNPC.ChatAction action) {
		add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				ConversationStates.ATTENDING, text, action);

		addWaitMessage(null, new SpeakerNPC.ChatAction() {

			@Override
			public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
				npc.say("Please wait, " + player.getTitle()
						+ "! I am still attending to "
						+ npc.getAttending().getTitle() + ".");
			}
		});
	}

	/**
	 * Makes this NPC say a text when it hears a certain trigger during a
	 * conversation.
	 *
	 * @param trigger
	 *            The text that causes the NPC to answer
	 * @param text
	 *            The answer
	 */
	public void addReply(String trigger, String text) {
		add(ConversationStates.ATTENDING, trigger, null,
				ConversationStates.ATTENDING, text, null);
	}

	/**
	 * @param triggers
	 * @param text
	 */
	public void addReply(List<String> triggers, String text) {
		add(ConversationStates.ATTENDING, triggers,
				ConversationStates.ATTENDING, text, null);
	}

        /** Makes NPC say a text and/or do an action when a trigger is said
        * @param trigger
        * @param text
        * @param action
	*/
    
        public void addReply(String trigger, String text, SpeakerNPC.ChatAction action) {
		add(ConversationStates.ATTENDING, trigger, null,
				ConversationStates.ATTENDING, text, action);

		addWaitMessage(null, new SpeakerNPC.ChatAction() {

			@Override
			public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
				npc.say("Please wait, " + player.getTitle()
						+ "! I am still attending to "
						+ npc.getAttending().getTitle() + ".");
			}
		});
	}



	public void addQuest(String text) {
		add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				ConversationStates.ATTENDING, text, null);
	}

	public void addJob(String jobDescription) {
		addReply(ConversationPhrases.JOB_MESSAGES, jobDescription);
	}

	public void addHelp(String helpDescription) {
		addReply(ConversationPhrases.HELP_MESSAGES, helpDescription);
	}

	public void addOffer(String offerDescription) {
		addReply(ConversationPhrases.OFFER_MESSAGES, offerDescription);
	}


	public void addGoodbye() {
		addGoodbye("Bye.");
	}

	public void addGoodbye(String text) {
		goodbyeMessage = text;
		add(ConversationStates.ANY, ConversationPhrases.GOODBYE_MESSAGES,
				ConversationStates.IDLE, text, new ChatAction() {

					@Override
					public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
						npc.onGoodbye(player);
					}

					@Override
					public String toString() {
						return "SpeakerNPC.onGoodbye";
					}
				});
	}

	/**
	 * Returns a copy of the transition table
	 *
	 * @return list of transitions
	 */
	public List<Transition> getTransitions() {
		return engine.getTransitions();
	}

	public Engine getEngine() {
		return engine;
	}

	@Override
	protected void handleObjectCollision() {
		stop();
	}

	@Override
	protected void handleSimpleCollision(int nx, int ny) {
		stop();
	}

}
