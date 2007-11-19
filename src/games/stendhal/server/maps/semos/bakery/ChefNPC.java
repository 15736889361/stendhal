package games.stendhal.server.maps.semos.bakery;

import games.stendhal.server.entity.npc.ProducerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * The bakery chef. Father of the camping girl.
 * He makes sandwiches for players.
 * 
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addJob("I'm the local baker. I also run a #pizza delivery service. We used to get a lot of orders from Ados before the war broke out and they blocked the road. At least it gives me more time to #make sandwiches for out valuable customers; everybody says they're great!");
		npc.addHelp("If you want to earn some money, you could do me a #favor and help me with the #pizza deliveries. My daughter #Sally used to do it, but she's camping at the moment.");
		npc.addReply("bread", "Oh, Erna handles that side of the business; just go over and talk to her.");
		npc.addReply("cheese",
		        "Cheese is pretty hard to find at the minute, we had a big rat infestation recently. I wonder where the little rodents took it all to?");
		npc.addReply("ham",
		        "Well, you look like a skilled hunter; why not go to the forest and hunt some up fresh? Don't bring me those little pieces of meat, though... I only make sandwiches from high quality ham!");
		npc.addReply("Sally",
		        "My daughter Sally might be able to help you get ham. She's a scout, you see; I think she's currently camped out south of Or'ril Castle.");
		npc.addReply("pizza", "I need someone who helps me delivering pizza. Maybe you could do that #task.");
		npc.addReply(Arrays.asList("sandwich", "sandwiches"),
		        "My sandwiches are tasty and nutritious. If you want one, just tell me to #make #1 #sandwich.");
		npc.addGoodbye();

		// Leander makes sandwiches if you bring him bread, cheese, and ham.
		Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	// use sorted TreeMap instead of HashMap
		requiredResources.put("bread", 1);
		requiredResources.put("cheese", 2);
		requiredResources.put("ham", 1);

		ProducerBehaviour behaviour = new ProducerBehaviour(
				"leander_make_sandwiches", "make", "sandwich",
				requiredResources, 3 * 60);

		npc.addProducer(behaviour,
				"Hallo! Glad to see you in my kitchen where I make #pizza and #sandwiches.");
	}
}
