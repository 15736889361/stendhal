package games.stendhal.server.actions;

import static org.junit.Assert.*;
import games.stendhal.server.entity.player.Player;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class StopActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testOnAction() {

		StopAction sa = new StopAction();
		PlayerTestHelper.generatePlayerRPClasses();
		Player player = new Player(new RPObject()) {
			@Override
			public void stopAttack() {
				stopattack = true;

			}

			@Override
			public void notifyWorldAboutChanges() {
				notify = true;

			}

		};
		RPAction action = new RPAction();
		sa.onAction(player, action);

		assertTrue(notify);
		assertFalse(stopattack);
		action.put("attack", "value");
		notify = false;
		stopattack = false;
		
		sa.onAction(player, action);

		assertTrue(notify);
		assertTrue(stopattack);

	}

	boolean stopattack;
	boolean notify;

}
