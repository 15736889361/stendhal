package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.amazon.hut.JailedBarbNPC;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class JailedBarbarianTest {


	//private static String questSlot = "jailedbarb";
	
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();
		
		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new JailedBarbNPC().configureZone(zone, null);
		new games.stendhal.server.maps.amazon.hut.PrincessNPC().configureZone(zone, null);
		new games.stendhal.server.maps.kalavan.castle.PrincessNPC().configureZone(zone, null); 
				
		final AbstractQuest quest = new JailedBarbarian();
		// princess Esclara's greeting response is defined in her quest
		final AbstractQuest quest2 = new AmazonPrincess();
		quest.addToWorld();
		quest2.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		
		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I need some help to escape from this prison. These ugly Amazonesses! Can you help me please?", npc.get("text"));
		en.step(player, "yes");
		assertEquals("Thank you! First I need a #scythe to cut down these ugly flowers. And beware of bringing me an old one! Let me know if you have one!", npc.get("text"));
		en.step(player, "scythe");
		assertEquals("You don't have a scythe yet! Go and get one and hurry up!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I already asked you to bring me a #scythe to cut the flowers down!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------
		Item item = ItemTestHelper.createItem("scythe", 1);
		player.getSlot("bag").add(item);
		
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I already asked you to bring me a #scythe to cut the flowers down!", npc.get("text"));
		en.step(player, "scythe");
		// [15:41] lula earns 1000 experience points.
		assertEquals("Thank you!! First part is done! Now I can cut all flowers down! Now please ask Princess Esclara why I am here! I think saying my name should tell her something...", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("Please go ask Princess Esclara why I am here! I think saying my name should prompt her to tell you", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Princess Esclara");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Huh, what are you doing here?", npc.get("text"));
		en.step(player, "task");
		assertEquals("I'm looking for a drink, should be an exotic one. Can you bring me one?", npc.get("text"));
		en.step(player, "no");
		assertEquals("Oh, never mind. Bye then.", npc.get("text"));

		en.step(player, "hi");
		assertEquals("Huh, what are you doing here?", npc.get("text"));
		en.step(player, "lorenz");
		assertEquals("You want to know why he is in there? He and his ugly friends dug the #tunnel to our sweet Island! That's why he got jailed!", npc.get("text"));
		en.step(player, "tunnel");
		assertEquals("I am angry now and won't speak any more of it! If you want to learn more you'll have to ask him about the #tunnel!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Goodbye, and beware of the barbarians.", npc.get("text"));

		// -----------------------------------------------


		// -----------------------------------------------


		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I bet Princess Esclara said I was imprisoned because of the #tunnel ... ", npc.get("text"));
		en.step(player, "tunnel");
		assertEquals("What she drives me nuts, like all the flowers! This makes me hungry, go and get an #egg for me! Just let me know, you got one.", npc.get("text"));
		en.step(player, "egg");
		assertEquals("I cannot see an egg!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------


		item = ItemTestHelper.createItem("egg", 1);
		player.getSlot("bag").add(item);
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I asked you to fetch an #egg for me!", npc.get("text"));
		en.step(player, "egg");
		// [15:43] lula earns 1000 experience points.
		assertEquals("Thank you again my friend. Now you have to tell Princess Ylflia, in Kalavan Castle, that I am #jailed here. Please hurry up!", npc.get("text"));
		en.step(player, "jailed");
		assertEquals("I know that *I'm* jailed! I need you to go tell Princess Ylflia that I am here!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I need you to go tell Princess Ylflia that I am #jailed here.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));
		
		npc = SingletonRepository.getNPCList().get("Princess Ylflia");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("How do you do?", npc.get("text"));
		en.step(player, "lorenz");
		assertEquals("Oh my dear. My father should not know it. Hope he is fine! Thanks for this message! Send him #greetings! You better return to him, he could need more help.", npc.get("text"));
		en.step(player, "greetings");
		assertEquals("Please, go and give Lorenz my #greetings.", npc.get("text"));

		
		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "greetings");
		assertEquals("Thanks my friend. Now a final task for you! Bring me a barbarian armor. Without this I cannot escape from here! Go! Go! And let me know when you have the #armor !", npc.get("text"));
		en.step(player, "armor");
		assertEquals("You have no barbarian armor with you! Go get one!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		// -----------------------------------------------


		// -----------------------------------------------


		// -----------------------------------------------
		item = ItemTestHelper.createItem("barbarian armor", 1);
		player.getSlot("bag").add(item);
		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("I am waiting for you to bring me a barbarian #armor so I am strong enough to escape.", npc.get("text"));
		en.step(player, "armor");
		// [15:43] lula earns 50000 experience points.
		assertEquals("Thats all! Now I am prepared for my escape! Here is something I have stolen from Princess Esclara! Do not let her know. And now leave me!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));

		en.step(player, "hi");
		assertEquals("Flowers, flowers. All over here these ugly flowers!", npc.get("text"));
		en.step(player, "quest");
		assertEquals("Thank you for your help! Now I can escape!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye bye, and cut down some of these ugly flowers!", npc.get("text"));
	}
}