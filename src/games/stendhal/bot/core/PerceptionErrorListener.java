package games.stendhal.bot.core;

import marauroa.client.net.IPerceptionListener;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CPerception;

/**
 * Logs errors during perception handling to stdout and stderr.
 */
public class PerceptionErrorListener implements IPerceptionListener {
	public boolean onAdded(final RPObject object) {
		return false;
	}

	public boolean onClear() {
		return false;
	}

	public boolean onDeleted(final RPObject object) {
		return false;
	}

	public void onException(final Exception exception,
			final MessageS2CPerception perception) {
		System.out.println(perception);
		System.err.println(perception);
		if (exception != null) {
			exception.printStackTrace();
		}
	}

	public boolean onModifiedAdded(final RPObject object, final RPObject changes) {
		return false;
	}

	public boolean onModifiedDeleted(final RPObject object, final RPObject changes) {
		return false;
	}

	public boolean onMyRPObject(final RPObject added, final RPObject deleted) {
		return false;
	}

	public void onPerceptionBegin(final byte type, final int timestamp) {
		// ignore
	}

	public void onPerceptionEnd(final byte type, final int timestamp) {
		// ignore
	}

	public void onSynced() {
		// ignore
	}

	public void onUnsynced() {
		// ignore
	}
}
