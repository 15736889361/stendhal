package games.stendhal.server.maps.fado.weaponshop;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to buy gems and gold and sell engagement ring.
 * <p>
 * He is also the NPC who can fix a broken emerald ring
 * (../../quests/Ringmaker.java)
 * <p>
 * He is also the NPC who casts the wedding ring (../../quests/Marriage.java)
 *
 * @author kymara
 */
public class RingSmithNPC implements ZoneConfigurator {
	private ShopList shops = SingletonRepository.getShopList();

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
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Ognir") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(18, 8));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(15, 10));
				nodes.add(new Node(16, 10));
				nodes.add(new Node(16, 14));
				nodes.add(new Node(18, 14));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi! Can I #help you?");
				addJob("I work with #gold, to fix and make jewellery.");
				addOffer("I sell engagement rings which I make myself. I also buy gems and gold, see the red catalogue on the table.");
				addReply(
						"gold",
						"It's cast from gold nuggets which you can pan for on Or'ril river. I don't cast it myself, but a smith in Ados does.");
				addHelp("I am an expert on #'wedding rings' and #'emerald rings', sometimes called the ring of #life.");
				addQuest("Well, you could consider getting married to be a quest! Ask me about #'wedding rings' if you need one.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellrings")), false);
				new BuyerAdder().add(this, new BuyerBehaviour(shops.get("buyprecious")), false);
				addGoodbye("Bye, my friend.");
			}
		};

		npc.setDescription("You see Ognir, a friendly bearded chap.");
		npc.setEntityClass("ringsmithnpc");
		npc.setPosition(18, 8);
		npc.initHP(100);
		zone.add(npc);
	}
}
