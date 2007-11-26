package games.stendhal.server.scripting;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.List;
import java.util.Map;

public class ScriptingNPC extends SpeakerNPC {

	public ScriptingNPC(String name) {
		super(name);
		initHP(100);
	}


	// TODO: use message constants from Behaviours.java
	public void behave(String method, String reply) {
		if ("greet".equalsIgnoreCase(method)) {
			addGreeting(reply);
		} else if ("job".equalsIgnoreCase(method)) {
			addJob(reply);
		} else if ("help".equalsIgnoreCase(method)) {
			addHelp(reply);
		} else if ("quest".equalsIgnoreCase(method)) {
			addQuest(reply);
		} else if ("bye".equalsIgnoreCase(method)) {
			addGoodbye(reply);
		} else {
			addReply(method, reply);
		}
	}

	public void behave(String method, List<String> triggers, String reply) throws NoSuchMethodException {
		if ("reply".equalsIgnoreCase(method)) {
			addReply(triggers, reply);
		} else {
			throw new NoSuchMethodException("Behaviour.add(" + method + ") not supported.");
		}
	}

	public void behave(List<String> triggers, String reply) {
		addReply(triggers, reply);
	}

	public void behave(String method, Map<String, Integer> items) throws NoSuchMethodException {
		if ("buy".equalsIgnoreCase(method)) {
			new BuyerAdder().add(this, new BuyerBehaviour(items), true);
		} else if ("sell".equalsIgnoreCase(method)) {
			addSeller(new SellerBehaviour(items));
		} else {
			throw new NoSuchMethodException("Behaviour.add(" + method + ") not supported.");
		}
	}

	public void behave(String method, int cost) throws NoSuchMethodException {
		if ("heal".equalsIgnoreCase(method)) {
			addHealer(cost);
		} else {
			throw new NoSuchMethodException("Behaviour.add(" + method + ") not supported.");
		}
	}

	@Override
	protected void createPath() {
		// do nothing
	}

	@Override
	protected void createDialog() {
		// do nothing
	}

}
