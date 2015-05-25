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
package org.openbp.common.io.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.openbp.common.logger.LogUtil;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

/**
 * The XML driver class implements a generic driver for XML serialzation/deserialization.
 * It builds upon Castor XML/bean mapping support.
 *
 * @author Heiko Erhardt
 */
public final class XMLDriver
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/**
	 * Mapping indicator table.
	 * Maps classes (Class objects) to an empty string to indicate that the class is
	 * contained in the mapping table.
	 */
	private Map mappedClasses = new HashMap();

	/** Castor XML context */
	private XMLContext context;

	/** Castor mapping table */
	private Mapping mapping;

	/** Class descriptor resolver that automatically loads mapping files */
	// private ClassDescriptorResolver resolver;

	/** Class loader to user */
	private ClassLoader loader;

	/** Encoding for XML I/O */
	private String encoding = "UTF-8";
	// private String encoding = "ISO-8859-1";

	/** Flag if output should be pretty-printed */
	private boolean prettyPrint;

	/** Debug mode flag. Performs object model validation. */
	private boolean debugMode = false;

	/** Singleton instance */
	private static XMLDriver singletonInstance;

	/** Default class loader for all instances */
	private static ClassLoader defaultClassLoader;


	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized XMLDriver getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new XMLDriver();
		}
		return singletonInstance;
	}

	/**
	 * Constructor.
	 */
	private XMLDriver()
	{
		this(defaultClassLoader);
	}

	/**
	 * Class loader constructor.
	 *
	 * @param loader Class loader to use for class access
	 */
	public XMLDriver(ClassLoader loader)
	{
		if (loader == null)
		{
			if (defaultClassLoader != null)
				loader = defaultClassLoader;
			else
				loader = getClass().getClassLoader();
		}
		this.loader = loader;

		mapping = new Mapping(loader);

		// The setIdResolver method is not standard to Castor's Mapping class,
		// this was a custom enhancement to Castor. Unfortunately, none of the Castor
		// maintainers cared to include it in the distribution.
		//
		// It's task is to force dynamic loading of mappings that are used by another mapping
		// (e. g. by the 'extends' attribute of the class tag). This is very helpful for
		// complex dependancy structures.
		// However, we should be compatible with current Castor versions.
		// In order to achive this, we must take care that mapping are loaded by calls to
		// @loadMapping in reverse hierarchy order (leaves first, climbin the hierarchy tree up).
		// This is sometimes difficult to figure out, so we can use the XClassMappingResolver class
		// temporarily for this task, get the order defined and then remove the following line again.
		//
		// mapping.setIDResolver (new XClassMappingResolver (this, loader));

		// Create a custom resolver that automatically loads mappings.
		// This doesn't work as expected - we might investigate this at a later time
		// resolver = new XMLDriverClassDescriptorResolver(this);
		// resolver = new ClassDescriptorResolverImpl();
	}

	/**
	 * Sets the default class loader for all instances.
	 * @nowarn
	 */
	public static void setDefaultClassLoader(ClassLoader defaultClassLoaderArg)
	{
		defaultClassLoader = defaultClassLoaderArg;
	}

	/**
	 * Gets the class loader to user.
	 * @nowarn
	 */
	public ClassLoader getLoader()
	{
		return loader;
	}

	private XMLContext getContext()
		throws MappingException
	{
		if (context == null)
		{
			context = new XMLContext();
			context.setProperty("org.exolab.castor.indent", isPrettyPrint() ? "true" : "false");
			context.setProperty("org.exolab.castor.debug", "true");
			context.addMapping(mapping);
		}
		return context;
	}

	//////////////////////////////////////////////////
	// @@ Serializable interface support
	//////////////////////////////////////////////////

	/**
	 * Serializes an object to an object output stream.
	 * This method needs to be called from inside the writeObject method
	 * if an object should be serialized using the XML driver.
	 * Serialization is used for RMI in the first line.
	 *
	 * @param o Object to serialize
	 * @param out Object output stream to write to
	 * @throws IOException On i/o or xml/marshalling error
	 */
	public void serializeObject(Object o, ObjectOutputStream out)
		throws IOException
	{
		try
		{
			// Default buffer size is 2048 bytes
			ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);

			serialize(o, bos);

			out.writeObject(bos.toByteArray());
		}
		catch (XMLDriverException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////////////////////
	// @@ Serialization
	//////////////////////////////////////////////////

	/**
	 * Serializes an object to an output file.
	 * @param o Object to serialize
	 * @param fileName Output file name
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public void serialize(Object o, String fileName)
		throws XMLDriverException
	{
		try
		{
			serialize(o, new FileWriter(fileName));
		}
		catch (IOException e)
		{
			String s = LogUtil.warn(getClass(), "Error writing XML data to file $0.", fileName, e);
			throw new XMLDriverException(s);
		}
		catch (XMLDriverException e)
		{
			String s = LogUtil.error(getClass(), "Error writing XML data to file $0.", fileName, e);
			throw new XMLDriverException(s);
		}
	}

	/**
	 * Serializes an object to an output stream.
	 * @param o Object to serialize
	 * @param out Output stream
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public void serialize(Object o, OutputStream out)
		throws XMLDriverException
	{
		try
		{
			serialize(o, new OutputStreamWriter(out, encoding));
		}
		catch (UnsupportedEncodingException e)
		{
			String s = LogUtil.error(getClass(), "Unsupported encoding $0.", encoding, e);
			throw new XMLDriverException(s);
		}
	}

	/**
	 * Serializes an object using the provided marshaller.
	 *
	 * @param o Object to serialize
	 * @param writer Output writer
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	private void serialize(Object o, Writer writer)
	{
		// Make sure the mapping is loaded
		Class cls = o.getClass();
		loadMapping(cls);

		// Marshal the object
		try
		{
			Marshaller marshaller = getContext().createMarshaller();
			marshaller.setWriter(writer);

			marshaller.marshal(o);
		}
		catch (Throwable t)
		{
			String s = LogUtil.error(getClass(), "Error serializing an object of class $0 to XML.", cls.getName(), t);
			throw new XMLDriverException(s);
		}
	}

	/**
	 * Serializes an object into a DOM node.
	 *
	 * @param o Object to serialize
	 * @param node The DOM node to marshal into
	 * @throws XMLDriverException On xml/marshalling error
	 */
	public void serialize(Object o, Node node)
		throws XMLDriverException
	{
		// Make sure the mapping is loaded
		Class cls = o.getClass();
		loadMapping(cls);

		// Marshal the object
		try
		{
			Marshaller marshaller = getContext().createMarshaller();
			if (encoding != null)
			{
				marshaller.setEncoding(encoding);
			}
			/*
			marshaller.setValidation(debugMode);
			 */

			Marshaller.marshal(o, node);
		}
		catch (Throwable t)
		{
			String s = LogUtil.error(getClass(), "Error serializing an object of class $0 to XML.", cls.getName(), t);
			throw new XMLDriverException(s);
		}
	}


	//////////////////////////////////////////////////
	// @@ Deserialization - File support
	//////////////////////////////////////////////////

	/**
	 * Deserializes an object of unknown type from an input file.
	 * @param fileName Input file name
	 * @return The deserialized object
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public Object deserializeFile(String fileName)
		throws XMLDriverException
	{
		return deserializeFile(null, fileName);
	}

	/**
	 * Deserializes an object of known type from an input file.
	 * @param cls Class of the object to deserialize
	 * @param fileName Input file name
	 * @return The deserialized object
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public Object deserializeFile(Class cls, String fileName)
		throws XMLDriverException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(fileName);
		}
		catch (FileNotFoundException e)
		{
			String s = LogUtil.error(getClass(), "Cannot open file $0.", fileName);
			throw new XMLDriverException(s);
		}

		try
		{
			return deserializeStream(cls, in);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Deserialization - Resource support
	//////////////////////////////////////////////////

	/**
	 * Deserializes an object of unknown type from an input file.
	 * @param resource Resource to open
	 * @return The deserialized object
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public Object deserializeResource(Resource resource)
		throws XMLDriverException
	{
		return deserializeResource (null, resource);
	}

	/**
	 * Deserializes an object of known type from an input file.
	 * @param cls Class of the object to deserialize
	 * @param resource Resource to open
	 * @return The deserialized object
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public Object deserializeResource(Class cls, Resource resource)
		throws XMLDriverException
	{
		InputStream in = null;
		try
		{
			in = resource.getInputStream();
		}
		catch (IOException e)
		{
			throw new XMLDriverException("Resource '" + resource.getDescription() + "' not found. ", e);
		}

		try
		{
			return deserializeStream(cls, in);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Deserialization - Stream support
	//////////////////////////////////////////////////

	/**
	 * Deserializes an object of known type from an input stream.
	 * @param cls Class of the object to deserialize
	 * @param in Input stream
	 * @return The deserialized object
	 * @throws XMLDriverException On i/o or xml/marshalling error
	 */
	public Object deserializeStream(Class cls, InputStream in)
		throws XMLDriverException
	{
		if (cls != null)
		{
			// Make sure the mapping is loaded
			loadMapping(cls);
		}

		// Marshal the object
		try
		{
			InputStreamReader isr = new InputStreamReader(in);

			Unmarshaller unmarshaller = getContext().createUnmarshaller();
			if (cls != null)
			{
				// We know the class, this should save the unmarshaller the lookup work
				unmarshaller.setClass (cls);
			}
			unmarshaller.setValidation(debugMode);

			return unmarshaller.unmarshal(isr);
		}
		catch (Throwable t)
		{
			String className = cls != null ? cls.getName() : "<unknown>";
			String s = LogUtil.error(getClass(), "Error deserializing an object of class $0 from XML.", className, t);
			throw new XMLDriverException(s);
		}
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the encoding for XML I/O.
	 * @nowarn
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * Sets the encoding for XML I/O.
	 * @nowarn
	 */
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Gets the flag if output should be pretty-printed.
	 * @nowarn
	 */
	public boolean isPrettyPrint()
	{
		return prettyPrint;
	}

	/**
	 * Sets the flag if output should be pretty-printed.
	 * @nowarn
	 */
	public void setPrettyPrint(boolean prettyPrint)
	{
		this.prettyPrint = prettyPrint;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Loads a set of XML mappings.
	 * Does not generate a persistence exception, writes any errors to the log file instead.
	 *
	 * @param itClasses Iterator that contains the class objects (java.lang.Class) to load the mappings for
	 * @return
	 *		true	All mappings were loaded successfully.<br>
	 *		false	There were errors loading the mappings. See the log file for further details.
	 */
	public boolean loadMappings(Iterator itClasses)
	{
		boolean success = true;
		while (itClasses.hasNext())
		{
			Class cls = (Class) itClasses.next();

			try
			{
				loadMapping(cls);
			}
			catch (XMLDriverException e)
			{
				LogUtil.error(getClass(), "Error loading mapping.", e);
				success = false;
			}
		}
		return success;
	}

	/**
	 * Loads a set of XML mappings.
	 * Does not generate a persistence exception, writes any errors to the log file instead.
	 *
	 * @param classes Classes to load the mappings for
	 * @return
	 *		true	All mappings were loaded successfully.<br>
	 *		false	There were errors loading the mappings. See the log file for further details.
	 */
	public boolean loadMappings(Class [] classes)
	{
		boolean success = true;
		for (int i = 0; i < classes.length; ++i)
		{
			try
			{
				loadMapping(classes [i]);
			}
			catch (XMLDriverException e)
			{
				LogUtil.error(getClass(), "Error loading mapping.", e);
				success = false;
			}
		}
		return success;
	}

	/**
	 * Loads the XML mapping for the specified class.
	 * Checks the mapping table first. If no mapping is present yet, the method tries to read the mapping
	 * from the Castor mapping file with the same name as the class in the same directory as the class.
	 *
	 * @param cls Class to load the mapping for
	 * @throws XMLDriverException If no mapping file exists for this class or if
	 * the mapping file could not be loaded successfully
	 */
	public void loadMapping(Class cls)
		throws XMLDriverException
	{
		Object o = mappedClasses.get(cls);
		if (o != null)
		{
			// Class has already been loaded or at least an attempt has been performed
			return;
		}

		// Force new context creation when changin mapping
		context = null;

		String className = cls.getName();

		int index = className.lastIndexOf('.');
		String mappingFileName = className.substring(index + 1);
		mappingFileName += "Map.xml";

		URL url = cls.getResource(mappingFileName);
		if (url == null)
		{
			// Set indicator that loading failed
			mappedClasses.put(cls, Boolean.FALSE);

			String s = LogUtil.error(getClass(), "Mapping resource not found for class $0." + className);
			throw new XMLDriverException(s);
		}

		// Load the mapping from the resource
		try
		{
			mapping.loadMapping(url);
		}
		catch (Throwable t)
		{
			// Set indicator that loading failed
			mappedClasses.put(cls, Boolean.FALSE);

			String s = LogUtil.error(getClass(), "Error loading mapping resource for class $0.", className, t);
			throw new XMLDriverException(s);
		}

		// Also try to load the mapping for the super class if it's one of ours
		Class superClass = cls.getSuperclass();
		if (superClass != null && superClass.getName().startsWith("org.openbp."))
		{
			try
			{
				loadMapping(superClass);
			}
			catch (XMLDriverException e)
			{
				// Set indicator that loading failed
				mappedClasses.put(cls, Boolean.FALSE);

				throw e;
			}
		}

		// Set indicator that mapping was loaded
		mappedClasses.put(cls, Boolean.TRUE);
	}

	/**
	 * Loads a mapping from a mapping file.
	 * The mapping is just added to the mapping list of the xml driver.
	 * Since there is no class file associated with it, the class cache remains unchanged.
	 *
	 * @param file Mapping file to load
	 * @throws XMLDriverException If no mapping file exists for this class or if
	 * the mapping file could not be loaded successfully
	 */
	public void loadMapping(File file)
		throws XMLDriverException
	{
		// Build the URL
		URL url;
		try
		{
			url = file.toURL();
		}
		catch (MalformedURLException e)
		{
			String s = LogUtil.error(getClass(), "Mapping file $0 does not exist", file, e);
			throw new XMLDriverException(s);
		}

		// Load the mapping from the resource
		try
		{
			mapping.loadMapping(url);
		}
		catch (Throwable t)
		{
			String s = LogUtil.error(getClass(), "Error loading mapping resource from file $0.", file.getPath(), t);
			throw new XMLDriverException(s);
		}
	}

	/**
	 * Gets the the castor mapping table.
	 *
	 * @return The castor mapping table
	 */
	public Mapping getMapping()
	{
		return mapping;
	}
}
