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
package org.openbp.cockpit.plugins.variables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.ProcessVariableImpl;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.model.ModelConnector;

/**
 * Container object for the list of process variables of a process.
 * We extend DisplayObject in order to have a nice display in the property browser.
 *
 * @author Heiko Erhardt
 */
public class VariablesContainer extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Process variables of the process (contains {@link ProcessVariable} objects) */
	private List processVariableList;

	/** Process that owns the process variables */
	private ProcessItem process;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 */
	public VariablesContainer()
	{
		setName("ProcessVariables");
		setDescription("Process variables");
	}

	/**
	 * Value constructor.
	 *
	 * @param process Process that owns the process variables
	 * @param processVariableList List of process variables or null
	 */
	public VariablesContainer(ProcessItem process, List processVariableList)
	{
		this();

		this.process = process;
		this.processVariableList = processVariableList;
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

		VariablesContainer src = (VariablesContainer) source;

		process = src.process;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			processVariableList = (List) CopyUtil.copyCollection(src.processVariableList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the process variables of the process.
	 * @return An iterator of {@link ProcessVariable} objects
	 */
	public Iterator getProcessVariables()
	{
		if (processVariableList == null)
			return EmptyIterator.getInstance();
		return processVariableList.iterator();
	}

	/**
	 * Creates a process variable and assigns a unique name to it to.
	 * @return The new process variable
	 */
	public ProcessVariable createProcessVariableListElement()
	{
		ProcessVariable param = new ProcessVariableImpl();

		// Create a unique id and attach the global to the process
		String name = NamedObjectCollectionUtil.createUniqueId(processVariableList, "ProcessVariable");
		param.setName(name);
		param.setProcess(process);

		// Assign it the Object type
		try
		{
			DataTypeItem type = (DataTypeItem) ModelConnector.getInstance().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER).getItem("Object", ItemTypes.TYPE, false);
			param.setDataType(type);
			param.setTypeName("Object");
		}
		catch (ModelException e)
		{
			// Never happens
		}

		return param;
	}

	/**
	 * Adds a process variable.
	 * @param processVariable The process variable to add
	 */
	public void addProcessVariable(ProcessVariable processVariable)
	{
		if (processVariableList == null)
			processVariableList = new ArrayList();
		processVariableList.add(processVariable);
		processVariable.setProcess(process);
	}

	/**
	 * Clears the process variables of the process.
	 */
	public void clearProcessVariables()
	{
		processVariableList = null;
	}

	/**
	 * Gets the process variables of the process.
	 * @return A list of {@link ProcessVariable} objects
	 */
	public List getProcessVariableList()
	{
		return processVariableList;
	}

	/**
	 * Sets the process variables of the process.
	 * @param processVariableList A list of {@link ProcessVariable} objects
	 */
	public void setProcessVariableList(List processVariableList)
	{
		this.processVariableList = processVariableList;
	}

	/**
	 * Gets the process that owns the process variables.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}
}
