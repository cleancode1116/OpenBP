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
package org.openbp.core.model.item.process;

import java.util.Iterator;

import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Param definition.
 * A parameter can be either a node parameter ({@link NodeParam}) or a process variable ({@link ProcessVariable}).
 *
 * @author Heiko Erhardt
 */
public interface Param
	extends ProcessObject
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type name of the parameter.
	 * @nowarn
	 */
	public String getTypeName();

	/**
	 * Sets the type name of the parameter.
	 * @nowarn
	 */
	public void setTypeName(String typeName);

	/**
	 * Gets the data links that are connected to the parameter.
	 * @return An iterator of {@link DataLink} objects
	 */
	public Iterator getDataLinks();

	/**
	 * Adds a data link.
	 * @param dataLink The data link to add
	 */
	public void addDataLink(DataLink dataLink);

	/**
	 * Removes a data link.
	 * @param dataLink The data link to remove
	 */
	public void removeDataLink(DataLink dataLink);

	/**
	 * Gets a data link by its name.
	 *
	 * @param name Name of the dataLink
	 * @return The data link or null if no such data link exists
	 */
	public DataLink getDataLinkByName(String name);

	/**
	 * Clears the data links that are connected to the parameter.
	 */
	public void clearDataLinks();

	/**
	 * Gets the data type associated with the parameter.
	 * @nowarn
	 */
	public DataTypeItem getDataType();

	/**
	 * Sets the data type associated with the parameter.
	 * @nowarn
	 */
	public void setDataType(DataTypeItem dataType);

	/**
	 * Gets the name of the parameter for parameter value context access ("node.socket.param").
	 * @nowarn
	 */
	public String getContextName();

	/**
	 * Gets the process the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public ProcessItem getProcess();
}
