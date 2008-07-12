/* $Id$ */
package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.PostTransitionAction;
import games.stendhal.server.entity.npc.fsm.PreTransitionCondition;
import games.stendhal.server.entity.npc.fsm.Transition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.CountingMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.Definition.DefinitionClass;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Dumps the transition table of an NPC for "dot" http://www.graphviz.org/ to
 * display a nice graph.
 * 
 * @author hendrik
 */
public class DumpTransitionsEx extends ScriptImpl {

	private static Logger logger = Logger.getLogger(DumpTransitionsEx.class);
	private StringBuilder dumpedTable;
	private CountingMap<PreTransitionCondition> conditions;
	private CountingMap<PostTransitionAction> actions;

	public static void generateRPEvent() {
		if (!RPClass.hasRPClass("transition_graph")) {
    		final RPClass rpclass = new RPClass("transition_graph");
    		rpclass.add(DefinitionClass.RPEVENT, "transition_graph", Definition.STANDARD);
		}
	}

	@Override
	public void execute(final Player admin, final List<String> args) {
		generateRPEvent();

		if (args.size() < 1) {
			admin.sendPrivateText("/script DumpTransitionsEx.class <npcname>");
			return;
		}

		final StringBuilder npcName = new StringBuilder();
		for (final String arg : args) {
			npcName.append(arg + " ");
		}
		final SpeakerNPC npc = SingletonRepository.getNPCList().get(npcName.toString().trim());
		if (npc == null) {
			admin.sendPrivateText("There is no NPC called " + npcName);
			return;
		}
		dump(npc);

		final RPEvent event = new RPEvent("transition_graph");
		event.put("data", dumpedTable.toString());
		admin.addEvent(event);
	}

	/**
	 * Returns the transition diagram as string.
	 * 
	 * @param npc
	 *            SpeakerNPC
	 * @return transition diagram
	 */
	public String getDump(final SpeakerNPC npc) {
		dump(npc);
		return dumpedTable.toString();
	}

	private void dump(final SpeakerNPC npc) {
		dumpedTable = new StringBuilder();
		conditions = new CountingMap<PreTransitionCondition>("C");
		actions = new CountingMap<PostTransitionAction>("A");

		dumpHeader();
		dumpNPC(npc);
		dumpCaption();
		dumpFooter();
	}

	private void dumpHeader() {
		dumpedTable.append("digraph finite_state_machine {\r\n");
		dumpedTable.append("rankdir=LR\r\n");
	}

	private void dumpNPC(final SpeakerNPC npc) {
		final List<Transition> transitions = npc.getTransitions();
		for (final Transition transition : transitions) {
			dumpedTable.append(getStateName(transition.getState()) + " -> "
					+ getStateName(transition.getNextState()));
			final String transitionName = getExtendedTransitionName(transition);
			dumpedTable.append(" [ label = \"" + transitionName + "\" ];\r\n");
		}
	}

	private String getExtendedTransitionName(final Transition transition) {
		String transitionName = transition.getTrigger().toString();

		if (transition.getCondition() != null) {
			final String key = conditions.add(transition.getCondition());
			transitionName = "(" + key + ") " + transitionName;
		}

		if (transition.getAction() != null) {
			final String key = actions.add(transition.getAction());
			transitionName = transitionName + " (" + key + ")";
		}

		return transitionName;
	}

	private static String getStateName(final int number) {
		final Integer num = Integer.valueOf(number);
		final Field[] fields = ConversationStates.class.getFields();
		for (final Field field : fields) {
			try {
				if (field.get(null).equals(num)) {
					return field.getName();
				}
			} catch (final IllegalArgumentException e) {
				logger.error(e, e);
			} catch (final IllegalAccessException e) {
				logger.error(e, e);
			}
		}
		return Integer.toString(number);
	}

	private void dumpFooter() {
		dumpedTable.append("}");
	}

	private void dumpCaption() {
		dumpedTable.append("\r\n");
		dumpedTable.append("\"caption\" [\r\n");
		dumpedTable.append("label = \"");
		dumpedTable.append("Caption");
		for (final Map.Entry<String, PreTransitionCondition> entry : conditions) {
			dumpedTable.append(" | " + entry.getKey() + "\t"
					+ captionEntryToString(entry.getValue().toString()));
		}
		for (final Map.Entry<String, PostTransitionAction> entry : actions) {
			dumpedTable.append(" | " + entry.getKey() + "\t"
					+ captionEntryToString(entry.getValue()));
		}
		dumpedTable.append("\"\r\n");
		dumpedTable.append("shape = \"record\"\r\n");
		dumpedTable.append("];\r\n");
	}

	private String captionEntryToString(final Object entry) {
		final String prefix = "games.stendhal.server.";
		String entryName = entry.toString();
		entryName = StringEscapeUtils.escapeHtml(entryName);
		if (entryName.startsWith(prefix)) {
			entryName = entryName.substring(prefix.length());
		}
		return entryName;
	}
}
