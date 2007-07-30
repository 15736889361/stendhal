package games.stendhal.tools.tiled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.common.net.Serializable;

/**
 * The class that stores the definition of a layer.
 * A Layer consists mainly of:<ul>
 * <li>width and height
 * <li>name <b>VERY IMPORTANT</b>
 * <li>data 
 * </ul>
 * 
 * @author miguel
 *
 */
public class LayerDefinition implements Serializable {
	/** To which map this layer belong */
	private StendhalMapStructure map;
	
	/** Width of the layer that SHOULD be the same that the width of the map. */
	private int width;
	/** Height of the layer that SHOULD be the same that the height of the map. */
	private int height;

	/** Name of the layer that MUST be one of the available:<ul>
	 * <li>0_floor
	 * <li>1_terrain
	 * <li>2_object
	 * <li>3_roof
	 * <li>4_roof_add
	 * <li>objects
	 * <li>collision
	 * <li>protection
	 * </ul>
	 */
	private String name;
	
	/** The data encoded as int in a array of size width*height */
	private int[] data;
	/** The same data in a raw byte array, so we save reencoding it again for serialization */
	private byte[] raw;

	/**
	 * Constructor
	 * @param layerWidth the width of the layer.
	 * @param layerHeight the height of the layer
	 */ 
	public LayerDefinition(int layerWidth, int layerHeight) {
		raw=new byte[4*layerWidth*layerHeight];
		width=layerWidth;
		height=layerHeight;
	}
	
	/**
	 * Sets the map to which this layer belong to.
	 * @param map the map
	 */
	void setMap(StendhalMapStructure map) {
		this.map=map;
	}

	/**
	 * Builds the real data array based on the byte array.
	 * It is only needed for objects, collision and protection, which is at most 40% of the layers.
	 */
	public void build() {
		data=new int[height*width];
		int offset=0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {								
				int tileId = 0;
				tileId |= ((int)raw[0+offset]& 0xFF);
				tileId |= ((int)raw[1+offset]& 0xFF) <<  8;
				tileId |= ((int)raw[2+offset]& 0xFF) << 16;
				tileId |= ((int)raw[3+offset]& 0xFF) << 24;
				
				data[x+y*width]=tileId;
				offset+=4;
			}
		}
	}

	/**
	 * Returns the allocated raw array so it can be modified.
	 * @return
	 */
	public byte[] exposeRaw() {
		return raw;
	}

	/**
	 * Set a tile at the given x,y position.
	 * @param x the x position
	 * @param y the y position 
	 * @param tileId the tile code to set ( Use 0 for none ).
	 */
	public void set(int x, int y, int tileId) {
		data[y*width+x]=tileId;
	}

	/**
	 * Returns the tile at the x,y position 
	 * @param x the x position
	 * @param y the y position 
	 * @return the tile that exists at that position or 0 for none.
	 */
	public int getTileAt(int x, int y) {
		return data[y*width+x];
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] encode() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer out = new OutputSerializer(out_stream);
		
		writeObject(out);
		out_stream.close();
		
		return array.toByteArray();
    }

	/**
	 * Deserialize a layer definition
	 * 
	 * @param in input serializer
	 * @return an instance of a layer definition
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static LayerDefinition decode(InputStream in) throws IOException, ClassNotFoundException {
		LayerDefinition layer=new LayerDefinition(0,0);

		InflaterInputStream szlib = new InflaterInputStream(in,new Inflater());
		InputSerializer ser=new InputSerializer(szlib);
		
		layer=(LayerDefinition) ser.readObject(layer);
		layer.build();
		return layer;
    }

	/**
	 * Returns the width of the layer
	 * @return
	 */
	public int getWidth() {
	    return width;
    }

	/**
	 * Returns the height of the layer
	 * @return
	 */
	public int getHeight() {
	    return height;
    }

	/**
	 * Returns the name of the tileset a tile belongs to.
	 * @param value the tile id
	 * @return the name of the tileset
	 */
	public TileSetDefinition getTilesetFor(int value) {
		if(value==0) {
			return null;
		}
		
		List<TileSetDefinition> tilesets=map.getTilesets();

		int pos=0;
		for(pos=0;pos<tilesets.size();pos++) {
			if(value<tilesets.get(pos).getFirstGid()) {
				break;
			}
		}
		
		return tilesets.get(pos-1);
    }

	/** 
	 * Sets the name of the layer 
	 * @param layerName the name of the layer
	 */
	public void setName(String layerName) {
	    name=layerName;
    }

	/**
	 * Returns the name of the layer
	 * @return
	 */
	public String getName() {
		return name;
    }

	public void readObject(InputSerializer in) throws IOException {
		name=in.readString();
		width=in.readInt();
		height=in.readInt();
		raw=in.readByteArray();
    }

	public void writeObject(OutputSerializer out) throws IOException {
		out.write(name);
		out.write(width);
		out.write(height);
		out.write(raw);
    }

	public int[] expose() {
	    return data;
    }
}
