package games.stendhal.server.entity.player;

import games.stendhal.server.entity.creature.Sheep;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class PlayerSheepManager {
	private Player player;

	PlayerSheepManager(Player player) {
		this.player = player;
	}

	public void storeSheep(final Sheep sheep) {
		if (!player.hasSlot("#flock")) {
			player.addSlot(new RPSlot("#flock"));
		}

		RPSlot slot = player.getSlot("#flock");
		slot.clear();

		/*
		 * RPSlot.add() destroys zoneid, so preserve/restore it.
		 *
		 * TODO: Remove if getID()/setID() are made purely virtual.
		 */
		String zoneid;

		if (sheep.has("zoneid")) {
			zoneid = sheep.get("zoneid");
		} else {
			zoneid = null;
		}

		slot.add(sheep);

		if (zoneid != null) {
			sheep.put("zoneid", zoneid);
		}

		player.put("sheep", sheep.getID().getObjectID());

	}

	/**
	 * Recreate a saved sheep.
	 *
	 * @return	A sheep, or <code>null</code> if none.
	 */
	public Sheep retrieveSheep() {
		if (player.hasSlot("#flock")) {
			RPSlot slot = player.getSlot("#flock");

			if (slot.size() > 0) {
				RPObject object = slot.getFirst();
				slot.remove(object.getID());
				player.removeSlot("#flock");

				return new Sheep(object, player);
			}
		}

		return null;
	}
}
