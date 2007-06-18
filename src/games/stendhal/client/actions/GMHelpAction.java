package games.stendhal.client.actions;

import games.stendhal.client.StendhalUI;

import java.awt.Color;

/**
 * Display command usage.
 * Eventually replace this with ChatCommand.usage().
 */
class GMHelpAction implements SlashAction  {

	/**
	 * Execute a chat command.
	 *
	 * @param	params		The formal parameters.
	 * @param	remainder	Line content after parameters.
	 *
	 * @return	<code>true</code> if  was handled.
	 */
	public boolean execute(String[] params, String remainder) {
		String[] lines = {
		        "For a detailed reference, visit #http://arianne.sourceforge.net/wiki/index.php?title=Stendhal:Administration",
		        "Here are the most-used GM commands:",
		        "- /adminlevel <player> [<newlevel>] \t\tDisplay or set the adminlevel of the specified <player>",
		        "- /tellall <message> \t\tSend a private message to all logged-in players",
		        "- /jail <player> <minutes> <reason>\t\tImprisons the player for a given length of time",
		        "- /gag <player> <minutes> <reason>\t\tGags the player for a given length of time (player is unable to send messages to anyone)",
		        "- /script <scriptname> \t\tLoad (or reload) a script on the server",
		        "- /teleport <player> <zone> <x> <y> \tTeleport the specified <player> to the given location",
		        "- /teleportto <player> \t\tTeleport yourself near the specified player",
		        "- /teleclickmode \t\t\t Makes you teleport to the location you double click",
		        "- /ghostmode \t\t\t Makes yourself invisible and intangible",
		        "- /alter <player> <attrib> <mode> <value> \tAlter stat <attrib> of <player> by the given amount; <mode> can be ADD, SUB, or SET. See /gmhelp_alter for details",
		        "- /altercreature <id> name/atk/def/hp/xp \tChange all the values of the creature. Useful in raids."
		        "- /summon <creature|item> <x> <y> \tSummon the specified item or creature at co-ordinates <x>, <y> in the current zone",
		        "- /summonat <player> <slot> <item> <amount> Summon the specified item into the specified slot of <player>; <amount> defaults to 1 if not specified",
		        "- /invisible \t\t\tToggles whether or not you are invisible to creatures",
		        "- /inspect <player> \t\t\tShow complete details of <player>",
		        "- /destroy <entity> \t\t\tDestroy an entity completely" };

		for (String line : lines) {
			StendhalUI.get().addEventLine(line, Color.gray);
		}

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return	The parameter count.
	 */
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return	The parameter count.
	 */
	public int getMinimumParameters() {
		return 0;
	}
}
