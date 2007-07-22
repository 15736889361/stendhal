/**
 * 
 */
package games.stendhal.tools.port1_2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import marauroa.common.Configuration;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.JDBCTransaction;
import marauroa.server.game.db.Transaction;

import org.apache.log4j.Logger;

/**
 * Converts the table structure from Marauroa 1.0 to the blob-field used in Marauroa 2.0.
 * Note: It cannot handle the blob-field of Stendhal &lt; 0.70. You have to execute
 * StendhalPlayerDatabase.main() from the lates 0.6x CVS Branch to convert the Stendhal blob
 * back to the marauroa 1.0 structure.
 */

// Note this class contains lots of code which was simply copied from Stendhal <= 0.61 and Marauroa 1.x

public class TablesToBlob {
	static Logger logger = Logger.getLogger(TablesToBlob.class);
	private String oldDBName = "marauroa";

	/**
	 * Creates a new TablesToBlob object
	 *
	 * @param oldbDBName the name of the old database
	 */
	public TablesToBlob(String oldbDBName) {
		this.oldDBName = oldbDBName;
	}

	/**
	 * Loads an object from the old database structure
	 *
	 * @param trans     Transaction
	 * @param object    RPObject to write the data into
	 * @param object_id id of the object to load
	 * @throws SQLException in case of an database exception
	 */
	private void loadRPObject(Transaction trans, RPObject object, int object_id)
			throws SQLException {
		Connection connection = ((JDBCTransaction) trans).getConnection();
		Statement stmt = connection.createStatement();
		String query = "select name,value from " + oldDBName + ".rpattribute where object_id="
				+ object_id + ";";
		logger.debug("loadRPObject is executing query " + query);

		ResultSet result = stmt.executeQuery(query);

		while (result.next()) {
			String name = result.getString("name");
			String value = result.getString("value");
			object.put(name, value);
		}

		result.close();

		query = "select name,capacity, slot_id from " + oldDBName + ".rpslot where object_id="
				+ object_id + ";";
		logger.debug("loadRPObject is executing query " + query);
		result = stmt.executeQuery(query);
		while (result.next()) {
			RPSlot slot = new RPSlot(result.getString("name"));

			object.addSlot(slot);

			int slot_id = result.getInt("slot_id");

			query = "select object_id from " + oldDBName + ".rpobject where slot_id=" + slot_id
					+ ";";
			logger.debug("loadRPObject is executing query " + query);
			ResultSet resultSlot = connection.createStatement().executeQuery(
					query);

			while (resultSlot.next()) {
				RPObject slotObject = new RPObject();

				loadRPObject(trans, slotObject, resultSlot.getInt("object_id"));
				slot.add(slotObject);
			}

			resultSlot.close();
		}

		result.close();
		stmt.close();
	}
	
	/**
	 * Loads an object from the old database structure
	 *
	 * @param trans     Transaction
	 * @param id id of the object to load
	 * @throws SQLException in case of an database exception
	 */
	private RPObject loadRPObject(Transaction trans, int id) throws SQLException {
		RPObject object = new RPObject();

		loadRPObject(trans, object, id);

		return object;
	}

	/**
	 * An interator which returns a list of all players
	 *
	 * @author hendrik
	 */
	public static class RPObjectIterator {
		private ResultSet set;

		public RPObjectIterator(ResultSet set) {
			this.set = set;
		}

		public boolean hasNext() {
			try {
				return set.next();
			} catch (SQLException e) {
				return false;
			}
		}

		public int next() throws SQLException {
			return set.getInt("object_id");
		}

		@Override
		public void finalize() {
			try {
				set.close();
			} catch (SQLException e) {
				logger.error("Finalize RPObjectIterator: ", e);
			}
		}
	}

	/**
	 * Returns an interator for all old players
	 *
	 * @param trans Transaction
	 * @return iterator over all old players
	 */
	public RPObjectIterator listOldPlayers(Transaction trans) {
		try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();
			String query = "select object_id from "+ oldDBName + ".rpobject where slot_id=0";

			logger.debug("iterator is executing query " + query);
			ResultSet result = stmt.executeQuery(query);
			return new RPObjectIterator(result);
		} catch (SQLException e) {
			logger.warn("error executing query", e);
			return null;
		}
	}
	
	/**
	 * Starts the transformation from the command line
	 *
	 * @param args the name of the old database
	 * @throws Exception in case of an unexspected error
	 */
	public static void main(String[] args) throws Exception {
		System.err.println("WARNING: THIS IS JUST A DIRTY, UNFINISHED, UNDOCUMENTED HACK");
		TablesToBlob t2b = new TablesToBlob(args[0]);
		t2b.transformation();
	}

	/**
	 * Starts the transformation
	 *
	 * @throws Exception in case of an unexspected error
	 */
	public void transformation() throws Exception {
		
		System.out.println("PORTING RPOBJECT, RPATTRIBUTE and RPSLOT tables from Marauroa 1.0 to object_data of Marauroa 2.0");
		System.out.println();
		Configuration.setConfigurationFile("server.ini");

		JDBCDatabase db = JDBCDatabase.getDatabase(); 

		Transaction trans = db.getTransaction();

		RPObjectIterator it = listOldPlayers(trans);

		while (it.hasNext()) {
			int id = it.next();

			long p1 = System.currentTimeMillis();
			RPObject object = this.loadRPObject(trans, id);
			System.out.println("Porting: " + object.get("name"));

			long p2 = System.currentTimeMillis();
			db.addCharacter(trans, object.get("name"), object.get("name"), object);
			trans.commit();
			long p3 = System.currentTimeMillis();

			System.out.println("Times LOAD(" + (p2 - p1) / 1000.0 + ")\tSTORE(" + (p3 - p2) / 1000.0 + ")");
		}
	}
}
