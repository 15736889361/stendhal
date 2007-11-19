package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.orril.river.CampingGirlNPC;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject.ID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class CampfireTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();

		assertTrue(MockStendhalRPRuleProcessor.get() instanceof MockStendhalRPRuleProcessor);
		MockStendlRPWorld.get();
		CampingGirlNPC sallyconf = new CampingGirlNPC();
		sallyconf.configureZone(new StendhalRPZone("testzone"), null);

		Campfire cf = new Campfire();
		cf.addToWorld();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		SpeakerNPC npc = NPCList.get().get("Sally");

		if (npc != null) {
			npc.setCurrentState(ConversationStates.IDLE);
		}
	}

	@Test
	public void testHiAndbye() {
		Player player;
		player = PlayerTestHelper.createPlayer();

		SpeakerNPC npc = NPCList.get().get("Sally");
		assertNotNull(npc);
		Engine en = npc.getEngine();
		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! Could you do me a #favor?", npc.get("text"));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));
	}

	@Test
	public void testDoQuest() {
		Player player;
		player = PlayerTestHelper.createPlayer();


		SpeakerNPC npc = NPCList.get().get("Sally");
		assertNotNull(npc);
		Engine en = npc.getEngine();
		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! Could you do me a #favor?", npc.get("text"));
		assertTrue(en.step(player, "favor"));

		assertEquals(
				"I need more wood to keep my campfire running, But I can't leave it unattended to go get some! Could you please get some from the forest for me? I need ten pieces.",
				npc.get("text"));
		assertTrue(en.step(player, "yes"));
		assertEquals(
				"Okay. You can find wood in the forest north of here. Come back when you get ten pieces of wood!",
				npc.get("text"));
		assertTrue(en.step(player, "bye"));
		assertEquals("Bye.", npc.get("text"));
		StackableItem wood = new StackableItem("wood", "", "", null);
		wood.setQuantity(10);
		wood.setID(new ID(2, "testzone"));
		player.getSlot("bag").add(wood);
		assertEquals(10, player.getNumberOfEquipped("wood"));
		assertTrue(en.step(player, "hi"));
		assertEquals(
				"Hi again! You've got wood, I see; do you have those 10 pieces of wood I asked about earlier?",
				npc.get("text"));
		assertTrue(en.step(player, "yes"));
		assertEquals(0, player.getNumberOfEquipped("wood"));
		assertTrue("Thank you! Here, take some meat!".equals(npc.get("text"))
				|| "Thank you! Here, take some ham!".equals(npc.get("text")));
		assertTrue((10 == player.getNumberOfEquipped("meat"))
				|| (10 == player.getNumberOfEquipped("ham")));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));

	}

	@Test
	public void testJobAndOffer() {
		Player player;
		player = PlayerTestHelper.createPlayer();


		SpeakerNPC npc = NPCList.get().get("Sally");
		assertNotNull(npc);

		Engine en = npc.getEngine();
		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! Could you do me a #favor?", npc.get("text"));
		assertTrue(en.step(player, "job"));
		assertEquals("Work? I'm just a little girl! I'm a scout, you know.",
				npc.get("text"));
		assertFalse(en.step(player, "offers"));	// no matching state transition
		assertEquals("Work? I'm just a little girl! I'm a scout, you know.",
				npc.get("text"));
		assertTrue(en.step(player, "help"));
		assertEquals(
				"You can find lots of useful stuff in the forest; wood and mushrooms, for example. But beware, some mushrooms are poisonous!",
				npc.get("text"));

		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));
	}
}
