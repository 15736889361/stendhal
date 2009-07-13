package games.stendhal.server.actions.admin;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;

import java.sql.SQLException;

import marauroa.common.game.RPAction;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.DatabaseFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class BanActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DatabaseFactory().initializeDatabase();
		MockStendlRPWorld.get();
	}

	@Test
	public void testPerform() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		AccountDAO accountDAO = DAORegister.get().get(AccountDAO.class);

		BanAction ban = new BanAction();
		Player player = PlayerTestHelper.createPlayer("bob");
		RPAction action = new RPAction();
		action.put("target", player.getName());
		if (!accountDAO.hasPlayer(transaction, player.getName())) {
			accountDAO.addPlayer(transaction, player.getName(), new byte[0], "schnubbel");
		}
		accountDAO.setAccountStatus(transaction, player.getName(), "active");
		assertEquals("active", accountDAO.getAccountStatus(transaction, player.getName()));
		ban.perform(player , action);
		assertEquals("banned", accountDAO.getAccountStatus(transaction, player.getName()));

		// just undo the changes so the next test starts clean
		TransactionPool.get().rollback(transaction);
	}
	
	@Test
	public void testCommandCenterPerform() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		AccountDAO accountDAO = DAORegister.get().get(AccountDAO.class);

		Player player = PlayerTestHelper.createPlayer("bobby");
		Player admin = PlayerTestHelper.createPlayer("admin");
		RPAction action = new RPAction();
		action.put("type", "ban");
		action.put("target", player.getName());
		action.put("reason", "whynot");
		if (!accountDAO.hasPlayer(transaction, player.getName())) {
			accountDAO.addPlayer(transaction, player.getName(), new byte[0], "schnubbel");
		}
		accountDAO.setAccountStatus(transaction, player.getName(), "active");
		if (!accountDAO.hasPlayer(transaction, admin.getName())) {
			accountDAO.addPlayer(transaction, admin.getName(), new byte[0], "schnubbel");
		}
		accountDAO.setAccountStatus(transaction, admin.getName(), "active");

		assertEquals("active", accountDAO.getAccountStatus(transaction, player.getName()));
		assertEquals("active", accountDAO.getAccountStatus(transaction, admin.getName()));
		assertFalse(CommandCenter.execute(admin , action));
		admin.clearEvents();
		admin.setAdminLevel(5000);
		assertTrue(CommandCenter.execute(admin , action));
		assertEquals("banned", accountDAO.getAccountStatus(transaction, player.getName()));
		assertEquals("active", accountDAO.getAccountStatus(transaction, admin.getName()));
		assertFalse(admin.events().isEmpty());
		assertThat(admin.events().get(0).toString(), containsString("[private_text=Attributes of Class(): ")); 
		assertThat(admin.events().get(0).toString(), containsString("[text=You have banned bobby for: whynot]"));
		assertThat(admin.events().get(0).toString(), containsString("[texttype=PRIVMSG]"));

		// just undo the changes so the next test starts clean
		TransactionPool.get().rollback(transaction);
	}
}
