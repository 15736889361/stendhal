package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.tunnel.WishmanNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class DragonLairTest {


	private static String questSlot = "dragon_lair";
	
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();
		
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		// must add the zone here as the wishman teleports player into dragon lair
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("-1_ados_outside_w"));
		new WishmanNPC().configureZone(zone, null);
		
			
		final AbstractQuest quest = new DragonLair();
		quest.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		
		npc = SingletonRepository.getNPCList().get("Wishman");
		en = npc.getEngine();
		
		// see if level 0 player can enter (they could)
		en.step(player, "hi");
		assertEquals("Greetings, my fellow traveler. What may I do for you?", npc.get("text"));
		en.step(player, "task");
		assertEquals("Would you like to visit our dragon lair?", npc.get("text"));
		en.step(player, "no");
		assertEquals("Ok, but our dragons will be sorry you didn't stop in for a visit.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Farewell. May your days be many and your heart be free.", npc.get("text"));

		en.step(player, "hi");
		assertEquals("Greetings, my fellow traveler. What may I do for you?", npc.get("text"));
		en.step(player, "task");
		assertEquals("Would you like to visit our dragon lair?", npc.get("text"));
		en.step(player, "yes");
		assertEquals("Great! Enjoy your visit. I know THEY will. Oh, watch out, we have a couple chaos dragonriders exercising our dragons. Don't get in their way!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Farewell. May your days be many and your heart be free.", npc.get("text"));
		// [21:59] green dragon has been killed by kymara
		// [21:59] kymara earns 1750 experience points.
		// [21:59] red dragon has been killed by kymara
		// [21:59] kymara earns 20700 experience points.
		
		// quest slot should now start with done
		assertTrue(player.isQuestCompleted(questSlot));
				
		en.step(player, "hi");
		assertEquals("Greetings, my fellow traveler. What may I do for you?", npc.get("text"));
		en.step(player, "task");
		assertTrue(npc.get("text").equals("I think they've had enough excitement for a while.  Come back in 7 days.") || npc.get("text").equals("I think they've had enough excitement for a while.  Come back in 1 week.") );
		en.step(player, "bye");
		assertEquals("Farewell. May your days be many and your heart be free.", npc.get("text"));
		
		// [22:00] Admin kymara changed your state of the quest 'dragon_lair' from 'done;1219874335035' to 'done;0'
		// [22:00] Changed the state of quest 'dragon_lair' from 'done;1219874335035' to 'done;0'
		
		player.setQuest(questSlot, "done;0");
		en.step(player, "hi");
		assertEquals("Greetings, my fellow traveler. What may I do for you?", npc.get("text"));
		en.step(player, "task");
		assertEquals("Would you like to visit our dragons again?", npc.get("text"));
		en.step(player, "no");
		assertEquals("Ok, but our dragons will be sorry you didn't stop in for a visit.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Farewell. May your days be many and your heart be free.", npc.get("text"));
		
		en.step(player, "hi");
		assertEquals("Greetings, my fellow traveler. What may I do for you?", npc.get("text"));
		en.step(player, "task");
		assertEquals("Would you like to visit our dragon lair?", npc.get("text"));
		en.step(player, "yes");
		assertEquals("Great! Enjoy your visit. I know THEY will. Oh, watch out, we have a couple chaos dragonriders exercising our dragons. Don't get in their way!", npc.get("text"));
		en.step(player, "bye");
		// [22:01] chaos green dragonrider has been killed by kymara
		// [22:01] kymara earns 31900 experience points.
		// [22:01] bone dragon has been killed by kymara
		// [22:01] kymara earns 2210 experience points.
		assertEquals("Farewell. May your days be many and your heart be free.", npc.get("text"));
		
	}
}