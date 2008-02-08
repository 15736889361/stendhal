package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;
import games.stendhal.server.entity.creature.Cat;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.SpeakerNPC;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.RPClass.CatTestHelper;
import utilities.RPClass.SheepTestHelper;

public class GetBaseSpeed {

	@BeforeClass
	public static void setUpClass() {
		PlayerTestHelper.generateCreatureRPClasses();
		CatTestHelper.generateRPClasses();
		SheepTestHelper.generateRPClasses();
	}

	@Test
	public void testgetBaseSpeed() {

		assertEquals(0.2, (new SpeakerNPC("bob")).getBaseSpeed(), 0.001);
		assertEquals(0.0, (new Creature()).getBaseSpeed(), 0.001);
		assertEquals(1.0, (PlayerTestHelper.createPlayer("player")).getBaseSpeed(),
				0.001);
		assertEquals(0.9, (new Cat()).getBaseSpeed(), 0.001);
		assertEquals(0.25, (new Sheep()).getBaseSpeed(), 0.001);

	}

}
