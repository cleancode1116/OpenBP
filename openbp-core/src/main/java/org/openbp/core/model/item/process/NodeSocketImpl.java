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

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.ContextNameUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.activity.ActivityParam;
import org.openbp.core.model.item.activity.ActivityParamImpl;
import org.openbp.core.model.item.activity.ActivitySocket;

/**
 * Standard implementation of a node socket.
 *
 * @author Heiko Erhardt
 */
public class NodeSocketImpl extends ProcessObjectImpl
	implements NodeSocket
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Entry socket */
	public static final String STR_ENTRY = "entry";

	/** Exit socket */
	public static final String STR_EXIT = "exit";

	/** Combined entry and exit socket */
	public static final String STR_COMBINED = "combined";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Param list (contains {@link NodeParam} objects, may not be null) */
	private List paramList;

	/** Entry/exit socket flag */
	private boolean entrySocket;

	/** Default socket flag */
	private boolean defaultSocket;

	/** Role or list of roles (comma-separated) that have the permission for this socket */
	private String role;

	/** Sequence number */
	private String sequenceId;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Name of the socket for parameter value context access ("node.socket") */
	private transient String contextName;

	/** Node the socket belongs to (may be null) */
	private transient Node node;

	/** Control links that are connected to the socket (contains {@link ControlLink} objects) */
	private transient List controlLinkList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public NodeSocketImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		NodeSocketImpl src = (NodeSocketImpl) source;

		entrySocket = src.entrySocket;
		defaultSocket = src.defaultSocket;
		role = src.role;
		sequenceId = src.sequenceId;
		geometry = src.geometry;

		if (copyMode == Copyable.COPY_DEEP)
		{
			// We assume we're doing a clone here.
			// References will be rebuilt after the clone,
			// so clear the control link references
			controlLinkList = null;
		}
		else
		{
			controlLinkList = src.controlLinkList;
		}

		node = src.node;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			paramList = (List) CopyUtil.copyCollection(src.paramList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			paramList = src.paramList;
		}
	}

	/**
	 * Copy between node socket and activity socket.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activitySocket Activity to copy from
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyFromActivitySocket(ActivitySocket activitySocket, int syncFlags)
	{
		// Copy DisplayObject properties
		setName(activitySocket.getName());

		entrySocket = activitySocket.isEntrySocket();
		defaultSocket = activitySocket.isDefaultSocket();
		role = activitySocket.getRole();
		sequenceId = activitySocket.getSequenceId();
		geometry = activitySocket.getGeometry();

		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(this, activitySocket, syncFlags);

		// Copy the parameters
		clearParams();
		for (Iterator it = activitySocket.getParams(); it.hasNext();)
		{
			ActivityParam activityParam = (ActivityParam) it.next();

			NodeParam nodeParam = new NodeParamImpl();
			nodeParam.copyFromActivityParam(activityParam);
			addParam(nodeParam);
		}
	}

	/**
	 * Copy between node socket and activity socket.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activitySocket Activity socket to copy to
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public void copyToActivitySocket(ActivitySocket activitySocket, int syncFlags)
	{
		// Copy DisplayObject properties
		activitySocket.setName(getName());

		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(activitySocket, this, syncFlags);

		activitySocket.setEntrySocket(entrySocket);
		activitySocket.setDefaultSocket(defaultSocket);
		activitySocket.setRole(role);
		activitySocket.setSequenceId(sequenceId);
		activitySocket.setGeometry(geometry);

		// Copy the parameters
		activitySocket.clearParams();
		for (Iterator it = getParams(); it.hasNext();)
		{
			NodeParam nodeParam = (NodeParam) it.next();

			ActivityParam activityParam = new ActivityParamImpl();
			nodeParam.copyToActivityParam(activityParam);
			activitySocket.addParam(activityParam);
		}
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return isEntrySocket() ? ModelObjectSymbolNames.NODE_SOCKET_IN : ModelObjectSymbolNames.NODE_SOCKET_OUT;
	}

	//////////////////////////////////////////////////
	// @@ DisplayObject overrides
	//////////////////////////////////////////////////

	/**
	 * Updates the context name in addition to the name change.
	 * @nowarn
	 */
	public void setName(String name)
	{
		super.setName(name);

		contextName = null;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getNode().getProcess(), getNode().getName() + ModelQualifier.OBJECT_DELIMITER + getName());
	}

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return node != null ? node.getProcess() : null;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return node != null ? node.getName() + ModelQualifier.OBJECT_DELIMITER + getName() : getName();
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the parameters of the socket.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		return paramList;
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return node;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return node.getSockets();
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (paramList != null)
		{
			int n = paramList.size();
			for (int i = 0; i < n; ++i)
			{
				NodeParam param = (NodeParam) paramList.get(i);

				// Establish back reference
				param.setSocket(this);

				param.maintainReferences(flag);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the parameter list.
	 * @return An iterator of {@link NodeParam} objects
	 */
	public Iterator getParams()
	{
		if (paramList == null)
			return EmptyIterator.getInstance();
		return paramList.iterator();
	}

	/**
	 * Creates a new parameter and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new parameter
	 */
	public NodeParam createParam(String name)
	{
		NodeParam param = new NodeParamImpl();

		if (name == null)
			name = "Param";
		name = NamedObjectCollectionUtil.createUniqueId(paramList, name);
		param.setName(name);

		return param;
	}

	/**
	 * Adds a parameter.
	 * @param param The parameter to add
	 */
	public void addParam(NodeParam param)
	{
		if (paramList == null)
			paramList = new SortingArrayList();
		paramList.add(param);

		param.setSocket(this);
	}

	/**
	 * Adds a parameter at a given position.
	 * @param param The parameter to add
	 * @param index The position to add the parameter (-1 to append)
	 */
	public void addParam(NodeParam param, int index)
	{
		if (index == - 1)
		{
			addParam(param);
			return;
		}

		if (paramList == null)
			paramList = new SortingArrayList();
		paramList.add(index, param);

		param.setSocket(this);
	}

	/**
	 * Removes a parameter.
	 * @param param The parameter to remove
	 */
	public void removeParam(NodeParam param)
	{
		CollectionUtil.removeReference(paramList, param);
	}

	/**
	 * Gets a parameter by its name.
	 *
	 * @param name Name of the parameter
	 * @return The parameter or null if no such parameter exists
	 */
	public NodeParam getParamByName(String name)
	{
		return paramList != null ? (NodeParam) NamedObjectCollectionUtil.getByName(paramList, name) : null;
	}

	/**
	 * Clears the parameter list.
	 */
	public void clearParams()
	{
		paramList = null;
	}

	/**
	 * Gets the parameter list.
	 * @return A list of {@link NodeParam} objects
	 */
	public List getParamList()
	{
		return paramList;
	}

	/**
	 * Sets the parameter list.
	 * @param paramList A list of {@link NodeParam} objects
	 */
	public void setParamList(List paramList)
	{
		this.paramList = paramList;

		if (paramList != null)
		{
			int n = paramList.size();
			for (int i = 0; i < n; ++i)
			{
				NodeParam param = (NodeParam) paramList.get(i);
				param.setSocket(this);
			}
		}
	}

	/**
	 * Gets the entry socket flag.
	 * @nowarn
	 */
	public boolean isEntrySocket()
	{
		return entrySocket;
	}

	/**
	 * Gets the exit socket flag.
	 * @nowarn
	 */
	public boolean isExitSocket()
	{
		return ! entrySocket;
	}

	/**
	 * Determines if the entry socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasEntrySocket()
	{
		return entrySocket;
	}

	/**
	 * Sets the entry socket flag.
	 * @nowarn
	 */
	public void setEntrySocket(boolean entrySocket)
	{
		this.entrySocket = entrySocket;
	}

	/**
	 * Gets the default socket flag.
	 * @nowarn
	 */
	public boolean isDefaultSocket()
	{
		return defaultSocket;
	}

	/**
	 * Determines if the default socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasDefaultSocket()
	{
		return defaultSocket;
	}

	/**
	 * Sets the default socket flag.
	 * @nowarn
	 */
	public void setDefaultSocket(boolean defaultSocket)
	{
		this.defaultSocket = defaultSocket;
	}

	/**
	 * Gets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public String getRole()
	{
		return role;
	}

	/**
	 * Sets the role or list of roles (comma-separated) that have the permission for this socket.
	 * @nowarn
	 */
	public void setRole(String role)
	{
		this.role = role;
	}

	/**
	 * Gets the sequence id.
	 * The sequence id is used e. g. to determine the order of the entries in automatically
	 * generated sub navigation bars of visuals.
	 * @nowarn
	 */
	public String getSequenceId()
	{
		return sequenceId;
	}

	/**
	 * Sets the sequence id.
	 * The sequence id is used e. g. to determine the order of the entries in automatically
	 * generated sub navigation bars of visuals.
	 * @nowarn
	 */
	public void setSequenceId(String sequenceId)
	{
		this.sequenceId = sequenceId;
	}

	/**
	 * Gets the node the socket belongs to.
	 * @nowarn
	 */
	public Node getNode()
	{
		return node;
	}

	/**
	 * Sets the node the socket belongs to.
	 * @nowarn
	 */
	public void setNode(Node node)
	{
		this.node = node;

		contextName = null;
	}

	/**
	 * Gets the control links that are connected to the socket.
	 * @return An iterator of {@link ControlLink} objects
	 */
	public Iterator getControlLinks()
	{
		if (controlLinkList == null)
			return EmptyIterator.getInstance();
		return controlLinkList.iterator();
	}

	/**
	 * Checks if there are any control links attached to the socket.
	 * @nowarn
	 */
	public boolean hasControlLinks()
	{
		return controlLinkList != null && controlLinkList.size() > 0;
	}

	/**
	 * Adds a controlLink.
	 * @param controlLink The controlLink to add
	 */
	public void addControlLink(ControlLink controlLink)
	{
		if (controlLinkList == null)
		{
			controlLinkList = new SortingArrayList();
			controlLinkList.add(controlLink);
		}
		else
		{
			if (! controlLinkList.contains(controlLink))
			{
				controlLinkList.add(controlLink);
			}
		}

		controlLink.setProcess(getProcess());
	}

	/**
	 * Removes a controlLink.
	 * @param controlLink The controlLink to remove
	 */
	public void removeControlLink(ControlLink controlLink)
	{
		CollectionUtil.removeReference(controlLinkList, controlLink);
	}

	/**
	 * Gets a controlLink by its name.
	 *
	 * @param name Name of the controlLink
	 * @return The controlLink or null if no such controlLink exists
	 */
	public ControlLink getControlLinkByName(String name)
	{
		return controlLinkList != null ? (ControlLink) NamedObjectCollectionUtil.getByName(controlLinkList, name) : null;
	}

	/**
	 * Clears the control links that are connected to the socket.
	 */
	public void clearControlLinks()
	{
		controlLinkList = null;
	}

	/**
	 * Gets the control links that are connected to the socket.
	 * @return A list of {@link ControlLink} objects
	 */
	public List getControlLinkList()
	{
		return controlLinkList;
	}

	/**
	 * Sets the control links that are connected to the socket.
	 * @param controlLinkList A list of {@link ControlLink} objects
	 */
	public void setControlLinkList(List controlLinkList)
	{
		this.controlLinkList = controlLinkList;
	}

	/**
	 * Gets the name of the socket for parameter value context access ("node.socket").
	 * @nowarn
	 */
	public String getContextName()
	{
		if (contextName == null)
		{
			contextName = ContextNameUtil.constructContextName(this);
		}

		return contextName;
	}

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}
}
