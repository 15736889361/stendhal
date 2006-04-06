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
package tiled.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import tiled.core.MapLayer;
import tiled.core.PropertiesLayer;
import tiled.core.StatefulTile;
import tiled.core.Tile;
import tiled.core.TileGroup;
import tiled.core.TileLayer;

/**
 * Simple orthogonal map.
 * @author mtotz
 */
public class Orthogonal extends MapView
{
  /** minimap tile size is 2 pixel */
  private static final int MINIMAP_TILE_SIZE = 2;
  
  /** retuns the tile size depending on the given zoom level */
  private Dimension getTileSize(double zoom, int padding)
  {
    return new Dimension((int)(map.getTileWidth() * zoom + padding),
                         (int)(map.getTileHeight() * zoom + padding));
  }

  /** size of the map */
  public Dimension getSize()
  {
    if (map == null) 
    {
      return new Dimension(1000, 1000);
    }

    Dimension dim = getTileSize(zoom, padding);
    
    dim.width  *= map.getWidth();
    dim.height *= map.getHeight();
    return dim;
  }

  /**
   * (re)draws a portion of the map. Note that clipArea is in Tile coordinate
   * space, not pixel space. The destination is always the upper left
   * corner(0,0) of g.
   * 
   * @param g
   *          the graphic to draw to
   * @param clipArea
   *          the are to draw in tile coordinates
   */
  public void draw(Graphics g, Rectangle clipArea)
  {
    draw(g,clipArea,zoom, padding);
  }
  
  /** draws the map with the given zoom level */
  private void draw(Graphics g, Rectangle clipArea, double zoom, int padding)
  {
    if (map == null)
      return;
    
    List<TileLayer> tileLayerList = new ArrayList<TileLayer>();
    
    // draw each tile layer
    for (MapLayer layer : map.getLayerList())
    {
      if (layer instanceof TileLayer && layer.isVisible())
      {
        TileLayer tileLayer = (TileLayer) layer;
        paintLayer(g,tileLayer,clipArea,zoom, padding);
        tileLayerList.add(tileLayer);
      }
    }
    PropertiesLayer propertiesLayer = map.getPropertiesLayer();
    paintPropertiesLayer(g,propertiesLayer,tileLayerList,clipArea,zoom, padding);
  }

  /** draws the properties overlay images */
  private void paintPropertiesLayer(Graphics g, PropertiesLayer propertiesLayer, List<TileLayer> tileLayer, Rectangle clipArea, double zoom, int padding)
  {
    // no opacity for properties icons
    ((Graphics2D) g).setComposite(AlphaComposite.SrcOver);
    
    // Determine tile size and offset
    Dimension tsize = getTileSize(zoom, padding);
    if (tsize.width <= 0 || tsize.height <= 0)
      return;
    
    g.setColor(Color.BLACK);
    

    int toffset = 0;

    int startX = clipArea.x;
    int startY = clipArea.y;
    int endX   = clipArea.x + clipArea.width;
    int endY   = clipArea.y + clipArea.height;
    
    int size = tsize.width / 4;
    Polygon p = new Polygon();
    p.addPoint(0,0);
    p.addPoint(size,0);
    p.addPoint(0,size);
    
    Polygon p2 = new Polygon();
    p2.addPoint(tsize.width-1,0);
    p2.addPoint(tsize.width-1-size,0);
    p2.addPoint(tsize.width-1,size);
    
    
    // Draw this map layer
    for (int y = startY, gy = startY * tsize.height + toffset; y < endY; y++, gy += tsize.height)
    {
      for (int x = startX, gx = startX * tsize.width + toffset; x < endX; x++, gx += tsize.width)
      {
        Properties props = getPropertiesAt(tileLayer, x, y);

        Graphics g2 = null;
        if (props != null && props.size() > 0)
        {
          g2 = g.create(gx,gy,tsize.width,tsize.height);
          g2.setColor(Color.YELLOW);
          g2.fillPolygon(p2);
          g2.setColor(Color.BLACK);
          g2.drawPolygon(p2);
        }
        
        props = propertiesLayer.getProps(x,y);
        if (props != null && props.size() > 0)
        {
          if (g2 == null)
            g2 = g.create(gx,gy,tsize.width,tsize.height);
          
          g2.setColor(Color.CYAN);
          g2.fillPolygon(p);
          g2.setColor(Color.BLACK);
          g2.drawPolygon(p);
          
        }
        
      }
    }
  }

  /** paints the specified region of the layer */
  protected void paintLayer(Graphics g, TileLayer layer, Rectangle clipArea, double zoom, int padding)
  {
    setLayerOpacity(g,layer);
    
    // Determine tile size and offset
    Dimension tsize = getTileSize(zoom, padding);
    if (tsize.width <= 0 || tsize.height <= 0)
      return;
    
    g.setColor(Color.BLACK);
    

    int toffset = 0;

    int startX = clipArea.x;
    int startY = clipArea.y;
    int endX   = clipArea.x + clipArea.width;
    int endY   = clipArea.y + clipArea.height;

    // Draw this map layer
    for (int y = startY, gy = startY * tsize.height + toffset; y < endY; y++, gy += tsize.height)
    {
      for (int x = startX, gx = startX * tsize.width + toffset; x < endX; x++, gx += tsize.width)
      {
        Tile tile = layer.getTileAt(x, y);

        if (tile != null && tile != map.getNullTile())
        {
          tile.draw(g, gx, gy, zoom);
        }
      }
    }
  }


