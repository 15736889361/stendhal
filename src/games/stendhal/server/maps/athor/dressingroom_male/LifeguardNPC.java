package games.stendhal.server.maps.athor.dressingroom_male;

import games.stendhal.server.entity.npc.OutfitChangerBehaviour;
import games.stendhal.server.entity.npc.ProducerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Dressing rooms at the Athor island beach (Inside / Level 0)
 *
 * @author daniel
 */
public class LifeguardNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addJob("I'm one of the lifeguards at this beach. And as you can see, I also take care of the men's dressing room.");
		npc.addHelp("Just tell me if you want to #borrow #trunks!");
		npc.addGoodbye("Have fun!");

		Map<String, Integer> priceList = new HashMap<String, Integer>();
		priceList.put("trunks", 5);
		OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList);
		npc.addOutfitChanger(behaviour, "borrow");

		// stuff needed for the SuntanCreamForZara quest
		Map<String, Integer> requiredResources = new HashMap<String, Integer>();
		requiredResources.put("arandula", 1);
		requiredResources.put("kokuda", 1);
		requiredResources.put("minor_potion", 1);

		ProducerBehaviour mixerBehaviour = new ProducerBehaviour("david_mix_cream",
				"mix", "suntan_cream", requiredResources, 10 * 60);

		npc.addProducer(mixerBehaviour, "Hallo!");

		npc.addReply(
		        Arrays.asList("suntan", "cream", "suntan_cream"),
		        "Pam's and mine suntan cream is famous all over the island. But the way to the labyrinth entrance is blocked, so we can't get all the ingredients we need. If you bring me the things we need, I can #mix our special suntan cream for you.");

		npc.addReply("arandula", "Arandula is a herb which is growing around Semos.");

		npc.addReply(
		        "kokuda",
		        "We can't find the Kokuda herb which is growing on this island, because the entrance of the labyrinth, where you can find this herb, is blocked.");

		npc.addReply("minor_potion", "It's a small bottle full of potion. You can buy it at several places.");

	};
}
