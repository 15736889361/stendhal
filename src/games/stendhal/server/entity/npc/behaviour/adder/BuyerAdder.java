package games.stendhal.server.entity.npc.behaviour.adder;

import games.stendhal.common.Grammar;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.log4j.Logger;

public class BuyerAdder {
	private static Logger logger = Logger.getLogger(BuyerAdder.class);

	public void add(final SpeakerNPC npc, final BuyerBehaviour behaviour,
			final boolean offer) {
		final Engine engine = npc.getEngine();

		if (offer) {
			engine.add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"I buy "
							+ Grammar.enumerateCollection(behaviour.dealtItems())
							+ ".", null);
		}

		engine.add(ConversationStates.ATTENDING, "sell", null,
				ConversationStates.SELL_PRICE_OFFERED, null,
				new SpeakerNPC.ChatAction() {

					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						if (sentence.hasError()) {
							engine.say("Sorry, I did not understand you. "
									+ sentence.getErrorString());
							engine.setCurrentState(ConversationStates.ATTENDING);
							return;
						}

						if (behaviour.parseRequest(sentence)) {
							if (behaviour.getAmount() > 1000) {
								logger.warn("Decreasing very large amount of "
										+ behaviour.getAmount()
										+ " " + behaviour.getChosenItemName()
										+ " to 1 for player "
										+ player.getName() + " talking to "
										+ engine.getName() + " saying "
										+ sentence);
								behaviour.setAmount(1);
							}

							if (behaviour.getAmount() > 0) {
								final int price = behaviour.getCharge(engine, player);

								if (price != 0) {
	    							engine.say(Grammar.quantityplnoun(behaviour.getAmount(), behaviour.getChosenItemName())
	    									+ " " + Grammar.isare(behaviour.getAmount()) + " worth "
	    									+ price + ". Do you want to sell "
	    									+ Grammar.itthem(behaviour.getAmount()) + "?");
								}
							} else {
								engine.say("Sorry, how many " + Grammar.plural(behaviour.getChosenItemName()) + " do you want to sell?!");

    							engine.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							if (behaviour.getChosenItemName() == null) {
								engine.say("Please tell me what you want to sell.");
							} else {
								engine.say("Sorry, I don't buy any "
										+ Grammar.plural(behaviour.getChosenItemName()) + ".");
							}

							engine.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, "Thanks.",
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final SpeakerNPC engine) {
						logger.debug("Buying something from player "
								+ player.getName());

						behaviour.transactAgreedDeal(engine, player);
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Ok, then how else may I help you?", null);
	}

}
