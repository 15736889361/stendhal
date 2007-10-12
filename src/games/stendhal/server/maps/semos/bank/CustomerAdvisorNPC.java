package games.stendhal.server.maps.semos.bank;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

public class CustomerAdvisorNPC extends SpeakerNPCFactory {

	@Override
	protected void createDialog(SpeakerNPC npc) {
		npc.addGreeting("Welcome to the bank of Semos! Do you need #help on your personal chest?");
		npc.addHelp("Follow the corridor to the right, and you will find the magic chests. You can store your belongings in any of them, and nobody else will be able to touch them! A number of spells have been cast on the chest areas to ensure #safety, please ask me about this if you want to know more.");
		npc.addReply("safety","When you are standing at a chest to organise your items, any other people or animals will not be able to come near you. A magical aura stops others from using scrolls to arrive near you, although unfortunately this also means you cannot use scrolls to exit the bank. You will need to walk out. Lastly let me tell you about safe #trading.");
		npc.addReply("trading","There is a large table in the top right hand corner of this bank. It is designed so that trading can be done safely. Here is how to use it: Each take a chair and sit at opposite sides of the table. Once you have agreed a trade, place up to 3 items at once on the 3 tiles directly adjacent to you on the table. Wait until the other person has done the same. Make sure you can see exactly what they have placed and how much of each item. Then you swap places. The narrow corridors are designed so that noone else can take the items you have placed. If someone gets in the way you can just go back and remove your items from the table until the area is clear again. I hope that's all clear. If not, try asking another player for a demonstration. Oh, and by the way, there is a spell to make sure noone can return to this world next to the table. If they exit to the astral plane by the table and then attempt to return there, they are magically moved to a safer place."); 	
		npc.addJob("I'm the Customer Advisor here at Semos Bank.");
		npc.addGoodbye("It was a pleasure to serve you.");
	}
}
