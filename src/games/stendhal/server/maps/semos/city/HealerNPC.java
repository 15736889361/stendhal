package games.stendhal.server.maps.semos.city;

import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

/**
 * A young lady (original name: Carmen) who heals players without charge. 
 */
public class HealerNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addGreeting();
		npc.addJob("My special powers help me to heal wounded people. I also sell potions and antidotes.");
		npc.addHelp("I can #heal you here for free, or you can take one of my prepared medicines with you on your travels; just ask for an #offer.");
		npc.addSeller(new SellerBehaviour(ShopList.get().get("healing")));
		npc.addHealer(0);
		npc.addGoodbye();
	}
}
