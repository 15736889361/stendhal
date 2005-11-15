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
package games.stendhal.server.entity.item;

/**
 * this interface tags all items which are stackable.
 * 
 * @author mtotz
 */
public interface Stackable
{
  /** returns the quantity */
  public int getQuantity();
  /** sets the quantity */
  public void setQuantity(int amount);
  /** adds some value to the quantity */
  public int add(int amount);
  /** adds the quantity of the other Stackable to this */
  public int add(Stackable other);
  /** returns true when both stackables are of the same type and can be merged */
  public boolean isStackable(Stackable other);

}
