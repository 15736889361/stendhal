package games.stendhal.server.entity.slot;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.chest.PersonalChest;

import marauroa.common.game.RPSlot;

import org.apache.log4j.Logger;

/**
 * a slot of a personal chest
 *
 * @author hendrik
 */
public class PersonalChestSlot extends ChestSlot {
	private static Logger logger = Logger.getLogger(PersonalChestSlot.class);
	private PersonalChest chest;

	public PersonalChestSlot(PersonalChest owner) {
	    super(owner);
	    this.chest = owner;
    }

	@Override
    public boolean isReachableForTakingThingsOutOfBy(Entity entity) {
		// Yes, this comparison of references is by design: Two player objects
		// are equal if they are for the same character but could be from two 
		// different session. Marauroa is supposted to prevent two session
		// for the same character being active at the same time, but we should
		// not depend on this as the banks have had lots of bugs in the past.
	    if (chest.getAttending() != entity) {
	    	logger.error(entity + " tried to take stuff out of the bank chest " + chest + " which is currently attenting " + chest.getAttending());
	    	return false;
	    }
	    return super.isReachableForTakingThingsOutOfBy(entity);
    }

	@Override
    public RPSlot getWriteableSlot() {
		return chest.getBankSlot();
    }
}
