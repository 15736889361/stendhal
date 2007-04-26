package games.stendhal.tools.tiled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.common.net.Serializable;

/**
 * Stores a definition of a tileset.
 * Mainly its name, the source image used and the starting global id.
 * 
 * @author miguel
 *
 */
public class TileSetDefinition implements Serializable {
	/** The name of the tileset. Useless */
	private String name;
	/** The source image of this tileset */
	private String source;
	/** The id where this tileset begins to number tiles. */ 
	private int gid;			

	/**
	 * Constructor
	 * @param name the *useless* name of the tileset.
	 * @param firstGid the id where this tileset begins to number tiles.
	 */
	public TileSetDefinition(String name, int firstGid) {
		this.name=name;
		this.gid=firstGid;
    }
	
	/**
	 * Returns the id where this tileset begins to number tiles
	 * @return the id where this tileset begins to number tiles
	 */
	public int getFirstGid() {
		return gid;
	}

	/**
	 * Set the filename of the source image of the tileset. 
	 * @param attributeValue the filename
	 */
	public void setSource(String attributeValue) {
		this.source=attributeValue;
    }
	
	/**
	 * Returns the filename of the source image of the tileset.
	 * @return the filename of the source image of the tileset.
	 */
	public String getSource() {
		return source;		
	}
	
	public byte[] encode() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		OutputSerializer out = new OutputSerializer(array);
		
		writeObject(out);
		
		return array.toByteArray();
    }

	public void readObject(InputSerializer in) throws IOException, ClassNotFoundException {
		name=in.readString();
		source=in.readString();
		gid=in.readInt();
    }

	public void writeObject(OutputSerializer out) throws IOException {
		out.write(name);
		out.write(source);
		out.write(gid);		
    }
	
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof TileSetDefinition)) {
			return false;
		}
		
		TileSetDefinition set=(TileSetDefinition) object;
		return set.name.equals(name) && set.source.equals(source) && set.gid==gid;
	}
}
