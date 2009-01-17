package games.stendhal.server.entity.mapstuff.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject.ID;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.RPClass.FishSourceTestHelper;

public class FishSourceTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendhalRPRuleProcessor.get();

		MockStendlRPWorld.get();
		
	}

	@Test
	public void testOnUsed() {
		FishSourceTestHelper.generateRPClasses();
		final FishSource fs = new FishSource("somefish");
		final Player player = PlayerTestHelper.createPlayer("bob");

		fs.onUsed(player);
		assertEquals("You need a fishing rod for fishing.",
				player.events().get(0).get("text"));
		player.clearEvents();
		final StackableItem fishingRod = new StackableItem("fishing rod", "", "",
				null);
		fishingRod.setQuantity(1);
		fishingRod.setID(new ID(2, "testzone"));
		player.getSlot("bag").add(fishingRod);
		assertTrue(player.isEquipped("fishing rod"));
		fs.onUsed(player);
		assertEquals("You have started fishing.", player.events().get(0).get("text"));
		player.clearEvents();
		fs.onUsed(player);
		assertFalse(player.has("private_text"));
		final Player player2 = PlayerTestHelper.createPlayer("bob");

		player2.getSlot("bag").add(fishingRod);
		fs.onUsed(player2);
		assertEquals("You have started fishing.", player2.events().get(0).get("text"));
	}

}
