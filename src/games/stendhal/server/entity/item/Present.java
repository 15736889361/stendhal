package games.stendhal.server.entity.item;

import games.stendhal.common.Grammar;
import games.stendhal.common.Rand;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Box;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.UseListener;

import java.util.Map;


import org.apache.log4j.Logger;
import marauroa.common.game.RPObject;

/**
 * a present which can be unwrapped.
 *
 * @author kymara
 */
public class Present extends Box implements UseListener {

	private Logger logger = Logger.getLogger(Present.class);

	// TODO: Make these configurable
	// for presents
	private static final String[] ITEMS = { "greater_potion", "pie", "sandwich", "carrot", "cherry", "blue_elf_cloak",
	        "summon_scroll" };

	/**
	 * Creates a new present
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Present(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * copy constructor
	 *
	 * @param item item to copy
	 */
	public Present(Present item) {
		super(item);
	}
    
        @Override
	protected boolean useMe(Player player) {
		this.removeOne();
		String itemName = ITEMS[Rand.rand(ITEMS.length)];
		Item item = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem(itemName);
		player.sendPrivateText("Congratulations, you've got " + Grammar.a_noun(itemName));
		player.equip(item, true);
		player.notifyWorldAboutChanges();
		return true;
        }

}
