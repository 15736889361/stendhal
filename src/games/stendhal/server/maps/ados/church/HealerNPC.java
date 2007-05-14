package games.stendhal.server.maps.ados.church;

import games.stendhal.server.entity.npc.ProducerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The healer (original name: Valo). He makes mega potions. 
 */
public class HealerNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
	    npc.addJob("Long ago I was a priest of this church. But my #ideas were not approved of by all."); 
	    npc.addReply("ideas",
		        "I have read many texts and learnt of strange ways. My healing powers became so strong I can now #concoct a special #mega_potion for warriors like you.");
	    npc.addReply("giant_heart",
		        "Giants dwell in caves east of here. Good luck slaying those beasts ...");
	    npc.addReply("heal", "I can #concoct a strong #mega_potion for you to use while you travel. I need one special ingredient and a small charge to cover my time.");
	    npc.addReply("mega_potion", "It is a powerful elixir. If you want one, ask me to #concoct #1 #mega_potion.");
	    npc.addReply("money", "That is your own concern. We of the cloth need not scurry around to make cash.");    
	    npc.addHelp("If you want to become wise like me, you should visit a library. There is much to learn.");
	    npc.addGoodbye("Fare thee well.");

		// Valo makes mega potions if you bring giant heart and money
		Map<String, Integer> requiredResources = new HashMap<String, Integer>();
		requiredResources.put("money", 20);
		requiredResources.put("giant_heart", 1);
		ProducerBehaviour behaviour = new ProducerBehaviour("Valo_concoct_potion",
				"concoct", "mega_potion", requiredResources, 2 * 60);

		npc.addProducer(behaviour,
		        "Greetings, young one. I #heal and I #help.");
	}
}
