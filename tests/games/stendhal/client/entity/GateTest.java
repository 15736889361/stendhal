package games.stendhal.client.entity;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;

import marauroa.common.game.RPObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class GateTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

	@Ignore
	@Test
	public void testAddChangeListener() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFillTargetInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetArea() {
		Gate g = new Gate();
		
		assertEquals(new Rectangle2D.Double(0, 0, 0, 0).toString(), g.getArea().toString());
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertEquals(new Rectangle2D.Double(1, 2, 3, 4), g.getArea());
		
	}

	@Test
	public void testGetAudibleArea() {
		Gate g = new Gate();
		Rectangle2D expected = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
		assertEquals(expected, g.getAudibleArea());
	}

	@Test
	public void testGetEntityClass() {
		Gate g = new Gate();
		assertThat(g.getEntityClass(), is(""));
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getEntityClass(), nullValue());
		object.put("class", "class");
		assertThat(g.getEntityClass(), is("class"));
	}

	@Test
	public void testGetEntitySubClass() {
		Gate g = new Gate();
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getEntitySubClass(), nullValue());
		object.put("subclass", "subclass");
		assertThat(g.getEntitySubClass(), is("subclass"));

	}

	@Test
	public void testGetHeight() {
		Gate g = new Gate();
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getHeight(), is(4.0));
		
	}

	@Test
	public void testGetID() {
		Gate g = new Gate();
		assertThat(g.getID(), nullValue());
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		object.setID(RPObject.INVALID_ID);
		g.initialize(object);
		assertThat(g.getID(), is(object.getID()));
		
	}

	@Test
	public void testGetName() {
		Gate g = new Gate();
		assertThat(g.getName(), is(""));
	}

	@Test
	public void testGetRPObject() {
		Gate g = new Gate();
		assertThat(g.getRPObject(), nullValue());
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getRPObject(), sameInstance(object));

	}

	@Test
	public void testGetResistance() {
		Gate g = new Gate();
		assertThat(g.getResistance(), is(0));
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		object.put("resistance", 100);
		g.initialize(object);
		assertThat(g.getResistance(), is(100));
	}

	@Test
	public void testGetResistanceIEntity() {
		Gate g = new Gate();
		assertThat(g.getResistance(new Entity()), is(0));
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		object.put("resistance", 100);
		g.initialize(object);
		assertThat(g.getResistance(new Entity()), is(100));

	}

	@Test
	public void testGetSlot() {
		Gate g = new Gate();
		assertThat(g.getSlot(null), nullValue());	}

	@Test
	public void testGetTitle() {
		Gate g = new Gate();
		assertThat(g.getTitle(), nullValue());
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		object.put("title", "title");
		g.initialize(object);
		assertThat(g.getTitle(), is("title"));
		
	}

	@Test
	public void testGetType() {
		Gate g = new Gate();
		assertThat(g.getType(), nullValue());
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		object.put("type", "type");
		g.initialize(object);
		assertThat(g.getType(), is("type"));
	}

	@Test
	public void testGetVisibility() {
		Gate g = new Gate();
		assertThat(g.getVisibility(), is(100));

	}

	@Test
	public void testGetWidth() {
		Gate g = new Gate();
		assertThat(g.getEntityClass(), is(""));
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getWidth(), is(3.0));

	}

	@Test
	public void testGetX() {
		Gate g = new Gate();
		assertThat(g.getEntityClass(), is(""));
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getX(), is(1.0));

	}

	@Test
	public void testGetY() {
		Gate g = new Gate();
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getY(), is(2.0));

	}

	@Test
	public void testInitialize() {
		Gate g = new Gate();
		RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 2);
		object.put("width", 3);
		object.put("height", 4);
		g.initialize(object);
		assertThat(g.getX(), is(1.0));
		assertThat(g.getY(), is(2.0));
		assertThat(g.getWidth(), is(3.0));
		assertThat(g.getHeight(), is(4.0));
		assertSame(object, g.getRPObject());
	}

	@Ignore
	@Test
	public void testIsObstacle() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsOnGround() {
		Gate g = new Gate();
		assertTrue(g.isOnGround());
	}

	@Ignore
	@Test
	public void testRemoveChangeListener() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testSetAudibleRange() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		Gate g = new Gate();
		assertThat(g.toString(), startsWith("games.stendhal.client.entity.Gate"));
	}

}
