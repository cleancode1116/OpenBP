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

import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;

/**
 * Standard implementation of a process variable.
 *
 * @author Heiko Erhardt
 */
public class ProcessVariableImpl extends ParamImpl
	implements ProcessVariable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Scope of the process variable */
	private int scope = SCOPE_PROCESS;

	/** Automatical assignment flag */
	private boolean autoAssign;

	/** Persistent variable property */
	private boolean persistentVariable;

	/** Save the value of the variable to the root context always */
	private boolean rootContextVariable;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Process the parameter belongs to (may not be null) */
	private transient ProcessItem process;

	/** Name of the parameter for parameter value context access ("node.socket.param") */
	private transient String contextName;

	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Scope: Process-local */
	public static final String STR_SCOPE_PROCESS = "subprocess";

	/** Scope: Context */
	public static final String STR_SCOPE_CONTEXT = "context";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ProcessVariableImpl()
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

		ProcessVariableImpl src = (ProcessVariableImpl) source;

		scope = src.scope;
		autoAssign = src.autoAssign;
		persistentVariable = src.persistentVariable;
		rootContextVariable = src.rootContextVariable;

		process = src.process;
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
	 * Gets text that can be used to display this object.
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String ret = super.getDisplayText();
		String statusDisplay = buildStatusDisplay();
		if (statusDisplay != null)
		{
			ret = ret + statusDisplay;
		}
		return ret;
	}

	private String buildStatusDisplay()
	{
		if (autoAssign || persistentVariable || rootContextVariable)
		{
			StringBuffer sb = new StringBuffer();
			sb.append(" (");
			if (autoAssign)
			{
				if (sb.length() > 2)
					sb.append(",");
				sb.append("auto");
			}
			if (persistentVariable)
			{
				if (sb.length() > 2)
					sb.append(",");
				sb.append("pers");
			}
			if (rootContextVariable)
			{
				if (sb.length() > 2)
					sb.append(",");
				sb.append("root");
			}
			sb.append(")");
			return sb.toString();
		}

		return null;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getProcess(), CoreConstants.PROCESS_VARIABLE_INDICATOR + getName());
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
		return CoreConstants.PROCESS_VARIABLE_INDICATOR + getName();
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
		return process.getProcessVariables();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the process variable.
	 * @return {@link ProcessVariable#SCOPE_PROCESS}/{@link ProcessVariable#SCOPE_CONTEXT}
	 */
	public int getScope()
	{
		return scope;
	}

	/**
	 * Sets the scope of the process variable.
	 * @param scope {@link ProcessVariable#SCOPE_PROCESS}/{@link ProcessVariable#SCOPE_CONTEXT}
	 */
	public void setScope(int scope)
	{
		this.scope = scope;
	}

	/**
	 * Gets the scope of the process variable as string value.
	 * @nowarn
	 */
	public String getScopeStr()
	{
		String ret = null;
		switch (scope)
		{
		case SCOPE_PROCESS:
			ret = STR_SCOPE_PROCESS;
			break;

		case SCOPE_CONTEXT:
			ret = STR_SCOPE_CONTEXT;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the scope of the process variable as string value.
	 * @nowarn
	 */
	public void setScopeStr(String scopeStr)
	{
		scope = SCOPE_PROCESS;

		if (scopeStr != null)
		{
			if (scopeStr.equals(STR_SCOPE_CONTEXT))
				scope = SCOPE_CONTEXT;
			else if (scopeStr.equals(STR_SCOPE_PROCESS))
				scope = SCOPE_PROCESS;
		}
	}

	/**
	 * Checks the automatical assignment flag.
	 * @nowarn
	 */
	public boolean hasAutoAssign()
	{
		return autoAssign;
	}

	/**
	 * Gets the automatical assignment flag.
	 * @nowarn
	 */
	public boolean isAutoAssign()
	{
		return autoAssign;
	}

	/**
	 * Sets the automatical assignment flag.
	 * @nowarn
	 */
	public void setAutoAssign(boolean autoAssign)
	{
		this.autoAssign = autoAssign;
	}

	/**
	 * Checks the root context flag.
	 * @nowarn
	 */
	public boolean hasRootContextVariable()
	{
		return rootContextVariable;
	}

	/**
	 * Gets the root context flag.
	 * @nowarn
	 */
	public boolean isRootContextVariable()
	{
		return rootContextVariable;
	}

	/**
	 * Sets the root context flag.
	 * @nowarn
	 */
	public void setRootContextVariable(boolean rootContextVariable)
	{
		this.rootContextVariable = rootContextVariable;
	}

	/**
	 * Checks the persistent variable property.
	 * @nowarn
	 */
	public boolean hasPersistentVariable()
	{
		return persistentVariable;
	}

	/**
	 * Gets the persistent variable property.
	 * @nowarn
	 */
	public boolean isPersistentVariable()
	{
		return persistentVariable;
	}

	/**
	 * Sets the persistent variable property.
	 * @nowarn
	 */
	public void setPersistentVariable(boolean persistentVariable)
	{
		this.persistentVariable = persistentVariable;
	}

	/**
	 * Gets the name of the parameter for parameter value context access ("_param").
	 * @nowarn
	 */
	public String getContextName()
	{
		if (contextName == null)
		{
			contextName = CoreConstants.PROCESS_VARIABLE_INDICATOR + getName();
		}

		return contextName;
	}
}
