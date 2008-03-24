package games.stendhal.server.entity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.player.Player;

import org.junit.Test;

import utilities.PlayerTestHelper;

public class EntityTest {
	@Test
	public void testnextTo() {
		PlayerTestHelper.generatePlayerRPClasses();
		final Entity en = new Entity() { };
		final Player pl = PlayerTestHelper.createPlayer("player");

		en.setPosition(2, 2);

		pl.setPosition(1, 1);
		assertEquals(1, pl.getX());
		assertEquals(1, pl.getY());

		// The function nextTo(Entity, double step) takes into account the width of both objects.
		assertTrue(en.nextTo(pl, 0.25));

		// The second overload nextTo(int x, int y, double step) can only look at the width of one object.
		assertFalse("Player at (1,1) is NOT next to (2,2)",
				en.nextTo(pl.getX(), pl.getY(), 0.25));
		assertFalse("Player at (1,1) is NOT next to (2,2) with distance 0.5",
				en.nextTo(pl.getX(), pl.getY(), 0.5));
		assertFalse("Player at (1,1) is NOT next to (2,2) with distance 0.75",
				en.nextTo(pl.getX(), pl.getY(), 0.75));
		assertTrue("Player at (1,1) is next to (2,2) with distance 1",
				en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(2, 1);
		assertTrue(en.nextTo(pl, 0.25));
		assertTrue(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(3, 1);
		assertTrue(en.nextTo(pl, 0.25));
		assertTrue(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(1, 0);
		assertFalse(en.nextTo(pl, 0.25));
		assertFalse(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(2, 0);
		assertFalse(en.nextTo(pl, 0.25));
		assertFalse(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(3, 0);
		assertFalse(en.nextTo(pl, 0.25));
		assertFalse(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(1, 2);
		assertTrue(en.nextTo(pl, 0.25));
		assertTrue(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(2, 2);
		assertTrue(en.nextTo(pl, 0.25));
		assertTrue(en.nextTo(pl.getX(), pl.getY(), 1));

		pl.setPosition(3, 2);
		assertTrue(en.nextTo(pl, 0.25));
		assertTrue(en.nextTo(pl.getX(), pl.getY(), 1));

	}

	@Test
	public void testSquaredDistanceonebyone() {
		Entity en = new Entity() {
		};

		en.setPosition(4, 4);
		assertThat("same position", en.squaredDistance(4, 4), is(0.0));
		
		assertThat("next to", en.squaredDistance(4, 3), is(0.0));
		assertThat("next to", en.squaredDistance(4, 5), is(0.0));
		assertThat("next to", en.squaredDistance(3, 3), is(0.0));
		assertThat("next to", en.squaredDistance(3, 4), is(0.0));
		assertThat("next to", en.squaredDistance(3, 5), is(0.0));
		assertThat("next to", en.squaredDistance(5, 3), is(0.0));
		assertThat("next to", en.squaredDistance(5, 4), is(0.0));
		assertThat("next to", en.squaredDistance(5, 5), is(0.0));

		assertThat("one tile between", en.squaredDistance(2, 2), is(2.0));
		assertThat("one tile between", en.squaredDistance(3, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(4, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(5, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 2), is(2.0));
		
		assertThat("one tile between", en.squaredDistance(2, 3), is(1.0));
		assertThat("one tile between", en.squaredDistance(2, 4), is(1.0));
		assertThat("one tile between", en.squaredDistance(2, 5), is(1.0));
		
		assertThat("one tile between", en.squaredDistance(6, 3), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 4), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 5), is(1.0));
		
		assertThat("one tile between", en.squaredDistance(2, 6), is(2.0));
		assertThat("one tile between", en.squaredDistance(3, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(4, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(5, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 6), is(2.0));
	}
	@Test
	public void testSquaredDistanceonebytwo() {
		Entity en = new Entity() { };
		en.setPosition(4, 4);
		en.setSize(2, 1);
		
		assertThat("same position", en.squaredDistance(4, 4), is(0.0));
		
		assertThat("next to", en.squaredDistance(4, 3), is(0.0));
		assertThat("next to", en.squaredDistance(4, 5), is(0.0));
		assertThat("next to", en.squaredDistance(3, 3), is(0.0));
		assertThat("next to", en.squaredDistance(3, 4), is(0.0));
		assertThat("next to", en.squaredDistance(3, 5), is(0.0));
		assertThat("next to", en.squaredDistance(5, 3), is(0.0));
		assertThat("next to", en.squaredDistance(5, 4), is(0.0));
		assertThat("next to", en.squaredDistance(5, 5), is(0.0));

		assertThat("one tile between", en.squaredDistance(2, 2), is(2.0));
		assertThat("one tile between", en.squaredDistance(3, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(4, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(5, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 2), is(1.0));
		assertThat("one tile between", en.squaredDistance(7, 2), is(2.0));

		assertThat("one tile between", en.squaredDistance(2, 3), is(1.0));
		assertThat("one tile between", en.squaredDistance(2, 4), is(1.0));
		assertThat("one tile between", en.squaredDistance(2, 5), is(1.0));
		
		assertThat("one tile between", en.squaredDistance(7, 3), is(1.0));
		assertThat("one tile between", en.squaredDistance(7, 4), is(1.0));
		assertThat("one tile between", en.squaredDistance(7, 5), is(1.0));
		
		assertThat("one tile between", en.squaredDistance(2, 6), is(2.0));
		assertThat("one tile between", en.squaredDistance(3, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(4, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(5, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(6, 6), is(1.0));
		assertThat("one tile between", en.squaredDistance(7, 6), is(2.0));

	}

}
