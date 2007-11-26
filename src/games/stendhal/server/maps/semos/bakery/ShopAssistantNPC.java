package games.stendhal.server.maps.semos.bakery;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Map;
import java.util.TreeMap;

/**
 * A woman who bakes bread for players.
 * 
 * @author daniel
 */
public class ShopAssistantNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addJob("I'm the shop assistant at this bakery.");
		npc.addReply("flour",
		        "We usually get our #flour from a mill northeast of here, but the wolves ate their delivery boy! If you help us out by bringing some, we can #bake delicious bread for you.");
		npc.addHelp("Bread is very good for you, especially for you adventurers who are always gulping down red meat. And my boss, Leander, happens to make the best sandwiches on the island!");
		npc.addGoodbye();

		// Erna bakes bread if you bring her flour.
		Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("flour", 2);

		ProducerBehaviour behaviour = new ProducerBehaviour("erna_bake_bread",
				"bake", "bread", requiredResources, 10 * 60);

		npc.addProducer(behaviour,
		        "Welcome to the Semos bakery! We'll #bake fine bread for anyone who helps bring our #flour delivery from the mill.");
	}
}
