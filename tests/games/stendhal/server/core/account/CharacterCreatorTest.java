package games.stendhal.server.core.account;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import marauroa.common.Log4J;
import marauroa.common.game.Result;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class CharacterCreatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void testCreate() throws SQLException {
		cleanDB();

		final CharacterCreator cc = new CharacterCreator("user", "player", null);
		assertEquals(Result.OK_CREATED, cc.create().getResult());
		assertEquals(Result.FAILED_PLAYER_EXISTS, cc.create().getResult());

		cleanDB();
	}

	private void cleanDB() throws SQLException {
		final DBTransaction transaction = TransactionPool.get().beginWork();;
		try {
			transaction.execute("DELETE FROM character_stats where name='player';", null);
			transaction.execute("DELETE rpobject , characters from rpobject , characters where characters.charname = 'player' and characters.object_id = rpobject.object_id;", null);
			TransactionPool.get().commit(transaction);
		} catch (final SQLException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}
}
