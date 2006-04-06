/*
 *  Tiled Map Editor, (c) 2004
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 *  
 *  modified for Stendhal, an Arianne powered RPG 
 *  (http://arianne.sf.net)
 *
 *  Matthias Totz <mtotz@users.sourceforge.net>
 */

package tiled.mapeditor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import tiled.mapeditor.MapEditor;

/**
 * Selects the whole layer.
 * @author mtotz
 */
public class SelectAllAction extends AbstractAction
{
  private static final long serialVersionUID = -1980981542520629392L;

  private MapEditor mapEditor;
  
  public SelectAllAction(MapEditor mapEditor)
  {
    super("All");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
    putValue(SHORT_DESCRIPTION, "Select entire map");
    this.mapEditor = mapEditor;
  }

  public void actionPerformed(ActionEvent e)
  {
  }
}