package games.stendhal.server.entity.npc.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the NPC conversation parser Expression class.
 *
 * @author Martin Fuchs
 */
public class ExpressionTest {

	@Test
	public final void testMatch() {
		Expression expr1 = ConversationParser.createTriggerExpression("cloak");
		Expression expr2 = ConversationParser.createTriggerExpression("cloaks");
		Expression expr3 = ConversationParser.createTriggerExpression("trousers");

		assertEquals("cloak", expr1.toString());
		assertEquals("|TYPE|cloak", expr2.toString());
		assertEquals("|TYPE|trouser", expr3.toString());

		assertTrue(expr1.matches(expr1));
		assertFalse(expr1.matches(expr2));
		assertFalse(expr1.matches(expr3));

		assertFalse(expr1.matchesNormalized(expr2));
		assertTrue(expr2.matchesNormalized(expr1));
		assertFalse(expr1.matchesNormalized(expr3));
		assertFalse(expr3.matchesNormalized(expr1));

		assertFalse(expr1.matchesNormalizedBeginning(expr2));
		assertTrue(expr2.matchesNormalizedBeginning(expr1));
		assertFalse(expr1.matchesNormalizedBeginning(expr3));
		assertFalse(expr3.matchesNormalizedBeginning(expr1));
	}
	
	@Test
	public final void testEquals() {
		Expression exp = new Expression("blabla");

		// compare with the same object
		assertTrue(exp.equals(exp));

		// check equals() with null parameter
		assertFalse(exp.equals(null));

		// negative equal() tests
		assertFalse(exp.equals("blabla"));

		Object x = "abc";
		Object y = new Expression("abc");
		assertFalse(y.equals(x));
		assertFalse(x.equals(y));

		assertFalse("should not break equals contract", "blabla".equals(exp));

		// positive equals() test
		x = new Expression("abc");
		y = new Expression("abc");
		assertTrue(y.equals(x));
		assertTrue(x.equals(y));
	}

	@Test
	public final void testTriggerMatching() {
		Sentence s1 = ConversationParser.parse("spade");
		Expression e1 = s1.getTriggerExpression();
		Sentence s2 = ConversationParser.parse("a spade");
		Expression e2 = s2.getTriggerExpression();
		assertFalse(s1.hasError());
		assertFalse(s2.hasError());
		assertTrue(e1.matchesNormalized(e2));
		assertTrue(e2.matchesNormalized(e1));
	}

	@Test
	public final void testTypeTriggerMatching() {
		// First show, that "do" without the exactMatching flag matches "done".
		Sentence m1 = ConversationParser.parseForMatching("done");
		assertFalse(m1.hasError());
		assertEquals("do/VER-PAS", m1.toString());
		Expression e1 = m1.getTriggerExpression();

		Sentence s = ConversationParser.parse("do");
		assertFalse(s.hasError());
		Expression e2 = s.getTriggerExpression();
		assertTrue(e2.matchesNormalized(e1));
		assertEquals("do/VER", s.toString());

		// Using the typeMatching flag, it doesn't match any more...
		m1 = ConversationParser.parseForMatching("|TYPE|done/VER-PAS");
		assertFalse(m1.hasError());
		assertEquals("|TYPE|done/VER-PAS", m1.toString());
		e1 = m1.getTriggerExpression();

		assertFalse(e2.matches(e1));
		assertFalse(e2.matchesNormalized(e1));

		// ...but "done" matches the given type string pattern.
		s = ConversationParser.parse("done");
		assertFalse(s.hasError());
		assertEquals("do/VER-PAS", s.toString());
		e2 = s.getTriggerExpression();
		assertTrue(e2.matches(e1));
		assertTrue(e2.matchesNormalized(e1));
	}

	@Test
	public final void testExactTriggerMatching() {
		// First show, that "do" without the exactMatching flag matches "done".
		Sentence m1 = ConversationParser.parseForMatching("done");
		assertFalse(m1.hasError());
		assertEquals("do/VER-PAS", m1.toString());
		Expression e1 = m1.getTriggerExpression();

		Sentence s = ConversationParser.parse("do");
		assertFalse(s.hasError());
		Expression e2 = s.getTriggerExpression();
		assertTrue(e2.matchesNormalized(e1));
		assertEquals("do/VER", s.toString());

		// Using the exactMatching flag, it doesn't match any more...
		m1 = ConversationParser.parseForMatching("|EXACT|dONe");
		assertFalse(m1.hasError());
		assertEquals("|EXACT|dONe", m1.toString());
		e1 = m1.getTriggerExpression();

		assertFalse(e2.matches(e1));
		assertFalse(e2.matchesNormalized(e1));

		// ...but "done" matches the given exact matching pattern.
		s = ConversationParser.parse("dONe");
		assertFalse(s.hasError());
		assertEquals("do/VER-PAS", s.toString());
		e2 = s.getTriggerExpression();
		assertTrue(e2.matches(e1));
		assertTrue(e2.matchesNormalized(e1));
	}

}
