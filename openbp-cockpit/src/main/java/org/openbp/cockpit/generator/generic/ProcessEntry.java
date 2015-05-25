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
package org.openbp.cockpit.generator.generic;

import org.openbp.common.MsgFormat;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.generic.msgcontainer.MsgContainer;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;

/**
 * Entry in a process.
 * Constitutes of a reference to the process initial node and additional display object information
 * (in order to customize display name and description).
 *
 * @author Heiko Erhardt
 */
public class ProcessEntry extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Initial node this entry refers to */
	private InitialNode initialNode;

	/** Name of the initial node this navigation bar entry refers to */
	private String entryName;

	/** Model that will be used to resolve item references (e\.g\. data types) */
	private Model model;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ProcessEntry()
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

		ProcessEntry src = (ProcessEntry) source;

		initialNode = src.initialNode;
		entryName = src.entryName;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the initial node this entry refers to.
	 * @nowarn
	 */
	public InitialNode getInitialNode()
	{
		return initialNode;
	}

	/**
	 * Sets the initial node this entry refers to.
	 * @nowarn
	 */
	public void setInitialNode(InitialNode initialNode)
	{
		this.initialNode = initialNode;
	}

	/**
	 * Gets the name of the initial node this navigation bar entry refers to.
	 * @nowarn
	 */
	public String getEntryName()
	{
		return entryName;
	}

	/**
	 * Sets the name of the initial node this navigation bar entry refers to.
	 * @nowarn
	 */
	public void setEntryName(String entryName)
	{
		this.entryName = entryName;
	}

	/**
	 * Gets the model that will be used to resolve item references (e\.g\. data types).
	 * @nowarn
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * Sets the model that will be used to resolve item references (e\.g\. data types).
	 * @nowarn
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}

	//////////////////////////////////////////////////
	// @@ Serialization support
	//////////////////////////////////////////////////

	/**
	 * This template method is called before the settings object is being serialized.
	 * It can be overridden to implement custom operations.
	 *
	 * @param model Model that will be used to resolve item references (e\.g\. data types)
	 */
	public void beforeSerialization(Model model)
	{
		if (initialNode != null)
		{
			entryName = model.determineItemRef(initialNode.getProcess()) + ModelQualifier.OBJECT_DELIMITER_CHAR + initialNode.getName();
		}
		else
		{
			entryName = null;
		}
	}

	/**
	 * This template method is called after the settings object has been deserialized.
	 * It will add all data members that are not present in the field list to the field list.
	 *
	 * @param msgs Container for error messages
	 * @param model Model that will be used to resolve item references (e\.g\. data types)
	 */
	public void afterDeserialization(MsgContainer msgs, Model model)
	{
		// We silently ignore any error here that might happen if some refactoring was done
		initialNode = null;

		if (entryName == null)
			return;

		int index = entryName.lastIndexOf(ModelQualifier.OBJECT_DELIMITER_CHAR);
		if (index < 0)
			return;

		String processName = entryName.substring(0, index);
		entryName = entryName.substring(index + 1);

		ProcessItem process;
		try
		{
			process = (ProcessItem) model.resolveItemRef(processName, ItemTypes.PROCESS);
		}
		catch (ModelException e)
		{
			String msg = e.getMessage();
			msgs.addMsg(null, "Cannot resolve the process name of an entry. Maybe the process has been deleted.\n" + msg);
			return;
		}

		Node node = process.getNodeByName(entryName);
		if (node instanceof InitialNode)
		{
			initialNode = (InitialNode) node;
		}
		else
		{
			String msg = MsgFormat.format("Cannot resolve the entry $0 in process $1. Maybe the entry has been deleted.", entryName, process.getQualifier());
			msgs.addMsg(null, msg);
		}
	}
}
