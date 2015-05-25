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
package org.openbp.guiclient.model.item;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.BasicTransferable;

/**
 * Transferable container for an item. An ItemTransferable supports two flavors so far:
 * - an itemFlavor
 * - flavors for specific itemTypes. Note that both flavors actually return the same object.
 *
 * @author Stephan Moritz
 */
public class ItemTransferable extends BasicTransferable
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param item Item as event object
	 */
	public ItemTransferable(Item item)
	{
		super(item);
	}

	/**
	 * Returns the supported data flavors.
	 * @return A list of DataFlavor objects
	 */
	public Collection getUserTransferDataFlavors()
	{
		return Collections.singletonList(ClientFlavors.MODEL_QUALIFIER);
	}

	/**
	 * Returns the tranfered object in the given flavor.
	 * @return The {@link ModelQualifier} of the transferred item for the {@link ClientFlavors#MODEL_QUALIFIER} or null otherwise
	 */
	public Object getUserTransferData(DataFlavor flavor)
		throws IOException
	{
		if (flavor.equals(ClientFlavors.ITEM))
		{
			return (Item) data;
		}

		if (flavor.equals(ClientFlavors.MODEL_QUALIFIER))
		{
			return ((Item) data).getQualifier();
		}

		return null;
	}

	/**
	 * Gets the transferred item.
	 *
	 * @return The item
	 */
	public Item getItem()
	{
		return (Item) data;
	}
}
