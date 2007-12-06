package games.stendhal.server.entity.npc.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.npc.ConversationParser;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Log4J;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class QuestNotInStateConditionTest {
	private static final String QUESTNAME = "questname";

	@BeforeClass
	public static void setUpClass() throws Exception {
		Log4J.init();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testFire() {
		String validState = "valid";
		assertTrue(new QuestNotInStateCondition(QUESTNAME, validState).fire(
				PlayerTestHelper.createPlayer(),
				ConversationParser.parse("QuestNotInStateConditionTest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));
		Player bob = PlayerTestHelper.createPlayer();

		bob.setQuest(QUESTNAME, "valid");
		assertFalse(new QuestNotInStateCondition(QUESTNAME, validState).fire(
				bob,
				ConversationParser.parse("QuestNotInStateConditionTest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));

		bob.setQuest(QUESTNAME, "");
		assertTrue(new QuestNotInStateCondition(QUESTNAME, validState).fire(
				bob,
				ConversationParser.parse("QuestNotInStateConditionTest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));

		bob.setQuest(QUESTNAME, null);
		assertTrue(new QuestNotInStateCondition(QUESTNAME, validState).fire(
				bob,
				ConversationParser.parse("QuestNotInStateConditionTest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));

		bob.setQuest(QUESTNAME, "done");
		assertTrue(new QuestNotInStateCondition(QUESTNAME, validState).fire(
				bob,
				ConversationParser.parse("QuestNotInStateConditionTest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));

	}

	@Test
	public final void testQuestNotInStateCondition() {
		new QuestNotInStateCondition(QUESTNAME, "");
	}

	@Test
	public final void testToString() {
		assertEquals(
				"QuestNotInState <questname,testToString>",
				new QuestNotInStateCondition(QUESTNAME, "testToString").toString());
	}

	@Test
	public void testEquals() throws Throwable {
		String state = "state";
		assertFalse(new QuestNotInStateCondition(QUESTNAME, state).equals(null));

		QuestNotInStateCondition obj = new QuestNotInStateCondition(QUESTNAME,
				state);
		assertTrue(obj.equals(obj));
		assertTrue(new QuestNotInStateCondition(QUESTNAME, state).equals(new QuestNotInStateCondition(
				QUESTNAME, state)));
		assertTrue(new QuestNotInStateCondition(null, null).equals(new QuestNotInStateCondition(
				null, null)));
		assertTrue(new QuestNotInStateCondition(null, state).equals(new QuestNotInStateCondition(
				null, state)));
		assertTrue(new QuestNotInStateCondition(QUESTNAME, null).equals(new QuestNotInStateCondition(
				QUESTNAME, null)));

		assertFalse(new QuestNotInStateCondition(QUESTNAME, state).equals(new QuestNotInStateCondition(
				QUESTNAME, state + "2")));

		assertFalse(new QuestNotInStateCondition(QUESTNAME, state).equals(new Object()));
		assertFalse(new QuestNotInStateCondition(null, state).equals(new QuestNotInStateCondition(
				QUESTNAME, state)));
		assertFalse(new QuestNotInStateCondition(QUESTNAME, state).equals(new QuestNotInStateCondition(
				null, state)));

		assertFalse(new QuestNotInStateCondition(QUESTNAME, null).equals(new QuestNotInStateCondition(
				QUESTNAME, state)));
		assertFalse(new QuestNotInStateCondition(QUESTNAME, state).equals(new QuestNotInStateCondition(
				QUESTNAME, null)));

		assertTrue(new QuestNotInStateCondition(QUESTNAME, state).equals(new QuestNotInStateCondition(
				QUESTNAME, state) {
		}));
	}

	@Test
	public void testhashcode() throws Throwable {
		String state = "state";
		QuestNotInStateCondition obj = new QuestNotInStateCondition(QUESTNAME,
				state);

		assertEquals(obj.hashCode(), obj.hashCode());
		assertEquals(new QuestNotInStateCondition(QUESTNAME, state).hashCode(),
				new QuestNotInStateCondition(QUESTNAME, state).hashCode());
		assertEquals(new QuestNotInStateCondition(null, null).hashCode(),
				new QuestNotInStateCondition(null, null).hashCode());
		assertEquals(new QuestNotInStateCondition(null, state).hashCode(),
				new QuestNotInStateCondition(null, state).hashCode());
		assertEquals(new QuestNotInStateCondition(QUESTNAME, null).hashCode(),
				new QuestNotInStateCondition(QUESTNAME, null).hashCode());
	}

}
