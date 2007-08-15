package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.PlayerHelper;
import games.stendhal.server.maps.ados.outside.AnimalKeeperNPC;
import games.stendhal.server.maps.ados.outside.VeterinarianNPC;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZooFoodTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendhalRPRuleProcessor.get();
		MockStendlRPWorld.get();
		AnimalKeeperNPC katinkaconf = new AnimalKeeperNPC();
		katinkaconf.configureZone(new StendhalRPZone("testzone"), null);
		VeterinarianNPC fellgoodconf = new VeterinarianNPC();
		fellgoodconf.configureZone(new StendhalRPZone("testzone"), null);
		ZooFood zf = new ZooFood();
		zf.addToWorld();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHiAndBye() {
		Player player;
		player = new Player(new RPObject());
		PlayerHelper.addEmptySlots(player);

		SpeakerNPC npc = NPCList.get().get("Katinka");
		assertNotNull(npc);
		Engine en1 = npc.getEngine();
		en1.step(player, "hi");
		assertTrue(npc.isTalking());
		assertEquals(
				"Welcome to the Ados Wildlife Refuge! We rescue animals from being slaughtered by evil adventurers. But we need help... maybe you could do a #task for us?",
				npc.get("text"));
		en1.step(player, "bye");
		assertFalse(npc.isTalking());
		assertEquals("Goodbye!", npc.get("text"));

		npc = NPCList.get().get("Dr. Feelgood");
		assertNotNull(npc);
		Engine en = npc.getEngine();
		en.step(player, "hi");
		assertFalse(npc.isTalking());
		assertEquals(
				"Sorry, can't stop to chat. The animals are all sick because they don't have enough food. See yourself out, won't you?",
				npc.get("text"));
		en.step(player, "bye");
		assertFalse(npc.isTalking());
		assertEquals(
				"Sorry, can't stop to chat. The animals are all sick because they don't have enough food. See yourself out, won't you?",
				npc.get("text"));

	}

	@Test
	public void testDoQuest() {
		Player player;
		player = new Player(new RPObject());
		PlayerHelper.addEmptySlots(player);

		SpeakerNPC katinkaNpc = NPCList.get().get("Katinka");
		assertNotNull(katinkaNpc);
		Engine enKatinka = katinkaNpc.getEngine();
		SpeakerNPC feelgoodNpc = NPCList.get().get("Dr. Feelgood");
		assertNotNull(feelgoodNpc);
		Engine enFeelgood = feelgoodNpc.getEngine();
		enKatinka.step(player, "hi");
		assertEquals(
				"Welcome to the Ados Wildlife Refuge! We rescue animals from being slaughtered by evil adventurers. But we need help... maybe you could do a #task for us?",
				katinkaNpc.get("text"));

		enKatinka.step(player, "task");
		assertEquals(
				"Our tigers, lions and bears are hungry. We need 10 pieces of ham to feed them. Can you help us?",
				katinkaNpc.get("text"));

		enKatinka.step(player, "yes");
		assertTrue(player.hasQuest("zoo_food"));
		assertEquals(
				"Okay, but please don't let the poor animals suffer too long! Bring me the pieces of ham as soon as you get them.",
				katinkaNpc.get("text"));

		enKatinka.step(player, "bye");
		assertEquals("Goodbye!", katinkaNpc.get("text"));
		assertTrue(player.hasQuest("zoo_food"));
		assertEquals("start", player.getQuest("zoo_food"));
		// feelgood is still in sorrow
		enFeelgood.step(player, "hi");
		assertFalse(feelgoodNpc.isTalking());
		assertEquals(
				"Sorry, can't stop to chat. The animals are all sick because they don't have enough food. See yourself out, won't you?",
				feelgoodNpc.get("text"));
		enFeelgood.step(player, "bye");
		assertFalse(feelgoodNpc.isTalking());
		assertEquals(
				"Sorry, can't stop to chat. The animals are all sick because they don't have enough food. See yourself out, won't you?",
				feelgoodNpc.get("text"));
		// bother katinka again
		enKatinka.step(player, "hi");
		assertEquals("Welcome back! Have you brought the 10 pieces of ham?",
				katinkaNpc.get("text"));
		enKatinka.step(player, "yes"); // lie
		assertEquals(
				"*sigh* I SPECIFICALLY said that we need 10 pieces of ham!",
				katinkaNpc.get("text"));
		enKatinka.step(player, "bye");
		assertEquals("Goodbye!", katinkaNpc.get("text"));
		// equip player with to less needed stuff
		StackableItem ham = new StackableItem("ham", "", "", null);
		ham.setQuantity(5);
		ham.setID(new ID(2, "testzone"));
		player.getSlot("bag").add(ham);
		assertEquals(5, player.getNumberOfEquipped("ham"));

		// bother katinka again
		enKatinka.step(player, "hi");
		assertEquals("Welcome back! Have you brought the 10 pieces of ham?",
				katinkaNpc.get("text"));
		enKatinka.step(player, "yes"); // lie
		assertEquals(
				"*sigh* I SPECIFICALLY said that we need 10 pieces of ham!",
				katinkaNpc.get("text"));
		enKatinka.step(player, "bye");
		assertEquals("Goodbye!", katinkaNpc.get("text"));
		// equip player with to needed stuff
		StackableItem ham2 = new StackableItem("ham", "", "", null);
		ham2.setQuantity(5);
		ham2.setID(new ID(3, "testzone"));
		player.getSlot("bag").add(ham2);
		assertEquals(10, player.getNumberOfEquipped("ham"));
		// bring stuff to katinka
		enKatinka.step(player, "hi");
		assertEquals("Welcome back! Have you brought the 10 pieces of ham?",
				katinkaNpc.get("text"));
		enKatinka.step(player, "yes");
		assertEquals("Thank you! You have rescued our rare animals.",
				katinkaNpc.get("text"));
		enKatinka.step(player, "bye");
		assertEquals("Goodbye!", katinkaNpc.get("text"));
		assertEquals("done", player.getQuest("zoo_food"));
		// feelgood is reacting
		enFeelgood.step(player, "hi");
		assertTrue(feelgoodNpc.isTalking());
		assertEquals(
				"Hello! Now that the animals have enough food, they don't get sick that easily, and I have time for other things. How can I help you?",
				feelgoodNpc.get("text"));
		enFeelgood.step(player, "offers");

		assertEquals(
				"I sell antidote, minor_potion, potion, and greater_potion.",
				feelgoodNpc.get("text"));
		enFeelgood.step(player, "bye");
		assertEquals("Bye!", feelgoodNpc.get("text"));

	}
}
