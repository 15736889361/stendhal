package games.stendhal.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class GagManagerTest {

	@BeforeClass
	public static void setUp() throws Exception {
		Entity.generateRPClass();
		ActiveEntity.generateRPClass();
		RPEntity.generateRPClass();
		Player.generateRPClass();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGagAbsentPlayer() {
		Player policeman = PlayerTestHelper.createPlayer();
		Player bob = PlayerTestHelper.createPlayer("bob");

		GagManager.get().gag("bob", policeman, 1, "test");
		assertEquals("Player bob not found", policeman.get("private_text"));
		assertFalse(GagManager.isGagged(bob));
	}

	@Test
	public final void testGagPlayer() {
		Player policeman = PlayerTestHelper.createPlayer();
		Player bob = PlayerTestHelper.createPlayer("bob");
		GagManager.get().gag(bob, policeman, 1, "test", bob.getName());
		assertEquals("You have gagged bob for 1 minutes. Reason: test.",
				policeman.get("private_text"));
		assertTrue(GagManager.isGagged(bob));
		GagManager.get().release(bob);
		assertFalse(GagManager.isGagged(bob));
	}

	@Test
	public final void testnegativ() {
		Player policeman = PlayerTestHelper.createPlayer();
		Player bob = PlayerTestHelper.createPlayer("bob");
		assertEquals(null, policeman.get("private_text"));
		GagManager.get().gag(bob, policeman, -1, "test", bob.getName());
		assertEquals("Infinity (negative numbers) is not supported.",
				policeman.get("private_text"));
		assertFalse(GagManager.isGagged(bob));
	}

	@Test
	public final void testOnLoggedIn() {
		Player policeman = PlayerTestHelper.createPlayer();
		Player bob =PlayerTestHelper.createPlayer("bob");;


		GagManager.get().gag(bob, policeman, 1, "test", bob.getName());
		assertEquals("You have gagged bob for 1 minutes. Reason: test.",
				policeman.get("private_text"));
		assertTrue(GagManager.isGagged(bob));
		GagManager.get().onLoggedIn(bob);
		assertTrue(GagManager.isGagged(bob));
		bob.setQuest("gag", "0");
		GagManager.get().onLoggedIn(bob);
		assertFalse(GagManager.isGagged(bob));
	}

	@Test
	public final void testgetTimeremaining() {
		Player bob = PlayerTestHelper.createPlayer();
		assertEquals(0L, GagManager.get().getTimeRemaining(bob));

	}
}
