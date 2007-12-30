package games.stendhal.server.entity.npc.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.npc.ConversationParser;
import games.stendhal.server.entity.npc.Sentence;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatCondition;
import games.stendhal.server.entity.player.Player;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class NotConditionTest {

	private final class AlwaysFalseCondition extends ChatCondition {
		@Override
		public boolean fire(Player player, Sentence sentence, SpeakerNPC engine) {
			return false;
		}

		@Override
		public String toString() {
			return "false";
		}
	}

	private AlwaysTrueCondition trueCondition;

	private ChatCondition falsecondition;

	@Before
	public void setUp() throws Exception {
		trueCondition = new AlwaysTrueCondition();
		falsecondition = new AlwaysFalseCondition();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void selftest() throws Exception {
		assertTrue("true",
				trueCondition.fire(PlayerTestHelper.createPlayer("player"),
						ConversationParser.parse("testNotConditionText"),
						SpeakerNPCTestHelper.createSpeakerNPC()));
		assertFalse("false", falsecondition.fire(
				PlayerTestHelper.createPlayer("player"),
				ConversationParser.parse("testNotConditionText"),
				SpeakerNPCTestHelper.createSpeakerNPC()));

	}

	@Test
	public final void testHashCode() {
		NotCondition obj = new NotCondition(trueCondition);
		assertEquals(obj.hashCode(), obj.hashCode());
		assertEquals(new NotCondition(null).hashCode(),
				new NotCondition(null).hashCode());
		assertEquals(new NotCondition(trueCondition).hashCode(),
				new NotCondition(trueCondition).hashCode());

	}

	@Test
	public final void testFire() {
		assertFalse(new NotCondition(trueCondition).fire(
				PlayerTestHelper.createPlayer("player"), ConversationParser.parse("notconditiontest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));
		assertTrue(new NotCondition(falsecondition).fire(
				PlayerTestHelper.createPlayer("player"), ConversationParser.parse("notconditiontest"),
				SpeakerNPCTestHelper.createSpeakerNPC()));
	}

	@Test
	public final void testNotCondition() {
		new NotCondition(trueCondition);
	}

	@Test
	public final void testToString() {
		assertEquals("not <true>", new NotCondition(trueCondition).toString());
		assertEquals("not <false>", new NotCondition(falsecondition).toString());
	}

	@Test
	public void testEquals() throws Throwable {

		assertFalse(new NotCondition(trueCondition).equals(null));

		NotCondition obj = new NotCondition(trueCondition);
		assertTrue(obj.equals(obj));
		assertTrue(new NotCondition(null).equals(new NotCondition(null)));
		assertTrue(new NotCondition(trueCondition).equals(new NotCondition(
				trueCondition)));
		assertFalse(new NotCondition(trueCondition).equals(new NotCondition(
				null)));
		assertFalse(new NotCondition(null).equals(new NotCondition(
				trueCondition)));
		assertFalse(new NotCondition(trueCondition).equals(new Integer(100)));
		assertTrue(new NotCondition(trueCondition).equals(new NotCondition(
				trueCondition) {
		}));
	}

}
