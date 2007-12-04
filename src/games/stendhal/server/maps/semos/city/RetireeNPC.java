package games.stendhal.server.maps.semos.city;

import games.stendhal.common.Rand;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.player.Player;

/**
 * A crazy old man (original name: Diogenes) who walks around the city.
 */ 
public class RetireeNPC  extends SpeakerNPCFactory {

	@Override
	public void createDialog(SpeakerNPC npc) {
		npc.addGreeting();
		npc.addJob("Ha ha! Job? I retired decades ago! Ha ha!");
		npc.addHelp("I can't help you, but you can help Stendhal; tell all your friends, and help out with development! Visit http://arianne.sourceforge.net and see how you can help!");
		npc.addGoodbye();
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
		        ConversationStates.ATTENDING,
		        null,
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        if (Rand.throwCoin() == 1) {
					        npc.say("Ah, quests... just like the old days when I was young! I remember one quest that was about... Oh look, a bird! Hmm, what? Ah, quests... just like the old days when I was young!");
				        } else {
				        	npc.say("You know that Sato over there buys sheep? Well, rumour has it that there's a creature deep in the dungeons who also buys sheep... and it pays much better than Sato, too!");
				        }
			        }
		        });

		// A convenience function to make it easier for admins to test quests.
		npc.add(ConversationStates.ATTENDING, "cleanme!", null, ConversationStates.ATTENDING, "What?",
		        new SpeakerNPC.ChatAction() {
			        @Override
			        public void fire(Player player, String text, SpeakerNPC npc) {
				        if (AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, "alter", false)) {
					        for (String quest : player.getQuests()) {
						        player.removeQuest(quest);
					        }
				        } else {
					        npc.say("What? No; you clean me! Begin with my back, thanks.");
					        player.damage(5, npc);
					        player.notifyWorldAboutChanges();
				        }
			        }
		        });
	}
}
