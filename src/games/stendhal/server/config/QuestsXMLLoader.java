package games.stendhal.server.config;

import games.stendhal.server.maps.quests.QuestInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class QuestsXMLLoader extends DefaultHandler {

	/** The Singleton instance. */
	private static QuestsXMLLoader instance;

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(QuestsXMLLoader.class);

	private Map<String, QuestInfo> questInfos = null;

	// used while parsing the XML structure
	private String name;

	private QuestInfo currentQuestInfo;

	private Map<String, String> currentList;

	private StringBuilder text;

	private String entryName;

	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.err.println("Usage: cmd filename");
			System.exit(1);
		}

		try {
			System.out.println(new QuestsXMLLoader().load(argv[0]).size());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private QuestsXMLLoader() {
		// hide constructor, this is a Singleton
	}

	public static QuestsXMLLoader get() {
		if (instance == null) {
			instance = new QuestsXMLLoader();
			try {
				instance.load("data/conf/quests.xml");
			} catch (SAXException e) {
				logger.error(e, e);
			}
		}
		return instance;
	}

	public QuestInfo get(String name) {
		QuestInfo questInfo = questInfos.get(name);
		if (questInfo == null) {
			questInfo = new QuestInfo();
			questInfo.setName(name);
			questInfo.setTitle(name + " (unknown)");
			questInfo.setDescription(name + " (unkown)");
		}
		return questInfo;
	}

	public Map<String, QuestInfo> load(String ref) throws SAXException {
		questInfos = new HashMap<String, QuestInfo>();
		// Use the default (non-validating) parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();

			InputStream is = getClass().getClassLoader().getResourceAsStream(ref);
			if (is == null) {
				throw new FileNotFoundException("cannot find resource '" + ref + "' in classpath");
			}
			saxParser.parse(is, this);
		} catch (ParserConfigurationException t) {
			logger.error(t);
		} catch (IOException e) {
			logger.error(e);
			throw new SAXException(e);
		}
		return questInfos;
	}

	@Override
	public void startDocument() {
		// do nothing
	}

	@Override
	public void endDocument() {
		// do nothing
	}

	@Override
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) {
		text = new StringBuilder();

		if (qName.equals("quest")) {
			currentQuestInfo = new QuestInfo();
			name = attrs.getValue("name");
			currentQuestInfo.setName(name);

		} else if (qName.equals("repeatable")) {

			// TODO handle name.equals("repeatable")

		} else if (qName.equals("history") || qName.equals("hints")) {
			currentList = new HashMap<String, String>();
		} else if (qName.equals("entry")) {
			entryName = attrs.getValue("name");
		}
	}

	@Override
	public void endElement(String namespaceURI, String sName, String qName) {

		if (qName.equals("quest")) {
			questInfos.put(name, currentQuestInfo);
		} else if (qName.equals("title")) {
			currentQuestInfo.setTitle(text.toString());
		} else if (qName.equals("description")) {
			currentQuestInfo.setDescription(text.toString());
		} else if (qName.equals("gm-description")) {
			currentQuestInfo.setDescriptionGM(text.toString());
		} else if (qName.equals("history")) {
			currentQuestInfo.setHistory(currentList);
		} else if (qName.equals("hints")) {
			currentQuestInfo.setHints(currentList);
		} else if (qName.equals("entry")) {
			currentList.put(entryName, text.toString());
		}
	}

	@Override
	public void characters(char buf[], int offset, int len) {
		text.append((new String(buf, offset, len)).trim() + " ");
	}
}
