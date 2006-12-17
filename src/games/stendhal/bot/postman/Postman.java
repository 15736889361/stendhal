/* CC-BY Hendrik Brummermann <nhb_web@nexgo.de>
 * (But becomes GPL because it is required to link against GPL code).
 */
package games.stendhal.bot.postman;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import marauroa.client.ariannexp;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/**
 * Postman
 *
 * @author hendrik
 */
public class Postman implements Runnable {
    private static Logger logger = Logger.getLogger(Postman.class);
    private Properties messages = new Properties();
    private ariannexp clientManager;
    private PostmanIRC postmanIRC;
    
    /**
     * Creates a new postman
     *
     * @param clientManager ClientManager
     * @param postmanIRC postmanIRC
     */
    public Postman(ariannexp clientManager, PostmanIRC postmanIRC) {
        this.clientManager = clientManager;
        this.postmanIRC = postmanIRC;
        
        //shout("Please restart your client every hour or so to save your progress. We have some trouble with server crashes.");
        
        try {
            this.messages.loadFromXML(new FileInputStream(System.getProperty("user.home") + "/.stendhal-postman.xml"));
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    /**
     * Starts the /who thread
     */
    public void startThread() {
        Thread t = new Thread(this, "Postman");
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Processes a talk event.
     *
     * @param object
     */
    public void processTalkEvent(RPObject object) {
        try {
            if (object == null) {
                return;
            }
            String greeting = "Hi, I am the postman. How can I #help you?";
            String intro = "I store messages for offline players and deliver them on login.\n";
            String helpMessage = "Usage:\n/msg postman help \t This help-message\n/msg postman tell #player #message \t I will deliver your #message when #player logs in.";
            if (object.getRPClass().getName().equals("player") && object.has("name")) {
            
	            if (object.has("private_text")) {
	                if (object.get("name").equals("postman")) {
	                    String text = object.get("private_text");

	                    java.text.Format formatter = new java.text.SimpleDateFormat("[HH:mm] ");
	                    String dateString = formatter.format(new Date());
	                    System.err.println(dateString + text);

	                    StringTokenizer st = new StringTokenizer(text, " ");
	                    String from = st.nextToken();
	                    String arianneCmd = st.nextToken(); // tells
	                    st.nextToken(); // you:
	                    //System.out.println(text);

	                    if (from.endsWith(":")) {
	                    	postmanIRC.sendMessage("#arianne-support", text);
	                    }
	                    if (arianneCmd.equals("tells")) {
	                        // Command was send by a player 
	                        String cmd = st.nextToken(); // cmd
	                        if (cmd.startsWith("/")) {
	                            cmd = cmd.substring(1);
	                        }
	                        if (cmd.equalsIgnoreCase("tell") || cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("/tell") || cmd.equalsIgnoreCase("/msg")) {
	                            onTell(from, st);
	                        } else if (cmd.equalsIgnoreCase("hi")) {
	                            tell(from, greeting); 
	                        } else if (cmd.equalsIgnoreCase("help") || cmd.equalsIgnoreCase("info") || cmd.equalsIgnoreCase("job") || cmd.equalsIgnoreCase("letter") || cmd.equalsIgnoreCase("offer") || cmd.equalsIgnoreCase("parcel")) {
	                            tell(from, intro + helpMessage);
	                        } else if (cmd.equalsIgnoreCase("where")) {
	                            onWhere();
	                        } else {
	                            tell(from, "Sorry, I did not understand you. (Did you forget the \"tell\"?)\n" + helpMessage);
	                        }
	                    } else if (arianneCmd.equals("Players")) {
	                        onWhoResponse(st);
	                    } else if (arianneCmd.equalsIgnoreCase("shouts:")) {
	                    	postmanIRC.sendMessage("#arianne", text);
	                    	postmanIRC.sendMessage("#arianne-support", text);
	                    } else if (arianneCmd.equalsIgnoreCase("asks") || arianneCmd.equalsIgnoreCase("answers") || arianneCmd.equalsIgnoreCase("answer")) {
	                    	// answer is a typo in old server
	                    	postmanIRC.sendMessage("#arianne-support", text);
	                    	if (arianneCmd.equalsIgnoreCase("asks")) {
	                    		dumpPlayerPosition();
	                    	}
	                    }
	                }

	            // Public message
	            } else if (object.has("text")) {
	                if (!object.get("name").equals("postman")) {
	                    String text = object.get("text");
	                    String playerName = "";
                        playerName = object.get("name");

	                    java.text.Format formatter = new java.text.SimpleDateFormat("[HH:mm] ");
	                    String dateString = formatter.format(new Date());
	                    System.err.println(dateString + playerName + ": " + text);

	                    StringTokenizer st = new StringTokenizer(text, " ");
	                    String cmd = "";
	                    if (st.hasMoreTokens()) {
	                        cmd = st.nextToken();
	                    }
	                    if (cmd.equalsIgnoreCase("hi")) {
	                        chat(greeting);
	                    } else if (cmd.equalsIgnoreCase("bye")) {
	                        chat("Bye.");
	                    } else if (cmd.equalsIgnoreCase("help") || cmd.equalsIgnoreCase("info") || cmd.equalsIgnoreCase("job") || cmd.equalsIgnoreCase("offer") || cmd.equalsIgnoreCase("letter") || cmd.equalsIgnoreCase("parcel")) {
	                        chat(intro + helpMessage);
	                    } else if (cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("tell")) {
	                        onTell(playerName, st);
	                    }
	                }
	            }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e, e);
        }
    }

	/**
     * response to "who"
     */
    private void onWhoResponse(StringTokenizer st) {
    	String lastUserPart = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            //System.err.println("Player: " + token);
            int pos = token.indexOf("(");
            if (pos < 0) {
            	lastUserPart = lastUserPart + " " + token;
            	continue;
            }
            String user = lastUserPart + token.substring(0, pos);
            lastUserPart = "";

            // Are there messages for this player? 
            Iterator itr = messages.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next().toString();
                if (key.startsWith(user + "!")) {
                    String from = key.substring(key.indexOf("!") + 1);
                    String message = messages.getProperty(key);
                    if (from.equals(user)) {
                        from = "You";
                    }
                    tell(user, from + " asked me to deliver this message: \n" + message.trim());
                    itr.remove();
                    break; // workaround: Only the last message processed in one turn is delievered
                }
            }
        }

        // Save to disk
        try {
            messages.storeToXML(new FileOutputStream(System.getProperty("user.home") + "/.stendhal-postman.xml"), "These are the messages postman should deliver.");
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    private void onTell(String from, StringTokenizer st) {
        String param = null;
        String msg = null;
        //System.err.println("!" + from + "! !" + cmd + "! !" + msg + "!");
        if (st.hasMoreTokens()) {
            param = st.nextToken(); // player
        }
        if (st.hasMoreTokens()) {
            msg = st.nextToken("\0").trim(); // the rest of the message
        }
        String old = messages.getProperty(param + "!" + from);
        tell(from, "Message accepted for delivery.");
        if (old != null) {
            //tell(from, "I am discarding your previous message to " + param + ": " + old);
            msg = old + "\n" + msg;
        }
        messages.put(param + "!" + from, msg);

        // Save to disk
        try {
            messages.storeToXML(new FileOutputStream(System.getProperty("user.home") + "/.stendhal-postman.xml"), "These are the messages postman should deliver.");
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    private void onWhere() {
        RPAction who = new RPAction();
        who.put("type","who");
        send(who);
    }

    private void tell(String to, String message) {
        if (to.equals("postman")) {
            logger.warn("I am not speaking to myself: " + message);
            return;
        }
        RPAction tell = new RPAction();
        tell.put("type","tell");
        tell.put("target", to);
        tell.put("text", message);
        send(tell);
    }

    private void chat(String message) {
        RPAction chat=new RPAction();
        chat.put("type","chat");
        chat.put("text", message);
        send(chat);
    }

    @SuppressWarnings("unused")
    private void shout(String message) {
        RPAction chat=new RPAction();
        chat.put("type","tellall");
        chat.put("text", message);
        send(chat);
    }

    private void dumpPlayerPosition() {
        RPAction chat=new RPAction();
        chat.put("type", "script");
        chat.put("target", "PlayerPositionMonitoring.class");
        send(chat);
	}

    
    public void run() {
        while (true) {
            RPAction who = new RPAction();
            who.put("type","who");
            send(who);
            
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                logger.error(e, e);
            }
        }
    }
    
    private void send(RPAction action) {
        clientManager.send(action);
    }
}
