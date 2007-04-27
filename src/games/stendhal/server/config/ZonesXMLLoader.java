/*
 * @(#) src/games/stendhal/server/config/ZonesXMLLoader.java
 *
 * $Id$
 */

package games.stendhal.server.config;

//
//

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.EntityFactoryHelper;
import games.stendhal.server.entity.portal.Portal;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.tools.tiled.LayerDefinition;
import games.stendhal.tools.tiled.ServerTMXLoader;
import games.stendhal.tools.tiled.StendhalMapStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Load and configure zones via an XML configuration file.
 */
public class ZonesXMLLoader extends DefaultHandler {

	protected static final int SCOPE_NONE = 0;

	protected static final int SCOPE_CONFIGURATOR = 1;

	protected static final int SCOPE_PORTAL = 2;

	protected static final int SCOPE_ENTITY = 3;

	/**
	 * Logger
	 */
	private static final Logger logger = Log4J.getLogger(ZonesXMLLoader.class);

	/**
	 * A list of zone descriptors.
	 */
	protected LinkedList<ZoneDesc> zoneDescriptors;

	/**
	 * The current zone descriptor.
	 */
	protected ZoneDesc zdesc;

	/**
	 * The current configurator descriptor.
	 */
	protected ConfiguratorDesc cdesc;

	/**
	 * The current entity descriptor.
	 */
	protected EntityDesc edesc;

	/**
	 * The current portal descriptor.
	 */
	protected PortalDesc pdesc;

	/**
	 * The current entity attribute name.
	 */
	protected String attrName;

	/**
	 * The current parameter name.
	 */
	protected String paramName;

	/**
	 * The current text content.
	 */
	protected StringBuffer content;

	/**
	 * The current [meaningful] xml tree scope.
	 */
	protected int scope;

	/**
	 * The zone group file.
	 */
	protected URI uri;

	/**
	 * Create an xml based loader of zones.
	 */
	public ZonesXMLLoader(URI uri) {
		this.uri = uri;

		content = new StringBuffer();
	}

	//
	// ZonesXMLLoader
	//

	/**
	 * Load a group of zones into a world.
	 *
	 * @throws	SAXException	If a SAX error occured.
	 * @throws	IOException	If an I/O error occured.
	 * @throws	FileNotFoundException
	 *				If the resource was not found.
	 */
	public void load() throws SAXException, IOException {
		InputStream in = getClass().getResourceAsStream(uri.getPath());

		if (in == null) {
			throw new FileNotFoundException("Cannot find resource: " + uri);
		}

		try {
			load(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Load a group of zones into a world using a config file.
	 *
	 * @param	in		The config file stream.
	 *
	 * @throws	SAXException	If a SAX error occured.
	 * @throws	IOException	If an I/O error occured.
	 */
	protected void load(InputStream in) throws SAXException, IOException {
		SAXParser saxParser;

		/*
		 * Use the default (non-validating) parser
		 */
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException ex) {
			throw new SAXException(ex);
		}

		/*
		 * Parse the XML
		 */
		zoneDescriptors = new LinkedList<ZoneDesc>();
		zdesc = null;
		cdesc = null;
		edesc = null;
		pdesc = null;
		attrName = null;
		paramName = null;
		scope = SCOPE_NONE;

		saxParser.parse(in, this);

		/*
		 * Load each zone
		 */
		for (ZoneDesc zdesc : zoneDescriptors) {
			String name = zdesc.getName();

			logger.info("Loading zone: " + name);

			try {
				StendhalMapStructure zonedata=null;
				zonedata=ServerTMXLoader.load(StendhalRPWorld.MAPS_FOLDER + zdesc.getFile());

				if (verifyMap(zdesc, zonedata)) {
					StendhalRPZone zone = load(zdesc, zonedata);
	
					/*
					 * Setup Descriptors
					 */
					Iterator<ZoneSetupDesc> diter = zdesc.getDescriptors();
	
					while (diter.hasNext()) {
						diter.next().doSetup(zone);
					}
				}
			} catch (Exception ex) {
				logger.error("Error loading zone: " + name, ex);
			}
		}
	}

	private static final String[] REQUIRED_LAYERS = {"0_floor", "1_terrain", "2_object", "3_roof", "objects", "collision", "protection"};
	private boolean verifyMap(ZoneDesc zdesc, StendhalMapStructure zonedata) {
		for (String layer : REQUIRED_LAYERS) {
			if (!zonedata.hasLayer(layer)) {
				logger.error("Required layer " + layer + " missing in zone " + zdesc.getFile());
				return false;
			}
		}
		return true;
    }

	/**
	 * Configure a zone.
	 *
	 *
	 */
	protected static void configureZone(StendhalRPZone zone, ConfiguratorDesc cdesc) {
		String className;
		Class clazz;
		Object obj;

		className = cdesc.getClassName();

		/*
		 * Load class
		 */
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			logger.error("Unable to find zone configurator: " + className);

			return;
		}

		/*
		 * Create instance
		 */
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException ex) {
			logger.error("Error creating zone configurator: " + className, ex);

			return;
		} catch (IllegalAccessException ex) {
			logger.error("Error accessing zone configurator: " + className, ex);

			return;
		}

		/*
		 * Apply class
		 */
		if (obj instanceof ZoneConfigurator) {
			logger.info("Configuring zone [" + zone.getID().getID() + "] with: " + className);

			((ZoneConfigurator) obj).configureZone(zone, cdesc.getParameters());
		} else {
			logger.warn("Unsupported zone configurator: " + className);
		}
	}

