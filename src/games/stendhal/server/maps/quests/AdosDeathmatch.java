package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.StandardInteraction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.LoginListener;
import games.stendhal.server.events.LoginNotifier;
import games.stendhal.server.events.TurnNotifier;
import games.stendhal.server.maps.deathmatch.BailAction;
import games.stendhal.server.maps.deathmatch.DealWithLogoutCoward;
import games.stendhal.server.maps.deathmatch.DeathmatchInfo;
import games.stendhal.server.maps.deathmatch.DoneAction;
import games.stendhal.server.maps.deathmatch.LeaveAction;
import games.stendhal.server.maps.deathmatch.StartAction;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Path;
import games.stendhal.server.util.Area;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Creating the Stendhal Deathmatch Game
 */
public class AdosDeathmatch extends AbstractQuest implements LoginListener {

	private NPCList npcs = NPCList.get();

	private StendhalRPZone zone = null;

	private Area arena = null;

	private DeathmatchInfo deathmatchInfo = null;

	public AdosDeathmatch() {
		// constructor for quest system
	}

	public AdosDeathmatch(String zoneName, StendhalRPZone zone, Area arena) {
		this.zone = zone;
		this.arena = arena;
		deathmatchInfo = new DeathmatchInfo(arena, zoneName, zone);
		zone.setTeleportAllowed(false);
		DeathmatchInfo.add(deathmatchInfo);
		
		LoginNotifier.get().addListener(this);
	}

	/**
	 * show the player the potential trophy
	 *
	 * @param x x-position of helmet
	 * @param y y-position of helmet
	 */
	public void createHelmet(int x, int y) {
		Item helmet = StendhalRPWorld.get().getRuleManager().getEntityManager().getItem("trophy_helmet");
		zone.assignRPObjectID(helmet);
		helmet.put("def", "20");
		helmet.setDescription("This is the grand prize for Deathmatch winners.");
		helmet.setX(x);
		helmet.setY(y);
		helmet.put("persistent", 1);
		zone.add(helmet);
	}

	public void createNPC(String name, int x, int y) {

		// We create an NPC
		SpeakerNPC npc = new SpeakerNPC(name) {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {

				// player is outside the fence
				add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, new StandardInteraction.Not(
				        new StandardInteraction.PlayerInAreaCondition(arena)), ConversationStates.INFORMATION_1,
				        "Welcome to Ados Deathmatch! Please talk to #Thonatus if you want to join", null);
				add(ConversationStates.INFORMATION_1, "Thonatus", null, ConversationStates.INFORMATION_1,
				        "Thonatus is the official Deathmatch Recrutor. He is in the swamp south west of Ados.", null);

				// player is inside
				add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				        new StandardInteraction.PlayerInAreaCondition(arena), ConversationStates.ATTENDING,
				        "Welcome to Ados Deathmatch! Do you need #help?", null);
				addJob("I'm the deathmatch assistant. Tell me, if you need #help on that.");
				addHelp("Say '#start' when you're ready! Keep killing #everything that #appears. Say 'victory' when you survived.");
				addGoodbye("I hope you enjoy the Deathmatch!");

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("everything", "appears"),
				        ConversationStates.ATTENDING,
				        "Each round you will face stronger enemies. Defend well, kill them or tell me if you want to #bail!",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("trophy", "helm", "helmet"),
				        ConversationStates.ATTENDING,
				        "If you win the deathmatch, we reward you with a trophy helmet. Each #victory will strengthen it.",
				        null);

				// 'start' command will start spawning creatures
				add(ConversationStates.ATTENDING, Arrays.asList("start", "go", "fight"), null,
				        ConversationStates.IDLE, null, new StartAction(deathmatchInfo));

				// 'victory' command will scan, if all creatures are killed and reward the player
				add(ConversationStates.ATTENDING, Arrays.asList("victory", "done", "yay"), null,
				        ConversationStates.ATTENDING, null, new DoneAction());

				// 'leave' command will send the victorious player home
				add(ConversationStates.ATTENDING, Arrays.asList("leave", "home"), null, ConversationStates.ATTENDING,
				        null, new LeaveAction());

				// 'bail' command will teleport the player out of it
				add(ConversationStates.ANY, Arrays.asList("bail", "flee", "run", "exit"), null,
				        ConversationStates.ATTENDING, null, new BailAction());
			}
		};

		npc.put("class", "darkwizardnpc");
		npc.set(x, y);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		zone.add(npc);
	}

	public void onLoggedIn(Player player) {
		// need to do this on the next turn
		TurnNotifier.get().notifyInTurns(1, new DealWithLogoutCoward(player));
    }

}
