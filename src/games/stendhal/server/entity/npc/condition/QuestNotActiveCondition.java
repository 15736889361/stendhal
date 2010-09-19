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
package games.stendhal.server.entity.npc.condition;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Is this quest either unknown, completed or rejected?
 */
public class QuestNotActiveCondition implements ChatCondition {

	private final String questname;

	/**
	 * Creates a new QuestNotActiveCondition.
	 * 
	 * @param questname
	 *            name of quest slot
	 */
	public QuestNotActiveCondition(final String questname) {
		this.questname = questname;
	}

	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		return (!player.hasQuest(questname) || player.isQuestInState(questname, "rejected") || player.isQuestCompleted(questname));
	}

	@Override
	public String toString() {
		return "QuestNotActive <" + questname + ">";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				QuestNotActiveCondition.class);
	}
}
