package games.stendhal.server.maps.semos.caves;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.config.ZoneConfigurator;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;
import games.stendhal.server.util.TimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BabyDragonSellerNPC implements ZoneConfigurator {

	private static final String QUEST_SLOT = "hatching_dragon";
	// A baby dragon takes this long to hatch
	private static final int REQUIRED_DAYS = 7;
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(StendhalRPZone zone) {

		SpeakerNPC npc = new SpeakerNPC("Terry") {
			@Override
			protected void createPath() {
			      	List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(66, 8));
				nodes.add(new Node(69, 8));
				nodes.add(new Node(69, 17));
				nodes.add(new Node(74, 17));
				nodes.add(new Node(74, 11));
				nodes.add(new Node(73, 11));
				nodes.add(new Node(73, 10));
				nodes.add(new Node(72, 10));
				nodes.add(new Node(72, 9));
				nodes.add(new Node(66, 9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
					    if (player.hasQuest(QUEST_SLOT)){
						// TODO: Remove the comments on numbers before commit!!
						long delay = REQUIRED_DAYS * 60 */* 60 * 24 */ 1000;
						long timeRemaining = (Long.parseLong(player.getQuest(QUEST_SLOT))
								      + delay) - System.currentTimeMillis();
						if (timeRemaining > 0L) {
						    engine.say("The egg is still hatching, and will be for at least another "
										+ TimeUtil.timeUntil((int) (timeRemaining / 1000L))
										+ ".");
								return;
					        }
						engine.say("Your egg has hatched! So, here you go, a nippy little baby dragon of your own. Don't forget it'll want some #food soon. And remember to #protect it.");

					       	BabyDragon babydragon = new BabyDragon(player);

					       	babydragon.setPosition(engine.getX(),engine.getY() + 1);

					       	StendhalRPZone zone = engine.getZone();
					       	zone.add(babydragon);

					       	player.setPet(babydragon);
						// clear the quest slot completely when it's not
						// being used to store egg hatching times
						player.removeQuest(QUEST_SLOT);
					       	player.notifyWorldAboutChanges();

					    }
					    else if (player.isEquipped("mythical_egg")){
						        engine.say("Where did you get that egg from?! Never mind. Tell me if you need me to #hatch it for you. It is my hobby, after all.");
					    } else {
							engine.say("Hi. I don't get so many visitors, down here.");
					    }
					}
				});
			        addReply("hatch", null, new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
					    if (!player.hasPet()) {
						if (player.isEquipped("mythical_egg")) {
						    player.drop("mythical_egg");
						    engine.say("Ok, I'll take your egg and hatch it in one of these nesting boxes. Come back in " + 7 + " days and you should be the proud owner of a new born baby dragon.");
						    player.setQuest(QUEST_SLOT,Long.toString(System.currentTimeMillis()));
						    player.notifyWorldAboutChanges();
						} else {
						    engine.say("You don't have any dragon eggs with you. I can't hatch a dragon without an egg.");
						}
					    } else {
							engine.say("You've already got a pet. If you get another they might fight ... or worse ...");
					   }
					}
				    });
				addJob("I breed baby dragons. You need an egg to get one #hatched.");
				addQuest("If you can get a dragon egg, I will #hatch it for you.");
				addHelp("I rear baby dragons. If you have an egg, I'll #hatch it. I can also tell you how to #travel with a pet and take #care of it. If you find any wild baby dragon, incidentally, you can make it your #own.");
				addGoodbye("Watch out for the giants on your way out!");
				addReply("food","Baby dragons feed on meat and ham. Their particular favourite is pizza, if you can get it.");
				addReply("care",
						"Baby dragons eat meat, ham and even pizza. Just place a piece on the ground and the dragon will run over to eat it. You can right-click on it and choose 'Look' at any time, to see its weight. They gain one unit of weight for every piece of food they eat.");
				addReply("travel",
						"You'll need your baby dragon to be close by in order for it to follow you when you change zones; you can say #pet to call it if it's not paying attention. If you decide to abandon it, you can right-click on YOURSELF and select 'Leave Pet'.");
				addReply("protect",
					 "Other creatures can small the strong scent of your baby dragon, and may attack it. It will fight back to defend itself but at times it will need help, or it will surely die.");
				addReply("own",
						"Like all pets and sheep, if you find any wild or abandoned baby dragon, you can right-click on them and select 'Own' to tame them. It will start following you, and I can bet you it'll want #food pretty soon.");
			}
		};

		npc.setEntityClass("man_005_npc");
		npc.setPosition(66, 8);
		npc.initHP(100);
		zone.add(npc);

		// Also put a dragon in the caves (people can't Own it as it is behind rocks)
		BabyDragon drag = new BabyDragon();
                drag.setPosition(62, 8);
                zone.add(drag);
	}
}
