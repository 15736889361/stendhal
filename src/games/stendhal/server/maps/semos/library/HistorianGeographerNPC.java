package games.stendhal.server.maps.semos.library;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.Path;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HistorianGeographerNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildSemosLibraryArea(zone, attributes);
	}

	private void buildSemosLibraryArea(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Zynn Iwuhos") {

			@Override
			protected void createPath() {
				List<Path.Node> nodes = new LinkedList<Path.Node>();
				nodes.add(new Path.Node(15, 2));
				nodes.add(new Path.Node(12, 2));
				nodes.add(new Path.Node(12, 5));
				nodes.add(new Path.Node(13, 5));
				nodes.add(new Path.Node(13, 6));
				nodes.add(new Path.Node(13, 5));
				nodes.add(new Path.Node(15, 5));
				nodes.add(new Path.Node(15, 6));
				nodes.add(new Path.Node(15, 5));
				nodes.add(new Path.Node(17, 5));
				nodes.add(new Path.Node(17, 6));
				nodes.add(new Path.Node(17, 2));
				setPath(nodes, true);
			}

			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, null, ConversationStates.ATTENDING,
				        null, new SpeakerNPC.ChatAction() {

					        @Override
					        public void fire(Player player, String text, SpeakerNPC engine) {
						        // A little trick to make NPC remember if it has met
						        // player before anc react accordingly
						        // NPC_name quest doesn't exist anywhere else neither is
						        // used for any other purpose
						        if (!player.isQuestCompleted("Zynn")) {
							        engine
							                .say("Hi, potential reader! Here you can find records of the history of Semos, and lots of interesting facts about this island of Faiumoni. If you like, I can give you a quick introduction to its #geography and #history! I also keep up with the #news, so feel free to ask me about that.");
							        player.setQuest("Zynn", "done");
						        } else {
							        engine.say("Hi again, " + player.getName() + ". How can I #help you this time?");
						        }
					        }
				        });
				addHelp("I can best help you by sharing my knowledge of Faiumoni's #geography and #history, as well as the latest #news.");
				addJob("I'm a historian and geographer, committed to writing down every objective fact about Faiumoni. Did you know I wrote most of the books in this library? Well, apart from \"Know How To Kill Creatures\", of course... Hayunn Naratha wrote that.");

				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				        ConversationStates.ATTENDING,
				        "I don't think there's really anything you could do for me right now. But thanks for asking!",
				        null);

				add(ConversationStates.ATTENDING, Arrays.asList("offer", "buy", "scroll", "scrolls", "home", "empty",
				        "marked", "summon", "magic", "wizard", "sorcerer"), null, ConversationStates.ATTENDING,
				        "I don't sell scrolls anymore... I had a big argument with my supplier, #Haizen.", null);

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("haizen", "haizen."),
				        null,
				        ConversationStates.ATTENDING,
				        "Haizen? He's a wizard who lives in a small hut between Semos and Ados. I used to sell his scrolls here, but we had an argument... you'll have to go see him yourself, I'm afraid.",
				        null);
			}
		};
		npcs.add(npc);

		zone.assignRPObjectID(npc);
		npc.put("class", "wisemannpc");
		npc.set(15, 2);
		npc.initHP(100);
		zone.add(npc);
	}
}
