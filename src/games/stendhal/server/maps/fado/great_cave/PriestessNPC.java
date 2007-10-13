package games.stendhal.server.maps.fado.great_cave;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a priestess NPC 
 * She is a 
 *
 * @author kymara
 */
public class PriestessNPC implements ZoneConfigurator {
	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Kendra Mattori") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 10));
				nodes.add(new Node(14, 10));
				nodes.add(new Node(14, 13));
				nodes.add(new Node(9, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			        addGreeting(null,new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
							engine.say("Hello, " + player.getTitle() + ".");

						}
					}
				);
			        addJob("As a priestess I can #offer you a number of potions and antidotes.");
				addHelp("My sister Salva has the gift of healing. She is out for a walk by the aqueduct, you should find her there if you need her.");
				addSeller(new SellerBehaviour(shops.get("superhealing")), true);
 				addGoodbye("Bye, for now.");
			}
		};

		npc.setDescription("You see a beautiful woman hidden under swathes of fabric.");
		npc.setEntityClass("cloakedwoman2npc");
		npc.setPosition(9, 10);
		npc.initHP(100);
		zone.add(npc);
	}
}