  /** 
   * converts the screen position to tile position.
   * 
   * @param screenCoords tile coords
   * @return screen coords (upper left corner of the tile)
   */
  public Point screenToTileCoords(Point screenCoords)
  {
    Dimension tsize = getTileSize(zoom, padding);
    Point p = new Point(screenCoords.x / tsize.width, screenCoords.y / tsize.height);
    if (p.x > map.getWidth())
    {
      p.x = map.getWidth();
    }
    if (p.y > map.getHeight())
    {
      p.y = map.getHeight();
    }
    
    return p;
  }

  /**
   * converts the tile position to screen position.
   * 
   * @param tileCoords screen coords
   * @return tile coords
   */
  public Point tileToScreenCoords(Point tileCoords)
  {
    Dimension tsize = getTileSize(zoom, padding);
    return new Point(tileCoords.x * tsize.width, tileCoords.y * tsize.height);
  }

  /** returns a minimap */
  protected BufferedImage prepareMinimapImage()
  {
    int width = (int) (map.getWidth() * MINIMAP_TILE_SIZE);
    int height = (int) (map.getHeight() * MINIMAP_TILE_SIZE);
    
    
    GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    BufferedImage image = config.createCompatibleImage(width,height);

    return image;
  }
  
  /** returns the minimap zoom level. */
  public double getMinimapScale()
  {
    return (1.0 / ((map.getTileWidth()) / MINIMAP_TILE_SIZE));
  }

  /** updates the minimap */
  public void updateMinimapImage(Rectangle modifiedRegion)
  {
    if (minimapImage == null)
    {
      // minimap not prepared yet
      return;
    }

    Graphics minimapGraphics = minimapImage.createGraphics();
    draw(minimapGraphics,modifiedRegion,getMinimapScale(), 0);
  }

  /**
   * Retuns list of tiles that lies in the given rectangle
   * @param rect the rectangle (in tile coordinate space)
   * @return list of tiles in the rectangle
   */
  public List<Point> getSelectedTiles(Rectangle rect,int layer)
  {
    List<Point> list = new ArrayList<Point>();

    MapLayer mapLayer = map.getLayer(layer);
    if (!(mapLayer instanceof TileLayer))
    {
      return list;
    }
    
    TileLayer tileLayer = (TileLayer) mapLayer;
    
    Dimension tsize = getTileSize(zoom, padding);
    Point p1 = rect.getLocation();
    Point p2 = rect.getLocation();
    p2.translate(rect.width,rect.height);
    
    if (p2.x % tsize.width > 0)
    {
      p2.x += tsize.width;
    }
    if (p2.y % tsize.height > 0)
    {
      p2.y += tsize.height;
    }
    
    p1 = screenToTileCoords(p1);
    p2 = screenToTileCoords(p2);
    

    // Draw this map layer
    for (int y = p1.y; y < p2.y; y++)
    {
      for (int x = p1.x; x < p2.x; x++)
      {
        Tile tile = tileLayer.getTileAt(x, y);

        if (tile != null && tile != map.getNullTile())
        {
          list.add(new Point(x,y));
        }
      }
    }
    
    return list;
  }

  /**
   * draws a frame around the tile.
   * @param g graphics context
   * @param tile the tile
   */
  public void drawTileHighlight(Graphics g, Point tile)
  {
    Point p = tileToScreenCoords(tile);
    Dimension tsize = getTileSize(zoom, padding);
    g.drawRect(p.x, p.y, tsize.width, tsize.height);
  }
  
  /**
   * Draws the tilegroup to a BufferedImage. 
   * @param tileGroup the tilegroup
   */
  public BufferedImage drawTileGroup(TileGroup tileGroup)
  {
    int tileWidth = -1;
    int tileHeight = -1;
    
    BufferedImage image = null;
    Graphics g = null;
    
    for (MapLayer layer : map.getLayerList())
    {
      if (layer instanceof TileLayer)
      {
        TileLayer tileLayer = (TileLayer) layer;
        if (g != null)
        {
          setLayerOpacity(g,tileLayer);
        }

        List<StatefulTile> tileList = tileGroup.getTileLayer(tileLayer);
        if (tileList != null)
        {
          for (StatefulTile tile : tileList)
          {
            if (tileWidth < 0)
            {
              tileWidth = tile.tile.getWidth();
              tileHeight = tile.tile.getHeight();
              GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
              image = config.createCompatibleImage(tileWidth * tileGroup.getWidth(),tileHeight * tileGroup.getHeight(),Transparency.TRANSLUCENT);
              g = image.createGraphics();
              g.setColor(Color.BLACK);
              g.fillRect(0,0,image.getWidth(), image.getHeight());
            }
            tile.tile.draw(g,tile.p.x*tileWidth,tile.p.y*tileHeight,1.0);
          }
        }
      }
    }
    
    return image;
  }


}
