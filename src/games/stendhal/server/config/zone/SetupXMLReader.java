/*
 * @(#) src/games/stendhal/server/config/zone/SetupXMLReader.java
 *
 * $Id$
 */

package games.stendhal.server.config.zone;

//
//

import games.stendhal.server.config.XMLUtil;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * A base setup xml reader.
 */
public abstract class SetupXMLReader {
	/**
	 * Logger
	 */
	private static final Logger logger = Log4J.getLogger(SetupXMLReader.class);


	//
	// SetupXMLReader
	//

	/**
	 * Create a setup descriptor from XML data.
	 *
	 * @param	element		The descriptor XML element.
	 *
	 * @return	A setup descriptor, or <code>null</code> if invalid.
	 */
	public abstract SetupDescriptor read(final Element element);


	/**
	 * Read paramaters from an XML element.
	 *
	 * @param	desc		The descriptor to load.
	 * @param	element		The XML element.
	 */
	protected void readParameters(final SetupDescriptor desc, final Element element) {
		List<Element> list = XMLUtil.getElements(element, "parameter");

		for(Element param : list) {
			if(!param.hasAttribute("name")) {
				logger.error("Unnamed parameter");
			} else {
				desc.setParameter(param.getAttribute("name"), XMLUtil.getText(param).trim());
			}
		}
	}
}
