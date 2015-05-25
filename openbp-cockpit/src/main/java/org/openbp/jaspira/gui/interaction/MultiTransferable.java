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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openbp.common.ExceptionUtil;

/**
 * A multi transferable represents the composition of various transferables into
 * a single transferable.
 *
 * @author Stephan Moritz
 */
public class MultiTransferable
	implements Transferable
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Map of data flavors to sub transferables */
	private Map flavorMap;

	/** Flavor cache */
	private DataFlavor [] acceptedFlavors;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Generates an empty MultiTransferable.
	 */
	public MultiTransferable()
	{
		flavorMap = new LinkedHashMap();
	}

	/**
	 * Generates a new MultiTransferable from a collection of transferables.
	 *
	 * @param transferables Transferables to be added
	 */
	public MultiTransferable(Collection transferables)
	{
		this();

		for (Iterator it = transferables.iterator(); it.hasNext();)
		{
			addTransferable((Transferable) it.next());
		}
	}

	/**
	 * Generates a new MultiTransferable out from an array of transferables.
	 *
	 * @param transferables Transferables to be added
	 */
	public MultiTransferable(Transferable [] transferables)
	{
		this();

		for (int i = 0; i < transferables.length; i++)
		{
			addTransferable(transferables [i]);
		}
	}

	/**
	 * Adds the given transferable to the structure.
	 *
	 * @param transferable Transferable to be added
	 */
	public void addTransferable(Transferable transferable)
	{
		DataFlavor [] flavors = transferable.getTransferDataFlavors();

		for (int i = 0; i < flavors.length; i++)
		{
			if (!flavorMap.containsKey(flavors [i]))
			{
				// Flavor is not yet existent
				flavorMap.put(flavors [i], transferable);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns an array of DataFlavor objects indicating the flavors the data
	 * can be provided in. The array should be ordered according to preference
	 * for providing the data (from most richly descriptive to least descriptive).
	 * @return An array of data flavors in which this data can be transferred
	 */
	public DataFlavor [] getTransferDataFlavors()
	{
		if (acceptedFlavors == null)
		{
			acceptedFlavors = new DataFlavor [flavorMap.size()];
			flavorMap.keySet().toArray(acceptedFlavors);
		}

		return acceptedFlavors;
	}

	/**
	 * Returns whether or not the specified data flavor is supported for this object.
	 *
	 * @param flavor Requested flavor for the data
	 * @return Boolean indicating whether or not the data flavor is supported
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavorMap.containsKey(flavor);
	}

	/**
	 * Returns an object which represents the data to be transferred.  The class
	 * of the object returned is defined by the representation class of the flavor.
	 *
	 * @param flavor requested flavor for the data; see DataFlavor#getRepresentationClass
	 * @exception IOException If the data is no longer available in the requested flavor
	 * @exception UnsupportedFlavorException If the requested data flavor is not supported
	 * @nowarn
	 */
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		Transferable sub = (Transferable) flavorMap.get(flavor);

		if (sub == null)
		{
			throw new UnsupportedFlavorException(flavor);
		}

		return sub.getTransferData(flavor);
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
