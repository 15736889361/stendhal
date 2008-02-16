package games.stendhal.server.maps.nalwor.hell;

import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the 2nd reaper in hell.
 * @author kymara
 */
public class Reaper2NPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}

	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("repaeR mirG") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("#elddir a evlos tsum uoy ecalp siht #evael ot kees uoy fI");
				add(ConversationStates.ATTENDING, "evael", null, ConversationStates.QUESTION_1, "?erus uoy erA .truh lliw tI", null);
				List<SpeakerNPC.ChatAction> processStep = new LinkedList<SpeakerNPC.ChatAction>();
				processStep.add(new TeleportAction("int_afterlife", 31, 23, Direction.UP));
				processStep.add(new DecreaseKarmaAction(-100.0));
				processStep.add(new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, SpeakerNPC npc) {
						player.subXP(10000);
						player.sendPrivateText(NotificationType.NEGATIVE, "The Reaper took 10000 XP and gave you bad karma.");
					}
				});
				add(ConversationStates.QUESTION_1, Arrays.asList("yes", "sey", "ok", "ko"), null, ConversationStates.IDLE, "!ahahahaH", new MultipleActions(processStep));
				add(ConversationStates.QUESTION_1, Arrays.asList("no", "on"), null, ConversationStates.ATTENDING, ".eniF", null);
				addReply("elddir", ".rorrim ym ksA");
				addJob(".gnivil eht fo sluos eht tsevrah I");
				addHelp("#evael ot hsiw uoy dluohs ,lleh fo setag eht ot syek eht dloh I");
				addOffer("... luos ruoy ekat ot em hsiw uoy sselnU");
				addGoodbye("... yawa dessap sah sgniht fo redro dlo ehT");
			}
		};
		npc.setEntityClass("grim_reaper2_npc");
		npc.setPosition(68, 76);
		npc.initHP(100);
		zone.add(npc);
	}
}
