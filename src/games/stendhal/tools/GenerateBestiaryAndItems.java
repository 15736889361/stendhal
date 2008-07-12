package games.stendhal.tools;

import games.stendhal.server.core.config.CreaturesXMLLoader;
import games.stendhal.server.core.config.ItemGroupsXMLLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.entity.creature.impl.DropItem;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenerateBestiaryAndItems {

	public static void generateCreatures() throws Exception {
		final CreaturesXMLLoader creatureLoader = SingletonRepository.getCreaturesXMLLoader();
		final List<DefaultCreature> creatures = creatureLoader.load("data/conf/creatures.xml");

		Collections.sort(creatures, new Comparator<DefaultCreature>() {

			public int compare(final DefaultCreature o1, final DefaultCreature o2) {
				return o1.getLevel() - o2.getLevel();
			}

			@Override
			public boolean equals(final Object obj) {
				return true;
			}
			
			@Override
			public int hashCode() {
				return 1;
			}
		});

		int level = -1;

		for (final DefaultCreature creature : creatures) {
			System.out.println(creature.getLevel() + ";" + creature.getATK()
					+ ";" + creature.getDEF() + ";" + creature.getHP() + ";"
					+ creature.getXP());
		}
		System.out.println();

		System.exit(0);

		for (final DefaultCreature creature : creatures) {
			if (creature.getLevel() != level) {
				level = creature.getLevel();
				System.out.println("= Level " + level + "=");
			}

			final String name = creature.getCreatureName();
			System.out.println("== " + name + " ==");
			System.out.println("{{Creature|");
			System.out.println("|name= " + name + "");
			System.out.println("|image= " + name + "");
			System.out.println("|hp= " + creature.getHP() + "");
			System.out.println("|atk= " + creature.getATK() + "");
			System.out.println("|def= " + creature.getDEF() + "");
			System.out.println("|exp= " + creature.getXP() / 20 + "");
			System.out.println("|behavior = '''(TODO)'''.");
			System.out.println("|location = '''(TODO)'''.");
			System.out.println("|strategy = '''(TODO)'''.");
			System.out.println("|loot = ");

			for (final DropItem item : creature.getDropItems()) {
				System.out.println(item.min + "-" + item.max + " " + item.name
						+ "<br>");
			}

			System.out.println("}}");
			System.out.println("");
		}
	}

	public static void generateItems() throws Exception {
		final ItemGroupsXMLLoader loader = new ItemGroupsXMLLoader(new URI(
				"/data/conf/items.xml"));
		final List<DefaultItem> items = loader.load();

		String clazz = null;

		for (final DefaultItem item : items) {
			if (!item.getItemClass().equals(clazz)) {
				clazz = item.getItemClass();
				System.out.println("= " + clazz + " =");
			}

			System.out.println("{{Item|");
			System.out.println("|name       = " + item.getItemName());
			System.out.println("|class      = " + item.getItemClass());
			System.out.println("|image      = " + item.getItemName());
			System.out.println("|description= TODO");
			System.out.println("|attributes = ");
			System.out.println("Attack " + "<br>");
			System.out.println("Defense " + "<br>");
			System.out.println("|equip      = ");
			System.out.println("<br>");
			System.out.println("       ");
			System.out.println("}}");
			System.out.println("");
		}
	}

	public static void main(final String[] args) throws Exception {
		generateCreatures();
		System.out.println(" *************************** ");
		generateItems();
	}

	/**
	 * = Monsters = This monster list is sorted from weakest creature to most
	 * mighty one. =Level 0= == Rat == {{Creature| |name = Rat |image= rat |hp =
	 * 20 |atk = 6 |def = 2 |exp = 5 |immunities = None. |behavior = Rats patrol
	 * dungeons and usually are found in packs of three or four creatures.
	 * |location = All around. They are a plague. You can find lots of them at
	 * forest. |strategy = Just hit first. Rats are not strong opponents. |loot =
	 * 0-7 GP }}
	 * 
	 * 
	 * 
	 * {{Item| |name = Club |class = Weapon |image = club |description= This
	 * common club, a bit more sofisticated than a wood stick. |attributes =
	 * Attack 10<br>
	 * Defense 0<br>
	 * |equip = Left hand<br>
	 * Right hand<br>
	 * Bag }}
	 */

}
