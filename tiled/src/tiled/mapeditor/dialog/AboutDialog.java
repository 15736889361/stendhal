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
 *  modified for stendhal, an Arianne powered RPG 
 *  (http://arianne.sf.net)
 *
 *  Matthias Totz <mtotz@users.sourceforge.net>
 */

package tiled.mapeditor.dialog;

import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

import tiled.mapeditor.MapEditor;


/**
 * The about dialog.
 */
public class AboutDialog extends JFrame
{
  private static final long serialVersionUID = 7565310866809925372L;

    JFrame parent;

    public AboutDialog(JFrame parent) {
        super("Tiled v" + MapEditor.version);

        this.parent = parent;
        ImageIcon icon;
        
        try {
            icon = new ImageIcon(ImageIO.read(
                        getClass().getResourceAsStream("/tiled/mapeditor/resources/logo.png")));

            JPanel content = new JPanel();
            JLabel label = new JLabel(icon);
            content.add(label);

            setContentPane(content);
            setResizable(false);
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            pack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(parent);
        }
        super.setVisible(visible);
    }
}
