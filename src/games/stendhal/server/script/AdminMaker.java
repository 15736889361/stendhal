package games.stendhal.server.script;

import games.stendhal.common.Level;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.scripting.ScriptImpl;
import games.stendhal.server.scripting.ScriptingNPC;
import games.stendhal.server.scripting.ScriptingSandbox;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * Creates a portable NPC who gives ALL players powerful items,
 * increases their level and makes them admins. This is used on
 * test-systems only. Therefore it is disabled in default install
 * and you have to use this parameter: -Dstendhal.testserver=junk
 * as a vm argument.
 *
 * As admin uses /script AdminMaker.class to summon her right next to him/her.
 * Please unload it with /script -unload AdminMaker.class
 */

public class AdminMaker extends ScriptImpl {

	private static Logger logger = Log4J.getLogger(AdminMaker.class);

	protected class UpgradeAction extends SpeakerNPC.ChatAction {

		private void xpGain(Player player) {
			final int level = player.getLevel();

			//increase level by xlevel per execution
			int xlevel = 10;

			//Player should at least be min_level after one execution
			final int min_level = 20;
			if (level + xlevel < min_level) {
				xlevel = min_level - level;
			}

			//Don't give more XP than needed when near/at max
			if (level + xlevel > Level.maxLevel()) {
				xlevel = Level.maxLevel() - level;
			}

			player.addXP(Level.getXP(level + xlevel) - Level.getXP(level));
		}

		private final List<String> itemsSingle = Arrays.asList("rod_of_the_gm", "golden_shield", "golden_armor", "golden_cloak", "golden_helmet", "golden_legs", "golden_boots", "hunter_crossbow");

		private final List<String> itemsStack = Arrays.asList("money", "greater_potion", "greater_antidote", "power_arrow", "deadly_poison");

		private void equip(Player player) {

			//Give player all single items from list he/she doesn't have
			for (String itemName : itemsSingle) {
				if (!player.isEquipped(itemName)) {
					Item itemObj = sandbox.getItem(itemName);
					player.equip(itemObj, true);
				}
			}

			//Give 5000 of each stack in list, regardless of how many are already there
			for (String itemName : itemsStack) {
		   		Item item = sandbox.getItem(itemName);
				if (item instanceof StackableItem) {
					StackableItem stackableItem = (StackableItem) item;
					stackableItem.setQuantity(5000);
					player.equip(stackableItem);
				}
			}
		}

		private void admin(Player player) {
			if (player.getAdminLevel() == 0) {
				//can't use destroy/summon/alter/script
				player.setAdminLevel(600);
				player.update();
				player.notifyWorldAboutChanges();
			}
		}

		@Override
		public void fire(Player player, String text, SpeakerNPC engine) {
			engine.say("I will give you some items, and adjust your level.");
			xpGain(player);
			equip(player);
			admin(player);
		}
	}

	protected class TeleportAction extends SpeakerNPC.ChatAction {

		private final List<Destination> DESTINATIONS = Arrays.asList(
			new Destination("0_nalwor_city", 88, 85),
			new Destination("-2_orril_dungeon", 106, 21),
			new Destination("-1_semos_mine_nw", 122, 91),
			new Destination("-6_kanmararn_city", 33, 52),
			new Destination("-2_ados_outside_nw", 28, 4)
		);

		private static final String TELE_QUEST_SLOT = "AdminMakerTele";

		private boolean RandomTeleport(Player player) {
			//Destination selection: random for first, then go in order
			//todo: maybe mix in a second kind of random like bunny/santa?

			//pick a Destination
			int i;
			if (player.hasQuest(TELE_QUEST_SLOT)) {
				i = Integer.parseInt(player.getQuest(TELE_QUEST_SLOT));
			} else {
				i = new Random().nextInt(DESTINATIONS.size());
			}
			i++;
			if (i >= DESTINATIONS.size()) {
				i = 0;
			}
			player.setQuest(TELE_QUEST_SLOT, "" + i);
			Destination picked = DESTINATIONS.get(i);

			//Teleport
			StendhalRPZone zone = StendhalRPWorld.get().getZone(picked.zone);
			if (!player.teleport(zone, picked.x, picked.y, null, player)) {
				logger.error("AdminMaker random teleport failed, " + picked.zone + " " + picked.x + " " + picked.y);
				return false;
			}
			return true;
		}

		//todo: a better way?
		private class Destination {
			public String zone;
			public int x;
			public int y;
			Destination(String zone, int x, int y) {
				this.zone = zone;
				this.x = x;
				this.y = y;
			}
		}

		@Override
		public void fire(Player player, String text, SpeakerNPC engine) {

			//before we send the player off into the unknown give a marked scroll
			Item markedScroll = sandbox.getItem("marked_scroll");
			markedScroll.setInfoString(player.getID().getZoneID() + " " + player.getX() + " " + player.getY());
			markedScroll.setBoundTo(player.getName());

			if (player.equip(markedScroll, false)) {
				//Teleport
				if (RandomTeleport(player)) {
					//todo: priv msg doesn't work
					player.sendPrivateText(player.getTitle() + " use the scroll to come back here. Use /teleport <playername> <zonename> <x> <y> to beam to a different place.");
				} else {
					engine.say("oops, looks like you found a bug.");
				}
			} else {
				engine.say("Ask me again when you have room for a scroll.");
			}

		}
	}

	@Override
	public void load(Player admin, List<String> args, ScriptingSandbox sandbox) {
		super.load(admin, args, sandbox);

		// Require parameter -Dstendhal.testserver=junk
		if (System.getProperty("stendhal.testserver") == null) {
			String msg = "Server must be started with this vm parameter: -Dstendhal.testserver=junk";
			if (admin != null) {
				admin.sendPrivateText(msg);
			    logger.warn("AdminMaker - " + msg + " . Executed by " + admin.getName());
			}
			return;
		}

		// create npc
		ScriptingNPC npc;
		npc = new ScriptingNPC("Admin Maker");
		npc.setEntityClass("tavernbarmaidnpc");

		// Place NPC in int_admin_playground on server start
		String myZone = "0_semos_city";
		sandbox.setZone(myZone);
		int x = 32;
		int y = 16;

		// if this script is executed by an admin, Admin Maker will be placed next to
		// him/her.
		if (admin != null) {
			sandbox.setZone(sandbox.getZone(admin));
			x = admin.getX() + 1;
			y = admin.getY();
		}

		//Set zone and position
		npc.setPosition(x, y);
		sandbox.add(npc);

		// Create Dialog
		npc.behave("greet", "Hi, how can i help you?");
		npc.behave("help", "Perhaps you would like a free power #upgrade and maybe a #random destination?");
		npc.addGoodbye();
		npc.add(ConversationStates.ATTENDING, "upgrade", null, ConversationStates.ATTENDING, null, new UpgradeAction());
		npc.add(ConversationStates.ATTENDING, "random", null, ConversationStates.ATTENDING, null, new TeleportAction());

	}

}
