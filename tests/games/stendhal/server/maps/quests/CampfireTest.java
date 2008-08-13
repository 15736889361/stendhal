package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
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

	private static final String ZONE_NAME = "testzone";

	private static final String CAMPFIRE = "campfire";

	private Player player;

	private SpeakerNPC npc;

	private StendhalRPZone zone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendhalRPRuleProcessor.get();
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	
	@Before
	public void setUp() throws Exception {
		 player = PlayerTestHelper.createPlayer("player");
		 zone = new StendhalRPZone("zone");
		new CampingGirlNPC().configureZone(zone, null);
		npc = NPCList.get().get("Sally");
		final Campfire cf = new Campfire();
		cf.addToWorld();
	}
	
	
	@After
	public void tearDown() throws Exception {
		player = null;
		NPCList.get().clear();
	}

	@Test
	public void testCanStartQuestNow() throws Exception {
		
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Hi! I need a little #favor ... ", npc.get("text"));
		assertTrue(en.step(player, "bye"));

		player.setQuest(CampfireTest.CAMPFIRE, "start");
		assertTrue(en.step(player, "hi"));
		assertEquals(
				"You're back already? Don't forget that you promised to collect ten pieces of wood for me!",
				npc.get("text"));
		assertTrue(en.step(player, "bye"));

		player.setQuest(CampfireTest.CAMPFIRE, String.valueOf(System.currentTimeMillis()));
		en.step(player, "hi");
		assertEquals(
				"Hi again!",
				npc.get("text"));
		assertTrue(en.step(player, "bye"));

		final long SIXMINUTESAGO = System.currentTimeMillis() - 6 * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
		player.setQuest(CampfireTest.CAMPFIRE, String.valueOf(SIXMINUTESAGO));
		en.step(player, "hi");
		assertEquals("delay is 5 minutes, so 6 minutes should be enough", "Hi again!", npc.get("text"));
		assertTrue(en.step(player, "bye"));
	}

	@Test
	public void testHiAndbye() {
		
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! I need a little #favor ... ", npc.get("text"));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Bye.", npc.get("text"));
	}

	@Test
	public void testDoQuest() {
		
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! I need a little #favor ... ", npc.get("text"));
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
		final StackableItem wood = new StackableItem("wood", "", "", null);
		wood.setQuantity(10);
		wood.setID(new ID(2, ZONE_NAME));
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
	public void testIsRepeatable() throws Exception {
		assertTrue(new Campfire().isRepeatable(null));
	}

	@Test
	public void testIsCompleted() {
		assertFalse(new Campfire().isCompleted(player));

		player.setQuest(CAMPFIRE, "start");
		assertFalse(new Campfire().isCompleted(player));

		player.setQuest(CAMPFIRE, "rejected");
		assertFalse(new Campfire().isCompleted(player));

		player.setQuest(CAMPFIRE, "notStartorRejected");
		assertTrue(new Campfire().isCompleted(player));
	}

	@Test
	public void testJobAndOffer() {
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Hi! I need a little #favor ... ", npc.get("text"));
		assertTrue(en.step(player, "job"));
		assertEquals("Work? I'm just a little girl! I'm a scout, you know.",
				npc.get("text"));
		assertFalse("no matching state transition", en.step(player, "offers")); 
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
	
	@Test
	public void testCanNotRepeatYet() {
		final String questState = Long.toString(new Date().getTime());
		
		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, questState);

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			assertEquals("Thanks, but I think the wood you brought me already will last me 5 minutes more.", npc.getText());
			assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
			assertEquals("quest state unchanged", questState, player.getQuest(CAMPFIRE));
		}
	}
	
	@Test
	public void testRepeatQuest() {
		final String questState = Long.toString(new Date().getTime() - 6 * 60 * 1000);
		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, questState);

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			assertEquals("My campfire needs wood again! Could you please get some from the forest for me? I need ten pieces.", npc.get("text"));
			assertEquals(ConversationStates.QUEST_OFFERED, en.getCurrentState());
			assertEquals("quest state unchanged", questState, player.getQuest(CAMPFIRE));
		}
	}
	
	@Test
	public void testAllowRestartAfterRejecting() {
		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, "rejected");

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			assertEquals("My campfire needs wood again! Could you please get some from the forest for me? I need ten pieces.", npc.get("text"));
			assertEquals(ConversationStates.QUEST_OFFERED, en.getCurrentState());
			assertEquals("quest state unchanged", "rejected", player.getQuest(CAMPFIRE));
		}
	}
	
	@Test
	public void testRefuseQuest() {
		final Engine en = npc.getEngine();
		final double karma = player.getKarma(); 

		en.setCurrentState(ConversationStates.QUEST_OFFERED);
		en.step(player, "no");
		
		assertEquals("Oh dear, how am I going to cook all this meat? Perhaps I'll just have to feed it to the animals..."
, npc.getText());
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("quest state 'rejected'", "rejected", player.getQuest(CAMPFIRE));
		assertEquals("karma penalty", karma - 5.0, player.getKarma(), 0.01);
	}
}
