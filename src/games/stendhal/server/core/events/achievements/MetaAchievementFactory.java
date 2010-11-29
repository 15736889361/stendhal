/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.events.achievements;

import games.stendhal.server.entity.npc.condition.PlayerHasCompletedAchievementsCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
/**
 * Factory for meta achievements
 * 
 * @author madmetzger
 */
public class MetaAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.META;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement("meta.quest.daily-weekly", "Conscientuous Comrade", 
										   "Complete all achievements for daily item quest, daily monster quest and weekly item quest",
										   Achievement.HARD_BASE_SCORE, 
										   new PlayerHasCompletedAchievementsCondition("quest.special.diq.500", "quest.special.wiq.5", "quest.special.dmq.500")));
		achievements.add(createAchievement("meta.quest.daily-weekly", "Expert Explorer", 
										   "Visit all outside regions",
										   Achievement.HARD_BASE_SCORE, 
										   new PlayerHasCompletedAchievementsCondition(
												   "zone.outside.semos", "zone.outside.ados", "zone.outside.fado",
												   "zone.outside.orril", "zone.outside.amazon", "zone.outside.athor",
												   "zone.outside.kikareukin", "zone.outside.kirdneh")));
		return achievements;
	}

}
