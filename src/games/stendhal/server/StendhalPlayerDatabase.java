package games.stendhal.server;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.GenericDatabaseException;
import marauroa.server.game.IPlayerDatabase;
import marauroa.server.game.JDBCPlayerDatabase;
import marauroa.server.game.JDBCTransaction;
import marauroa.server.game.NoDatabaseConfException;
import marauroa.server.game.Transaction;

import org.apache.log4j.Logger;

public class StendhalPlayerDatabase extends JDBCPlayerDatabase {

	private static final Logger logger = Log4J.getLogger(StendhalPlayerDatabase.class);

	private StendhalPlayerDatabase(Properties connInfo) throws NoDatabaseConfException, GenericDatabaseException {
		super(connInfo);
		runDBScript("games/stendhal/server/stendhal_init.sql");
	}

	public static IPlayerDatabase resetDatabaseConnection() throws Exception {
		Configuration conf = Configuration.getConfiguration();
		Properties props = new Properties();

		props.put("jdbc_url", conf.get("jdbc_url"));
		props.put("jdbc_class", conf.get("jdbc_class"));
		props.put("jdbc_user", conf.get("jdbc_user"));
		props.put("jdbc_pwd", conf.get("jdbc_pwd"));
		return new StendhalPlayerDatabase(props);
	}

