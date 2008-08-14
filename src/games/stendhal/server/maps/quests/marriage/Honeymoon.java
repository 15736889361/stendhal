package games.stendhal.server.maps.quests.marriage;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatCondition;
import games.stendhal.server.entity.npc.parser.Expression;
import games.stendhal.server.entity.npc.parser.JokerExprMatcher;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;

import marauroa.common.game.IRPZone;

class Honeymoon {
	private final NPCList npcs = SingletonRepository.getNPCList();
	private MarriageQuestInfo marriage;

	public Honeymoon(MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}

	private void honeymoonStep() {
		final SpeakerNPC linda = npcs.get("Linda");
		// tell her you want a honeymoon
		linda.add(
				ConversationStates.ATTENDING,
				"honeymoon",
				null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
                        final IRPZone fadoHotel = npc.getZone();
                        final Area hotelReception = new Area(fadoHotel, new Rectangle(11, 46, 19, 10));

                        Player husband;
                        Player wife;
                        String partnerName;
                        husband = player;
                        partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
                        wife = SingletonRepository.getRuleProcessor().getPlayer(partnerName);
                        
						if (!(player.hasQuest(marriage.getQuestSlot())) || !("just_married".equals(player.getQuest(marriage.getQuestSlot())))) {
							// person is not just married
							npc.say("Sorry, our honeymoon suites are only available for just married customers.");
							npc.setCurrentState(ConversationStates.ATTENDING);						
						} else if (wife == null) { 
							//wife is not online
                            npc.say("Come back when " + partnerName + " is with you - you're meant to have your honeymoon together!");
                            npc.setCurrentState(ConversationStates.IDLE);
                        } else if (!(wife.hasQuest(marriage.getQuestSlot())
                                     && wife.getQuest(marriage.getSpouseQuestSlot()).equals(husband.getName()))) {
                        	//wife is not married to this husband
                            npc.say("Oh dear, this is embarassing. You seem to be married, but " + partnerName + " is not married to you.");
                            npc.setCurrentState(ConversationStates.ATTENDING);
                        } else if (!hotelReception.contains(wife)) {
                        	//  wife has not bothered to come to reception desk
                            npc.say("Could you get " + partnerName + " to come to the reception desk, please. Then please read our catalogue here and tell me the room number that you would like.");
                        }  else { 
                        	//wife and husband fulfill all conditions
							npc.say("How lovely! Please read our catalogue here and tell me the room number that you would like.");
						}
					}
				});
		// player says room number
		linda.add(ConversationStates.QUESTION_1,
				// match for all numbers as trigger expression
				"NUM", new JokerExprMatcher(),
				new ChatCondition() {
					@Override
                    public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						final Expression number = sentence.getNumeral();

						if (number != null) {
    						final int roomNr = number.getAmount();

    						// check for correct room numbers
    						if ((roomNr >= 1) && (roomNr <= 15)) {
    							return true;
    						}
						}

    					return false;
                    }
				}, ConversationStates.IDLE, null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {

                        final String room = Integer.toString(sentence.getNumeral().getAmount());
                        final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
                                                                                       "int_fado_lovers_room_" + room);
						if (zone.getPlayers().size() > 0) {
							npc.say("Sorry, that room is currently occupied, would you give me your next choice please?");
							npc.setCurrentState(ConversationStates.QUESTION_1);
						} else {

							Player husband;
							Player wife;
							String partnerName;
							husband = player;
							partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
							wife = SingletonRepository.getRuleProcessor().getPlayer(
                                                                                partnerName);
							// I (kym) have to make two of these because when I only did one, the second one sometimes had quantity 0 sometimes didn't.
							final StackableItem invite1 = (StackableItem) SingletonRepository.getEntityManager().getItem(
																												  "invitation scroll");
							invite1.setQuantity(1);
                            final StackableItem invite2 = (StackableItem) SingletonRepository.getEntityManager().getItem(
                                                                                                                  "invitation scroll");
                            invite2.setQuantity(1);
                            
							invite1.setInfoString("int_fado_hotel_0 4 40");
							invite2.setInfoString("int_fado_hotel_0 4 40");
							if (wife.equip(invite1) &&  husband.equip(invite2)) {
								npc.say("Great choice! I will arrange that now."); 
								husband.setQuest(marriage.getQuestSlot(), "done");
								wife.setQuest(marriage.getQuestSlot(), "done");
								wife.teleport(zone, 5, 5, Direction.DOWN, player);
								husband.teleport(zone, 6, 5, Direction.DOWN, player);
								final String scrollmessage = "Linda tells you: Use the scroll in your bag to return to the hotel, our special honeymoon suites are so private that they don't use normal entrances and exits!";
								wife.sendPrivateText(scrollmessage);
                                husband.sendPrivateText(scrollmessage);
								wife.notifyWorldAboutChanges();
								husband.notifyWorldAboutChanges();
								npc.setCurrentState(ConversationStates.IDLE);
							} else {
								npc.say("You each need one space in your bags to take a scroll. Please make a space and then ask me again. Thank you.");
							}
						}
					}
				});

		// player says something which isn't a room number
//		npc.add(ConversationStates.QUESTION_1, "",
//			new SpeakerNPC.ChatCondition() {
//				@Override public boolean fire(Player player, Sentence sentence, SpeakerNPC npc) {
//					return !ROOMS.contains(text);
//				}
//			}, ConversationStates.QUESTION_1,
//			"Sorry, that's not a room number we have available.", null
//		);

	}

	public void addToWorld() {
		honeymoonStep();
	}

}
