// $Id$
package games.stendhal.server.entity.npc.fsm;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatAction;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatCondition;
import games.stendhal.server.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * a finite state machine.
 */
public class Engine {

	private static final Logger logger = Logger.getLogger(Engine.class);

	// TODO: remove this dependency cycle, this is just here to simplify refactoring
	// TODO: later: remove dependency on games.stendhal.server.entity.npc.* and Player
	private SpeakerNPC speakerNPC;

	private int maxState;

	// FSM state transition table
	private List<Transition> stateTransitionTable = new LinkedList<Transition>();

	// current FSM state
	private int currentState = ConversationStates.IDLE;

	/**
	 * Creates a new FSM.
	 *
	 * @param speakerNPC the speaker npc for which this FSM is created
	 * must not be null
	 */
	public Engine(SpeakerNPC speakerNPC) {
		if (speakerNPC == null) {
			throw new IllegalArgumentException("speakerNpc must not be null");
		}

		this.speakerNPC = speakerNPC;
	}

	private Transition get(int state, String trigger, ChatCondition condition) {
		for (Transition transition : stateTransitionTable) {
			if (transition.matches(state, trigger)) {
				PreTransitionCondition cond = transition.getCondition();

				if (cond == condition)
					return transition;
    			else if (cond!=null && cond.equals(condition))
    				return transition;
			}
		}

		return null;
	}

	/**
	 * Calculates and returns an unused state
	 *
	 * @return unused state
	 */
	public int getFreeState() {
		maxState++;
		return maxState;
	}

	/**
	 * Adds a new transition to FSM
	 *
	 * @param state old state
	 * @param trigger      input trigger
	 * @param condition    additional precondition
	 * @param nextState    state after the transition
	 * @param reply        output
	 * @param action       additional action after the condition
	 */
	public void add(int state, String trigger, ChatCondition condition, int nextState, String reply, ChatAction action) {
		if (state > maxState) {
			maxState = state;
		}

		Transition existing = get(state, trigger, condition);
		if (existing != null) {
			// A previous state, trigger, condition combination exist.
			logger.warn("Adding to " + existing + " the state [" + state + "," + trigger + "," + nextState + "," + condition + "]");
			existing.setReply(existing.getReply() + " " + reply);
		}

		Transition item = new Transition(state, trigger, condition, nextState, reply, action);
		stateTransitionTable.add(item);
	}

	/**
	 * Adds a new set of transitions to the FSM
	 *
	 * @param state the starting state of the FSM
	 * @param triggers a list of inputs for this transition
	 * @param condition null or condition that has to return true for this transition to be considered
	 * @param nextState the new state of the FSM
	 * @param reply a simple text reply (may be null for no reply)
	 * @param action a special action to be taken (may be null)
	 */
	public void add(int state, List<String> triggers, ChatCondition condition, int nextState, String reply,
	        ChatAction action) {
		for (String trigger : triggers) {
			add(state, trigger, condition, nextState, reply, action);
		}
	}

	/**
	 * Gets the current state
	 *
	 * @return current state
	 */
	public int getCurrentState() {
		return currentState;
	}

	/**
	 * Sets the current State without doing a normal transition.
	 *
	 * @param currentState new state
	 */
	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	/**
	 * Do one transition of the finite state machine.
	 *
	 * @param player Player
	 * @param text   input
	 * @return true if a transition was made, false otherwise
	 */
	public boolean step(Player player, String text) {
		if (matchTransition(MatchType.EXACT_MATCH, player, text)) {
			return true;
		} else if (matchTransition(MatchType.SIMILAR_MATCH, player, text)) {
			return true;
		} else if (matchTransition(MatchType.ABSOLUTE_JUMP, player, text)) {
			return true;
		} else if (matchTransition(MatchType.SIMILAR_JUMP, player, text)) {
			return true;
		} else {
			// Couldn't match the text with the current FSM state
			logger.debug("Couldn't match any state: " + getCurrentState() + ":" + text);
			return false;
		}
	}


	/**
	 * Do one transition of the finite state machine with debugging output
	 * and reset of the previous response
	 *
	 * @param player Player
	 * @param text   input
	 * @return true if a transition was made, false otherwise
	 */
	public boolean stepTest(Player player, String text) {
		logger.debug(">>> " + text);
		speakerNPC.remove("text");
		boolean res = step(player, text);
		logger.debug("<<< " + speakerNPC.get("text"));
		return res;
	}

	private boolean matchTransition(MatchType type, Player player, String text) {
		List<Transition> listCondition = new LinkedList<Transition>();
		List<Transition> listConditionLess = new LinkedList<Transition>();
		int i;

		// First we try to match with stateless transitions.
		for (Transition transition : stateTransitionTable) {
			if (matchesTransition(type, text, transition)) {
				if (transition.isConditionFulfilled(player, text, speakerNPC)) {
					if (transition.getCondition() == null) {
						listConditionLess.add(transition);
					} else {
						listCondition.add(transition);
					}
				}
			}
		}

		if (listCondition.size() > 0) {
			if (listCondition.size() > 1) {
				logger.warn("Chosing random action because of "+listCondition.size()+" entries in listCondition: " + listCondition);
				i = Rand.rand(listCondition.size());
			} else {
				i = 0;
			}

			executeTransition(player, text, listCondition.get(i));
			return true;
		}

		// Then look for transitions without conditions.
		if (listConditionLess.size() > 0) {
			if (listConditionLess.size() > 1) {
				logger.warn("Chosing random action because of "+listConditionLess.size()+" entries in listConditionLess: " + listConditionLess);
				i = Rand.rand(listConditionLess.size());
    		} else {
    			i = 0;
    		}

			executeTransition(player, text, listConditionLess.get(i));
			return true;
		}

		return false;
	}

	private boolean matchesTransition(MatchType type, String text, Transition transition) {
		if (type ==  MatchType.EXACT_MATCH) {
			return transition.matches(currentState, text);
		} else if (type ==  MatchType.SIMILAR_MATCH) {
			return transition.matchesBeginning(currentState, text);
		} else if (type == MatchType.ABSOLUTE_JUMP) {
			return (currentState != ConversationStates.IDLE) && transition.matchesWild(text);
		} else if (type == MatchType.SIMILAR_JUMP) {
			return (currentState != ConversationStates.IDLE) && transition.matchesWildBeginning(text);
		} else {
			return false;
		}
	}

	private void executeTransition(Player player, String text, Transition state) {
		int nextState = state.getNextState();
		if (state.getReply() != null) {
			speakerNPC.say(state.getReply());
		}

		currentState = nextState;

		if (state.getAction() != null) {
			state.getAction().fire(player, text, speakerNPC);
		}
	}

	/**
	 * Returns a copy of the transition table
	 *
	 * @return list of transitions
	 */
	public List<Transition> getTransitions() {

		// return a copy so that the caller cannot mess up our internal structure
		return new LinkedList<Transition>(stateTransitionTable);
	}

}
