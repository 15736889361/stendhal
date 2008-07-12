package games.stendhal.tools.statistics;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalPlayerDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import marauroa.common.Configuration;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.Transaction;

/**
 * Dumps the items of all players into a table called items.
 * 
 * @author hendrik
 */
public class ItemDumper {
	StendhalPlayerDatabase db;

	Transaction trans;

	PreparedStatement ps;

	java.sql.Date date;

	/**
	 * Creates a new ItemDumper.
	 * 
	 * @param db
	 *            JDBCPlayerDatabase
	 */
	private ItemDumper(final StendhalPlayerDatabase db) {
		this.db = db;
		this.trans = db.getTransaction();
	}

	/**
	 * dumps the items.
	 * 
	 * @throws Exception
	 *             in case of an unexpected Exception
	 */
	private void dump() throws Exception {
		final String query = "insert into items(datewhen, charname, slotname, itemname, amount) values(?, ?, ?, ?, ?)";
		date = new java.sql.Date(new java.util.Date().getTime());
		final Connection connection =  trans.getConnection();
		ps = connection.prepareStatement(query);

		for (final RPObject object : db) {
			final String name = object.get("name");
			final int id = object.getInt("id");
			System.out.println(id + " " + name);
			for (final RPSlot slot : object.slots()) {
				final String slotName = slot.getName();
				for (final RPObject item : slot) {
					if (item.has("type") && item.get("type").equals("item")) {
						logItem(name, slotName, item);
					}
				}
			}
		}

		ps.close();
		trans.commit();
	}

	/**
	 * logs an item.
	 * 
	 * @param name
	 *            character name
	 * @param slotName
	 *            slot name
	 * @param item
	 *            item name
	 * @throws SQLException
	 *             in case of a database error
	 */
	private void logItem(final String name, final String slotName, final RPObject item)
			throws SQLException {
		final String itemName = item.get("name");
		int quantity = 1;
		if (item.has("quantity")) {
			quantity = item.getInt("quantity");
		}
		ps.setDate(1, date);
		ps.setString(2, name);
		ps.setString(3, slotName);
		ps.setString(4, itemName);
		ps.setInt(5, quantity);
		ps.executeUpdate();
	}

	/**
	 * starts the ItemDumper.
	 * 
	 * @param args
	 *            ignored
	 * @throws Exception
	 *             in case of an unexpected item
	 */
	public static void main(final String[] args) throws Exception {
		SingletonRepository.getRPWorld();
		Configuration.setConfigurationFile("marauroa-prod.ini");
		final StendhalPlayerDatabase db = (StendhalPlayerDatabase) StendhalPlayerDatabase.newConnection();
		final ItemDumper itemDumper = new ItemDumper(db);
		itemDumper.dump();
	}
}
