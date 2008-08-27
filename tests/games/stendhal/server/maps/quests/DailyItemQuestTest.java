package games.stendhal.server.maps.quests;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsIn.*;
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
import games.stendhal.server.maps.ados.townhall.MayorNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class DailyItemQuestTest {


	private static String questSlot = "daily_item";
	
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();
		
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		
		new MayorNPC().configureZone(zone, null);
			
		final AbstractQuest quest = new DailyItemQuest();
		quest.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		
		npc = SingletonRepository.getNPCList().get("Mayor Chalmers");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("On behalf of the citizens of Ados, welcome.", npc.get("text"));
		en.step(player, "task");
		assertTrue(npc.get("text").startsWith("Ados is in need of supplies. Go fetch "));
		en.step(player, "complete");
		assertTrue(npc.get("text").startsWith("You didn't fetch "));
		en.step(player, "bye");
		assertEquals("Good day to you.", npc.get("text"));
		
		player.setQuest(questSlot,"pina colada;100");
		Item item = ItemTestHelper.createItem("pina colada");
		player.getSlot("bag").add(item);
		final int xp = player.getXP();
		
		en.step(player, "hi");
		assertEquals("On behalf of the citizens of Ados, welcome.", npc.get("text"));
		en.step(player, "complete");
		assertFalse(player.isEquipped("pina colada"));
		assertThat(player.getXP(), greaterThan(xp));
		assertTrue(player.isQuestCompleted(questSlot));
		// [10:50] kymara earns 455960 experience points. 
		assertEquals("Good work! Let me thank you on behalf of the people of Ados!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Good day to you.", npc.get("text"));
		
		en.step(player, "hi");
		assertEquals("On behalf of the citizens of Ados, welcome.", npc.get("text"));
		en.step(player, "task");
		assertThat(npc.get("text"), 
				isOneOf("I can only give you a new quest once a day. Please check back in 24 hours.",
						"I can only give you a new quest once a day. Please check back in 1 day."));
		en.step(player, "bye");
		assertEquals("Good day to you.", npc.get("text"));

		// -----------------------------------------------
		player.setQuest(questSlot, "done;0");
		// [10:51] Changed the state of quest 'daily_item' from 'done;1219834233092;1' to 'done;0' 
		en.step(player, "hi");
		assertEquals("On behalf of the citizens of Ados, welcome.", npc.get("text"));
		en.step(player, "task");
		assertTrue(npc.get("text").startsWith("Ados is in need of supplies. Go fetch "));
		en.step(player, "bye");
		assertEquals("Good day to you.", npc.get("text"));

		// -----------------------------------------------

		// [10:53] Changed the state of quest 'daily_item' from 'dwarf cloak;1219834342834;0' to 'dwarf cloak;0' 
		player.setQuest(questSlot, "dwarf cloak;0");
		en.step(player, "hi");
		assertEquals("On behalf of the citizens of Ados, welcome.", npc.get("text"));
		en.step(player, "task");
		assertEquals("You're already on a quest to fetch a dwarf cloak. Say #complete if you brought it! Perhaps there are no supplies of that left at all! You could fetch #another item if you like, or return with what I first asked you.", npc.get("text"));
		en.step(player, "another");
		assertEquals("I see. Please, ask me for another #quest when you think you can help Ados again.", npc.get("text"));
		en.step(player, "quest");
		assertTrue(npc.get("text").startsWith("Ados is in need of supplies. Go fetch "));
		en.step(player, "bye");
		assertEquals("Good day to you.", npc.get("text"));
		
	}
}