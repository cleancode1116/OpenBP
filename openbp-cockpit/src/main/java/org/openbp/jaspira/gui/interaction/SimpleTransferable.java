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

import org.openbp.common.ExceptionUtil;

/**
 * Transferable for a single object.
 *
 * @author Heiko Erhardt
 */
public class SimpleTransferable
	implements Transferable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Transferred object */
	private Object object;

	/** Data flavors supported by this transferable */
	private DataFlavor [] dataflavors;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param object Object to transfer
	 * @param flavor Single flavor supported by this object
	 */
	public SimpleTransferable(Object object, DataFlavor flavor)
	{
		this.object = object;

		dataflavors = new DataFlavor [] { flavor };
	}

	/**
	 * Constructor.
	 * @param object Object to transfer
	 * @param flavors Flavors supported by this object
	 */
	public SimpleTransferable(Object object, DataFlavor [] flavors)
	{
		this.object = object;

		dataflavors = flavors;
	}

	//////////////////////////////////////////////////
	// @@ Transferable implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor [] getTransferDataFlavors()
	{
		return dataflavors;
	}

	/**
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		for (int i = 0; i < dataflavors.length; i++)
		{
			if (dataflavors [i].equals(flavor))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferData(DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		for (int i = 0; i < dataflavors.length; i++)
		{
			if (dataflavors [i].equals(flavor))
			{
				return object;
			}
		}

		throw new UnsupportedFlavorException(flavor);
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
