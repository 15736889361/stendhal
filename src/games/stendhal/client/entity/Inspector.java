package games.stendhal.client.entity;

import games.stendhal.client.gui.wt.EntityContainer;
import marauroa.common.game.RPSlot;

public interface Inspector {

	public EntityContainer inspectMe(Entity entity, RPSlot content,
			EntityContainer container);
}
