/* $Id$ */
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
package games.stendhal.client.entity;

import games.stendhal.client.GameObjects;
import games.stendhal.client.Sprite;
import games.stendhal.client.SpriteStore;

import java.awt.Color;

import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPObject;

/** A Player entity */
public abstract class Speaker extends RPEntity
  {
  public Speaker(GameObjects gameObjects, RPObject object) throws AttributeNotFoundException
    {
    super(gameObjects, object);
    }

  protected void buildAnimations(RPObject object)
    {
    SpriteStore store=SpriteStore.get();

    sprites.put("move_up", store.getAnimatedSprite(translate(object.get("type")),0,4,1,2));
    sprites.put("move_right", store.getAnimatedSprite(translate(object.get("type")),1,4,1,2));
    sprites.put("move_down", store.getAnimatedSprite(translate(object.get("type")),2,4,1,2));
    sprites.put("move_left", store.getAnimatedSprite(translate(object.get("type")),3,4,1,2));

    sprites.get("move_up")[3]=sprites.get("move_up")[1];
    sprites.get("move_right")[3]=sprites.get("move_right")[1];
    sprites.get("move_down")[3]=sprites.get("move_down")[1];
    sprites.get("move_left")[3]=sprites.get("move_left")[1];
    }

  protected Sprite defaultAnimation()
    {
    animation="move_up";
    return sprites.get("move_up")[0];
    }

  public void modifyAdded(RPObject object, RPObject changes) throws AttributeNotFoundException
    {
    super.modifyAdded(object,changes);

    /** Add text lines */
    if(changes.has("text") && distance(client.getPlayer())<15*15)
      {
      String text=changes.get("text");
      client.addEventLine(getName(),text);

      gameObjects.addText(this, getName()+" says: "+text.replace("|",""), Color.yellow);
      }

    if(changes.has("private_text"))
      {
      client.addEventLine(changes.get("private_text"),Color.orange);
      gameObjects.addText(this, changes.get("private_text").replace("|",""), Color.orange);
      }

    if(changes.has("dead"))// && (stendhal.showEveryoneXPInfo || getID().equals(client.getPlayer().getID())))
      {
      client.addEventLine(getName()+" has died. "+getName()+"'s new level is "+getLevel());
      }
    }
  }
