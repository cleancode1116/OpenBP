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
package org.openbp.jaspira.gui.interaction;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openbp.common.ExceptionUtil;

/**
 * Basic Transferable that contains data flavors for all implemented interfaces and
 * subclasses of a given transferable. A list of classes and interfaces can be
 * supplied to define those that should NOT be converted into flavors.
 *
 * @author Stephan Moritz
 */
public class BasicTransferable
	implements Transferable
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The transfer data */
	protected Object data;

	/** Contains a set of the flavors supported by this transferable object */
	private Set supportedFlavors;

	/////////////////////////////////////////////////////////////////////////
	// @@ COnstruction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param data Transfer data
	 */
	public BasicTransferable(Object data)
	{
		this(data, null);
	}

	/**
	 * Constructor.
	 * @param data Transfer data
	 * @param exclusions List of classes (Class objects) to exclude of null
	 */
	public BasicTransferable(Object data, List exclusions)
	{
		this.data = data;

		// Hash so that we do not need to care for duplicates.
		supportedFlavors = new HashSet();

		if (data != null)
		{
			buildClassFlavors(data.getClass(), exclusions);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Initialization
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Retrieves all  superclasses and implemented interface of a given class
	 * and adds the appropriate flavors to the list of supported flavors.
	 * Uses recursion.
	 *
	 * @param cls The class to work upon
	 * @param exclusions Any class object contained herein will NOT be converted into a DataFlavor
	 */
	private void buildClassFlavors(Class cls, List exclusions)
	{
		if (cls == null)
		{
			// Recursion break
			return;
		}

		// We add ourselves
		if (exclusions == null || !exclusions.contains(cls))
		{
			supportedFlavors.add(new DataFlavor(cls, cls.getName()));
		}

		// Work on implemented interfaces
		Class [] interfaces = cls.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
		{
			buildClassFlavors(interfaces [i], exclusions);
		}

		// work on the superclass
		buildClassFlavors(cls.getSuperclass(), exclusions);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Implemantation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the object in a way associated with a flavor.
	 * Delegates first to a template method (getUserTransferData).
	 * If this yields no result returns the data object if flavor is supported.
	 * Throws an UnsupportedFlavorException otherwise.
	 * @param flavor Flavor to retrieve
	 * @return The resulting transfer data
	 * @throws UnsupportedFlavorException If the flavor is not supported
	 * @throws IOException On i/o error
	 */
	public final Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		Object result = getUserTransferData(flavor);
		if (result != null)
		{
			return result;
		}

		if (supportedFlavors.contains(flavor))
		{
			return data;
		}

		throw new UnsupportedFlavorException(flavor);
	}

	/**
	 * Template mehtod for subclasses to add their own flavor support.
	 *
	 * @param flavor Flavor to retrieve
	 * @return The object to return or null if the template method doesn't support this flavor
	 */
	public Object getUserTransferData(DataFlavor flavor)
		throws IOException
	{
		return null;
	}

	/**
	 * Returns an array of all flavors supported by this Transferable.
	 * @nowarn
	 */
	public final DataFlavor [] getTransferDataFlavors()
	{
		Collection userFlavors = getUserTransferDataFlavors();

		DataFlavor [] flavors = new DataFlavor [supportedFlavors.size() + userFlavors.size()];

		int i = 0;

		for (Iterator it = userFlavors.iterator(); it.hasNext(); i++)
		{
			flavors [i] = (DataFlavor) it.next();
		}

		for (Iterator it = supportedFlavors.iterator(); it.hasNext(); i++)
		{
			flavors [i] = (DataFlavor) it.next();
		}

		return flavors;
	}

	/**
	 * Template method for subclasses to add their own flavors. This should usually
	 * return a List (or other Collection with a fixed order), order from the most precise
	 * to the most general flavor.
	 *
	 * @return Collection
	 */
	public Collection getUserTransferDataFlavors()
	{
		return Collections.EMPTY_LIST;
	}

	/**
	 * Checks if this transferable supports the given flavor.
	 * @return
	 *		true	The flavor is supported by the transfer data of this object.<br>
	 *		false	Unsupported flavor
	 * @nowarn
	 */
	public final boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return getUserTransferDataFlavors().contains(flavor) || supportedFlavors.contains(flavor);
	}

	/**
	 * Gets the transfer data in the desired format (convenience method that supresses exceptions).
	 * You should call this method only if have previously checked if the flavor is supported
	 * by calling {@link #isDataFlavorSupported}.
	 *
	 * @param flavor Flavor to get
	 * @return The desired object or null if the flavor is not supported or an i/o error has occurred.
	 * In the latter case, the method prints a stack trace to stderr.
	 */
	public Object getSafeTransferData(DataFlavor flavor)
	{
		try
		{
			return getTransferData(flavor);
		}
		catch (UnsupportedFlavorException e)
		{
			// Silently ignore
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}
		return null;
	}
}
