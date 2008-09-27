package games.stendhal.server.actions.admin;

import static marauroa.common.game.Definition.Type.BYTE;
import static marauroa.common.game.Definition.Type.FLAG;
import static marauroa.common.game.Definition.Type.FLOAT;
import static marauroa.common.game.Definition.Type.INT;
import static marauroa.common.game.Definition.Type.LONG_STRING;
import static marauroa.common.game.Definition.Type.NOTYPE;
import static marauroa.common.game.Definition.Type.SHORT;
import static marauroa.common.game.Definition.Type.STRING;
import static marauroa.common.game.Definition.Type.VERY_LONG_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.Definition;
import marauroa.common.game.RPAction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class AlterActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	

	@Test
	public final void testPerform() {
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "");
		rpAction.put("mode", "");
		rpAction.put("stat", "");
		rpAction.put("value", "");
		Player player = PlayerTestHelper.createPlayer("bob");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("Entity not found", player.events().get(0).get("text"));
	}

	@Test
	public final void testUnknownAttribute() {
		Player player = PlayerTestHelper.createPlayer("bob");
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "");
		rpAction.put("value", "");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("Attribute you are altering is not defined in RPClass(player)", player.events().get(0).get("text"));
	}
	
	@Test
	public final void testNameAttribute() {
		Player player = PlayerTestHelper.createPlayer("bob");
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "name");
		rpAction.put("value", "");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("Sorry, name cannot be changed.", player.events().get(0).get("text"));
	}
	
	@Test
	public final void testAdminlevelAttribute() {
		Player player = PlayerTestHelper.createPlayer("bob");
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "adminlevel");
		rpAction.put("value", "");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("Use #/adminlevel #<playername> #[<newlevel>] to display or change adminlevel.", player.events().get(0).get("text"));
	}
	@Test
	public final void testTitleAttribute() {
		Player player = PlayerTestHelper.createPlayer("bob");
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "title");
		rpAction.put("value", "");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("The title attribute may not be changed directly.", player.events().get(0).get("text"));
	}
	
	@Test
	public final void testValidAttributeInvalidNumber() {
		Player player = PlayerTestHelper.createPlayer("bob");
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "hp");
		rpAction.put("value", "");
		assertTrue(player.events().isEmpty());
		action.perform(player, rpAction);
		assertFalse(player.events().isEmpty());
		assertEquals("Please issue a numeric value instead of ''", player.events().get(0).get("text"));
	}
	
	@Test
	public final void testValidAttributeValidNumber() {
		Player player = PlayerTestHelper.createPlayer("bob");
		player.put("base_hp", 10);
		StendhalRPZone zone = new StendhalRPZone("testzone");
		zone.add(player);
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "#" + player.getID().getObjectID());
		rpAction.put("mode", "");
		rpAction.put("stat", "hp");
		rpAction.put("value", "50");
		assertEquals(0, player.getHP());
		action.perform(player, rpAction);
		assertEquals(0, player.getHP());
		
		rpAction.put("value", "-10");
		assertEquals(0, player.getHP());
		action.perform(player, rpAction);
		assertEquals(0, player.getHP());
		
		rpAction.put("value", "5");
		assertEquals(0, player.getHP());
		action.perform(player , rpAction);
		assertEquals(5, player.getHP());
	}
	
	
	@Test
	public final void testValidAttributeValidName() {
		Player player = PlayerTestHelper.createPlayer("bob");
		player.put("base_hp", 10);
		StendhalRPZone zone = new StendhalRPZone("testzone");
		
		MockStendlRPWorld.get().addRPZone(zone);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
		zone.add(player);
		assertNotNull(SingletonRepository.getRuleProcessor().getPlayer("bob"));
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "bob");
		rpAction.put("mode", "");
		rpAction.put("stat", "hp");
		rpAction.put("value", "50");
		assertEquals(0, player.getHP());
		action.perform(player, rpAction);
		assertEquals(0, player.getHP());
		
		rpAction.put("value", "-10");
		assertEquals(0, player.getHP());
		action.perform(player, rpAction);
		assertEquals(0, player.getHP());
		
		rpAction.put("value", "5");
		assertEquals(0, player.getHP());
		action.perform(player , rpAction);
		assertEquals(5, player.getHP());
	}
	
	@Test
	public final void testValidAttributeValidNameDifferentZone() {
		Player player = PlayerTestHelper.createPlayer("bob");
		player.put("base_hp", 10);
		StendhalRPZone zone = new StendhalRPZone("testzone");
		StendhalRPZone zoneaway = new StendhalRPZone("testzonefaraway");
		zone.add(player);
		Player playerAway = PlayerTestHelper.createPlayer("bobaway");
		playerAway.put("base_hp", 10);
		zoneaway.add(playerAway);
		
		
		MockStendlRPWorld.get().addRPZone(zone);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
		MockStendlRPWorld.get().addRPZone(zoneaway);
		MockStendhalRPRuleProcessor.get().addPlayer(playerAway);
		
		assertNotNull(SingletonRepository.getRuleProcessor().getPlayer("bobaway"));
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		rpAction.put("target", "bobaway");
		rpAction.put("mode", "");
		rpAction.put("stat", "hp");
		rpAction.put("value", "50");
		assertEquals(0, playerAway.getHP());
		action.perform(player, rpAction);
		assertEquals(0, playerAway.getHP());
		
		rpAction.put("value", "-10");
		assertEquals(0, playerAway.getHP());
		action.perform(player, rpAction);
		assertEquals(0, playerAway.getHP());
		
		rpAction.put("value", "5");
		assertEquals(0, playerAway.getHP());
		action.perform(player , rpAction);
		assertEquals(5, playerAway.getHP());
	}
	
	@Test
	public final void testHasNeededAttributes() {
		AlterAction action = new AlterAction();
		RPAction rpAction = new RPAction();
		assertFalse(action.hasNeededAttributes(rpAction));
		rpAction.put("target", "");
		assertFalse(action.hasNeededAttributes(rpAction));
		rpAction.put("mode", "");
		assertFalse(action.hasNeededAttributes(rpAction));
		rpAction.put("stat", "");
		assertFalse(action.hasNeededAttributes(rpAction));
		rpAction.put("value", "");
		assertTrue(action.hasNeededAttributes(rpAction));
	}
	
	@Test
	public final void testIsParsableByInteger() {
		AlterAction action = new AlterAction();
		  Definition def = new Definition();
    	 def.setType(BYTE);
		assertTrue(action.isParsableByInteger(def));		

		def.setType(FLAG);
		assertFalse(action.isParsableByInteger(def));		

		
		def.setType(INT);
		assertTrue(action.isParsableByInteger(def));		

		def.setType(LONG_STRING);
		assertFalse(action.isParsableByInteger(def));		

		def.setType(NOTYPE);
		assertFalse(action.isParsableByInteger(def));		

		def.setType(SHORT);
		assertTrue(action.isParsableByInteger(def));		

		def.setType(STRING);
		assertFalse(action.isParsableByInteger(def));		

		def.setType(VERY_LONG_STRING);
		assertFalse(action.isParsableByInteger(def));

		def.setType(FLOAT);
		assertFalse(action.isParsableByInteger(def));		

	}
}
