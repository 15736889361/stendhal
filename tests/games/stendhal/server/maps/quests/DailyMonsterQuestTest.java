package games.stendhal.server.maps.quests;

import static games.stendhal.server.entity.npc.ConversationStates.ATTENDING;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.LevelBasedComparator;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

import java.util.Collections;
import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.PrivateTextMockingTestPlayer;
import utilities.SpeakerNPCTestHelper;
import utilities.RPClass.CreatureTestHelper;

public class DailyMonsterQuestTest {

	private static SpeakerNPC mayor;
	private static DailyMonsterQuest dmq;
	private static Engine en;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		mayor = SpeakerNPCTestHelper.createSpeakerNPC("Mayor Sakhs");
		NPCList.get().add(mayor);
		dmq = new DailyMonsterQuest();
		dmq.init("DMQTest");
		dmq.addToWorld();
		en = mayor.getEngine();
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
	public void testfire() {

		assertThat(en.getCurrentState(), is(ConversationStates.IDLE));
		Player bob = PlayerTestHelper.createPlayer("bob");
		assertFalse(en.step(bob, ""));
		assertThat(en.getCurrentState(), is(ConversationStates.IDLE));

		en.setCurrentState(ATTENDING);
		CreatureTestHelper.generateRPClasses();
		SingletonRepository.getEntityManager().getCreature("rat");
		assertThat(en.getCurrentState(), is(ATTENDING));
		assertTrue(en.step(bob, "quest"));
		assertThat(en.getCurrentState(), is(ATTENDING));
		assertTrue(bob.hasQuest("daily"));
	}
	@Test
	public void testClaimDone() {

		PrivateTextMockingTestPlayer bob = PlayerTestHelper.createPrivateTextMockingTestPlayer("bob");
		en.setCurrentState(ATTENDING);
		CreatureTestHelper.generateRPClasses();
		SingletonRepository.getEntityManager().getCreature("rat");
		assertThat(en.getCurrentState(), is(ATTENDING));
		assertTrue(en.step(bob, "quest"));
		assertThat(en.getCurrentState(), is(ATTENDING));
		assertTrue(bob.hasQuest("daily"));
		assertTrue(en.step(bob, "complete"));
		assertEquals("", bob.getPrivateTextString());
		
	}
	
	
	
	
	@Test
	public void testPickIdealCreature() {
		DailyMonsterQuest dmqp = new DailyMonsterQuest();
		DailyMonsterQuest.DailyQuestAction dmqpick = dmqp.new DailyQuestAction();
		CreatureTestHelper.generateRPClasses();
		assertNull("empty list", dmqpick.pickIdealCreature(-1, false, new LinkedList<Creature>()));
		LinkedList<Creature> creatureList = new LinkedList<Creature>();
		creatureList.add(SingletonRepository.getEntityManager().getCreature("rat"));
		assertThat("1 rat in list", dmqpick.pickIdealCreature(-1, false, creatureList).getName(), is("rat"));
		assertThat("1 rat in list", dmqpick.pickIdealCreature(1000, false, creatureList).getName(), is("rat"));
		creatureList.add(SingletonRepository.getEntityManager().getCreature("balrog"));
		assertThat("rat and balrog in list", dmqpick.pickIdealCreature(-1, false, creatureList).getName(), is("rat"));

	}

	@Test
	public void testPickIdealCreatureratLONGLIST() {
		DailyMonsterQuest dmqp = new DailyMonsterQuest();
		DailyMonsterQuest.DailyQuestAction dmqpick = dmqp.new DailyQuestAction();
		CreatureTestHelper.generateRPClasses();
		LinkedList<Creature> creatureList = new LinkedList<Creature>();
		Creature creat;
		for (int i = 0; i < 3; i++) {
			creat = new Creature();
			creat.setLevel(i);
			creatureList.add(creat);
		}

		for (int i = 10; i < 50; i++) {
			creat = new Creature();
			creat.setLevel(i);
			creatureList.add(creat);
		}
		for (int i = 10; i < 20; i++) {
			creat = new Creature();
			creat.setLevel(i);
			creatureList.add(creat);
		}

		
		for (int i = 80; i < 100; i++) {
			creat = new Creature();
			creat.setLevel(i);
			creatureList.add(creat);
		}
		Collections.sort(creatureList, new LevelBasedComparator());
		for (int level = 0; level < 120; level++) {
			assertThat("1 rat in list", dmqpick.pickIdealCreature(level, false, creatureList).getLevel(),
					lessThanOrEqualTo(level + 5));
		}

	}

}