	/**
	 * Configure a portal.
	 *
	 *
	 */
	protected static void configurePortal(StendhalRPZone zone, PortalDesc pdesc) {
		String className;
		Portal portal;
		Object reference;

		if ((className = pdesc.getImplementation()) == null) {
			/*
			 * Default implementation
			 */
			className = Portal.class.getName();
		}

		try {
			if ((portal = (Portal) EntityFactoryHelper.create(className, pdesc.getParameters(), pdesc.getAttributes())) == null) {
				logger.warn("Unable to create portal: " + className);

				return;
			}

			zone.assignRPObjectID(portal);

			portal.set(pdesc.getX(), pdesc.getY());
			portal.setReference(pdesc.getReference());

			if ((reference = pdesc.getDestinationReference()) != null) {
				portal.setDestination(pdesc.getDestinationZone(), reference);
			}

			if (pdesc.isReplacing()) {
				Portal oportal = zone.getPortal(pdesc.getX(), pdesc.getY());

				if (oportal != null) {
					logger.debug("Replacing portal: " + oportal);

					zone.remove(oportal);
				}
			}

			zone.add(portal);
		} catch (IllegalArgumentException ex) {
			logger.error("Error with portal factory", ex);
		}
	}

	/**
	 * Configure a generic entity.
	 *
	 *
	 */
	protected static void configureEntity(StendhalRPZone zone, EntityDesc edesc) {
		String className;
		Entity entity;
		if ((className = edesc.getImplementation()) == null) {
			logger.error("Entity without factory at " + zone.getID().getID() + "[" + edesc.getX() + "," + edesc.getY()
			        + "]");
			return;
		}

		try {
			if ((entity = EntityFactoryHelper.create(className, edesc.getParameters(), edesc.getAttributes())) == null) {
				logger.warn("Unable to create entity: " + className);

				return;
			}

			zone.assignRPObjectID(entity);

			entity.set(edesc.getX(), edesc.getY());

			zone.add(entity);
		} catch (IllegalArgumentException ex) {
			logger.error("Error with entity factory", ex);
		}
	}

	/**
	 * Load zone data and create a zone from it.
	 * Most of this should be moved directly into ZoneXMLLoader.
	 *
	 *
	 */
	protected StendhalRPZone load(ZoneDesc desc, StendhalMapStructure zonedata) throws SAXException, IOException {
		String name = desc.getName();
		StendhalRPZone zone = new StendhalRPZone(name);

		zone.addTilesets(name+"_tilesets", zonedata.getTilesets());
		zone.addLayer(name + "_0_floor", zonedata.getLayer("0_floor"));
		zone.addLayer(name + "_1_terrain", zonedata.getLayer("1_terrain"));
		zone.addLayer(name + "_2_object", zonedata.getLayer("2_object"));
		zone.addLayer(name + "_3_roof", zonedata.getLayer("3_roof"));

		LayerDefinition layer = zonedata.getLayer("4_roof_add");
		if (layer != null) {
			zone.addLayer(name + "_4_roof_add", layer);
		}
		zone.addCollisionLayer(name + "_collision", zonedata.getLayer("collision"));
		zone.addProtectionLayer(name + "_protection", zonedata.getLayer("protection"));

		if(desc.isInterior()) {
			zone.setPosition();
		} else {
			zone.setPosition(desc.getLevel(), desc.getX(), desc.getY());
		}

		StendhalRPWorld.get().addRPZone(zone);

		zone.populate(zonedata.getLayer("objects"));

		return zone;
	}

