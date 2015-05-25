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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openbp.common.CollectionUtil;
import org.openbp.common.string.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides methods for general use.
 */
public final class XMLUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private XMLUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Document parsing
	//////////////////////////////////////////////////

	/**
	 * Parses an input source and creates a document from it.
	 *
	 * @param is XML input source
	 * @return The document read
	 * @throws IOException On error
	 */
	public static Document parseDocument(InputSource is)
		throws IOException, SAXException
	{
		if (is == null)
			return null;

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);

			return doc;
		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("XML parser configuration problem: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses an input file and creates a document from it.
	 *
	 * @param fileName Name of file to read
	 * @return XML node
	 * @throws IOException On error
	 * @throws SAXException On error
	 */
	public static Document parseDocument(String fileName)
		throws IOException, SAXException
	{
		if (fileName == null)
			return null;

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(fileName);

			return doc;
		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("XML parser configuration problem: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses an input file and creates a document from it.
	 * Before parsing, all characters that are not 7 bit ASCII characters will be
	 * automatically converted to their HTML representation, i\. e\. &"#207;".<br>
	 * Optionally, substitutes $(key) parameters in the input file by their values.
	 *
	 * @param fileName Name of file to read
	 * @param substitute Map mapping parameter keys to values or null
	 * @return XML node
	 * @throws IOException On error
	 * @throws SAXException On error
	 */
	public static Document parseDocument(String fileName, Map substitute)
		throws IOException, SAXException
	{
		InputStream in = null;

		try
		{
			in = new FileInputStream(fileName);

			// Read source document
			String fileString = readFile(in);

			// Convert to 7 bit chars
			fileString = XMLEscapeHelper.encodeXMLString(fileString);
			if (substitute != null)
			{
				fileString = StringUtil.substitute(fileString, substitute);
			}

			InputStream bin = new ByteArrayInputStream(fileString.getBytes());
			return parseDocument(new InputSource(bin));
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Document/element serialization
	//////////////////////////////////////////////////

	/**
	 * Serializes the XML document to a file.
	 *
	 * @param document Document or null
	 * @param fileName Name of the file to serialize into
	 * @exception IOException Error on converting or writing file
	 */
	public static void serialize(Document document, String fileName)
		throws IOException
	{
		if (document == null)
			return;

		Exception targetException = null;

		// Use a XSLT transformer for writing the XML file
		Transformer transformer;
		try
		{
			transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			FileOutputStream os = new FileOutputStream(new File(fileName));
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		}
		catch (TransformerConfigurationException e)
		{
			targetException = e;
		}
		catch (FileNotFoundException e)
		{
			targetException = e;
		}
		catch (TransformerException e)
		{
			targetException = e;
		}
		catch (TransformerFactoryConfigurationError e)
		{
			throw new IOException("Error serializing XML data to file '" + fileName + "':" + e.toString());
		}

		if (targetException != null)
		{
			throw new IOException("Error serializing XML data to file '" + fileName + "': " + targetException.getMessage());
		}
	}

	/**
	 * Serializes the XML node tree to a string.
	 *
	 * @param document XML node or null
	 * @return XML string or null on error or if document is null
	 * @exception IOException Error on converting
	 */
	public static String serialize(Document document)
		throws IOException
	{
		if (document == null)
			return null;

		Exception targetException = null;

		// Use a XSLT transformer for writing the XML file
		Transformer transformer;
		try
		{
			transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
			return os.toString();
		}
		catch (TransformerConfigurationException e)
		{
			targetException = e;
		}
		catch (TransformerException e)
		{
			targetException = e;
		}
		catch (TransformerFactoryConfigurationError e)
		{
			throw new IOException("Error serializing XML data:" + e.toString());
		}

		if (targetException != null)
		{
			throw new IOException("Error serializing XML data: " + targetException.getMessage());
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Node utilities
	//////////////////////////////////////////////////

	/**
	 * Gets a named child node.
	 * In contrast to the Element.getElementsByTagName method, this method is fine
	 * if there is only a single element by that name.<br>
	 * Note: Only child \belements\b will be considered.
	 *
	 * @deprecated {@link #getChildElement}
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child
	 * @return The child node or null if no such node exists
	 */
	public static Node getChildNode(Node parent, String childName)
	{
		return getChildElement(parent, childName);
	}

	/**
	 * Gets a named child node.
	 * In contrast to the Element.getElementsByTagName method, this method is fine
	 * if there is only a single element by that name.<br>
	 * Note: Only child \belements\b will be considered.
	 *
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child
	 * @return The child node or null if no such node exists
	 */
	public static Element getChildElement(Node parent, String childName)
	{
		return (Element) getChildNode(parent, childName, Node.ELEMENT_NODE);
	}

	/**
	 * Gets a named child node by the node type.
	 * In contrast to the Element.getElementsByTagName
	 * method, this method is fine if there is only a single element by that name.\n Note: Only
	 * child node of the given node type will be considered.
	 *
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child
	 * @param nodeType see types of interface Node
	 * @return The child node or null if no such node exists
	 */
	public static Node getChildNode(Node parent, String childName, short nodeType)
	{
		// Search the child nodes for the correct one
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if (child.getNodeType() != nodeType)
				continue;

			if (childName == null || childName.equals(child.getNodeName()))
			{
				return child;
			}
		}

		return null;
	}

	/**
	 * Gets a named child elements in flat level.
	 * In contrast to the Element.getElementsByTagName
	 * method, this method is fine if you want a list of elements by the name but only that
	 * elements which of their owner nodes are the parent node.
	 *
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child

	 * @return The child element array
	 */
	public static Element [] getChildElements(Node parent, String childName)
	{
		return getChildElements(parent, new String [] { childName });
	}

	/**
	 * Gets a named child elements in flat level.
	 * In contrast to the Element.getElementsByTagName
	 * method, this method is fine if you want a list of elements by the name but only that
	 * elements which of their owner nodes are the parent node.
	 *
	 * @param parent Parent node
	 * @param childNames Names of the child node or null for the first child

	 * @return The child element array
	 */
	public static Element [] getChildElements(Node parent, String [] childNames)
	{
		ArrayList childNameList = new ArrayList();
		CollectionUtil.addAll(childNameList, childNames);

		ArrayList elements = new ArrayList();

		// Search the child nodes for the correct one
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (childNameList.contains(child.getNodeName()))
			{
				elements.add(child);
			}
		}

		return (Element []) elements.toArray(new Element [elements.size()]);
	}

	/**
	 * Gets the text value of a named child node.
	 * First the child node is retrieved by it's name, then the {@link #getNodeValue} method
	 * is applied to it.<br>
	 * Note: Only child \belements\b will be considered.
	 *
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child
	 * @return The text value or null if the node has no text value, has no children or
	 * it's child has not text value or if the text value is empty.<br>
	 * Any spaces are trimmed from the value.
	 */
	public static String getChildNodeValue(Node parent, String childName)
	{
		String value = null;

		Node child = getChildNode(parent, childName);
		if (child != null)
			value = XMLUtil.getNodeValue(child);

		return value;
	}

	/**
	 * Gets the text value of a named child node.
	 * First the child node is retrieved by it's name, then the {@link #getNodeValue} method
	 * is applied to it.<br>
	 * Note: Only child \belements\b will be considered.
	 *
	 * @param parent Parent node
	 * @param childName Name of the child node or null for the first child
	 * @param nodeType see types of interface Node
	 * @return The text value or null if the node has no text value, has no children or
	 * it's child has not text value or if the text value is empty.<br>
	 * Any spaces are trimmed from the value.
	 */
	public static String getChildNodeValue(Node parent, String childName, short nodeType)
	{
		String value = null;

		Node child = getChildNode(parent, childName, nodeType);
		if (child != null)
			value = getNodeValue(child);

		return value;
	}

	/**
	 * Gets the text value of a node.
	 * First, the node is checked if has a text value itself.
	 * If not, the method tries to retrieve a value from the child node of the node.
	 *
	 * @param node Node to check
	 * @return The text value or null if the node has no text value, has no children or
	 * it's child has not text value or if the text value is empty.<br>
	 * Any spaces are trimmed from the value.
	 */
	public static String getNodeValue(Node node)
	{
		String value = null;

		if (node != null)
		{
			value = node.getNodeValue();
			if (value == null)
			{
				// Not a text node, check the child node
				Node child = node.getFirstChild();
				if (child != null)
					value = child.getNodeValue();
			}

			if (value != null)
			{
				value = value.trim();
				if (value.length() == 0)
					value = null;
			}
		}

		return value;
	}

	/**
	 * Checks if a node resembles the given XML tag (including attributes).
	 *
	 * @param node Node to compare
	 * @param tag XML tag to compare, e. g. '<tagname att1="abc" att2="def">'
	 *
	 * @return
	 *	true:	Node is equal tag <br>
	 *	false:	Node is not equal tag
	 */
	public static boolean compareStringNode(Node node, String tag)
	{
		if (tag.indexOf(node.getNodeName()) == -1)
			return false;

		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			for (int i = 0; i < attributes.getLength(); i++)
			{
				if (tag.indexOf(attributes.item(i).getNodeName()) == -1)
					return false;
				if (tag.indexOf(attributes.item(i).getNodeValue()) == -1)
					return false;
			}
		}

		return true;
	}

	/**
	 * Reads the input stream and gets a string.
	 *
	 * @param in Inputstream
	 *
	 * @throws IOException Stream error
	 *
	 * @return String
	 */
	private static String readFile(InputStream in)
		throws IOException
	{
		byte [] data = new byte [in.available()];
		in.read(data);
		return new String(data);
	}
}
