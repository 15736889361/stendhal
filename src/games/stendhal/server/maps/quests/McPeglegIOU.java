package games.stendhal.server.maps.quests;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.StandardInteraction;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: McPegleg's IOU
 *
 * PARTICIPANTS: - a corpse in kanmararn - McPegleg
 *
 * NOTE: The corpse with cointaisn the IOU is created in KanmararnSoldiers.java
 * Without it this quest cannot be started (so the player won't notice the
 * problem at all).
 *
 * STEPS: - find IOU in a corpse in kanmararn - bring it to McPegleg
 *
 * REWARD: - 250 money
 *
 * REPETITIONS: - None.
 */
public class McPeglegIOU extends AbstractQuest {

	private static final String QUEST_SLOT = "IOU";

	@Override
	public void init(String name) {
		super.init(name, QUEST_SLOT);
	}

	private void step_1() {
		// find the IOU in a corpse in kanmararn.
		// this is implemented in KanmararnSoldiers
	}

	private void step_2() {

		SpeakerNPC npc = npcs.get("McPegleg");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("iou", "henry",
				"charles", "note"),
				new StandardInteraction.QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(Player player, String text,
							SpeakerNPC engine) {
						// from all notes that the player is carrying, try to
						// find the IOU note
						List<Item> notes = player.getAllEquipped("note");
						Item iouNote = null;
						for (Item note : notes) {
							if ("charles".equalsIgnoreCase(note.getInfoString())) {
								iouNote = note;
								break;
							}
						}
						if (iouNote != null) {
							engine.say("Where did you get that from? Anyways, here is the money *sighs*");
							player.drop(iouNote);
							StackableItem money = (StackableItem) StendhalRPWorld.get().getRuleManager().getEntityManager().getItem(
									"money");
							money.setQuantity(250);
							player.equip(money);
							player.setQuest(QUEST_SLOT, "done");
							engine.setCurrentState(1);
						} else {
							engine.say("I can't see that you got a valid IOU with my signature!");
						}
					}
				});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("iou", "henry",
				"charles", "note"),
				new StandardInteraction.QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"You already got cash for that damned IOU!", null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		step_1();
		step_2();
	}

}
