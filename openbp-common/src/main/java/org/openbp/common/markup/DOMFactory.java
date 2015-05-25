/*
 *   Copyright 2007 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.common.markup;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory class that supports the creation of DOM trees.
 */
public class DOMFactory
{
	//////////////////////////////////////////////////
	// @@ Static data
	//////////////////////////////////////////////////

	/** Document instance this factory operates on */
	private Document doc;

	//////////////////////////////////////////////////
	// @@ Document creation
	//////////////////////////////////////////////////

	/**
	 * Constructor for a new, empty document.
	 */
	public DOMFactory()
	{
		// Create a DOM document (needed to create documenation elements).
		try
		{
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			doc = builder.newDocument();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Constructor that specifies the name of the root node.
	 *
	 * @param rootTag Name of the root tag
	 */
	public DOMFactory(String rootTag)
	{
		this();

		Element rootElement = doc.createElement(rootTag);
		doc.appendChild(rootElement);
	}

	/**
	 * Gets the document instance this factory operates on.
	 * @nowarn
	 */
	public Document getDocument()
	{
		return doc;
	}

	/**
	 * Sets the document instance this factory operates on.
	 * @nowarn
	 */
	public void setDocument(Document doc)
	{
		this.doc = doc;
	}

	/**
	 * Gets the root element of the DOM tree.
	 * @nowarn
	 */
	public Element getRootElement()
	{
		return doc.getDocumentElement();
	}

	//////////////////////////////////////////////////
	// @@ Text node creation
	//////////////////////////////////////////////////

	/**
	 * Creates an element containing a string value and adds it to a parent element.
	 * In contrast to the other createTextNode methods, this method generates also a empty tag.
	 *
	 * @param parent Parent element the new element shall be linked to or null
	 * @param nodeName Name of the element
	 * @param value Value of the element or null
	 *
	 * @return The element
	 */
	public Element createTextOrEmptyNode(Element parent, String nodeName, String value)
	{
		Element item = doc.createElement(nodeName);
		if (value != null)
		{
			item.appendChild(doc.createTextNode(value));
		}
		if (parent != null)
		{
			parent.appendChild(item);
		}
		return item;
	}

	/**
	 * Creates an element containing a string value and adds it to a parent element.
	 *
	 * @param parent Parent element the new element shall be linked to or null
	 * @param nodeName Name of the element
	 * @param value Value of the element or null
	 *
	 * @return The element or null if the value is null
	 */
	public Element createTextNode(Element parent, String nodeName, String value)
	{
		Element item = createTextNode(nodeName, value);
		if (parent != null && item != null)
		{
			parent.appendChild(item);
		}
		return item;
	}

	/**
	 * Generates a XML text node from String.
	 *
	 * @param nodeName Name of the element
	 * @param value Value of the element
	 *
	 * @return The element or null if the value is null
	 */
	public Element createTextNode(String nodeName, String value)
	{
		// Create element
		if (value == null)
			return null;
		Element item = doc.createElement(nodeName);
		item.appendChild(doc.createTextNode(value));
		return item;
	}

	/**
	 * Generates a XML text node from String.
	 *
	 * @param nodeName Name of the element
	 * @param value Value of the element
	 *
	 * @return The element or null if the value is null
	 */
	public Element createTextNode(String nodeName, int value)
	{
		return createTextNode(nodeName, String.valueOf(value));
	}

	/**
	 * Generates a XML text node from String.
	 *
	 * @param nodeName Name of the element
	 * @param value Value of the element
	 *
	 * @return The element or null if the value is null
	 */
	public Element createTextNode(String nodeName, Integer value)
	{
		if (value == null)
			return null;
		return createTextNode(nodeName, value.toString());
	}

	/**
	 * Generates a XML text node from boolean.
	 *
	 * @param nodeName Name of the element
	 * @param value Value of the element
	 *
	 * @return The element or null if the value is null
	 */
	public Element createTextNode(String nodeName, boolean value)
	{
		// Create element
		if (!value)
			return null;
		return createTextNode(nodeName, String.valueOf(value));
	}

	/**
	 * Creates an empty element.
	 *
	 * @param nodeName Name of the element
	 *
	 * @return The element
	 */
	public Element createElement(String nodeName)
	{
		return doc.createElement(nodeName);
	}

	/**
	 * Creates an empty element and adds that to the parent element.
	 *
	 * @param parent The parent element
	 * @param nodeName Name of the element
	 *
	 * @return The element
	 */
	public Element createElement(Element parent, String nodeName)
	{
		Element e = doc.createElement(nodeName);
		parent.appendChild(e);
		return e;
	}
}
