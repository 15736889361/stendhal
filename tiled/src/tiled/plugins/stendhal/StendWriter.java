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

package tiled.plugins.stendhal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileFilter;

import tiled.core.Map;
import tiled.plugins.MapWriterPlugin;

/**
 * Writes the (uncompressed) .stend map file format
 * @author mtotz
 */
public class StendWriter extends Writer implements MapWriterPlugin
{

  /** writes the map */
  public void writeMap(Map map, String filename)
  {
    try
    {
      FileOutputStream os = new FileOutputStream(new File(filename));
      writeMap(map,os);
      os.close();
    } catch (Exception e)
    {
      throw new RuntimeException(e); 
    }
  }

  /** writes the map */
  public void writeMap(Map map, OutputStream outputStream)
  {
    writeMap(map,outputStream,false);
  }

  /** all filefilters */
  public FileFilter[] getFilters()
  {
    return new FileFilter[] { new FileFilter()
        {

          public boolean accept(File pathname)
          {
            return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".stend");
          }

          public String getDescription()
          {
            return "Stendhal Map Files (*.stend)";
          }

        } };
  }

  /** returns the description */
  public String getPluginDescription()
  {
    return "Mapreader for the Stendhal map format";
  }
}
