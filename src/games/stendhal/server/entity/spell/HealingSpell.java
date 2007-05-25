/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

/*
 * HealingSpell.java
 *
 * Created on March 29, 2007, 5:37 PM
 */

package games.stendhal.server.entity.spell;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.UseListener;

import java.util.Map;


/**
 * @author timothyb89
 * A healing spell. It restores the user to full HP (for now).
 */
public class HealingSpell extends Spell implements UseListener {
    public int healAmount = 0;
    
    private HealingSpell(String name, Map<String, String> attributes) {
		super(name, attributes);
    }
    
    @Override
    public String describe() {
        return "You see a healing spell.";
    }
    
    
    /** getHealingAmount
     * Gets the amount the healing spell will heal you.
     */
    
    public int getHealingAmount(Player player) {
        return player.getBaseHP() - healAmount;
    }
    
    
    
    public void onUsed(RPEntity user) {
        Player player = (Player) user;
        
        // checks to see if player has enough mana
        boolean canCastSpell = false;
        if (player.getBaseMana() >= 25) {
            // now we also check if they have enough actual mana
            if (player.getMana() >= 25) {
                canCastSpell = false;
            } else player.sendPrivateText("You do not have enough mana to cast this spell."); // if not...
        } else player.sendPrivateText("You do not have enough base mana to cast this spell.");
        
        // now that thats over with, get on with the spell casting...
        if (canCastSpell) {
            // now we cast the spell and take away mana.
            
            // finds the hp to restore
            int hp = player.getHP();
            int baseHP = player.getBaseHP();
            int newhp = baseHP - hp;
            
            // sets the new HP
            player.setHP(newhp);
            
            //takes away the mana
            int mana = player.getMana();
            int newmana = mana - 25;
            
            // sets the new mana amount
            player.setMana(newmana);
            
            //now that everything has been set, notify the player.
            player.sendPrivateText("You have been healed. You now have #" + player.getMana() + " mana left.");
            
            // saves changes (last because the stats are refreshed by default on zone change)
            player.update();
            player.notifyWorldAboutChanges();
            
            // done!
            
        } else player.sendPrivateText("You were not able to cast the spell.");
    }
}
