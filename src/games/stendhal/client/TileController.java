package games.stendhal.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class TileController implements ObjectChangeListener {
	final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	//TODO: add 2 more for events and slots so you can add listeners distinguished
	// maybe extend this
	
	public TileController() {
		pcs.addPropertyChangeListener("bag", new BagController());

	}

	public void deleted() {
		for (final PropertyChangeListener listener : pcs.getPropertyChangeListeners()) {
			listener.propertyChange(null);
		}
	}

	public void modifiedAdded(final RPObject changes) {
		for (final String attrib : changes) {
			pcs.firePropertyChange(attrib, null, changes.get(attrib));
		}
		for (final RPEvent event : changes.events()) {
			pcs.firePropertyChange(event.getName(), null, event);
		}
		for (final RPSlot slot : changes.slots()) {
			pcs.firePropertyChange(slot.getName(), null, slot);
		}
	}

	public void modifiedDeleted(final RPObject changes) {
		for (final String attrib : changes) {
			pcs.firePropertyChange(attrib, changes.get(attrib), null);
		}
		for (final RPEvent event : changes.events()) {
			pcs.firePropertyChange(event.getName(), event, null);
		}
		for (final RPSlot slot : changes.slots()) {
			pcs.firePropertyChange(slot.getName(), slot, null);
		}

	}

}
