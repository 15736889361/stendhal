
package games.stendhal.server;

import games.stendhal.server.config.QuestsXMLLoader;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.QuestInfo;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * Loads and manages all quests 
 */
public class StendhalQuestSystem {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(StendhalQuestSystem.class);

	private List<IQuest> quests = new LinkedList<IQuest>();

	private QuestsXMLLoader questInfos;

	private static StendhalQuestSystem stendhalQuestSystem;

	public static StendhalQuestSystem get() {
		if (stendhalQuestSystem == null) {
			stendhalQuestSystem = new StendhalQuestSystem();
		}
		return stendhalQuestSystem;
	}

	private StendhalQuestSystem() {
		// hide constructor, this is a Singleton
	}

	/**
	 * initiales the QuestSystem
	 */
	public void init() {
		questInfos = QuestsXMLLoader.get();
		loadQuest("AdosDeathmatch");
		loadQuest("ArmorForDagobert");
		loadQuest("BeerForHayunn");
		loadQuest("Blackjack");
		loadQuest("Campfire");
		//loadQuest("CarmenCataclysm");
		loadQuest("CleanStorageSpace");
		loadQuest("CloakCollector");
		loadQuest("CloaksForBario");
		loadQuest("ClubOfThorns");
		loadQuest("DailyItemQuest");
		loadQuest("DailyMonsterQuest");
		loadQuest("DiceGambling");
		//loadQuest("DiogenesCataclysm");
		loadQuest("ElfPrincess");
		loadQuest("FindGhosts");
		loadQuest("FishermansLicenseQuiz");
		loadQuest("FishermansLicenseCollector");
		loadQuest("HatForMonogenes");
		//loadQuest("HayunnCataclysm");
		loadQuest("HungryJoshua");
		loadQuest("IntroducePlayers");
		loadQuest("JailedDwarf");
		loadQuest("LearnAboutKarma");
		loadQuest("LookBookforCeryl");
		loadQuest("LookUpQuote");
		loadQuest("KanmararnSoldiers");
		loadQuest("KillDarkElves");
		loadQuest("Marriage");
		//loadQuest("MonogenesCataclysm");
		// loadQuest("MeetBunny");
		loadQuest("MeetHackim");
		loadQuest("McPeglegIOU");
		loadQuest("MeetHayunn");
		loadQuest("MeetIo");
		loadQuest("MeetKetteh");
		loadQuest("MeetMonogenes");
		// loadQuest("MeetSanta");
		loadQuest("MeetZynn");
		loadQuest("NewsFromHackim");
		//loadQuest("NomyrCataclysm");
		loadQuest("ObsidianKnife");
		loadQuest("PizzaDelivery");
		loadQuest("PlinksToy");
		loadQuest("ReverseArrow");
		loadQuest("RingMaker");
		// loadQuest("SatoCataclysm");
		// loadQuest("SemosMineTownRevivalWeeks");
		loadQuest("SevenCherubs");
		loadQuest("Soup");
		loadQuest("StuffForVulcanus");
		loadQuest("SuntanCreamForZara");
		loadQuest("TakeGoldforGrafindle");
		loadQuest("ToysCollector");
		loadQuest("VampireSword");
		loadQuest("WeaponsCollector");
		loadQuest("WeaponsCollector2");
		loadQuest("ZooFood");

	}

	private boolean loadQuest(String name) {
		try {
			Class questClass = Class.forName("games.stendhal.server.maps.quests." + name);

			if (!IQuest.class.isAssignableFrom(questClass)) {
				logger.error("Class " + name + " doesn't implement IQuest interface.");
				return false;
			}

			// Create a new instance.
			logger.info("Loading Quest: " + name);
			Constructor constr = questClass.getConstructor();
			IQuest quest = (IQuest) constr.newInstance();

			// init and add to world
			quest.init(name);
			quest.addToWorld();

			quests.add(quest);
			return true;
		} catch (Exception e) {
			logger.warn("Quest(" + name + ") loading failed.", e);
			return false;
		}
	}

	private void dumpQuest(StringBuilder sb, IQuest quest, Player player) {
		QuestInfo questInfo = questInfos.get(quest.getName());
		sb.append("\t" + questInfo.getTitle() + "\r\n");
		List<String> history = quest.getHistory(player);
		for (String entry : history) {
			String text = questInfo.getHistory().get(entry);
			if (text == null) {
				text = entry;
			}
			sb.append("\t\t * " + text + "\r\n");
		}
		sb.append("\r\n");
	}

	public String listQuests(Player player) {
		StringBuilder sb = new StringBuilder();

		// Open quests
		sb.append("\r\n\r\n");
		sb.append("Open Quests\r\n");
		sb.append("========\r\n");
		for (IQuest quest : quests) {
			if (quest.isStarted(player) && !quest.isCompleted(player)) {
				dumpQuest(sb, quest, player);
			}
		}

		// Completed Quests
		sb.append("\r\n\r\n");
		sb.append("Completed Quests\r\n");
		sb.append("============\r\n");
		for (IQuest quest : quests) {
			if (quest.isCompleted(player)) {
				dumpQuest(sb, quest, player);
			}
		}

		return sb.toString();
	}
	public String listQuest(Player player,String questName) {
		StringBuilder sb = new StringBuilder();
		for (IQuest quest : quests) {
			if (quest.getName().equals(questName)){
				dumpQuest(sb, quest, player);	
			}
		}
		return sb.toString();
	}
}
