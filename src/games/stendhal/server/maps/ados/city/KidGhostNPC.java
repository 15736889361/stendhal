package games.stendhal.server.maps.ados.city;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Ghost NPC
 *
 * @author kymara
 */
public class KidGhostNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone, attributes);
	}
	private void buildNPC(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC ghost = new SpeakerNPC("Ben") {
			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(34, 121));
				nodes.add(new Node(24, 121));
				nodes.add(new Node(24, 112));
				nodes.add(new Node(13, 112));
				nodes.add(new Node(13, 121));
				nodes.add(new Node(6, 121));
				nodes.add(new Node(6, 112));
				nodes.add(new Node(13, 112));
				nodes.add(new Node(13, 121));
				nodes.add(new Node(24, 121));
				nodes.add(new Node(24, 112));
				nodes.add(new Node(34, 112));
				setPath(new FixedPath(nodes, true));
			}
			@Override
		    protected void createDialog() {
			    add(ConversationStates.IDLE,
			    	ConversationPhrases.GREETING_MESSAGES,
			    	null,
			    	ConversationStates.IDLE,
			    	null,
			    	new SpeakerNPC.ChatAction() {
			    		@Override
			    		public void fire(Player player, String text,
			    				SpeakerNPC npc) {
			    			if (!player.hasQuest("find_ghosts")) {
			    				player.setQuest("find_ghosts", "looking:said");
			    			}
			    			String npcQuestText = player.getQuest("find_ghosts");
			    			String[] npcDoneText = npcQuestText.split(":");
			    			List<String> list = Arrays.asList(npcDoneText[0].split(";"));
						    if (!list.contains(npc.getName())) {
							    player.setQuest("find_ghosts", npcDoneText[0]
									    + ";" + npc.getName()
									    + ":" +  npcDoneText[1]);
							    npc.say("Hello! Hardly anyone speaks to me. The other children pretend I don't exist. I hope you remember me.");
							    player.addXP(100);
							    player.addKarma(10);
							} else {
							    npc.say("Hello again. I'm glad you remember me. I'll just keep walking here till I have someone to play with.");
							}
						}
					});
			}

		};
		ghost.setDescription("You see a ghostly figure of a small boy.");
		ghost.setResistance(0);

		npcs.add(ghost);
		zone.assignRPObjectID(ghost);
		ghost.put("class", "kid7npc");
		// He is a ghost so he is see through
		ghost.setVisibility(50);
		ghost.set(34, 121);
		// He has low HP
		ghost.initHP(30);
		zone.add(ghost);
	}
}
