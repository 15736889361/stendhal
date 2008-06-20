package games.stendhal.server.entity.mapstuff.office;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.maps.MockStendlRPWorld;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class testStoreableEntityListTest {

	private final class EntityExtension extends Entity {
		@Override
		public String getTitle() {
			return "testentity";
		}

		@Override
		public void onRemoved(StendhalRPZone zone) {
			
			removecounter++;
			super.onRemoved(zone);
		}
	}

	private int removecounter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MockStendlRPWorld.reset();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRemoveByName() {
		StendhalRPZone zone = new StendhalRPZone("name") {
			@Override
			public void storeToDatabase() {
				// do nothing
			}
		};
		StoreableEntityList<Entity> storelist = new StoreableEntityList<Entity>(zone, Entity.class) {

			@Override
			protected String getName(Entity entity) {
				return entity.getTitle();
			}
		};
		Entity ent = new EntityExtension();
		Entity ent2 = new EntityExtension();
		assertTrue(storelist.getList().isEmpty());
		storelist.add(ent);
		assertFalse(storelist.getList().isEmpty());
		assertThat(storelist.getList().size(), is(1));
		storelist.add(ent2);
		assertThat(storelist.getList().size(), is(2));
		storelist.removeByName(ent.getTitle());
		assertThat(removecounter, is(2));
		assertThat(storelist.getList().size(), is(0));
		
		assertThat("removebyname() removes all instances", storelist.getList().size(), is(0));
	}

}
