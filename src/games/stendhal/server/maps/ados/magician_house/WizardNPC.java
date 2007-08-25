package games.stendhal.server.maps.ados.magician_house;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WizardNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildMagicianHouseArea(zone, attributes);
	}

	private void buildMagicianHouseArea(StendhalRPZone zone, Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Haizen") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 4));
				nodes.add(new Node(13, 4));
				nodes.add(new Node(13, 9));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(9, 8));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(2, 9));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(7, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("I am a wizard who sells #magic #scrolls. Just ask me for an #offer!");
				addHelp("You can take powerful magic with you on your adventures with the aid of my #magic #scrolls!");

				addSeller(new SellerBehaviour(shops.get("scrolls")));

				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				        ConversationStates.ATTENDING,
				        "I don't have any tasks for you right now. If you need anything from me, just ask.", null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("magic", "scroll", "scrolls"),
				        null,
				        ConversationStates.ATTENDING,
				        "I #offer scrolls that help you to travel faster: #home scrolls and the #markable #empty scrolls. For the more advanced customer, I also have #summon scrolls!",
				        null);
				add(ConversationStates.ATTENDING, Arrays.asList("home", "home_scroll"), null,
				        ConversationStates.ATTENDING,
				        "Home scrolls take you home immediately, a good way to escape danger!", null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("empty", "marked", "empty_scroll", "markable", "marked_scroll"),
				        null,
				        ConversationStates.ATTENDING,
				        "Empty scrolls are used to mark a position. Those marked scrolls can take you back to that position. They are a little expensive, though.",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        "summon",
				        null,
				        ConversationStates.ATTENDING,
				        "A summon scroll empowers you to summon animals to you; advanced magicians will be able to summon stronger monsters than others. Of course, these scrolls can be dangerous if misused.",
				        null);

				addGoodbye();
			}
		};
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		npc.put("class", "wisemannpc");
		npc.setPosition(7, 2);
		npc.initHP(100);
		zone.add(npc);
	}
}
