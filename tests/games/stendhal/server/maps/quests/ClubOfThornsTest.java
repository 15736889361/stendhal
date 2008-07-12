package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ClubOfThornsTest {
	private static final String NPC = "Orc Saman";
	private static final String KEY_NAME = "kotoch prison key";
	private static final String QUEST_NAME = "club_thorns";
	private static final String VICTIM = "mountain orc chief";
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}
	
	@Before
	public void setUp() throws Exception {
	}
	
	@After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().remove(NPC);
	}
	
	@Test
	public final void rejectQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final ClubOfThorns quest = new ClubOfThorns();
		quest.addToWorld();
		final SpeakerNPC npc = quest.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		final double karma = player.getKarma();
		
		// Greetings missing. Jump straight to attending
		en.setCurrentState(ConversationStates.ATTENDING);
		
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Make revenge! Kill de Mountain Orc Chief! unnerstand? ok?", npc.get("text"));
		
		en.stepTest(player, "no");
		assertEquals("Answer to refusal", "Ugg! i want hooman make #task, kill!", npc.get("text"));
		assertEquals("Karma penalty", karma - 6.0, player.getKarma(), 0.01);
	}
	
	@Test
	public final void doQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final ClubOfThorns quest = new ClubOfThorns();
		quest.addToWorld();
		final SpeakerNPC npc = quest.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		double karma = player.getKarma();
		
		// Kill a mountain orc chief to to allow checking the slot gets cleaned
		player.setSoloKill(VICTIM);
		// Greetings missing. Jump straight to attending
		en.setCurrentState(ConversationStates.ATTENDING);
		
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Make revenge! Kill de Mountain Orc Chief! unnerstand? ok?", npc.get("text"));
		
		// test the stuff that should be done at the quest start
		en.stepTest(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertEquals("Take dat key. he in jail. Kill! Denn, say me #kill! Say me #kill!", npc.get("text"));
		assertTrue(player.isEquipped(KEY_NAME));
		assertEquals("player", player.getFirstEquipped(KEY_NAME).getBoundTo());
		assertEquals("Karma bonus for accepting the quest", 
			karma + 6.0, player.getKarma(), 0.01);
		assertEquals("start", player.getQuest(QUEST_NAME));
		assertFalse("Cleaning kill slot", player.hasKilled(VICTIM));
		
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Make revenge! #Kill Mountain Orc Chief!", npc.get("text"));
		
		en.stepTest(player, "kill");
		assertEquals("kill Mountain Orc Chief! Kotoch orcs nid revenge!", npc.get("text"));
		
		// Kill a mountain orc chief
		player.setSoloKill("mountain orc chief");
		// Try restarting the task in the middle
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Make revenge! #Kill Mountain Orc Chief!", npc.get("text"));
		assertTrue("Keeping the kill slot, while the quest is active", player.hasKilled(VICTIM));
		
		// completion and rewards
		karma = player.getKarma();
		en.stepTest(player, "kill");
		assertEquals("Revenge! Good! Take club of hooman blud.", npc.get("text"));
		assertTrue(player.isEquipped("club of thorns"));
		assertEquals("The club is bound", "player", player.getFirstEquipped("club of thorns").getBoundTo());
		assertEquals("Final karma bonus", karma + 10.0, player.getKarma(), 0.01);
		assertEquals("XP", 1000, player.getXP());
		assertEquals("done", player.getQuest(QUEST_NAME));
		
		// don't allow restarting
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Saman has revenged! dis Good!", npc.get("text"));
		assertEquals("done", player.getQuest(QUEST_NAME));
	}
}
