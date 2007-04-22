package games.stendhal.server.config;

import java.io.*;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;
import marauroa.common.Log4J;

public class ZoneXMLLoader extends DefaultHandler {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(ZoneXMLLoader.class);

	public static class XMLZone {

		public String name;

		public int width;

		public int height;

		public Map<String, byte[]> layers;

		public XMLZone() {
			layers = new HashMap<String, byte[]>();
		}

		public byte[] getLayer(String key) {
			return layers.get(key);
		}
	}

	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.err.println("Usage: cmd filename");
			System.exit(1);
		}
		try {
			XMLZone zone = new ZoneXMLLoader().load(argv[0]);
			System.out.println("zone " + zone.name + " loaded successfully");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	private ZoneXMLLoader() {
		// hide constructor; this is a Singleton 
	}

	private static ZoneXMLLoader instance;

	public static ZoneXMLLoader get() {
		if (instance == null) {
			instance = new ZoneXMLLoader();
		}

		return instance;
	}

	private XMLZone currentZone;

	public XMLZone load(String ref) throws SAXException {
		// Use the default (non-validating) parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();

			InputStream is = getClass().getClassLoader().getResourceAsStream(ref);
			if (is == null) {
				throw new FileNotFoundException("cannot find resource '" + ref + "' in classpath");
			}

			saxParser.parse(new java.util.zip.InflaterInputStream(is), this);
		} catch (ParserConfigurationException t) {
			logger.error(t);
		} catch (IOException e) {
			logger.error(e);
			throw new SAXException(e);
		}

		return currentZone;
	}

	@Override
	public void startDocument() {
		currentZone = new XMLZone();
	}

	@Override
	public void endDocument() {
		// do nothing
	}

	private StringBuffer st;

	private String layerName;

	@Override
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) {
		if (qName.equals("map")) {
			currentZone.name = attrs.getValue("name");
		} else if (qName.equals("size")) {
			currentZone.width = Integer.parseInt(attrs.getValue("width"));
			currentZone.height = Integer.parseInt(attrs.getValue("height"));
		} else if (qName.equals("layer")) {
			layerName = attrs.getValue("name");
			st = new StringBuffer();
			st.append(currentZone.width + " " + currentZone.height);
		}
	}

	@Override
	public void endElement(String namespaceURI, String sName, String qName) {
		if (qName.equals("map")) {
			// System.out.println
			// (actualZone.name+"\t"+actualZone.x+"\t"+actualZone.y+"\t"+actualZone.level+"\t"+actualZone.width+"\t"+actualZone.height);
			// for(String entry: actualZone.layers.keySet())
			// {
			// System.out.println (entry);
			// System.out.println (actualZone.layers.get(entry));
			// }
		} else if (qName.equals("layer")) {
			currentZone.layers.put(layerName, st.toString().getBytes());
			st = null;
		}
	}

	@Override
	public void characters(char buf[], int offset, int len) {
		if (st != null) {
			st.append(buf, offset, len);
		}
	}
}
