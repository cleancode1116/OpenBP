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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Container class that stores information about a drop opportunity into a particular
 * region of a drop client.
 *
 * @author Jens Ferchland
 */
public class Importer
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Name to identify this drop region to the drop client */
	protected Object regionId;

	/** Drop client that owns the region */
	private InteractionClient client;

	/** Data flavors supported by this importer */
	private DataFlavor [] flavors;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param regionId Name to identify this drop region to the drop client
	 * @param client Drop client that owns the region
	 * @param flavors Data flavors supported by this importer
	 */
	public Importer(Object regionId, InteractionClient client, DataFlavor [] flavors)
	{
		this.regionId = regionId;
		this.client = client;
		this.flavors = flavors;
	}

	/**
	 * Gets the data flavors supported by this importer.
	 * @nowarn
	 */
	public DataFlavor [] getFlavors()
	{
		return flavors;
	}

	//////////////////////////////////////////////////
	// @@ member access
	//////////////////////////////////////////////////

	/**
	 * Imports the given transferable into the drop client at the given location.
	 *
	 * @param data Transferable to import
	 * @param p Import position in glass coordinates
	 * @return
	 *		true	The data was successfully imported.<br>
	 *		false	An error occured while importing the data.
	 */
	public boolean importData(Transferable data, Point p)
	{
		return client.importData(regionId, data, p);
	}
}
