package games.stendhal.client.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import marauroa.common.game.RPObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PlayerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public final void testOnEnter() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnChangedAdded() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetArea() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetDrawedArea() {
		fail("Not yet implemented"); // TODO
	}

	@Test

	public final void testBuildOfferedActions() {
		RPObject rpo = new RPObject();
		rpo.put("type", "player");
		rpo.put("outfit",0);
		Player pl =new Player();
		pl.init(rpo);
		List<String> expected = new ArrayList<String>();
		expected.add("Look");
		expected.add("Attack");
		expected.add("Add to Buddies");
		ArrayList<String> list = new ArrayList<String>();
		pl.buildOfferedActions(list);
		Assert.assertNotNull(list);
		Assert.assertEquals(expected, list);
	}

	@Test
	@Ignore
	public final void testOnAction() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testBuildAnimations() {
		fail("Not yet implemented"); // TODO
	}

	





	@Test
	public final void testGetHearingArea() {
		RPObject rpo = new RPObject();
		rpo.put("type", "player");
		rpo.put("outfit",0);
		User pl = new User();
		pl.init(rpo);
		Rectangle2D rect = pl.getHearingArea();
		assertEquals(new Rectangle2D.Double(-20.0, -20.0, 40, 40), rect);
		pl.setAudibleRange(4);
		assertEquals(new Rectangle2D.Double(-20.0, -20.0, 40, 40), rect);
	}

}
