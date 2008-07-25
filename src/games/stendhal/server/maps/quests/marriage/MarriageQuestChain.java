package games.stendhal.server.maps.quests.marriage;


/**
 * QUEST: Marriage
 * <p>
 * PARTICIPANTS:
 * <li> Sister Benedicta, the nun of Fado Church
 * <li> the Priest of Fado Church
 * <li> Ognir, the Ring Maker in Fado
 * <p>
 * STEPS:
 * <li> The nun explains that when two people are married, they can be together
 * whenever they want
 * <li> When two players wish to become engaged, they tell the nun
 * <li> The nun gives them invitation scrolls for the wedding, marked with the
 * church
 * <li>The players get a wedding ring made to give the other at the wedding
 * <li> They can get dressed into an outfit in the hotel
 * <li> When an engaged player goes to the priest, he knows they are there to be
 * married
 * <li> The marriage rites are performed
 * <li> The players are given rings
 * <li> When they go to the Hotel they choose a lovers room
 * <li> Champagne and fruit baskets is put in their bag (room if possible)
 * <li> They leave the lovers room when desired with another marked scroll
 * 
 * <p>
 * REWARD:
 * <li> Wedding Ring that teleports you to your spouse if worn - 1500 XP in
 * total
 * <li> nice food in the lovers room
 * <p>
 * 
 * REPETITIONS:
 * <li> None.
 * 
 * @author kymara
 */
public class MarriageQuestChain  {
	private static MarriageQuestInfo marriage = new MarriageQuestInfo();


	private void getDressedStep() {

		// Just go to the NPCs Tamara and Timothy
		// you can only get into the room if you have the quest slot for
		// marriage
	}



	public void addToWorld() {
		new Engagement(marriage).addToWorld();
		new MakeRings(marriage).addToWorld();
		getDressedStep();
		new Marriage(marriage).addToWorld();
		new Honeymoon(marriage).addToWorld();
		new Divorce(marriage).addToWorld();
	}

}
