package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.city.GreeterNPC;
import games.stendhal.server.maps.semos.tavern.TraderNPC;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class HatForMonogenesTest {

	private SpeakerNPC npc;
	private Engine en;
	private SpeakerNPC npcXin;
	private Engine enXin;
	private AbstractQuest quest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		assertTrue(SingletonRepository.getNPCList().getNPCs().isEmpty());

	}

	@After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().clear();
	}

	@Before
	public void setUp() {
		npc = new SpeakerNPC("Monogenes");
		SingletonRepository.getNPCList().add(npc);
		final SpeakerNPCFactory npcConf = new GreeterNPC();
		npcConf.createDialog(npc);
		en = npc.getEngine();

		final ZoneConfigurator zoneConf = new TraderNPC();
		zoneConf.configureZone(new StendhalRPZone("int_semos_tavern"), null);
		npcXin = SingletonRepository.getNPCList().get("Xin Blanca");
		enXin = npcXin.getEngine();

		quest = new MeetMonogenes();
		quest.addToWorld();
		quest = new HatForMonogenes();
		quest.addToWorld();

	}

	@Test
	public void testQuest() {
		final Player player = PlayerTestHelper.createPlayer("player");
		en.step(player, "hi");
		assertEquals(
				"Hello there, stranger! Don't be too intimidated if people are quiet and reserved... the fear of Blordrough and his forces has spread all over the country, and we're all a bit concerned. I can offer a few tips on socializing though, would you like to hear them?",
				npc.get("text"));
		en.step(player, "no");
		assertEquals(
				"And how are you supposed to know what's happening? By reading the Semos Tribune? Hah! Bye, then.",
				npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Hi again, player. How can I #help you this time?", npc.get("text"));
		en.step(player, "task");
		assertEquals(
				"Could you bring me a #hat to cover my bald head? Brrrrr! The days here in Semos are really getting colder...",
				npc.get("text"));
		en.step(player, "hat");
		assertEquals(
				"You don't know what a hat is?! Anything light that can cover my head; like leather, for instance. Now, will you do it?",
				npc.get("text"));
		en.step(player, "no");
		assertEquals(
				"You surely have more importants things to do, and little time to do them in. I'll just stay here and freeze to death, I guess... *sniff*",
				npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Hi again, player. How can I #help you this time?", npc.get("text"));
		en.step(player, "task");
		assertEquals(
				"Could you bring me a #hat to cover my bald head? Brrrrr! The days here in Semos are really getting colder...",
				npc.get("text"));
		en.step(player, "hat");
		assertEquals(
				"You don't know what a hat is?! Anything light that can cover my head; like leather, for instance. Now, will you do it?",
				npc.get("text"));
		en.step(player, "yes");
		assertEquals("Thanks, my good friend. I'll be waiting here for your return!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));

		// -----------------------------------------------

		final Item item = ItemTestHelper.createItem("money", 25);
		player.getSlot("bag").add(item);
		enXin.step(player, "hi");
		assertEquals("Greetings! How may I help you?", npcXin.get("text"));
		enXin.step(player, "buy leather helmet");
		assertEquals("1 leather helmet will cost 25. Do you want to buy it?", npcXin.get("text"));
		enXin.step(player, "yes");
		assertEquals("Congratulations! Here is your leather helmet!", npcXin.get("text"));
		enXin.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Hey! Is that leather hat for me?", npc.get("text"));
		en.step(player, "no");
		assertEquals("I guess someone more fortunate will get his hat today... *sneeze*", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));
		en.step(player, "hi");
		assertEquals("Hey! Is that leather hat for me?", npc.get("text"));
		npc.remove("text");
		player.drop("leather helmet");
		int oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP, player.getXP());
		assertEquals(null, npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals(
				"Hey, my good friend, remember that leather hat I asked you about before? It's still pretty chilly here...",
				npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));

		// -----------------------------------------------

		player.equip("bag", SingletonRepository.getEntityManager().getItem("leather helmet"));
		en.step(player, "hi");
		assertEquals("Hey! Is that leather hat for me?", npc.get("text"));
		oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP + 50, player.getXP());

		assertEquals("Bless you, my good friend! Now my head will stay nice and warm.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye.", npc.get("text"));
		// (sorry i meant to put it on ground to test if he noticed it went
		// missing, i did, but i forgot i had one on my head too, he took that.)
	}

	@Test
	public void testGetHistory() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		final List<String> history = new ArrayList<String>();
		assertEquals(history, quest.getHistory(player));

		player.setQuest("hat_monogenes", "");
		history.add("FIRST_CHAT");
		history.add("GET_HAT");
		assertEquals(history, quest.getHistory(player));

		player.equip("bag", ItemTestHelper.createItem("leather hat"));
		history.add("GOT_HAT");

		assertEquals(history, quest.getHistory(player));
		player.setQuest("hat_monogenes", "done");
		history.add("DONE");

		assertEquals(history, quest.getHistory(player));

	}

	@Test
	public void testinit() {
		quest.init("bla");
		assertEquals("bla", quest.getName());
	}

}
