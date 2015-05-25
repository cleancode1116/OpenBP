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

import org.openbp.core.model.ModelObjectSymbolNames;

/**
 * Standard implementation of an initial node.
 *
 * @author Heiko Erhardt
 */
public class InitialNodeImpl extends SingleSocketNodeImpl
	implements InitialNode
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Public scope */
	public static final String STR_SCOPE_PUBLIC = "public";

	/** Protected scope */
	public static final String STR_SCOPE_PROTECTED = "protected";

	/** Private scope */
	public static final String STR_SCOPE_PRIVATE = "private";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Entry scope */
	private int entryScope = SCOPE_PUBLIC;

	/** Default entry */
	private boolean defaultEntry;

	/** Role or list of roles (comma-separated) that have the permission for this socket */
	private String role;

	/** Public web service entry */
	private boolean wsPublicEntry;

	/** Corresponding web service exit name */
	private String wsCorrespondingExitName;

	/** Corresponding web service exit */
	private transient FinalNode wsCorrespondingExit;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public InitialNodeImpl()
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

		InitialNodeImpl src = (InitialNodeImpl) source;

		entryScope = src.entryScope;
		defaultEntry = src.defaultEntry;
		role = src.role;
		wsPublicEntry = src.wsPublicEntry;
		wsCorrespondingExit = src.wsCorrespondingExit;
		wsCorrespondingExitName = src.wsCorrespondingExitName;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.INITIAL_NODE;
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

		if ((flag & RESOLVE_LOCAL_REFS) != 0)
		{
			// Determine corresponding final node from name.
			if (wsCorrespondingExitName != null)
			{
				Node node = getProcess().getNodeByName(wsCorrespondingExitName);
				if (node instanceof FinalNode)
				{
					wsCorrespondingExit = (FinalNode) node;
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (socket == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "Node must have an exit socket.");
			success = false;
		}
		else
		{
			if (!socket.isExitSocket())
			{
				getModelMgr().getMsgContainer().addMsg(this, "Node socket is not an exit socket.");
				success = false;
			}
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the initial node.
	 * @return {@link InitialNode#SCOPE_PUBLIC}/{@link InitialNode#SCOPE_PROTECTED}/{@link InitialNode#SCOPE_PRIVATE}
	 */
	public int getEntryScope()
	{
		return entryScope;
	}

	/**
	 * Sets the scope of the initial node.
	 * @param entryScope {@link InitialNode#SCOPE_PUBLIC}/{@link InitialNode#SCOPE_PROTECTED}/{@link InitialNode#SCOPE_PRIVATE}
	 */
	public void setEntryScope(int entryScope)
	{
		this.entryScope = entryScope;
	}

	/**
	 * Gets the scope of the initial node as string value.
	 * @nowarn
	 */
	public String getEntryScopeStr()
	{
		String ret = null;
		switch (entryScope)
		{
		case SCOPE_PUBLIC:
			break;

		case SCOPE_PROTECTED:
			ret = STR_SCOPE_PROTECTED;
			break;

		case SCOPE_PRIVATE:
			ret = STR_SCOPE_PRIVATE;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the scope of the initial node as string value.
	 * @nowarn
	 */
	public void setEntryScopeStr(String entryScopeStr)
	{
		entryScope = SCOPE_PUBLIC;

		if (entryScopeStr != null)
		{
			if (entryScopeStr.equals(STR_SCOPE_PROTECTED))
				entryScope = SCOPE_PROTECTED;
			else if (entryScopeStr.equals(STR_SCOPE_PRIVATE))
				entryScope = SCOPE_PRIVATE;
		}
	}

	/**
	 * Determines if the default entry flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasDefaultEntry()
	{
		return defaultEntry;
	}

	/**
	 * Gets the default entry flag.
	 * @nowarn
	 */
	public boolean isDefaultEntry()
	{
		return defaultEntry;
	}

	/**
	 * Sets the default entry flag.
	 * @nowarn
	 */
	public void setDefaultEntry(boolean defaultEntry)
	{
		this.defaultEntry = defaultEntry;
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
	 * Determines if the web service public entry flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasWSPublicEntry()
	{
		return wsPublicEntry;
	}

	/**
	 * Get the web service public entry flag.
	 * @nowarn
	 */
	public boolean isWSPublicEntry()
	{
		return wsPublicEntry;
	}

	/**
	 * Sets the web service public entry flag.
	 * @nowarn
	 */
	public void setWSPublicEntry(boolean publicEntry)
	{
		wsPublicEntry = publicEntry;
	}

	/**
	 * Gets the corresponding web service exit.
	 * @nowarn
	 */
	public FinalNode getWSCorrespondingFinalNode()
	{
		return wsCorrespondingExit;
	}

	/**
	 * Sets the corresponding web service exit.
	 * @nowarn
	 */
	public void setWSCorrespondingFinalNode(FinalNode exit)
	{
		wsCorrespondingExit = exit;
	}

	/**
	 * Gets the corresponding web service final node name.
	 * @nowarn
	 */
	public String getWSCorrespondingFinalNodeName()
	{
		return wsCorrespondingExitName;
	}

	/**
	 * Sets the corresponding web service final node name.
	 * @nowarn
	 */
	public void setWSCorrespondingFinalNodeName(String exitName)
	{
		wsCorrespondingExitName = exitName;
	}
}
