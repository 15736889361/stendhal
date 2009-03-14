package games.stendhal.server.maps;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.AttackableCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeaturesTestArea implements ZoneConfigurator {

	private final EntityManager manager;

	static class QuestRat extends Creature {

		public QuestRat(final Creature copy) {
			super(copy);
		}

		@Override
		public void onDead(final Entity killer, final boolean remove) {
			if (killer instanceof RPEntity) {
				final RPEntity killerRPEntity = (RPEntity) killer;
				if (!killerRPEntity.isEquipped("golden key")) {
					final Item item = SingletonRepository.getEntityManager().getItem("golden key");
					killerRPEntity.equipOrPutOnGround(item);
				}
			}
		}

		@Override
		public void update() {
			noises = new LinkedList<String>(noises);
			noises.add("Thou shall not obtain the key!");
		}
	}

	public FeaturesTestArea() {
		manager = SingletonRepository.getEntityManager();
	}

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createDoorAndKey(zone, attributes);
		attackableAnimal(zone, attributes);
	}

	private void createDoorAndKey(final StendhalRPZone zone, final Map<String, String> attributes) {
		final List<String> slots = new LinkedList<String>();
		slots.add("bag");

		DefaultItem item = new DefaultItem("key", "gold", "golden key", -1);
		item.setImplementation(Item.class);
		item.setWeight(1);
		item.setEquipableSlots(slots);
		manager.addItem(item);

		item = new DefaultItem("key", "golden", "silver key", -1);
		item.setImplementation(Item.class);
		item.setWeight(1);
		item.setEquipableSlots(slots);
		manager.addItem(item);

		final Creature creature = new QuestRat(manager.getCreature("rat"));
		final CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 40, 5, creature, 1);
		zone.add(point);
	}

	private void attackableAnimal(final StendhalRPZone zone, final Map<String, String> attributes) {
		Creature creature = new AttackableCreature(manager.getCreature("orc"));
		CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 4, 56, creature, 1);
		point.setRespawnTime(60 * 60 * 3);
		zone.add(point);

		creature = manager.getCreature("deer");
		point = new CreatureRespawnPoint(zone, 14, 56, creature, 1);
		point.setRespawnTime(60 * 60 * 3);
		zone.add(point);
	}
}
