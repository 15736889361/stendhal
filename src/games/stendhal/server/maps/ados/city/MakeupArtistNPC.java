package games.stendhal.server.maps.ados.city;

import games.stendhal.common.Direction;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.OutfitChangerBehaviour;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates the NPCs and portals in Ados City.
 *
 * @author hendrik
 */
public class MakeupArtistNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildFidorea(zone);
	}

	private void buildFidorea(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Fidorea") {

			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi, there. Do you need #help with anything?");
				addHelp("If you don't like your mask, you can #return and I will remove it, or you can just wait until it wears off.");
				addQuest("Are you looking for toys for Anna? She loves my costumes, perhaps she'd like a #dress to try on. If you already got her one, I guess she'll have to wait till I make more costumes!"); // this is a hint that one of the items Anna wants is a dress (goblin dress)
				addJob("I am a makeup artist.");
				addReply(
				        "dress",
				        "I read stories that goblins wear a dress as a kind of armor! If you're scared of goblins, like me, maybe you can buy a dress somewhere. ");
				//addReply("offer", "Normally I sell masks. But I ran out of clothes and cannot by new ones until the cloth seller gets back from his search.");
				addGoodbye("Bye, come back soon.");

				Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("mask", 2);
				OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList, 100, "Your mask has worn off.");
				addOutfitChanger(behaviour, "buy");
			}
		};
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		npc.put("class", "woman_008_npc");
		npc.set(20, 12);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);

	}
}
