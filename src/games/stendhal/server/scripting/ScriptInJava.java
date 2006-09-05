// *********************************
//   unfinished experimental stuff
// *********************************

package games.stendhal.server.scripting;

import games.stendhal.server.entity.Player;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Manager for scripts written in Java.
 *
 * @author hendrik
 */
public class ScriptInJava extends ScriptingSandbox {
	private static Logger logger = Logger.getLogger(ScriptInJava.class);
	private Script script = null;
	private String classname = null;

	/**
	 * Creates a new script written in Java.
	 *
	 * @param scriptname Name of the script
	 */
	public ScriptInJava(String scriptname) {
		super(scriptname);
		this.classname = "games.stendhal.server.script." + scriptname.substring(0, scriptname.length() - 6);
	}

	/**
	 * creates a new instance of the script
	 *
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	private void newInstance() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		   // Create new class loader 
		   // with current dir as CLASSPATH
		   File file = new File("./data/script");
		   ClassLoader loader = new URLClassLoader(new URL[] {file.toURL()});
		   // load class through new loader
		   Class aClass = loader.loadClass(classname);
		   script = (Script) aClass.newInstance();
	}


	@Override
	public boolean load(Player admin, String[] args) {
		Class[] signature = new Class[] {Player.class, List.class, ScriptingSandbox.class};
		Object[] params = new Object[] {admin, Arrays.asList(args), this};

		try {
			newInstance();
			Method[] methods = Script.class.getMethods();
			for (Method method : methods) {
				logger.warn(method);
			}
			Method theMethod = Script.class.getMethod("load", signature);
			theMethod.invoke(script, params);
		} catch (Exception e) {
			logger.error(e, e);
			setMessage(e.toString());
			return false;
		}
		return true;
	}

	@Override
	public boolean execute(Player admin, String[] args) {
		Class[] signature = new Class[] {Player.class, List.class};
		Object[] params = new Object[] {admin, Arrays.asList(args)};

		try {
			Method theMethod = script.getClass().getDeclaredMethod("execute", signature);
			theMethod.invoke(script, params);
		} catch (Exception e) {
			logger.error(e, e);
			setMessage(e.getMessage());
			return false;
		}
		return true;
	}
	
	
	@Override
	public void unload() {
		Class[] signature = new Class[] {};
		Object[] params = new Object[] {};
		try {
			Method theMethod = script.getClass().getDeclaredMethod("unload", signature);
			theMethod.invoke(script, params);
		} catch (Exception e) {
			logger.error(e, e);
			setMessage(e.getMessage());
		}

		super.unload();
	}

	
}
