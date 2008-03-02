package games.stendhal.server.core.engine;

import marauroa.server.game.db.IDatabase;
import games.stendhal.server.core.config.CreaturesXMLLoader;
import games.stendhal.server.core.config.QuestsXMLLoader;
import games.stendhal.server.core.events.LoginNotifier;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rp.guilds.GuildList;
import games.stendhal.server.core.rp.guilds.GuildPermissionList;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultActionManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultRuleManager;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.player.GagManager;
import games.stendhal.server.entity.player.Jail;
import games.stendhal.server.entity.slot.BankAccessorManager;
import games.stendhal.server.maps.athor.ship.AthorFerry;


/**
 * Takes an instance of every 'singleton' created and provides setters and getters.
 * <p>
 * 
 * 
 * It is not meant to be a high sophisticated class.
 * 
 * Just one step into getting rid of the singleton hell;
 * 
 * 
 * 
 * 
 * 
 * @author astridEmma
 *
 */
public class SingletonRepository {
	
	private static EntityManager entityManager;

	public static StendhalRPRuleProcessor getRuleProcessor() {
		return StendhalRPRuleProcessor.get();
	}

	public static StendhalRPWorld getRPWorld() {
		return StendhalRPWorld.get();
	}

	public static TurnNotifier getTurnNotifier() {
		return TurnNotifier.get();
	}

	public static GagManager getGagManager() {
		return GagManager.get();
	}

	public static LoginNotifier getLoginNotifier() {
		return LoginNotifier.get();
	}

	public static Jail getJail() {
		return Jail.get();
	}

	public static NPCList getNPCList() {
		return NPCList.get();
	}

	public static StendhalQuestSystem getStendhalQuestSystem() {
		return StendhalQuestSystem.get();
	}

	public static CreaturesXMLLoader getCreaturesXMLLoader() {
		return CreaturesXMLLoader.get();
	}

	public static QuestsXMLLoader getQuestsXMLLoader() {
		return QuestsXMLLoader.get();
	}

	public static IDatabase getPlayerDatabase() {
		return StendhalPlayerDatabase.getDatabase();
	}

	public static GuildPermissionList getGuildPermissionList() {
		return GuildPermissionList.get();
	}

	public static ShopList getShopList() {
		return ShopList.get();
	}

	public static GuildList getGuildList() {
		return GuildList.get();
	}

	public static BankAccessorManager getBankAccessorManager() {
		return BankAccessorManager.get();
	}

	public static AthorFerry getAthorFerry() {
		return AthorFerry.get();
	}


	public static EntityManager getEntityManager() {
		if (entityManager == null) {
				entityManager = new DefaultEntityManager();
			}
			return entityManager;
	}

	//TODO currently not yet used
	static void setEntityManager(EntityManager entityManager) {
		SingletonRepository.entityManager = entityManager;
	}

	public static DefaultActionManager getActionManager() {
		return DefaultActionManager.getInstance();
	}

	public static DefaultRuleManager getRuleSet() {
		return new DefaultRuleManager();
	}

}
