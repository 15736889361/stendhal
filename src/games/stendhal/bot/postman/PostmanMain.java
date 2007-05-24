/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.bot.postman;

import games.stendhal.client.update.Version;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marauroa.client.BannedAddressException;
import marauroa.client.TimeoutException;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;

/**
 * Starts Postman and connect to server.
 *
 * @author hendrik
 */
public class PostmanMain extends Thread {

	private String host;

	private String username;

	private String password;

	protected String character;

	private String port;

	protected Postman postman = null;

	protected long lastPerceptionTimestamp = 0;

	protected Map<RPObject.ID, RPObject> world_objects;

	protected marauroa.client.ClientFramework clientManager;

	protected PerceptionHandler handler;

	/**
	 * Creates a PostmanMain
	 *
	 * @param h host
	 * @param u username
	 * @param p password
	 * @param c character name
	 * @param P port
	 * @param t TCP?
	 * @throws SocketException on an network error
	 */
	public PostmanMain(String h, String u, String p, String c, String P) throws SocketException {
		host = h;
		username = u;
		password = p;
		character = c;
		port = P;

		world_objects = new HashMap<RPObject.ID, RPObject>();

		handler = new PerceptionHandler(new IPerceptionListener() {
			public boolean onAdded(RPObject arg0) {
	            return false;
            }

			public boolean onClear() {
	            return false;
            }

			public boolean onDeleted(RPObject arg0) {
	            return false;
            }

			public void onException(Exception e, MessageS2CPerception perception) {
				System.out.println(perception);
				System.err.println(perception);
				e.printStackTrace();
            }

			public boolean onModifiedAdded(RPObject arg0, RPObject arg1) {
	            return false;
            }

			public boolean onModifiedDeleted(RPObject arg0, RPObject arg1) {
	            return false;
            }

			public boolean onMyRPObject(RPObject arg0, RPObject arg1) {
	            return false;
            }

			public void onPerceptionBegin(byte arg0, int arg1) {
            }

			public void onPerceptionEnd(byte arg0, int arg1) {
            }

			public void onSynced() {
            }

			public void onUnsynced() {
            }
		});

		clientManager = new marauroa.client.ClientFramework("games/stendhal/log4j.properties") {

			@Override
			protected String getGameName() {
				return "stendhal";
			}

			@Override
			protected String getVersionNumber() {
				return Version.VERSION;
			}

			@Override
			protected void onPerception(MessageS2CPerception message) {
				lastPerceptionTimestamp = System.currentTimeMillis();
				try {
					handler.apply(message, world_objects);

					for (RPObject object : world_objects.values()) {
						if (object.has("private_text") || object.has("text")) {
							postman.processTalkEvent(object);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
				for (TransferContent item : items) {
					item.ack = true;
				}

				return items;
			}

			@Override
			protected void onServerInfo(String[] info) {
				// do nothing
			}
			
			@Override
			protected void onPreviousLogins(List<String> previousLogins) {
				
			}

			@Override
			protected void onAvailableCharacters(String[] characters) {
				try {
					chooseCharacter(character);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onTransfer(List<TransferContent> items) {
				// do nothing
			}
		};
	}

	@Override
	public void run() {
		try {
			clientManager.connect(host, Integer.parseInt(port));
			clientManager.login(username, password);
			PostmanIRC postmanIRC = new PostmanIRC(host);
			postmanIRC.connect();
			postman = new Postman(clientManager, postmanIRC);
			postman.startThread();
		} catch (SocketException e) {
			System.err.println("Socket Exception");
			Runtime.getRuntime().halt(1);
			return;
		} catch (TimeoutException e) {
			System.err.println("Cannot connect to Stendhal server. Server is down?");
			// TODO: shutdown cleanly
			//return;
			Runtime.getRuntime().halt(1);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace(System.err);
			Runtime.getRuntime().halt(1);
		}

		boolean cond = true;
		while (cond) {
			clientManager.loop(0);

			if ((lastPerceptionTimestamp > 0) && (lastPerceptionTimestamp + 10 * 1000 < System.currentTimeMillis())) {
				System.err.println("Timeout");
				Runtime.getRuntime().halt(1);
			}

			try {
				sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		try {
	        long start = System.currentTimeMillis();
	        while (clientManager.logout() == false) {
	        	/*
	        	 * Milliseconds to wait before exit.
	        	 */
	        	if (start + 20000 < System.currentTimeMillis()) {
	        		Runtime.getRuntime().halt(2);
	        	}
	        	try {
	        		sleep(100);
	        	} catch (InterruptedException e) {
	        		// ignore
	        	}
	        }
        } catch (InvalidVersionException e) {
	        e.printStackTrace();
        } catch (TimeoutException e) {
	        e.printStackTrace();
        } catch (BannedAddressException e) {
	        e.printStackTrace();
        }
	}

	/**
	 * Main entry point
	 *
	 * @param args see help
	 */
	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				int i = 0;
				String username = null;
				String password = null;
				String character = null;
				String host = null;
				String port = null;

				while (i != args.length) {
					if (args[i].equals("-u")) {
						username = args[i + 1];
					} else if (args[i].equals("-p")) {
						password = args[i + 1];
					} else if (args[i].equals("-c")) {
						character = args[i + 1];
					} else if (args[i].equals("-h")) {
						host = args[i + 1];
					} else if (args[i].equals("-P")) {
						port = args[i + 1];
					}
					i++;
				}

				if ((username != null) && (password != null) && (character != null) && (host != null) && (port != null)) {
					PostmanMain postmanMain = new PostmanMain(host, username, password, character, port);
					postmanMain.start();
					return;
				}
			}

			System.out.println("Stendhal textClient");
			System.out.println();
			System.out.println("  games.stendhal.bot.PostmanMain -u username -p pass -h host -P port -c character");
			System.out.println();
			System.out.println("Required parameters");
			System.out.println("* -h\tHost that is running Marauroa server");
			System.out.println("* -P\tPort on which Marauroa server is running");
			System.out.println("* -u\tUsername to log into Marauroa server");
			System.out.println("* -p\tPassword to log into Marauroa server");
			System.out.println("* -c\tCharacter used to log into Marauroa server");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