	//
	// ContentHandler
	//

	@Override
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) {
		String s;
		int level;
		int x;
		int y;
		String zone;
		String file;
		Object reference;

		if (qName.equals("zones")) {
			// Ignore
		} else if (qName.equals("zone")) {
			if ((zone = attrs.getValue("name")) == null) {
				logger.warn("Unnamed zone");
				return;
			}

			if ((file = attrs.getValue("file")) == null) {
				logger.warn("Zone [" + zone + "] without 'file' attribute");
				return;
			}

			/**
			 * Interior zones don't have levels (why not?)
			 */
			if ((s = attrs.getValue("level")) == null) {
				level = ZoneDesc.UNSET;
				x = ZoneDesc.UNSET;
				y = ZoneDesc.UNSET;
			} else {
				try {
					level = Integer.parseInt(s);
				} catch (NumberFormatException ex) {
					logger.warn("Zone [" + zone + "] has invalid level: " + s);
					return;
				}

				if ((s = attrs.getValue("x")) == null) {
					logger.warn("Zone [" + zone + "] without x coordinate");
					return;
				}

				try {
					x = Integer.parseInt(s);
				} catch (NumberFormatException ex) {
					logger.warn("Zone [" + zone + "] has invalid x coordinate: " + s);
					return;
				}

				if ((s = attrs.getValue("y")) == null) {
					logger.warn("Zone [" + zone + "] without y coordinate");
					return;
				}

				try {
					y = Integer.parseInt(s);
				} catch (NumberFormatException ex) {
					logger.warn("Zone [" + zone + "] has invalid y coordinate: " + s);
					return;
				}
			}

			zdesc = new ZoneDesc(zone, file, level, x, y);
		} else if (qName.equals("title")) {
			content.setLength(0);
		} else if (qName.equals("configurator")) {
			if ((s = attrs.getValue("class-name")) == null) {
				logger.warn("Configurator without class-name");
			} else {
				cdesc = new ConfiguratorDesc(s);
				scope = SCOPE_CONFIGURATOR;
			}
		} else if (qName.equals("entity")) {
			if ((s = attrs.getValue("x")) == null) {
				logger.warn("Entity without 'x' coordinate");
				return;
			}

			try {
				x = Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				logger.warn("Invalid entity 'x' coordinate: " + s);
				return;
			}

			if ((s = attrs.getValue("y")) == null) {
				logger.warn("Entity without 'y' coordinate");
				return;
			}

			try {
				y = Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				logger.warn("Invalid entity 'y' coordinate: " + s);
				return;
			}

			edesc = new EntityDesc(x, y);
			scope = SCOPE_ENTITY;
		} else if (qName.equals("portal")) {
			if ((s = attrs.getValue("x")) == null) {
				logger.warn("Portal without 'x' coordinate");
				return;
			}

			try {
				x = Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				logger.warn("Invalid portal 'x' coordinate: " + s);
				return;
			}

			if ((s = attrs.getValue("y")) == null) {
				logger.warn("Portal without 'y' coordinate");
				return;
			}

			try {
				y = Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				logger.warn("Invalid portal 'y' coordinate: " + s);
				return;
			}

			if ((s = attrs.getValue("ref")) == null) {
				logger.warn("Portal without 'ref' value");
				return;
			}

			/*
			 * For now, treat valid number strings as Integer refs
			 */
			try {
				reference = new Integer(s);
			} catch (NumberFormatException ex) {
				reference = s;
			}

			pdesc = new PortalDesc(x, y, reference);
			scope = SCOPE_PORTAL;

			if ((s = attrs.getValue("replacing")) != null) {
				pdesc.setReplacing(s.equals("true"));
			}
		} else if (qName.equals("attribute")) {
			if ((attrName = attrs.getValue("name")) == null) {
				logger.warn("Unnamed attribute");
			} else {
				content.setLength(0);
			}
		} else if (qName.equals("parameter")) {
			if ((paramName = attrs.getValue("name")) == null) {
				logger.warn("Unnamed parameter");
			} else {
				content.setLength(0);
			}
		} else if (qName.equals("implementation")) {
			if ((s = attrs.getValue("class-name")) == null) {
				logger.warn("Implmentation without class-name");
			} else if (pdesc != null) {
				pdesc.setImplementation(s);
			} else if (edesc != null) {
				edesc.setImplementation(s);
			}
		} else if (qName.equals("destination")) {
			if ((zone = attrs.getValue("zone")) == null) {
				logger.warn("Portal destination without zone");
				return;
			}

			if ((s = attrs.getValue("ref")) == null) {
				logger.warn("Portal dest without 'ref' value");
				return;
			}

			/*
			 * For now, treat valid number strings as Integer refs
			 */
			try {
				reference = new Integer(s);
			} catch (NumberFormatException ex) {
				reference = s;
			}

			if ((scope == SCOPE_PORTAL) && (pdesc != null)) {
				pdesc.setDestination(zone, reference);
			}
		} else {
			logger.warn("Unknown XML element: " + qName);
		}
	}

	@Override
	public void endElement(String namespaceURI, String sName, String qName) {
		if (qName.equals("zone")) {
			if (zdesc != null) {
				zoneDescriptors.add(zdesc);
				zdesc = null;
			}
		} else if (qName.equals("title")) {
			if (zdesc != null) {
				String s = content.toString().trim();

				if (s.length() != 0) {
					zdesc.setTitle(s);
				}
			}
		} else if (qName.equals("configurator")) {
			if (zdesc != null) {
				if (cdesc != null) {
					zdesc.addDescriptor(cdesc);
					cdesc = null;
				}
			}

			scope = SCOPE_NONE;
		} else if (qName.equals("entity")) {
			if (zdesc != null) {
				if (edesc != null) {
					zdesc.addDescriptor(edesc);
					edesc = null;
				}
			}

			scope = SCOPE_NONE;
		} else if (qName.equals("portal")) {
			if (zdesc != null) {
				if (pdesc != null) {
					zdesc.addDescriptor(pdesc);
					pdesc = null;
				}
			}

			scope = SCOPE_NONE;
		} else if (qName.equals("attribute")) {
			if (attrName != null) {
				if ((scope == SCOPE_PORTAL) && (pdesc != null)) {
					pdesc.setAttribute(attrName, content.toString().trim());
				} else if ((scope == SCOPE_ENTITY) && (edesc != null)) {
					edesc.setAttribute(attrName, content.toString().trim());
				}
			}
		} else if (qName.equals("parameter")) {
			if (paramName != null) {
				if ((scope == SCOPE_CONFIGURATOR) && (cdesc != null)) {
					cdesc.setParameter(paramName, content.toString().trim());
				} else if ((scope == SCOPE_PORTAL) && (pdesc != null)) {
					pdesc.setParameter(paramName, content.toString().trim());
				} else if ((scope == SCOPE_ENTITY) && (edesc != null)) {
					edesc.setParameter(paramName, content.toString().trim());
				}
			}
		}
	}

	@Override
	public void characters(char buf[], int offset, int len) {
		content.append(buf, offset, len);
	}

	//
	//

	/*
	 * XXX - THIS REQUIRES StendhalRPWorld SETUP (i.e. marauroa.ini)
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java " + ZonesXMLLoader.class.getName() + " <filename>");
			System.exit(1);
		}

		ZonesXMLLoader loader = new ZonesXMLLoader(new URI(args[0]));

		try {
			loader.load();
		} catch (org.xml.sax.SAXParseException ex) {
			System.err.print("Source " + args[0] + ":" + ex.getLineNumber() + "<" + ex.getColumnNumber() + ">");

			throw ex;
		}
	}

	//
	//

	/**
	 * A zone descriptor.
	 */
	protected static class ZoneDesc {
		public static final int	UNSET	= Integer.MIN_VALUE;

		protected String name;

		protected String file;

		protected String title;

		protected int level;

		protected int x;

		protected int y;

		protected ArrayList<ZoneSetupDesc> descriptors;

		public ZoneDesc(String name, String file, int level, int x, int y) {
			this.name = name;
			this.file = file;
			this.level = level;
			this.x = x;
			this.y = y;

			descriptors = new ArrayList<ZoneSetupDesc>();
		}

		//
		// ZoneDesc
		//

		/**
		 * Add a setup descriptor.
		 *
		 */
		public void addDescriptor(ZoneSetupDesc desc) {
			descriptors.add(desc);
		}

		/**
		 * Get the zone file.
		 *
		 */
		public String getFile() {
			return file;
		}

		/**
		 * Get the level.
		 *
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * Get the zone name.
		 *
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get an iterator of setup descriptors.
		 *
		 */
		public Iterator<ZoneSetupDesc> getDescriptors() {
			return descriptors.iterator();
		}

		/**
		 * Get the zone title.
		 *
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Get the X coordinate.
		 *
		 */
		public int getX() {
			return x;
		}

		/**
		 * Get the Y coordinate.
		 *
		 */
		public int getY() {
			return y;
		}

		public boolean isInterior() {
			return (getLevel() == UNSET);
		}

		/**
		 * Set the zone title.
		 *
		 */
		public void setTitle(String title) {
			this.title = title;
		}
	}

	/**
	 * A zone setup descriptor.
	 */
	protected static abstract class ZoneSetupDesc {

		protected HashMap<String, String> parameters;

		public ZoneSetupDesc() {
			parameters = new HashMap<String, String>();
		}

		//
		// ZoneSetupDesc
		//

		public abstract void doSetup(StendhalRPZone zone);

		/**
		 * Get the parameters.
		 *
		 */
		public Map<String, String> getParameters() {
			return parameters;
		}

		/**
		 * Set a parameters.
		 *
		 */
		public void setParameter(String name, String value) {
			parameters.put(name, value);
		}
	}

	/**
	 * A zone configurator descriptor.
	 */
	protected static class ConfiguratorDesc extends ZoneSetupDesc {

		protected String className;

		public ConfiguratorDesc(String className) {
			this.className = className;
		}

		//
		//
		//

		/**
		 * Get the class name.
		 *
		 */
		public String getClassName() {
			return className;
		}

		//
		// ZoneSetupDesc
		//

		@Override
		public void doSetup(StendhalRPZone zone) {
			configureZone(zone, this);
		}
	}

	/**
	 * An entity descriptor.
	 */
	protected static class EntityDesc extends ZoneSetupDesc {

		protected int x;

		protected int y;

		protected String className;

		protected HashMap<String, String> attributes;


		public EntityDesc(int x, int y) {
			this.x = x;
			this.y = y;

			className = null;
			attributes = new HashMap<String, String>();
		}

		//
		//
		//

		/**
		 * Get the attributes.
		 *
		 */
		public Map<String, String> getAttributes() {
			return attributes;
		}

		/**
		 * Get the implementation class name.
		 *
		 */
		public String getImplementation() {
			return className;
		}

		/**
		 * Get the X coordinate.
		 *
		 */
		public int getX() {
			return x;
		}

		/**
		 * Get the Y coordinate.
		 *
		 */
		public int getY() {
			return y;
		}

		/**
		 * Set an attribute.
		 *
		 */
		public void setAttribute(String name, String value) {
			attributes.put(name, value);
		}

		/**
		 * Set the implementation class name.
		 *
		 */
		public void setImplementation(String className) {
			this.className = className;
		}

		//
		// ZoneSetupDesc
		//

		@Override
		public void doSetup(StendhalRPZone zone) {
			configureEntity(zone, this);
		}
	}

	/**
	 * A portal descriptor.
	 */
	protected static class PortalDesc extends EntityDesc {

		protected Object reference;

		protected String destinationZone;

		protected Object destinationReference;

		protected boolean replacing;

		public PortalDesc(int x, int y, Object reference) {
			super(x, y);

			this.reference = reference;

			destinationZone = null;
			destinationReference = null;
			replacing = false;
		}

		//
		//
		//

		/**
		 * Get the destination reference.
		 *
		 */
		public Object getDestinationReference() {
			return destinationReference;
		}

		/**
		 * Get the destination zone.
		 *
		 */
		public String getDestinationZone() {
			return destinationZone;
		}

		/**
		 * Get the reference.
		 *
		 */
		public Object getReference() {
			return reference;
		}

		/**
		 * Determine if existing portals are replaced.
		 *
		 */
		public boolean isReplacing() {
			return replacing;
		}

		/**
		 * Set the destination zone/reference.
		 *
		 */
		public void setDestination(String zone, Object reference) {
			this.destinationZone = zone;
			this.destinationReference = reference;
		}

		/**
		 * Set whether to replace any existing portal.
		 *
		 */
		public void setReplacing(boolean replacing) {
			this.replacing = replacing;
		}

		//
		// ZoneSetupDesc
		//

		@Override
		public void doSetup(StendhalRPZone zone) {
			configurePortal(zone, this);
		}
	}
}
