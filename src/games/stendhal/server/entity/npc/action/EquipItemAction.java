package games.stendhal.server.entity.npc.action;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.log4j.Logger;

/**
 * Equips the specified item.
 */
public class EquipItemAction implements ChatAction {
	private static Logger logger = Logger.getLogger(EquipItemAction.class);

	private final String itemName;
	private final int amount;
	private final boolean bind;

	/**
	 * Creates a new EquipItemAction.
	 * 
	 * @param itemName
	 *            name of item
	 */
	public EquipItemAction(final String itemName) {
		this(itemName, 1, false);
	}

	/**
	 * Creates a new EquipItemAction.
	 * 
	 * @param itemName
	 *            name of item
	 * @param amount
	 *            for StackableItems
	 */
	public EquipItemAction(final String itemName, final int amount) {
		this(itemName, amount, false);
	}

	/**
	 * Creates a new EquipItemAction.
	 * 
	 * @param itemName
	 *            name of item
	 * @param amount
	 *            for StackableItems
	 * @param bind
	 *            bind to player
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind) {
		this.itemName = itemName;
		this.amount = amount;
		this.bind = bind;
	}

	public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		if (item != null) {
    		if (item instanceof StackableItem) {
    			final StackableItem stackableItem = (StackableItem) item;
    			stackableItem.setQuantity(amount);
    		}
    		if (bind) {
    			item.setBoundTo(player.getName());
    		}
    		player.equip(item, true);
    		player.notifyWorldAboutChanges();
		} else {
			logger.error("Cannot find item '" + itemName + "' to equip", new Throwable());
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("equip item <");
		sb.append(amount);
		sb.append(" ");
		sb.append(itemName);
		if (bind) {
			sb.append(" (bind)");
		}
		sb.append(">");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + amount;
		if (itemName == null) {
			result = PRIME * result;
		} else {
			result = PRIME * result + itemName.hashCode();
		}
		
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EquipItemAction other = (EquipItemAction) obj;
		if (amount != other.amount) {
			return false;
		}
		if (itemName == null) {
			if (other.itemName != null) {
				return false;
			}
		} else if (!itemName.equals(other.itemName)) {
			return false;
		}
		return true;
	}

}