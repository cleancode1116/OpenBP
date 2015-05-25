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
package org.openbp.core.model.item.activity;

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.core.model.ModelObject;

/**
 * An activity socket defines an entry or an exit of an activity.
 * The socket may have a number of parameters.
 *
 * @author Heiko Erhardt
 */
public interface ActivitySocket
	extends DisplayObject, ModelObject
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the parameter list.
	 * @return An iterator of {@link ActivityParam} objects
	 */
	public Iterator getParams();

	/**
	 * Gets a parameter by its name.
	 *
	 * @param name Name of the parameter
	 * @return The parameter or null if no such parameter exists
	 */
	public ActivityParam getParamByName(String name);

	/**
	 * Creates a new parameter and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new parameter
	 */
	public ActivityParam createParam(String name);

	/**
	 * Adds a parameter.
	 * @param param The parameter to add
	 */
	public void addParam(ActivityParam param);

	/**
	 * Clears the parameter list.
	 */
	public void clearParams();

	/**
	 * Gets the parameter list.
	 * @return A list of {@link ActivityParam} objects
	 */
	public List getParamList();

	/**
	 * Sets the parameter list.
	 * @param paramList A list of {@link ActivityParam} objects
	 */
	public void setParamList(List paramList);

	/**
	 * Gets the entry/exit socket flag.
	 * @nowarn
	 */
	public boolean isEntrySocket();

	/**
	 * Gets the entry/exit socket flag.
	 * @nowarn
	 */
	public boolean isExitSocket();

	/**
	 * Determines if the entry/exit socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasEntrySocket();

	/**
	 * Sets the entry/exit socket flag.
	 * @nowarn
	 */
	public void setEntrySocket(boolean entrySocket);

	/**
	 * Gets the default socket flag.
	 * @nowarn
	 */
	public boolean isDefaultSocket();

	/**
	 * Determines if the default socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasDefaultSocket();

	/**
	 * Sets the default socket flag.
	 * @nowarn
	 */
	public void setDefaultSocket(boolean defaultSocket);

	/**
	 * Gets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public String getRole();

	/**
	 * Sets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public void setRole(String role);

	/**
	 * Gets the sequence id.
	 * The sequence id is used e. g. to determine the order of the entries in automatically
	 * generated sub navigation bars of visuals.
	 * @nowarn
	 */
	public String getSequenceId();

	/**
	 * Sets the sequence number.
	 * The sequence number is used e. g. to determine the order of the entries in automatically
	 * generated sub navigation bars of visuals.
	 * @nowarn
	 */
	public void setSequenceId(String sequenceId);

	/**
	 * Gets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public void setGeometry(String geometry);

	/**
	 * Gets the activity the socket belongs to (may be null).
	 * @nowarn
	 */
	public ActivityItem getActivity();

	/**
	 * Sets the activity the socket belongs to (may be null).
	 * @nowarn
	 */
	public void setActivity(ActivityItem activity);
}
