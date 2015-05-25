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

import org.openbp.common.generic.Copyable;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ContextNameUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;

/**
 * Standard implementation of a process node.
 *
 * @author Heiko Erhardt
 */
public abstract class NodeImpl extends ProcessObjectImpl
	implements Node
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type of queue for the current node */
	private String queueType;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	/** Event handler definition */
	private HandlerDefinition eventHandlerDefinition;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Process the node belongs to (may not be null) */
	private transient ProcessItem process;

	/** Name of the parameter for parameter value context access ("node.socket.param") */
	private transient String contextName;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public NodeImpl()
	{
		setEventHandlerDefinition(new HandlerDefinition());
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

		NodeImpl src = (NodeImpl) source;

		queueType = src.queueType;
		geometry = src.geometry;
		process = src.process;

		setEventHandlerDefinition(new HandlerDefinition());
		eventHandlerDefinition.copyFrom(src.eventHandlerDefinition, Copyable.COPY_DEEP);
		eventHandlerDefinition.setOwner(this);
	}

	/**
	 * Returns the node representation of this node, which is a direct clone of itself.
	 *
	 * @copy NodeProvider.toNode
	 */
	public Node toNode(ProcessItem process, int syncFlags)
	{
		try
		{
			return (Node) clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	/**
	 * Determines if this node supports multiple control links on a single exit socket.
	 *
	 * @return
	 *		true	More than one control link can be attached to the exit sockets of this node.
	 *		false	An exit socket of this node supports one control link at a time only.
	 */
	public boolean isMultiExitLinkNode()
	{
		if (getProcess() != null && ProcessTypes.BUSINESSPROCESS.equals(getProcess().getProcessType()))
			return true;
		return false;
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
		return new ModelQualifier(getProcess(), getName());
	}

	/**
	 * Gets the name of the node for parameter value context access ("node").
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

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Sets the process the object belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process)
	{
		this.process = process;

		contextName = null;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return getName();
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
		return process;
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
		return process.getNodes();
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

		if ((flag & INSTANTIATE_ITEM) != 0)
		{
			eventHandlerDefinition.instantiate();
		}

		for (Iterator it = getSockets(); it.hasNext();)
		{
			NodeSocket socket = (NodeSocket) it.next();

			// Establish back reference
			socket.setNode(this);

			socket.maintainReferences(flag);
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = eventHandlerDefinition.addHandlerAssociations(null, "Node event handler class", Association.NORMAL);
		associations = AssociationUtil.addAssociations(associations, - 1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the node sockets.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public abstract Iterator getSockets();

	/**
	 * Gets the node sockets in the order determined by the {@link NodeSocket#setSequenceId(String)} property.
	 * @return An iterator of {@link NodeSocket} objects
	 */
	public abstract Iterator getOrderedSockets();

	/**
	 * Gets the number of node sockets.
	 * @return The number of node sockets
	 */
	public abstract int getNumberOfSockets();

	/**
	 * Gets a socket by its name.
	 *
	 * @param name Name of the socket
	 * @return The socket or null if no such socket exists
	 */
	public abstract NodeSocket getSocketByName(String name);

	/**
	 * Gets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public String getQueueType()
	{
		return queueType;
	}

	/**
	 * Sets the type of queue for the current node.
	 * The queue type may be used to process different node or activity types by different engine instances (e. g. different servers).
	 * @nowarn
	 */
	public void setQueueType(String queueType)
	{
		this.queueType = queueType;
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

	/**
	 * Checks if the handler definition is not empty.
	 * Used by Castor to decide wether an entry is necessary in the XML file.
	 * @nowarn
	 */
	public boolean hasEventHandlerDefinition()
	{
		return eventHandlerDefinition.isDefined();
	}

	/**
	 * Gets the event handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getEventHandlerDefinition()
	{
		return eventHandlerDefinition;
	}

	/**
	 * Sets the event handler definition.
	 * @nowarn
	 */
	public void setEventHandlerDefinition(HandlerDefinition eventHandlerDefinition)
	{
		this.eventHandlerDefinition = eventHandlerDefinition;
		if (eventHandlerDefinition != null)
		{
			eventHandlerDefinition.setOwner(this);
		}
	}
}