	@Override
	public boolean hasRPObject(Transaction trans, int id) {
		Log4J.startMethod(logger, "hasRPObject");
		try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();
			String query = "select count(*) as amount from avatars where object_id=" + id;

			logger.debug("hasRPObject is executing query " + query);

			ResultSet result = stmt.executeQuery(query);

			boolean rpObjectExists = false;

			if (result.next()) {
				if (result.getInt("amount") != 0) {
					rpObjectExists = true;
				}
			}

			result.close();
			stmt.close();

			return rpObjectExists;
		} catch (SQLException e) {
			logger.error("error checking if database has RPObject (" + id + ")", e);
			return false;
		} finally {
			Log4J.finishMethod(logger, "hasRPObject");
		}
	}

	@Override
	public RPObject loadRPObject(Transaction trans, int id) throws Exception {
		Connection connection = ((JDBCTransaction) trans).getConnection();
		
		// init rpclasses
		StendhalRPWorld.get();

		String query = "select data from avatars where object_id=" + id;
		logger.debug("storeRPObject is executing query " + query);

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		if (rs.next()) {
			Blob data = rs.getBlob("data");
			InputStream input = data.getBinaryStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			// set read buffer size
			byte[] rb = new byte[1024];
			int ch = 0;
			// process blob
			while ((ch = input.read(rb)) != -1) {
				output.write(rb, 0, ch);
			}
			byte[] content = output.toByteArray();
			input.close();
			output.close();

			ByteArrayInputStream inStream = new ByteArrayInputStream(content);
			InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
			InputSerializer inser = new InputSerializer(szlib);

			rs.close();
			stmt.close();

			RPObject object = (RPObject) inser.readObject(new RPObject());
			object.put("#db_id", id);

			return object;
		}

		rs.close();
		stmt.close();
		return null;
	}

	public synchronized int storeRPObject(Transaction trans, RPObject object) throws SQLException {
		Connection connection = ((JDBCTransaction) trans).getConnection();

		int object_id = -1;

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);

		try {
			object.writeObject(serializer, DetailLevel.FULL);
			out_stream.close();
		} catch (IOException e) {
			logger.error("Problem while serializing rpobject: " + object, e);
			throw new SQLException("Problem while serializing rpobject");
		}
		byte[] content = array.toByteArray();

		// setup stream for blob
		ByteArrayInputStream inStream = new ByteArrayInputStream(content);

		String objectid = null;

		if (object.has("#db_id")) {
			objectid = object.get("#db_id");
			object_id = object.getInt("#db_id");
		}

		String name = null;
		if (object.has("name")) {
			name = object.get("name");
		}

		String outfit = "0";
		if (object.has("outfit_org")) {
			outfit = object.get("outfit_org");
		} else if (object.has("outfit")) {
			outfit = object.get("outfit");
		}

		int level = 0;
		if (object.has("level")) {
			level = object.getInt("level");
		}

		int xp = 0;
		if (object.has("xp")) {
			xp = object.getInt("xp");
		}

		String query;

		if ((objectid != null) && hasRPObject(trans, object_id)) {
			query = "update avatars set name='" + name + "',outfit='" + outfit + "',level=" + level + ",xp=" + xp
			        + ",data=? where object_id=" + objectid;
		} else {
			query = "insert into avatars(object_id,name,outfit,level,xp,data) values(" + objectid + ",'" + name + "','"
			        + outfit + "'," + level + "," + xp + ",?)";
		}
		logger.debug("storeRPObject is executing query " + query);

		PreparedStatement ps = connection.prepareStatement(query);
		ps.setBinaryStream(1, inStream, inStream.available());
		ps.executeUpdate();
		ps.close();

		// If object is new, get the objectid we gave it.
		if (objectid == null) {
			Statement stmt = connection.createStatement();
			query = "select LAST_INSERT_ID() as inserted_id from avatars";
			logger.debug("storeRPObject is executing query " + query);
			ResultSet result = stmt.executeQuery(query);

			result.next();
			object_id = result.getInt("inserted_id");

			stmt.close();
		}

		return object_id;
	}

	private static IPlayerDatabase playerDatabase = null;

	/**
	 * This method returns an instance of PlayerDatabase
	 * 
	 * @return A shared instance of PlayerDatabase
	 */
	public static IPlayerDatabase getDatabase() throws NoDatabaseConfException {
		Log4J.startMethod(logger, "getDatabase");
		try {
			if (playerDatabase == null) {
				logger.info("Starting Stendhal JDBC Database");
				playerDatabase = resetDatabaseConnection();
			}

			return playerDatabase;
		} catch (Exception e) {
			logger.error("cannot get database connection", e);
			throw new NoDatabaseConfException(e);
		} finally {
			Log4J.finishMethod(logger, "getDatabase");
		}
	}

	@Override
	public RPObjectIterator iterator(Transaction trans) {
		Log4J.startMethod(logger, "iterator");
		try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();
			String query = "select object_id from avatars";

			logger.debug("iterator is executing query " + query);
			ResultSet result = stmt.executeQuery(query);
			return new RPObjectIterator(result);
		} catch (SQLException e) {
			logger.warn("error executing query", e);
			return null;
		} finally {
			Log4J.finishMethod(logger, "iterator");
		}
	}

	/**
	 * Returns the points in the specified hall of fame
	 *
	 * @param trans      Transaction
	 * @param playername name of the player
	 * @param fametype   type of the hall of fame
	 * @return points or 0 in case there is no entry
	 * @throws GenericDatabaseException in case of an database error
	 */
	public int getHallOfFamePoints(Transaction trans, String playername, String fametype)
	        throws GenericDatabaseException {
		Log4J.startMethod(logger, "addStatisticsEvent");
		int res = 0;
		try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();

			String query = "SELECT points FROM halloffame WHERE charname='" + escapeSQLString(playername)
			        + "' AND fametype='" + escapeSQLString(fametype) + "'";
			ResultSet result = stmt.executeQuery(query);
			if (result.next()) {
				res = result.getInt("points");
			}
			result.close();
			stmt.close();
		} catch (SQLException sqle) {
			logger.warn("error reading hall of fame", sqle);
			throw new GenericDatabaseException(sqle);
		} finally {
			Log4J.finishMethod(logger, "addStatisticsEvent");
		}
		return res;
	}

	/**
	 * Stores an entry in the hall of fame
	 *
	 * @param trans      Transaction
	 * @param playername name of the player
	 * @param fametype   type of the hall of fame
	 * @param points     points to store
	 * @throws GenericDatabaseException in case of an database error
	 */
	public void setHallOfFamePoints(Transaction trans, String playername, String fametype, int points)
	        throws GenericDatabaseException {
		Log4J.startMethod(logger, "addStatisticsEvent");
		try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();

			// first try an update
			String query = "UPDATE halloffame SET points='" + escapeSQLString(Integer.toString(points))
			        + "' WHERE charname='" + escapeSQLString(playername) + "' AND fametype='"
			        + escapeSQLString(fametype) + "';";
			int count = stmt.executeUpdate(query);
			if (count == 0) {
				// no row was modified, so we need to do an insert
				query = "INSERT INTO halloffame (charname, fametype, points) VALUES ('" + escapeSQLString(playername)
				        + "','" + escapeSQLString(fametype) + "','" + escapeSQLString(Integer.toString(points)) + "');";
				stmt.executeUpdate(query);
			}
			stmt.close();
		} catch (SQLException sqle) {
			logger.warn("error adding game event", sqle);
			throw new GenericDatabaseException(sqle);
		} finally {
			Log4J.finishMethod(logger, "addStatisticsEvent");
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("PORTING 'new' AVATARS system back to RPOBJECT, RPATTRIBUTE and RPSLOT tables");
		System.out.println();
		Configuration.setConfigurationFile("marauroa.ini");
		JDBCPlayerDatabase odb = (JDBCPlayerDatabase) StendhalPlayerDatabase.resetDatabaseConnection();

		JDBCPlayerDatabase sdb = (JDBCPlayerDatabase) JDBCPlayerDatabase.getDatabase(); 

		Transaction transA = odb.getTransaction();
		Transaction transB = sdb.getTransaction();

		JDBCPlayerDatabase.RPObjectIterator it = odb.iterator(transA);

		while (it.hasNext()) {
			int id = it.next();

			long p1 = System.currentTimeMillis();
			RPObject object = odb.loadRPObject(transA, id);
			System.out.println("Porting: " + object.get("name"));

			long p2 = System.currentTimeMillis();
			sdb.storeRPObject(transB, object);
			transB.commit();
			long p3 = System.currentTimeMillis();

			System.out.println("Times LOAD(" + (p2 - p1) / 1000.0 + ")\tSTORE(" + (p3 - p2) / 1000.0 + ")");
		}
	}


	/**
	 * Cleans the old chat log entries.
	 */
	public void cleanChatLog(Transaction trans) {
		/*try {
			Connection connection = ((JDBCTransaction) trans).getConnection();
			Statement stmt = connection.createStatement();
			logger.info("cleaning chat log");
			stmt.executeUpdate("UPDATE gameEvents SET param1=null, param2=null WHERE param2 IS NOT NULL AND event='chat' AND timedate < DATE_SUB(CURDATE(), INTERVAL 2 DAY);");
			stmt.close();
		} catch (SQLException e) {
			logger.error(e, e);
		}*/
	}
}
