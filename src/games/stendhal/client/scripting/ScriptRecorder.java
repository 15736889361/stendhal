package games.stendhal.client.scripting;

import games.stendhal.client.StendhalUI;
import games.stendhal.client.entity.User;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Record chat/commands
 *
 * @author hendrik
 */
public class ScriptRecorder {

	private String classname ;

	private String filename ;

	private PrintStream ps ;

	private long lastTimestamp ;

	/**
	 * Creates a new ScriptRecorder
	 *
	 * @param classname Name of Class to record
	 * @throws FileNotFoundException if the file cannot be created
	 */
	public ScriptRecorder(String classname) throws FileNotFoundException {
		this.classname = classname;
		filename = System.getProperty("java.io.tmpdir") + "/" + classname + ".java";
		StendhalUI.get().addEventLine("Starting recoding to " + filename);
		lastTimestamp = 0;
		ps = new PrintStream(filename);
	}

	/**
	 * Starts the recording by writing the header
	 */
	public void start() {
		ps.println("package games.stendhal.client.script;");
		ps.println("import games.stendhal.client.scripting.*;");
		ps.println("/**");
		ps.println(" * TODO: write documentation");
		ps.println(" * ");
		ps.println(" * @author recorded by " + User.get().getName());
		ps.println(" */");
		ps.println("public class " + classname + " extends ClientScriptImpl {");
		ps.println("");
		ps.println("\t@Override");
		ps.println("\tpublic void run(String args) {");
		lastTimestamp = System.currentTimeMillis();
	}

	/**
	 * Records a chat/command
	 *
	 * @param text command to record
	 */
	public void recordChatLine(String text) {

		// ignore recording related commands
		if (text.startsWith("/record")) {
			return;
		}

		// write sleep command (and add a paragraph if the wait time was large
		long thisTimestamp = System.currentTimeMillis();
		long diff = thisTimestamp - lastTimestamp;
		if (diff > 5000) {
			ps.println("");
			if (diff > 60000) {
				ps.println("\t\t// -----------------------------------");
			}
			ps.println("\t\tcsi.sleepSeconds(" + (diff / 1000) + ");");
		} else if (diff > 0) {
			ps.println("\t\tcsi.sleepMillis(" + diff + ");");
		}

		// write invoke command
		ps.println("\t\tcsi.invoke(\"" + text.replace("\"", "\\\"") + "\");");

		// reduce wait time by one turn because csi.invokes waits one turn
		lastTimestamp = thisTimestamp + 300;
	}

	/**
	 * finishes the recording by writing the footer and closing the stream.
	 */
	public void end() {
		ps.println("\t}");
		ps.println("}");
		ps.close();
		StendhalUI.get().addEventLine("Stoping recoding to " + filename);
	}
}
