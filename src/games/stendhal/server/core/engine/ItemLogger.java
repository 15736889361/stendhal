package games.stendhal.server.core.engine;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PassiveEntity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.StringChecker;
import marauroa.server.game.db.Transaction;

/**
 * Item Logger.
 *
 * @author hendrik
 */
public class ItemLogger {
	
	private static final String ATTR_ITEM_LOGID = "logid";
	private static final Logger logger = Logger.getLogger(ItemLogger.class);
	

	private String getQuantity(final RPObject item) {
		int quantity = 1;
		if (item.has("quantity")) {
			quantity = item.getInt("quantity");
		}
		return Integer.toString(quantity);
	}

	public void loadOnLogin(final Player player, final RPSlot slot, final Item item) {
		if (item.has(ATTR_ITEM_LOGID)) {
			return;
		}
		itemLog(item, player, "create", item.get("name"), getQuantity(item), "olditem", slot.getName());
	}

	public void destroyOnLogin(final Player player, final RPSlot slot, final RPObject item) {
		itemLog(item, player, "destroy", item.get("name"), getQuantity(item), "on login", slot.getName());
    }

	public void destroy(final RPEntity entity, final RPSlot slot, final RPObject item) {
		itemLog(item, entity, "destroy", item.get("name"), getQuantity(item), "quest", slot.getName());
    }

	public void dropQuest(final Player player, final Item item) {
		itemLog(item, player, "destroy", item.get("name"), getQuantity(item), "quest", null);
    }

	public void timeout(final Item item) {
		itemLog(item, null, "destroy", item.get("name"), getQuantity(item), "timeout", item.getZone().getID().getID() + " " + item.getX() + " " + item.getY());
    }

	public void displace(final Player player, final PassiveEntity item, final StendhalRPZone zone, final int x, final int y) {
		itemLog(item, player, "ground-to-ground", zone.getID().getID(), item.getX() + " " + item.getY(), zone.getID().getID(), x + " " + y);
    }

	public void equipAction(final Player player, final Entity entity, final String[] sourceInfo, final String[] destInfo) {
	    itemLog(entity, player, sourceInfo[0] + "-to-" + destInfo[0], sourceInfo[1], sourceInfo[2], destInfo[1], destInfo[2]);
    }

	public void merge(final RPEntity entity, final Item oldItem, final Item outlivingItem) {
		if (!(entity instanceof Player)) {
			return;
		}
		final Player player = (Player) entity;

		itemLogAssignIDIfNotPresent(oldItem, outlivingItem);
		final String oldQuantity = getQuantity(oldItem);
		final String oldOutlivingQuantity = getQuantity(outlivingItem);
		final String newQuantity = Integer.toString(Integer.parseInt(oldQuantity) + Integer.parseInt(oldOutlivingQuantity));
	    itemLog(oldItem, player, "merge in", outlivingItem.get(ATTR_ITEM_LOGID), oldQuantity, oldOutlivingQuantity, newQuantity);
	    itemLog(outlivingItem, player, "merged in", oldItem.get(ATTR_ITEM_LOGID), oldOutlivingQuantity, oldQuantity, newQuantity);
    }

	public void splitOff(final RPEntity player, final Item item, final int quantity) {
		final String oldQuantity = getQuantity(item);
		final String outlivingQuantity = Integer.toString(Integer.parseInt(oldQuantity) - quantity);
	    itemLog(item, player, "split out", "-1", oldQuantity, outlivingQuantity, Integer.toString(quantity));
    }

	

	public void splitOff(final Player player, final Item item, final StackableItem newItem, final int quantity) {
		itemLogAssignIDIfNotPresent(item, newItem);
		final String outlivingQuantity = getQuantity(item);
		final String newQuantity = getQuantity(newItem);
		final String oldQuantity = Integer.toString(Integer.parseInt(outlivingQuantity) + Integer.parseInt(newQuantity));
	    itemLog(item, player, "split out", newItem.get(ATTR_ITEM_LOGID), oldQuantity, outlivingQuantity, newQuantity);
	    itemLog(newItem, player, "splitted out", item.get(ATTR_ITEM_LOGID), oldQuantity, newQuantity, outlivingQuantity);
    }

	public void itemLog(final RPObject item, final RPEntity player, final String event, final String param1, final String param2, final String param3, final String param4) {
		if (!item.getRPClass().subclassOf("item")) {
			return;
		}
	
		final Transaction transaction =  SingletonRepository.getPlayerDatabase().getTransaction();
		try {
	
			itemLogAssignIDIfNotPresent(transaction, item);
			itemLogWriteEntry(transaction, item, player, event, param1, param2, param3, param4);
	
			transaction.commit();
		} catch (final SQLException e) {
			logger.error(e, e);
			try {
				transaction.rollback();
			} catch (final SQLException e1) {
				logger.error(e1, e1);
			}
		}
	}

