package games.stendhal.server.maps.semos.bakery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.PlayerHelper;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChefNPCTest {
	private Engine en;
	private Player player;
	private SpeakerNPC npc;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		Log4J.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		npc = new SpeakerNPC("chef");
		ChefNPC  cnpc = new ChefNPC();

		en = npc.getEngine();
		cnpc.createDialog(npc);

		 player = new Player(new RPObject());
		 player.setName("bob");
		 PlayerHelper.addEmptySlots(player);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHiAndBye() {

		en.step(player, "hi");
		assertTrue(npc.isTalking());
		assertEquals("Hallo! Glad to see you in my kitchen where I make #pizza and #sandwiches.", npc.get("text"));
		en.step(player, "bye");
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));

	}
	@Test
	public void testHiAndMakeNoStuff() {


		en.step(player, "hi");
		assertTrue(npc.isTalking());
		assertEquals("Hallo! Glad to see you in my kitchen where I make #pizza and #sandwiches.", npc.get("text"));
		en.step(player, "make");
		assertTrue(npc.isTalking());
		assertEquals("I can only make 1 sandwich if you bring me 1 loaf of #bread, 2 pieces of #cheese, and 1 piece of #ham.", npc.get("text"));
		en.step(player, "bye");
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));
	}
	@Test
	public void testHiAndMakeWithStuffSingle() {


		en.step(player, "hi");
		assertTrue(npc.isTalking());
		assertEquals("Hallo! Glad to see you in my kitchen where I make #pizza and #sandwiches.", npc.get("text"));
		StackableItem cheese =  new StackableItem("cheese","","",null);
		cheese.setQuantity(2);
		cheese.setID(new ID(2,"testzone"));
		player.getSlot("bag").add(cheese);
		StackableItem bread =  new StackableItem("bread","","",null);
		bread.setQuantity(1);
		bread.setID(new ID(1,"testzone"));
		player.getSlot("bag").add(bread );
		StackableItem ham = new StackableItem("ham","","",null);
		ham.setID(new ID(3,"testzone"));
		player.getSlot("bag").add( ham);
		assertEquals(2,player.getNumberOfEquipped("cheese"));
		assertEquals(1,player.getNumberOfEquipped("bread"));
		assertEquals(1,player.getNumberOfEquipped("ham"));

		en.step(player, "make");
		assertTrue(npc.isTalking());
		assertEquals("I need you to fetch me 1 loaf of #bread, 2 pieces of #cheese, and 1 piece of #ham for this job. Do you have it?", npc.get("text"));
		en.step(player, "yes");
		String[] questStatus = player.getQuest("leander_make_sandwiches").split(";");
		String[] expected ={"1","sandwich",""};
		assertEquals(expected[0],questStatus[0]); //amount
		assertEquals(expected[1],questStatus[1]); //item

		assertTrue(npc.isTalking());
		assertEquals("OK, I will make 1 sandwich for you, but that will take some time. Please come back in 3 minutes.", npc.get("text"));
		assertEquals(0,player.getNumberOfEquipped("cheese"));
		assertEquals(0,player.getNumberOfEquipped("bread"));
		assertEquals(0,player.getNumberOfEquipped("ham"));
		en.step(player, "bye");
		assertFalse(npc.isTalking());
		player.setQuest("leander_make_sandwiches","1;;0");

		en.step(player, "hi");
		assertEquals("Welcome back! I'm done with your order. Here you have 1 sandwich.", npc.get("text"));
		assertEquals(1,player.getNumberOfEquipped("sandwich"));
	}
	@Test
	public void testHiAndMakeWithStuffMultiple() {


		en.step(player, "hi");
		assertTrue(npc.isTalking());
		assertEquals("Hallo! Glad to see you in my kitchen where I make #pizza and #sandwiches.", npc.get("text"));
		StackableItem cheese =  new StackableItem("cheese","","",null);
		cheese.setQuantity(4);
		cheese.setID(new ID(2,"testzone"));
		player.getSlot("bag").add(cheese);
		StackableItem bread =  new StackableItem("bread","","",null);
		bread.setQuantity(2);
		bread.setID(new ID(1,"testzone"));
		player.getSlot("bag").add(bread );
		StackableItem ham = new StackableItem("ham","","",null);
		ham.setQuantity(2);
		ham.setID(new ID(3,"testzone"));
		player.getSlot("bag").add( ham);
		assertEquals(4,player.getNumberOfEquipped("cheese"));
		assertEquals(2,player.getNumberOfEquipped("bread"));
		assertEquals(2,player.getNumberOfEquipped("ham"));

		en.step(player, "make 2 sandwiches");
		assertTrue(npc.isTalking());
		assertEquals("I need you to fetch me 2 pieces of #ham, 4 pieces of #cheese, and 2 loaves of #bread for this job. Do you have it?", npc.get("text"));
		en.step(player, "yes");
		String[] questStatus = player.getQuest("leander_make_sandwiches").split(";");
		String[] expected ={"2","sandwich",""};
		assertEquals(expected[0],questStatus[0]); //amount
		assertEquals(expected[1],questStatus[1]); //item

		assertTrue(npc.isTalking());
		assertEquals("OK, I will make 2 sandwich for you, but that will take some time. Please come back in 6 minutes.", npc.get("text"));
		assertEquals(0,player.getNumberOfEquipped("cheese"));
		assertEquals(0,player.getNumberOfEquipped("bread"));
		assertEquals(0,player.getNumberOfEquipped("ham"));
		en.step(player, "bye");
		assertFalse(npc.isTalking());
		player.setQuest("leander_make_sandwiches","2;;0");

		en.step(player, "hi");
		assertEquals("Welcome back! I'm done with your order. Here you have 2 sandwiches.", npc.get("text"));
		assertEquals(2,player.getNumberOfEquipped("sandwich"));
	}


}
