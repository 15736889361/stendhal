
package games.stendhal.server.entity.player;

import games.stendhal.common.ItemTools;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.slot.EntitySlot;
import games.stendhal.server.entity.slot.KeyedSlot;

import java.util.Arrays;
import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public abstract class UpdateConverter {

	private static final List<String> ITEM_NAMES_OLD = Arrays.asList(
			"flail_+2", "leather_armor_+1", "leather_cuirass_+1",
			"chain_armor_+1", "scale_armor_+1", "chain_armor_+3",
			"scale_armor_+2", "twoside_axe_+3", "elf_cloak_+2", "mace_+1",
			"mace_+2", "hammer_+3", "chain_helmet_+2", "golden_helmet_+3",
			"longbow_+1", "lion_shield_+1"
	);
	private static final List<String> ITEM_NAMES_NEW = Arrays.asList(
			"morning star", "leather scale armor", "pauldroned leather cuirass",
			"enhanced chainmail", "iron scale armor", "golden chainmail",
			"pauldroned iron cuirass", "golden twoside axe", "blue elf cloak", "enhanced mace",
			"golden mace", "golden hammer", "aventail", "horned golden helmet",
			"composite bow", "enhanced lion shield"
	);

	private static final List<String> ITEM_NAMES_OLD_0_66 = Arrays.asList(
			"key golden", "key silver", "book black", "book blue",
			"duergar elder", "duergar black", "giant elder",
			"chaos sorceror"
	);
	private static final List<String> ITEM_NAMES_NEW_0_66 = Arrays.asList(
			"golden key", "silver key", "black book", "blue book",
			"elder duergar", "black duergar", "elder giant",
			"chaos sorcerer"
	);

	/**
	 * Update old item names to the current naming.
	 *
	 * @param name
	 * @return the currentName of an Item
	 */
	public static String updateItemName(String name) {
		if (name != null) {
    		// handle renamed items
    		int idx = ITEM_NAMES_OLD.indexOf(name);
    		if (idx != -1) {
    			name = ITEM_NAMES_NEW.get(idx);
    		}

    		// Remove underscore characters from old database item names - ConversationParser
    		// is now capable to work with space separated item names.
    		name = ItemTools.itemNameToDisplayName(name);

    		// rename some additional items to fix grammar in release 0.66
    		idx = ITEM_NAMES_OLD_0_66.indexOf(name);
    		if (idx != -1) {
    			name = ITEM_NAMES_NEW_0_66.get(idx);
    		}
		}

		return name;
	}

	public static Item updateItem(String name) {
		// process the old keys for houses, now that we have change locks implemented
		Item item;
		if (name.startsWith("private key ")) {
			// which zone the house is in
			final String zoneName;
			final String doorId;
			// number tracks the lock changes
			final int number = 0;
			final String[] parts = name.split(" ");
			if (parts.length > 2) {
			   	try {
					// house number
					final int id;
					id = Integer.parseInt(parts[2]);
					if (id < 26) {
						zoneName = "kalavan";
					} else if (id < 50) {
						zoneName = "kirdneh";
					} else {
						zoneName = "ados";
					}
					doorId = zoneName + " house " + Integer.toString(id);
					// now set the infostring of the house key to doorId;number;
					item = SingletonRepository.getEntityManager().getItem("house key");
					((HouseKey) item).setup(doorId, number, null);
				} catch (final NumberFormatException e) {
					// shouldn't happen - give up and this will generate a warning
					item = SingletonRepository.getEntityManager().getItem(name);
				}
			} else {
				// shouldn't happen - give up and this will generate a warning
				item = SingletonRepository.getEntityManager().getItem(name);
			}
		} else {
			// item wasn't private key, just make it as normal
			item = SingletonRepository.getEntityManager().getItem(name);
		}
		return item;
	}

	/**
     * Updates a player RPObject from an old version of Stendhal.
     * 
     * @param object
     *            RPObject representing a player
     */
    static void updatePlayerRPObject(final RPObject object) {
    	final String[] slotsNormal = { "bag", "rhand", "lhand", "head", "armor",
    			"legs", "feet", "finger", "cloak", "bank", "bank_ados",
    			"zaras_chest_ados", "bank_fado", "bank_nalwor", "spells",
    			"keyring" };
    
    	final String[] slotsSpecial = { "!quests", "!kills", "!buddy", "!ignore",
    			"!visited", "skills", "!tutorial", "!features" };
    
    	// Port from 0.03 to 0.10
    	if (!object.has("base_hp")) {
    		object.put("base_hp", "100");
    		object.put("hp", "100");
    	}
    
    	// Port from 0.13 to 0.20
    	if (!object.has("outfit")) {
    		object.put("outfit", new Outfit().getCode());
    	}
    
    	// create slots if they do not exist yet:
    
    	// Port from 0.20 to 0.30: bag, rhand, lhand, armor, head, legs, feet
    	// Port from 0.44 to 0.50: cloak, bank
    	// Port from 0.57 to 0.58: bank_ados, bank_fado
    	// Port from 0.58 to ?: bank_nalwor, keyring, finger
    	for (final String slotName : slotsNormal) {
    		if (!object.hasSlot(slotName)) {
    			object.addSlot(new EntitySlot(slotName));
    		}
    	}
    
    	// Port from 0.44 to 0.50: !buddy
    	// Port from 0.56 to 0.56.1: !ignore
    	// Port from 0.57 to 0.58: skills
    	for (final String slotName : slotsSpecial) {
    		if (!object.hasSlot(slotName)) {
    			object.addSlot(new KeyedSlot(slotName));
    		}
    		final RPSlot slot = object.getSlot(slotName);
    		if (slot.size() == 0) {
    			final RPObject singleObject = new RPObject();
    			slot.add(singleObject);
    		}
    	}
    
    	// Port from 0.30 to 0.35
    	if (!object.has("atk_xp")) {
    		object.put("atk_xp", "0");
    		object.put("def_xp", "0");
    	}
    
    	if (object.has("devel")) {
    		object.remove("devel");
    	}
    
    	// From 0.44 to 0.50
    	if (!object.has("release")) {
    		object.put("release", "0.00");
    		object.put("atk", "10");
    		object.put("def", "10");
    	}
    
    	if (!object.has("age")) {
    		object.put("age", "0");
    	}
    
    	if (!object.has("karma")) {
    		// A little beginner's luck
    		object.put("karma", 10);
    	}
    	if (!object.has("mana")) {
    		object.put("mana", 0);
    	}
    	if (!object.has("base_mana")) {
    		object.put("base_mana", 0);
    	}
    
    	// Renamed to skills
    	if (object.has("!skills")) {
    		object.remove("!skills");
    	}
    
    	if (!object.has("height")) {
    		object.put("height", 2);
    	}
    	if (!object.has("width")) {
    		object.put("width", 1);
    	}
    
    	// port to 0.66
    	transformKillSlot(object);
    }

	/**
	 * Transform kill slot content to the new kill recording system.
	 * @param object 
	 */
	static void transformKillSlot(final RPObject object) {
		final RPObject kills = Player.getKeyedSlotObject(object, "!kills");

		if (kills != null) {
    		final RPObject newKills = new RPObject();
    		for (final String attr : kills) {
    			// skip "id" entries
    			if (!attr.equals("id")) {
        			String newAttr = attr;
        			String value = kills.get(attr);

        			// Is it stored using the old recording system without an dot?
        			if (attr.indexOf('.') < 0) {
        				newAttr = updateItemName(newAttr);
        				newAttr = value + "." + newAttr;
        				value = "1";
        			}

        			newKills.put(newAttr, value);
    			}
    		}

    		final RPSlot slot = object.getSlot("!kills");
    		slot.remove(kills.getID());
    		slot.add(newKills);
		}
	}

	/**
	 * Update the quest slot to the current version.
	 * @param player
	 */
	public static void updateQuests(final Player player) {
		final EntityManager entityMgr = SingletonRepository.getEntityManager();

		// rename old quest slot "Valo_concoct_potion" to "valo_concoct_potion"
		// We avoid to lose potion in case there is an entry with the old and the new name at the same
		// time by combining them by calculating the minimum of the two times and the sum of the two amounts.
		migrateSumTimedQuestSlot(player, "Valo_concoct_potion", "valo_concoct_potion");

		// From 0.66 to 0.67
		// update quest slot content, 
		// replace "_" with " ", for item/creature names
		for (final String questSlot : player.getQuests()) {
			if (player.hasQuest(questSlot)) {
				final String itemString = player.getQuest(questSlot);

				final String[] parts = itemString.split(";");

				final StringBuilder buffer = new StringBuilder();
				boolean first = true;

				for (int i = 0; i < parts.length; ++i) {
					final String oldName = parts[i];

					// Convert old item names to their new representation with correct grammar
					// and without underscores.
					String newName = UpdateConverter.updateItemName(oldName);

					// check for valid item and creature names if the update converter changed the name
					if (!newName.equals(oldName)) {
						if (!entityMgr.isCreature(newName) && !entityMgr.isItem(newName)) {
							newName = oldName;
						}
					}

					if (first) {
						buffer.append(newName);
						first = false;
					} else {
						buffer.append(';');
						buffer.append(newName);
					}
				}

				player.setQuest(questSlot, buffer.toString());
			}
		}
    }

	 // update the name of a quest to the new spelling
//	private static void renameQuestSlot(Player player, String oldName, String newName) {
//		String questState = player.getQuest(oldName);
//
//		if (questState != null) {
//			player.setQuest(newName, questState);
//			player.removeQuest(oldName);
//		}
//	}

	 // update the name of a quest to the new spelling and accumulate the content
	private static void migrateSumTimedQuestSlot(final Player player, final String oldName, final String newName) {
		final String oldState = player.getQuest(oldName);

		if (oldState != null) {
			String questState = oldState;
			final String newState = player.getQuest(newName);

			if (newState != null) {
				final String[] oldParts = oldState.split(";");
				final String[] newParts = newState.split(";");

				if ((oldParts.length == 3) && (newParts.length == 3)) {
					try {
        				final int oldAmount = Integer.parseInt(oldParts[0]);
        				int newAmount = Integer.parseInt(newParts[0]);
        				final String oldItem = oldParts[1];
        				final String newItem = newParts[1];
        				final long oldTime = Long.parseLong(oldParts[2]);
        				long newTime = Long.parseLong(newParts[2]);

        				if (oldItem.equals(newItem)) {
        					newAmount += oldAmount;

        					if (oldTime < newTime) {
        						newTime = oldTime;
        					}

        					questState = Integer.toString(newAmount) + ';' + newItem + ';' + Long.toString(newTime);
        				}
        			} catch (final NumberFormatException e) {
        				e.printStackTrace();
        			}
				}
			}

			player.setQuest(newName, questState);
			player.removeQuest(oldName);
		}
	}

}
