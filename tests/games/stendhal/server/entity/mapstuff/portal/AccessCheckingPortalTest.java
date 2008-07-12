package games.stendhal.server.entity.mapstuff.portal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.portal.AccessCheckingPortal.SendMessage;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;

import java.util.Set;

import marauroa.common.Log4J;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.PrivateTextMockingTestPlayer;
import utilities.RPClass.PortalTestHelper;

public class AccessCheckingPortalTest extends PlayerTestHelper {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
		PortalTestHelper.generateRPClasses();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		SingletonRepository.getTurnNotifier().getEventListForDebugging().clear();
		assertTrue(SingletonRepository.getTurnNotifier().getEventListForDebugging().isEmpty());
	}

	@Test
	public final void testAccessCheckingPortal() {
		new MockAccessCheckingPortal();
	}

	@Test
	public final void testOnUsed() {
		final AccessCheckingPortal port = new MockAccessCheckingPortal();
		final Object ref = new Object();
		port.setDestination("zonename", ref);
		final Portal destPort = new Portal();
		destPort.setIdentifier(ref);
		final StendhalRPZone zone = new StendhalRPZone("zonename");
		zone.add(destPort);
		MockStendlRPWorld.get().addRPZone(zone);

		Player player = createPlayer("mayNot");
		assertFalse(port.onUsed(player));

		player = createPlayer("player-may");
		assertTrue(port.onUsed(player));
	}

	@Test
	public final void testIsAllowed() {
		final AccessCheckingPortal port = new MockAccessCheckingPortal();
		Player player = createPlayer("player-may");
		assertTrue(port.isAllowed(player));
		player = createPlayer("mayNot");
		assertFalse(port.isAllowed(player));
	}

	@Test
	public final void testRejected() {
		final AccessCheckingPortal port = new MockAccessCheckingPortal();
		final PrivateTextMockingTestPlayer player = createPrivateTextMockingTestPlayer("mayNot");
		port.rejected(player);
		final Set<TurnListener> bla = SingletonRepository.getTurnNotifier().getEventListForDebugging().get(
				Integer.valueOf(0));
		final TurnListener[] listenerset = new TurnListener[bla.size()];
		bla.toArray(listenerset);
		assertTrue(listenerset[0] instanceof AccessCheckingPortal.SendMessage);
		final SendMessage sm = (SendMessage) listenerset[0];
		sm.onTurnReached(0);
		assertEquals("rejected", player.getPrivateTextString());
	}

	@Test
	public final void testSetRejectedMessage() {
		final AccessCheckingPortal port = new MockAccessCheckingPortal();
		final PrivateTextMockingTestPlayer player = createPrivateTextMockingTestPlayer("mayNot");
		port.setRejectedMessage("setRejectMessage");
		port.rejected(player);
		final Set<TurnListener> bla = SingletonRepository.getTurnNotifier().getEventListForDebugging().get(
				Integer.valueOf(0));
		final TurnListener[] listenerset = new TurnListener[bla.size()];
		bla.toArray(listenerset);
		assertTrue(listenerset[0] instanceof AccessCheckingPortal.SendMessage);
		final SendMessage sm = (SendMessage) listenerset[0];
		sm.onTurnReached(0);
		assertEquals("setRejectMessage", player.getPrivateTextString());
	}

	class MockAccessCheckingPortal extends AccessCheckingPortal {

		public MockAccessCheckingPortal() {
			super("rejected");
		}

		@Override
		protected boolean isAllowed(final RPEntity user) {
			return "player-may".equals(user.getName());
		}

	}
}
