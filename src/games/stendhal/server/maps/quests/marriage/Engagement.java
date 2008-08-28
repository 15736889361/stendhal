package games.stendhal.server.maps.quests.marriage;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;

import marauroa.common.game.IRPZone;

class Engagement {
	private MarriageQuestInfo marriage;
	
	private final NPCList npcs = SingletonRepository.getNPCList();
	private SpeakerNPC nun;

	private Player groom;
	private Player bride;
	
	public Engagement(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}
	
	private void engagementStep() {
		nun = npcs.get("Sister Benedicta");
		nun.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence,
							final SpeakerNPC engine) {
						if (!player.hasQuest(marriage.getQuestSlot())) {
							engine.say("The great quest of all life is to be #married.");
						} else if (player.isQuestCompleted(marriage.getQuestSlot())) {
							engine.say("I hope you are enjoying married life.");
						} else {
							engine.say("Haven't you organised your wedding yet?");
						}
					}
				});

		nun.add(ConversationStates.ATTENDING,
				"married",
				null,
				ConversationStates.ATTENDING,
				"If you have a partner, you can marry them at a #wedding. Once you have a wedding ring, you can be together whenever you want.",
				null);

		nun.add(ConversationStates.ATTENDING,
				"wedding",
				null,
				ConversationStates.ATTENDING,
				"You may marry here at this church. If you want to #engage someone, just tell me who.",
				null);

		nun.add(ConversationStates.ATTENDING, 
				"engage", 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence,
							final SpeakerNPC npc) {
						// find out whom the player wants to marry.
						final String brideName = sentence.getSubjectName();

						if (brideName == null) {
							npc.say("You have to tell me who you want to marry.");
						} else {
							startEngagement(npc, player, brideName);
						}
					}
				});

		nun.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.QUESTION_2, 
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence,
							final SpeakerNPC npc) {
						askBrideE();
					}
				});

		nun.add(ConversationStates.QUESTION_1, 
				ConversationPhrases.NO_MESSAGES,
				null, 
				ConversationStates.IDLE, 
				"What a shame! Goodbye!", 
				null);

		nun.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence,
							final SpeakerNPC npc) {
						finishEngagement();
					}
				});

		nun.add(ConversationStates.QUESTION_2, 
				ConversationPhrases.NO_MESSAGES,
				null, 
				ConversationStates.IDLE, 
				"What a shame! Goodbye!", 
				null);
	}

	private void startEngagement(final SpeakerNPC nun, final Player player,
			final String partnerName) {
		final IRPZone outsideChurchZone = nun.getZone();
		final Area inFrontOfNun = new Area(outsideChurchZone, new Rectangle(51, 52, 6, 5));
		groom = player;
		bride = SingletonRepository.getRuleProcessor().getPlayer(partnerName);

		if (!inFrontOfNun.contains(groom)) {
			nun.say("My hearing is not so good, please both come close to tell me who you want to get engaged to.");
		} else if (marriage.isMarried(groom)) {
			nun.say("You are married already, " 
					+ groom.getName()
					+ "! You can't marry again.");
		} else if ((bride == null) || !inFrontOfNun.contains(bride)) {
			nun.say("My hearing is not so good, please both come close to tell me who you want to get engaged to.");
		} else if (bride.getName().equals(groom.getName())) {
			nun.say("You can't marry yourself!");
		} else if (marriage.isMarried(bride)) {
			nun.say("You are married already, " 
					+ bride.getName()
					+ "! You can't marry again.");
		} else {
			askGroomE();
		}

	}

	private void askGroomE() {
		nun.say(groom.getName() + ", do you want to get engaged to "
				+ bride.getName() + "?");
		nun.setCurrentState(ConversationStates.QUESTION_1);
	}

	private void askBrideE() {
		nun.say(bride.getName() + ", do you want to get engaged to "
				+ groom.getName() + "?");
		nun.setCurrentState(ConversationStates.QUESTION_2);
		nun.setAttending(bride);
	}

	private void giveInvite(final Player player) {
		final StackableItem invite = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"invitation scroll");
		invite.setQuantity(4);
		// location of church
		invite.setInfoString("int_fado_church 12 20");

		// perhaps change this to a hotel room where they can get dressed into
		// wedding outfits?
		// then they walk to the church?
		player.equip(invite, true);
	}

	private void finishEngagement() {
		// we check if each of the bride and groom are engaged, or both, and only give invites 
		// if they were not already engaged.
		String additional;
		if (!marriage.isEngaged(groom)) {
			giveInvite(groom);
			if (!marriage.isEngaged(bride)) {
				giveInvite(bride);
				additional = "And here are some invitations you can give to your guests.";
			} else {
				additional = "I have given invitations for your guests to " + groom.getName() + ". " + bride.getName() + ", if Ognir was already making you a ring, you will now have to go and ask him to make another.";
				}
		} else if (!marriage.isEngaged(bride)) {
			giveInvite(bride);
			additional = "I have given invitations for your guests to " + bride.getName() + ". " + groom.getName() + ", if Ognir was already making you a ring, you will now have to go and ask him to make another.";
		} else {
			additional = "I have not given you more invitation scrolls, as you were both already engaged, and had them before. If you were having rings forged you will both need to make them again.";
		}		
		nun.say("Congratulations, "
				+ groom.getName()
				+ " and "
				+ bride.getName()
				+ ", you are now engaged! Please make sure you have got wedding rings made before you go to the church for the service. " + additional);
		// Memorize that the two engaged so that the priest knows
		groom.setQuest(marriage.getQuestSlot(), "engaged");
		bride.setQuest(marriage.getQuestSlot(), "engaged");
		// Clear the variables so that other players can become groom and bride
		// later
		groom = null;
		bride = null;
	}

	public void addToWorld() {
		engagementStep();
	}
}
