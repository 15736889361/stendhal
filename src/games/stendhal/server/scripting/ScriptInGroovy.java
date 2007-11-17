package games.stendhal.server.scripting;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.player.Player;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.util.HashMap;


import org.apache.log4j.Logger;

public class ScriptInGroovy extends ScriptingSandbox {

	private String groovyScript;

	private Binding groovyBinding;

	private static final Logger logger = Logger.getLogger(ScriptInGroovy.class);

	public ScriptInGroovy(String filename) {
		super(filename);
		groovyScript = filename;
		groovyBinding = new Binding();
		groovyBinding.setVariable("game", this);
		groovyBinding.setVariable("logger", logger);
		groovyBinding.setVariable("storage", new HashMap<Object,Object>());

		// TODO: get rid of these variables, use the Singleton getters
		// in the scripts
		groovyBinding.setVariable("rules", StendhalRPRuleProcessor.get());
		groovyBinding.setVariable("world", StendhalRPWorld.get());
	}

	// ------------------------------------------------------------------------

	@Override
	public boolean load(Player player, String[] args) {
		groovyBinding.setVariable("player", player);
		groovyBinding.setVariable("args", args);
		GroovyShell interp = new GroovyShell(groovyBinding);
		boolean ret = true;
		
		try {
			File f = new File(groovyScript);
			interp.evaluate(f);
		} catch (Exception e) {
			logger.error("Exception while sourcing file " + groovyScript, e);
			setMessage(e.getMessage());
			ret = false;
		} catch (Error e) {
			logger.error("Exception while sourcing file " + groovyScript, e);
			setMessage(e.getMessage());
			ret = false;
		}
		
		return (ret);
	}

	@Override
	public boolean execute(Player player, String[] args) {
		return load(player, args);
	}
}
