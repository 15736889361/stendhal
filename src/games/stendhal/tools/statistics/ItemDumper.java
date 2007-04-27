package games.stendhal.tools.statistics;

import games.stendhal.server.StendhalPlayerDatabase;
import games.stendhal.server.StendhalRPWorld;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import marauroa.common.Configuration;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.GenericDatabaseException;
import marauroa.server.game.JDBCPlayerDatabase;
import marauroa.server.game.JDBCTransaction;
import marauroa.server.game.Transaction;

/**
 * Dumps the items of all players into a table called items
 *
 * @author hendrik
 */
public class ItemDumper {
	JDBCPlayerDatabase db;
	Transaction trans;
	PreparedStatement ps;
	java.sql.Date date;

	/**
	 * Creates a new ItemDumper
	 *
	 * @param db JDBCPlayerDatabase
	 * @throws GenericDatabaseException if no database connection can be created
	 */
	private ItemDumper(JDBCPlayerDatabase db) throws GenericDatabaseException {
		this.db = db;
		this.trans = db.getTransaction();
	}

	/**
	 * dumps the items
	 *
	 * @throws Exception in case of an unexspected Exception
	 */
	private void dump() throws Exception {
		JDBCPlayerDatabase.RPObjectIterator it = db.iterator(trans);
		String query = "insert into items(datewhen, charname, slotname, itemname, amount) values(?, ?, ?, ?, ?)";
	    date = new java.sql.Date(new java.util.Date().getTime());
		Connection connection = ((JDBCTransaction) trans).getConnection();
		ps = connection.prepareStatement(query);

		while (it.hasNext()) {
			int id = it.next();
			RPObject object = db.loadRPObject(trans, id);
			String name = object.get("name");
			System.out.println(id + " " + name);
			for (RPSlot slot : object.slots()) {
				String slotName = slot.getName();
				for (RPObject item : slot) {
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
	 * logs an item
	 *
	 * @param name     character name
	 * @param slotName slot name
	 * @param item     item name
	 * @throws SQLException in case of a database error
	 */
	private void logItem(String name, String slotName, RPObject item) throws SQLException {
	    String itemName = item.get("name");
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
	 * starts the ItemDumper
	 *
	 * @param args ignored
	 * @throws Exception in case of an unexspected item
	 */
	public static void main(String[] args) throws Exception {
		StendhalRPWorld.get();
		Configuration.setConfigurationFile("marauroa-prod.ini");
		JDBCPlayerDatabase db = (JDBCPlayerDatabase) StendhalPlayerDatabase.resetDatabaseConnection();
		ItemDumper itemDumper = new ItemDumper(db);
		itemDumper.dump();
	}
}
