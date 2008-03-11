package games.stendhal.server.maps.semos.plains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test for ExperiencedWarriorNPC: Starkad.
 *
 * @author Christian Schnepf
 */
public class ExperiencedWarriorNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "0_semos_plains_s";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ZonePlayerAndNPCTestImpl.setUpBeforeClass();

		SpeakerNPC npc = new SpeakerNPC("Starkad");
		SingletonRepository.getNPCList().add(npc);

		ExperiencedWarriorNPC npcConf = new ExperiencedWarriorNPC();
		npcConf.createDialog(npc);

		setupZone(ZONE_NAME);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public ExperiencedWarriorNPCTest() {
		super(ZONE_NAME, "Starkad");
	}

	@Test
	public void testHiAndBye() {
		SpeakerNPC npc = getNPC("Starkad");
		Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Greetings! How may I help you?", npc.get("text"));

		assertTrue(en.step(player, "bye"));
		assertEquals("Farewell and godspeed!", npc.get("text"));
	}

	@Test
	public void testQuest() {
		SpeakerNPC npc = getNPC("Starkad");
		Engine en = npc.getEngine();

		//test the basic messages
		assertTrue(en.step(player, "hi"));
		assertEquals("Greetings! How may I help you?", npc.get("text"));

		assertTrue(en.step(player, "job"));
		assertEquals("My job? I'm a well known warrior, strange that you haven't heard of me!", npc.get("text"));

		assertTrue(en.step(player, "quest"));
		assertEquals("Thanks, but I don't need any help at the moment.", npc.get("text"));

		assertTrue(en.step(player, "help"));
		assertEquals("If you want, I can tell you about the #creatures I have encountered.", npc.get("text"));

		assertTrue(en.step(player, "offer"));
		assertEquals("I offer you information on #creatures I've seen for a reasonable fee.", npc.get("text"));

		assertTrue(en.step(player, "creatures"));
		assertEquals("Which creature you would like to hear more about?", npc.get("text"));

		//do a false monster test
		assertTrue(en.step(player, "C-Monster"));
		assertEquals("I have never heard of such a creature! Please tell the name again.", npc.get("text"));

		//test with not having enough cash
		assertTrue(en.step(player, "angel"));
		assertEquals("This information costs 490. Are you still interested?", npc.get("text"));

		assertFalse(player.isEquipped("money", 490));
		assertTrue(en.step(player, "yes"));
		assertEquals("You don't have enough money with you.", npc.get("text"));

		//test with having the cash
		assertTrue(equipWithMoney(player, 490));
		assertTrue(player.isEquipped("money", 490));

		assertTrue(en.step(player, "creatures"));
		assertEquals("Which creature you would like to hear more about?", npc.get("text"));

		assertTrue(en.step(player, "angel"));
		assertEquals("This information costs 490. Are you still interested?", npc.get("text"));

		//lazy assertion since the phrases differ each time.  assume that the npc repeats the creature
		assertTrue(en.step(player, "yes"));
		assertTrue(npc.get("text").toLowerCase().contains("angel"));

		//say goodbye
		assertTrue(en.step(player, "bye"));
		assertEquals("Farewell and godspeed!", npc.get("text"));
	}
}
