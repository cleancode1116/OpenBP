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
import java.util.List;

import org.openbp.core.model.item.activity.ActivitySocket;

/**
 * A node socket constitutes an entry or an exit of a node.
 * The node parameter contains the same data as an activity parameter, but may have a control link connected to it.
 * The socket may have a number of parameters.<br>
 * An exit socket can be designated as default socket. If the node is not implemented yet,
 * the engine will use this socket as output path of the node.
 *
 * @author Heiko Erhardt
 */
public interface NodeSocket
	extends ProcessObject
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Copy between node socket and activity socket.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activitySocket Activity to copy from
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyFromActivitySocket(ActivitySocket activitySocket, int syncFlags);

	/**
	 * Copy between node socket and activity socket.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activitySocket Activity socket to copy to
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyToActivitySocket(ActivitySocket activitySocket, int syncFlags);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the parameter list.
	 * @return An iterator of {@link NodeParam} objects
	 */
	public Iterator getParams();

	/**
	 * Creates a new parameter and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new parameter
	 */
	public NodeParam createParam(String name);

	/**
	 * Adds a parameter.
	 * @param param The parameter to add
	 */
	public void addParam(NodeParam param);

	/**
	 * Adds a parameter at a given position.
	 * @param param The parameter to add
	 * @param index The position to add the parameter (-1 to append)
	 */
	public void addParam(NodeParam param, int index);

	/**
	 * Removes a parameter.
	 * @param param The parameter to remove
	 */
	public void removeParam(NodeParam param);

	/**
	 * Gets a parameter by its name.
	 *
	 * @param name Name of the parameter
	 * @return The parameter or null if no such parameter exists
	 */
	public NodeParam getParamByName(String name);

	/**
	 * Clears the parameter list.
	 */
	public void clearParams();

	/**
	 * Gets the parameter list.
	 * @return A list of {@link NodeParam} objects
	 */
	public List getParamList();

	/**
	 * Gets the entry socket flag.
	 * @nowarn
	 */
	public boolean isEntrySocket();

	/**
	 * Gets the exit socket flag.
	 * @nowarn
	 */
	public boolean isExitSocket();

	/**
	 * Determines if the entry socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasEntrySocket();

	/**
	 * Sets the entry socket flag.
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
	 * Sets the sequence id.
	 * The sequence id is used e. g. to determine the order of the entries in automatically
	 * generated sub navigation bars of visuals.
	 * @nowarn
	 */
	public void setSequenceId(String sequenceId);

	/**
	 * Gets the node the socket belongs to.
	 * @nowarn
	 */
	public Node getNode();

	/**
	 * Sets the node the socket belongs to.
	 * @nowarn
	 */
	public void setNode(Node node);

	/**
	 * Gets the control links that are connected to the socket.
	 * @return An iterator of {@link ControlLink} objects
	 */
	public Iterator getControlLinks();

	/**
	 * Checks if there are any control links attached to the socket.
	 * @nowarn
	 */
	public boolean hasControlLinks();

	/**
	 * Adds a controlLink.
	 * @param controlLink The controlLink to add
	 */
	public void addControlLink(ControlLink controlLink);

	/**
	 * Removes a controlLink.
	 * @param controlLink The controlLink to remove
	 */
	public void removeControlLink(ControlLink controlLink);

	/**
	 * Gets a controlLink by its name.
	 *
	 * @param name Name of the controlLink
	 * @return The controlLink or null if no such controlLink exists
	 */
	public ControlLink getControlLinkByName(String name);

	/**
	 * Clears the control links that are connected to the socket.
	 */
	public void clearControlLinks();

	/**
	 * Gets the control links that are connected to the socket.
	 * @return A list of {@link ControlLink} objects
	 */
	public List getControlLinkList();

	/**
	 * Sets the control links that are connected to the socket.
	 * @param controlLinkList A list of {@link ControlLink} objects
	 */
	public void setControlLinkList(List controlLinkList);

	/**
	 * Gets the name of the socket for parameter value context access ("node.socket").
	 * @nowarn
	 */
	public String getContextName();

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);
}
