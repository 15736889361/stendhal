package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.blacksmith.BlacksmithAssistantNPC;
import games.stendhal.server.maps.semos.tavern.TraderNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class NewsFromHackimTest {

	private Player player = null;
	private SpeakerNPC npcHackim = null;
	private Engine enHackim = null;
	private SpeakerNPC npcXin = null;
	private Engine enXin = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		npcHackim = new SpeakerNPC("Hackim Easso");
		SingletonRepository.getNPCList().add(npcHackim);
		final SpeakerNPCFactory npcConf = new BlacksmithAssistantNPC();
		npcConf.createDialog(npcHackim);
		enHackim = npcHackim.getEngine();

		final ZoneConfigurator zoneConf = new TraderNPC();
		zoneConf.configureZone(new StendhalRPZone("int_semos_tavern"), null);
		npcXin = SingletonRepository.getNPCList().get("Xin Blanca");
		enXin = npcXin.getEngine();

		final AbstractQuest quest = new NewsFromHackim();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		enHackim.step(player, "hi");
		assertEquals("Hi stranger, I'm Hackim Easso, the blacksmith's assistant. Have you come here to buy weapons?", npcHackim.get("text"));
		enHackim.step(player, "bye");
		assertEquals("Bye.", npcHackim.get("text"));

		// -----------------------------------------------

		enHackim.step(player, "hi");
		assertEquals("Hi again, player. How can I #help you this time?", npcHackim.get("text"));
		enHackim.step(player, "task");
		assertEquals("Pssst! C'mere... do me a favour and tell #Xin #Blanca that the new supply of weapons is ready, will you?", npcHackim.get("text"));
		enHackim.step(player, "Xin");
		assertEquals("You don't know who Xin is? Everybody at the tavern knows Xin. He's the guy who owes beer money to most of the people in Semos! So, will you do it?", npcHackim.get("text"));
		enHackim.step(player, "yes");
		assertEquals("Thanks! I'm sure that #Xin will reward you generously. Let me know if you need anything else.", npcHackim.get("text"));
		enHackim.step(player, "bye");
		assertEquals("Bye.", npcHackim.get("text"));

		// -----------------------------------------------

		enXin.step(player, "hi");
		assertEquals("Ah, it's ready at last! That is very good news indeed! Here, let me give you a little something for your help... Take this set of brand new leather leg armour! Let me know if you want anything else.", npcXin.get("text"));
		// [22:38] rosie earns 10 experience points.
		enXin.step(player, "task");
		assertEquals("Talk to Hackim Easso in the smithy, he might want you.", npcXin.get("text"));

		// -----------------------------------------------

		enHackim.step(player, "hi");
		assertEquals("Hi again, player. How can I #help you this time?", npcHackim.get("text"));
		enHackim.step(player, "task");
		assertEquals("Thanks, but I don't have any messages to pass on to #Xin. I can't smuggle so often now... I think Xoderos is beginning to suspect something. Anyway, let me know if there's anything else I can do.", npcHackim.get("text"));
		enHackim.step(player, "bye");
		assertEquals("Bye.", npcHackim.get("text"));
	}
}
