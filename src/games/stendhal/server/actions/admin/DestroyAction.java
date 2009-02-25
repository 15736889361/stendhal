package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.NAME;
import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Blood;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.spawner.FlowerGrower;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public class DestroyAction extends AdministrationAction {


	

	public static void register() {
		CommandCenter.register("destroy", new DestroyAction(), 700);

	}

	@Override
	public void perform(final Player player, final RPAction action) {

		final Entity inspected = getTarget(player, action);

		if (inspected == null) {
			final String text = "Entity not found";

			player.sendPrivateText(text);
			return;
		}
		
		String clazz = inspected.getRPClass().getName();
		String name = "";
		 
		if (inspected.has(NAME)) {
                      name = inspected.get(NAME);
		} 
		
if (inspected.isContained()) {
	RPObject slot = inspected.getContainer();
	
	SingletonRepository.getRuleProcessor().addGameEvent(player.getName(), "removed",
			name + " " + clazz, slot.getID().toString(), Integer.toString(inspected.getX()),
			Integer.toString(inspected.getY()));

	String slotname = inspected.getContainerSlot().getName();
	int objectID = inspected.getID().getObjectID();
	if (null != inspected.getContainerSlot().remove(inspected.getID())) {
		player.sendPrivateText("Removed contained " + name + " " + clazz + " with ID " + objectID + " from " + slotname);
	} else {
		player.sendPrivateText("could not remove contained " + inspected + " " + clazz + " with ID " + objectID + " from " + slotname);

	}
	

} else {
		if (inspected instanceof Player) {
			final String text = "You can't remove players";
			player.sendPrivateText(text);
			return;
		}

		if (inspected instanceof SpeakerNPC) {
			final String text = "You can't remove SpeakerNPCs";
			player.sendPrivateText(text);
			return;
		}

        if (inspected instanceof Portal) {
            final String text = "You can't remove portals. Try blocking it with a few of /script AdminSign.class.";
            player.sendPrivateText(text);
            return;
		}

		final StendhalRPZone zone = inspected.getZone();

		if (inspected instanceof RPEntity) {
			if (inspected instanceof Creature) {
				// *destroyed creatures should not drop items
				((Creature) inspected).clearDropItemList();
			} 
			((RPEntity) inspected).onDead(player);
		} else if ((inspected instanceof Item) || (inspected instanceof FlowerGrower) || (inspected instanceof Blood) || (inspected instanceof Corpse)) {
			zone.remove(inspected);
		} else {
			player.sendPrivateText("You can't remove this type of entity");
			return;
		}
	
		if (inspected instanceof TurnListener) {
			TurnListener listener = (TurnListener) inspected;
			TurnNotifier.get().dontNotify(listener);
		}
	
		

		SingletonRepository.getRuleProcessor().addGameEvent(player.getName(), "removed",
				name + " " + clazz, zone.getName(), Integer.toString(inspected.getX()),
				Integer.toString(inspected.getY()));

		player.sendPrivateText("Removed " + name + " " + clazz + " with ID " + action.get(TARGET));
	}
}

}
