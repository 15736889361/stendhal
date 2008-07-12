package games.stendhal.server.actions.chat;

import static org.junit.Assert.assertEquals;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.junit.Test;

import utilities.PlayerTestHelper;

public class AwayActionTest {

	@Test(expected = NullPointerException.class)
	public void testPlayerIsNull() {
		final RPAction action = new RPAction();
		action.put("type", "away");
		final AwayAction aa = new AwayAction();
		aa.onAction(null, action);
	}

	@Test
	public void testOnAction() {
		final Player bob = PlayerTestHelper.createPrivateTextMockingTestPlayer("bob");
		final RPAction action = new RPAction();
		action.put("type", "away");
		final AwayAction aa = new AwayAction();
		aa.onAction(bob, action);
		assertEquals(null, bob.getAwayMessage());
		action.put("message", "bla");
		aa.onAction(bob, action);
		assertEquals("bla", bob.getAwayMessage());
	}

	@Test
	public void testOnInvalidAction() {
		final Player bob = PlayerTestHelper.createPrivateTextMockingTestPlayer("bob");
		bob.clearEvents();
		final RPAction action = new RPAction();
		action.put("type", "bla");
		action.put("message", "bla");
		final AwayAction aa = new AwayAction();
		aa.onAction(bob, action);
		assertEquals(null, bob.getAwayMessage());
	}

}
