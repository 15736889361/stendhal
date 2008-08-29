package games.stendhal.server.maps.quests;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.nalwor.hell.CaptiveNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class HelpTomiTest {


	private static String questSlot = "help_tomi";
	
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();
		
		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new CaptiveNPC().configureZone(zone, null);
		
			
		final AbstractQuest quest = new HelpTomi();
		quest.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		
		npc = SingletonRepository.getNPCList().get("tomi");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("help!", npc.get("text"));
		en.step(player, "help");
		assertEquals("where is my ice?", npc.get("text"));
		en.step(player, "ice");
		assertEquals("my ice? ice plz", npc.get("text"));
		en.step(player, "task");
		assertEquals("my ice? ice plz", npc.get("text"));
		en.step(player, "bye");
		assertEquals("bye", npc.get("text"));
		
		Item item = ItemTestHelper.createItem("ice sword");
		player.getSlot("bag").add(item);
		final int xp = player.getXP();
		en.step(player, "hi");
		assertEquals("help!", npc.get("text"));
		en.step(player, "help");
		assertEquals("where is my ice?", npc.get("text"));
		en.step(player, "ice");
		assertEquals("my ice :)", npc.get("text"));
		assertFalse(player.isEquipped("ice sword"));
		assertThat(player.getXP(), greaterThan(xp));
		assertTrue(player.isQuestCompleted(questSlot));
		// [22:07] kymara earns 1000 experience points.
		en.step(player, "bye");
		assertEquals("bye", npc.get("text"));
		
		en.step(player, "hi");
		assertEquals("help!", npc.get("text"));
		en.step(player, "ice");
		assertEquals("where is my ice?", npc.get("text"));
		en.step(player, "bye");
		assertEquals("bye", npc.get("text"));
		
		item = ItemTestHelper.createItem("ice sword");
		player.getSlot("bag").add(item);
		final int xp2 = player.getXP();
		en.step(player, "hi");
		assertEquals("help!", npc.get("text"));
		en.step(player, "ice");
		assertEquals("my ice :) :) ", npc.get("text"));
		assertFalse(player.isEquipped("ice sword"));
		assertThat(player.getXP(), greaterThan(xp2));
		assertTrue(player.isQuestCompleted(questSlot));
		// [22:07] kymara earns 4000 experience points.
		en.step(player, "bye");
		assertEquals("bye", npc.get("text"));
		
		item = ItemTestHelper.createItem("ice sword");
		player.getSlot("bag").add(item);
		final int xp3 = player.getXP();
		en.step(player, "hi");
		assertEquals("help!", npc.get("text"));
		en.step(player, "ice");
		assertEquals("my ice :) :) :) ", npc.get("text"));
		assertFalse(player.isEquipped("ice sword"));
		assertThat(player.getXP(), greaterThan(xp3));
		assertTrue(player.isQuestCompleted(questSlot));
		// [22:07] kymara earns 9000 experience points.
		en.step(player, "bye");
		assertEquals("bye", npc.get("text"));
	}
}