	/**
	 * Assigns the next logid to the specified item in case it does not already have one.
	 *
	 * @param items item
	 */
	public void itemLogAssignIDIfNotPresent(final RPObject... items) {
		final Transaction transaction =  SingletonRepository.getPlayerDatabase().getTransaction();
		try {
			for (final RPObject item : items) {
				if (item.getRPClass().subclassOf("item")) {
					itemLogAssignIDIfNotPresent(transaction, item);
				}
			}
	
			transaction.commit();
		} catch (final SQLException e) {
			logger.error(e, e);
			try {
				transaction.rollback();
			} catch (final SQLException e1) {
				logger.error(e1, e1);
			}
		}
	}

	/**
	 * Assigns the next logid to the specified item in case it does not already have one.
	 *
	 * @param transaction database transaction
	 * @param item item
	 * @throws SQLException in case of a database error
	 */
	private void itemLogAssignIDIfNotPresent(final Transaction transaction, final RPObject item) throws SQLException {
		if (item.has(ATTR_ITEM_LOGID)) {
			return;
		}
	
		// increment the last_id value (or initialize it in case that table has 0 rows).
		final int count = transaction.getAccessor().execute("UPDATE itemid SET last_id = last_id+1;");
		if (count < 0) {
			logger.error("Unexpected return value of execute method: " + count);
		} else if (count == 0) {
			// Note: This is just a workaround in case the itemid table is empty.
			// In case itemlog was emptied, too; this workaround does not work because
			// there are still items with higher ids out there.
			logger.warn("Initializing itemid table, this may take a few minutes in case this database is not empty.");
			transaction.getAccessor().execute("INSERT INTO itemid (last_id) SELECT max(itemid) + 1 FROM itemlog;");
			logger.warn("itemid initialized.");
		}
	
		// read last_id from database
		final int id = transaction.getAccessor().querySingleCellInt("SELECT last_id FROM itemid");
		item.put(ATTR_ITEM_LOGID, id);
		itemLogInsertName(transaction, item);
	}

	/**
	 * Logs the name of the item on first
	 * @param transaction
	 * @param item
	 * @throws SQLException
	 */
	private void itemLogInsertName(final Transaction transaction, final RPObject item) throws SQLException {
		itemLogWriteEntry(transaction, item, null, "register", getAttribute(item, "name"), getAttribute(item, "quantity"), getAttribute(item, "infostring"), getAttribute(item, "bound"));
	}

	private void itemLogWriteEntry(final Transaction transaction, final RPObject item, final RPEntity player, final String event, final String param1, final String param2, final String param3, final String param4) throws SQLException {
		String playerName = null;
		if (player != null) {
			playerName = player.getName();
		}
		final String query = "INSERT INTO itemlog (itemid, source, event, " 
			+ "param1, param2, param3, param4) VALUES (" 
			+ item.getInt(ATTR_ITEM_LOGID) + ", '" 
			+ StringChecker.trimAndEscapeSQLString(playerName, 64) + "', '" 
			+ StringChecker.trimAndEscapeSQLString(event, 64) + "', '" 
			+ StringChecker.trimAndEscapeSQLString(param1, 64) + "', '" 
			+ StringChecker.trimAndEscapeSQLString(param2, 64) + "', '" 
			+ StringChecker.trimAndEscapeSQLString(param3, 64) + "', '" 
			+ StringChecker.trimAndEscapeSQLString(param4, 64) + "');";
	
		transaction.getAccessor().execute(query);
	}
	/**
	 * gets an optional attribute 
	 *
	 * @param object object to read the optional attribute from
	 * @param attribute 
	 * @return attribute name of attribute
	 */
	private String getAttribute(final RPObject object, final String attribute) {
		if (object.has(attribute)) {
			return object.get(attribute);
		} else {
			return "null";
		}
	}
	/*
	create             name         quantity          quest-name / killed creature / summon zone x y / summonat target target-slot quantity / olditem
	slot-to-slot       source       source-slot       target    target-slot
	ground-to-slot     zone         x         y       target    target-slot
	slot-to-ground     source       source-slot       zone         x         y
	ground-to-ground   zone         x         y       zone         x         y
	use                old-quantity new-quantity
	destroy            name         quantity          by admin / by quest / on login / timeout on ground
	merge in           outliving_id      destroyed-quantity   outliving-quantity       merged-quantity
	merged in          destroyed_id      outliving-quantity   destroyed-quantity       merged-quantity
	split out          new_id            old-quantity         outliving-quantity       new-quantity
	splitted out       outliving_id      old-quantity         new-quantity             outliving-quantity

	the last two are redundant pairs to simplify queries
	 */


}
