package games.stendhal.server.entity.item.scroll;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.DelayedPlayerTextSender;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents the balloon that takes the player to 7 kikareukin clouds,
 * after which it will teleport player to a random location in 6 kikareukin islands.
 */
public class BalloonScroll extends TimedTeleportScroll {

	private static final long DELAY = 6 * MathHelper.MILLISECONDS_IN_ONE_HOUR;
	private static final int NEWTIME = 540;
	
	/**
	 * Creates a new timed marked BalloonScroll scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public BalloonScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public BalloonScroll(final BalloonScroll item) {
		super(item);
	}

	@Override
	protected String getBeforeReturnMessage() {
		return "It feels like the clouds won't take your weight much longer ... ";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "You fell through a hole in the clouds, back to solid ground.";
	}

	// Only let player use balloon from 6 kika clouds
	// Balloons used more frequently than every 6 hours only last 5 minutes
	@Override
	protected boolean useTeleportScroll(final Player player) {
		if (!"6_kikareukin_islands".equals(player.getZone().getName())) {
			player.sendPrivateText("The balloon tried to float you away but the altitude was too low for it to even lift you. " 
									  + "Try from somewhere higher up.");
			return false; 
		} 
		long lastuse = -1;
		if (player.hasQuest("balloon")){
			lastuse = Long.parseLong(player.getQuest("balloon"));		
		} 
		final long timeRemaining = (lastuse + DELAY) - System.currentTimeMillis();
		if (timeRemaining > 0){
			// player used the balloon within the last DELAY hours
			// so this use of balloon is going to be shortened 
			// (the clouds can't take so much weight on them)
			// delay message for 1 turn for technical reasons
			final DelayedPlayerTextSender dpts = new DelayedPlayerTextSender(player, 
										  "The clouds are weakened from your recent time on them, and will not hold you for long.");
			SingletonRepository.getTurnNotifier().notifyInSeconds(1, dpts);
			setInfoString("7_kikareukin_clouds 31 12 " + Integer.toString(NEWTIME) + " 6_kikareukin_islands -1 -1");
		}
		player.setQuest("balloon",Long.toString(System.currentTimeMillis()));
		return super.useTeleportScroll(player);
	}
}
