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

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectImpl;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;

/**
 * An activity socket defines an entry or an exit of an activity.
 * The socket may have a number of parameters.<br>
 * An exit socket can be designated as default socket. If the activity is not implemented yet,
 * the engine will use this socket as output path of the activity.
 *
 * @author Heiko Erhardt
 */
public class ActivitySocketImpl extends ModelObjectImpl
	implements ActivitySocket
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Param list (contains {@link ActivityParam} objects) */
	private List paramList;

	/** Entry/exit socket flag */
	private boolean entrySocket;

	/** Default socket flag */
	private boolean defaultSocket;

	/** Role or list of roles (comma-separated) that have the permission for this socket */
	private String role;

	/** Sequence id */
	private String sequenceId;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	//////////////////////////////////////////////////
	// @@ Data member
	//////////////////////////////////////////////////

	/** Activity the socket belongs to (may be null) */
	private transient ActivityItem activity;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ActivitySocketImpl()
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

		ActivitySocketImpl src = (ActivitySocketImpl) source;

		entrySocket = src.entrySocket;
		defaultSocket = src.defaultSocket;
		role = src.role;
		sequenceId = src.sequenceId;
		geometry = src.geometry;

		activity = src.activity;

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

	//////////////////////////////////////////////////
	// @@ ModelObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getOwningModel()
	{
		return activity != null ? activity.getOwningModel() : null;
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
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.NODE_SOCKET;
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
		return activity;
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
		return activity.getSockets();
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getActivity(), getName());
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
				ActivityParam param = (ActivityParam) paramList.get(i);

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
	 * @return An iterator of {@link ActivityParam} objects
	 */
	public Iterator getParams()
	{
		if (paramList == null)
			return EmptyIterator.getInstance();
		return paramList.iterator();
	}

	/**
	 * Gets a parameter by its name.
	 *
	 * @param name Name of the parameter
	 * @return The parameter or null if no such parameter exists
	 */
	public ActivityParam getParamByName(String name)
	{
		return paramList != null ? (ActivityParam) NamedObjectCollectionUtil.getByName(paramList, name) : null;
	}

	/**
	 * Creates a new parameter and assigns a new name to it.
	 * @param name Name of the new parameter or null for the default ("Param")
	 * @return The new parameter
	 */
	public ActivityParam createParam(String name)
	{
		ActivityParam param = new ActivityParamImpl();

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
	public void addParam(ActivityParam param)
	{
		if (paramList == null)
			paramList = new SortingArrayList();
		paramList.add(param);

		param.setSocket(this);
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
	 * @return A list of {@link ActivityParam} objects
	 */
	public List getParamList()
	{
		return paramList;
	}

	/**
	 * Sets the parameter list.
	 * @param paramList A list of {@link ActivityParam} objects
	 */
	public void setParamList(List paramList)
	{
		this.paramList = paramList;

		if (paramList != null)
		{
			int n = paramList.size();
			for (int i = 0; i < n; ++i)
			{
				ActivityParam param = (ActivityParam) paramList.get(i);
				param.setSocket(this);
			}
		}
	}

	/**
	 * Gets the entry/exit socket flag.
	 * @nowarn
	 */
	public boolean isEntrySocket()
	{
		return entrySocket;
	}

	/**
	 * Gets the entry/exit socket flag.
	 * @nowarn
	 */
	public boolean isExitSocket()
	{
		return !entrySocket;
	}

	/**
	 * Determines if the entry/exit socket flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasEntrySocket()
	{
		return entrySocket;
	}

	/**
	 * Sets the entry/exit socket flag.
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
	 * Gets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information (required by the Modeler).
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}

	/**
	 * Gets the activity the socket belongs to (may be null).
	 * @nowarn
	 */
	public ActivityItem getActivity()
	{
		return activity;
	}

	/**
	 * Sets the activity the socket belongs to (may be null).
	 * @nowarn
	 */
	public void setActivity(ActivityItem activity)
	{
		this.activity = activity;
	}
}
