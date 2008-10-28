package games.stendhal.server.entity.creature;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

/**
 * <p>A creature that will give no XP to killers.
 * <p>It calculates the DM score (points) due to the DM starter.
 * <p>All players who did damage get the kill attributed.
 *
 * @author hendrik
 */
public class DeathMatchCreature extends Creature {

	private int points;
	
	// save only the name to enable GC of the player object
	private String playerName; 

	/**
	 * DeathCreature.
	 * 
	 * @param copy
	 *            creature to wrap
	 */
	public DeathMatchCreature(final Creature copy) {
		super(copy);
	}

	/**
	 * Only this player gets a points reward.
	 * 
	 * @param player
	 *            Player to reward
	 */
	public void setPlayerToReward(final Player player) {
		this.playerName = player.getName();
	}
	
	@Override
	public Creature getInstance() {
		return new DeathMatchCreature(this);
	}

	@Override
	protected void rewardKillers(final int oldXP) {
	  
		for (final String killerName : playersToReward) {
			final Player killer = SingletonRepository.getRuleProcessor().getPlayer(killerName);
			// check logout
			if (killer == null) {
				continue;
			}
			
			final Integer damageDone = damageReceived.get(killer);
			if (damageDone == null) {
				continue;
			}
			// set the DM points score only for the player who started the DM
			if (killerName.equals(playerName)) {
				points = (int) (killer.getLevel()
					* ((float) damageDone / (float) totalDamageReceived));
			}	
			// For some quests etc., it is required that the player kills a
			// certain creature without the help of others.
			// Find out if the player killed this RPEntity on his own, but
			// don't overwrite solo with shared.
			final String killedName = getName();
			
			if (killedName != null) {
				if (damageDone == totalDamageReceived) {
					killer.setSoloKill(killedName);
				} else {
					killer.setSharedKill(killedName);
				}
			}
			
			killer.notifyWorldAboutChanges();
			
		}
		
	}
	
	/**
	 * Calculates the deathmatch points for this kill.
	 * 
	 * @return number of points to reward
	 */
	public int getDMPoints() {
		return points;
	}

}
