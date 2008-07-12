package games.stendhal.server.maps.quests;

import java.util.HashMap;
import java.util.Map;

/**
 * Static info about quests (read from quest.xml).
 * 
 * @author hendrik
 */
public class QuestInfo {

	private String name;

	private String title;

	private boolean[] repeatable = new boolean[3];

	private String description;

	private String descriptionGM;

	private Map<String, String> history = new HashMap<String, String>();

	private Map<String, String> hints = new HashMap<String, String>();

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDescriptionGM() {
		return descriptionGM;
	}

	public void setDescriptionGM(final String descriptionGM) {
		this.descriptionGM = descriptionGM;
	}

	public Map<String, String> getHints() {
		return hints;
	}

	public void setHints(final Map<String, String> hints) {
		this.hints = hints;
	}

	public Map<String, String> getHistory() {
		return history;
	}

	public void setHistory(final Map<String, String> history) {
		this.history = history;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean[] getRepeatable() {
		return repeatable;
	}

	public void setRepeatable(final boolean[] repeatable) {
		this.repeatable = repeatable;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}
}
