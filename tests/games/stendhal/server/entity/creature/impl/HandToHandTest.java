package games.stendhal.server.entity.creature.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.RPClass.CreatureTestHelper;

public class HandToHandTest {

	@BeforeClass
	public static void setUpbeforeClass() throws Exception {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testAttack() throws Exception {
		MockStendhalRPRuleProcessor.get();
		final HandToHand hth = new HandToHand();
		final Creature creature = createMock(Creature.class);
		expect(creature.isAttackTurn(0)).andReturn(true);
		expect(creature.attack()).andReturn(true);
		creature.tryToPoison();
		replay(creature);
		hth.attack(creature);
		verify(creature);
	}
	
	@Test
	public void testNotAttackTurnAttack() throws Exception {
		MockStendhalRPRuleProcessor.get();
		final HandToHand hth = new HandToHand();
		final Creature creature = createMock(Creature.class);
		expect(creature.isAttackTurn(0)).andReturn(false);
		replay(creature);
		hth.attack(creature);
		verify(creature);
	}
	
	@Test
	public void testCanAttackNow() {
		final HandToHand hth = new HandToHand();
		final Creature creature = new Creature();	
		assertFalse("no target yet", hth.canAttackNow(creature));
		final RPEntity victim = new RPEntity() {

			@Override
			protected void dropItemsOn(final Corpse corpse) {
				
			}

			@Override
			public void logic() {
				
			}
		};
		victim.put("id", 1);
		creature.setTarget(victim);
		assertTrue("new ones stand on same positon", hth.canAttackNow(creature));
		victim.setPosition(10, 10);
		assertFalse("too far away", hth.canAttackNow(creature));
		
		
	}
	
	
	
	@Test
	public void testCanAttackNowBigCreature() {
		final StendhalRPZone zone = new StendhalRPZone("hthtest");
		final HandToHand hth = new HandToHand();
		final Creature creature = SingletonRepository.getEntityManager().getCreature("balrog");
		assertNotNull(creature);
		assertThat(creature.getWidth(), is(11.0));
		assertThat(creature.getHeight(), is(12.0));
		creature.setPosition(10, 10);
		assertFalse("no target yet", hth.canAttackNow(creature));
		final RPEntity victim = PlayerTestHelper.createPlayer("bob");
		victim.setHP(1);
		zone.add(creature);
		zone.add(victim);
		creature.setTarget(victim);
		
		for (int i = 9; i < 12; i++) {
			for (int j = 9; j < 13; j++) {
				victim.setPosition(i, j);
				assertTrue(creature.nextTo(victim));
				assertTrue(victim.nextTo(creature));
				assertTrue("can attack now (" + i + "," + j + ")", hth.canAttackNow(creature));
			}
		}
		
		
		victim.setPosition(8, 13);
		assertFalse(creature.nextTo(victim));
		assertFalse(victim.nextTo(creature));
		assertFalse("can attack now ", hth.canAttackNow(creature));

		
		
	}

	
	private static boolean mockinvisible;

	@Test
	public void testhasValidTargetNonAttacker() {
		HandToHand hth = new HandToHand();
		
		Creature nonAttacker = createMock(Creature.class);
		expect(nonAttacker.isAttacking()).andReturn(false);
		replay(nonAttacker);
		
		assertFalse(hth.hasValidTarget(nonAttacker));
		verify(nonAttacker);
	}
	
	@Test
		public void testhasValidTargetInvisibleVictim() {
		
		Creature victim = createMock(Creature.class);
		expect(victim.isInvisibleToCreatures()).andReturn(true);
		
		
		Creature attacker = createMock(Creature.class);
		expect(attacker.isAttacking()).andReturn(true);
		expect(attacker.getAttackTarget()).andReturn(victim);
		
		
		replay(victim);
		replay(attacker);
		
		HandToHand hth = new HandToHand();
		assertFalse(hth.hasValidTarget(attacker));
		
	}
	@Test
	public void testhasValidTargetvisibleVictim() {
	
	StendhalRPZone zoneA = new StendhalRPZone("A");
	StendhalRPZone zoneB = new StendhalRPZone("B");
		
		
	RPEntity victim = createMock(RPEntity.class);
	expect(victim.isInvisibleToCreatures()).andReturn(false);
	expect(victim.getZone()).andReturn(zoneA);
	
	Creature attacker = createMock(Creature.class);
	expect(attacker.isAttacking()).andReturn(true);
	expect(attacker.getAttackTarget()).andReturn(victim);
	
	expect(attacker.getZone()).andReturn(zoneB);
	
	replay(victim);
	replay(attacker);
	
	HandToHand hth = new HandToHand();
	assertFalse(hth.hasValidTarget(attacker));
	
}
	
	
	@Test
	public void testHasValidTarget() {
		final StendhalRPZone zone = new StendhalRPZone("hthtest");
		
		final HandToHand hth = new HandToHand();
		final Creature creature = new Creature();	
		assertFalse("is not attacking", hth.hasValidTarget(creature));
		final RPEntity victim = new RPEntity() {
		
			@Override
			public boolean isInvisibleToCreatures() {
				return mockinvisible;
			}
			@Override
			protected void dropItemsOn(final Corpse corpse) {
				
			}

			@Override
			public void logic() {
				
			}
		};
		victim.put("id", 1);
		creature.setTarget(victim);
		mockinvisible = true;
		assertTrue(victim.isInvisibleToCreatures());
		assertFalse("victim is invisible", hth.hasValidTarget(creature));
		mockinvisible = false;
		assertFalse(victim.isInvisibleToCreatures());
		zone.add(victim);
		assertFalse("not in same zone", hth.hasValidTarget(creature));
		zone.add(creature);
		assertFalse("in same zone, on same spot and dead", hth.hasValidTarget(creature));
		
		creature.setTarget(victim);
		victim.setHP(1);
		assertTrue("in same zone, on same spot", hth.hasValidTarget(creature));

		victim.setPosition(12, 0);
		assertTrue("in same zone, not too far away", hth.hasValidTarget(creature));
		victim.setPosition(13, 0);
		assertFalse("in same zone, too far away", hth.hasValidTarget(creature));
		

	}
	
	@Test
	public void testFindNewtarget() throws Exception {
		MockStendhalRPRuleProcessor.get();
		final HandToHand hth = new HandToHand();
		final Creature lonesomeCreature = new Creature();
		assertFalse(lonesomeCreature.isAttacking());
		hth.findNewTarget(lonesomeCreature);
		assertFalse(lonesomeCreature.isAttacking());
		
		
		final Creature creature = createMock(Creature.class);
		expect(creature.getNearestEnemy(7)).andReturn(creature);
		creature.setTarget(creature);
		replay(creature);
		
		hth.findNewTarget(creature);

		verify(creature);
	}

}
