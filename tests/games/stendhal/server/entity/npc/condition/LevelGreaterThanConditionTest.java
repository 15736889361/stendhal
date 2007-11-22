package games.stendhal.server.entity.npc.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.player.Player;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class LevelGreaterThanConditionTest {
	Player level100Player;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		level100Player = PlayerTestHelper.createPlayer();
		level100Player.setLevel(100);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public final void testHashCode() {
		assertEquals(new LevelGreaterThanCondition(101).hashCode(),
				new LevelGreaterThanCondition(101).hashCode());

	}

	@Test
	public final void testFire() {
		assertTrue(new LevelGreaterThanCondition(99).fire(level100Player,
				"greaterthan", null));
		assertFalse(new LevelGreaterThanCondition(100).fire(level100Player,
				"greaterthan", null));
		assertFalse(new LevelGreaterThanCondition(101).fire(level100Player,
				"greaterthan", null));
	}

	@Test
	public final void testLevelGreaterThanCondition() {
		new LevelGreaterThanCondition(0);

	}

	@Test
	public final void testToString() {
		assertEquals("level < 0 ", new LevelGreaterThanCondition(0).toString());
	}

	@Test
	public final void testEqualsObject() {
		assertEquals(new LevelGreaterThanCondition(101),
				new LevelGreaterThanCondition(101));
		assertFalse((new LevelGreaterThanCondition(101)).equals(new LevelGreaterThanCondition(
				102)));
		assertFalse((new LevelGreaterThanCondition(102)).equals(new LevelGreaterThanCondition(
				101)));

	}

}
