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
 * Standard implementation of an final node.
 *
 * @author Heiko Erhardt
 */
public class FinalNodeImpl extends SingleSocketNodeImpl
	implements FinalNode
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Public scope */
	public static final String STR_SCOPE_PUBLIC = "public";

	/** Private scope */
	public static final String STR_SCOPE_PRIVATE = "private";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Exit scope */
	private int exitScope = SCOPE_PUBLIC;

	/** Name of the jump target of this final node, if any */
	private String jumpTarget;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public FinalNodeImpl()
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

		FinalNodeImpl src = (FinalNodeImpl) source;

		exitScope = src.exitScope;
		jumpTarget = src.jumpTarget;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return getJumpTarget() != null ? ModelObjectSymbolNames.JUMP_NODE : ModelObjectSymbolNames.FINAL_NODE;
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
			getModelMgr().getMsgContainer().addMsg(this, "Node must have an entry socket.");
			success = false;
		}
		else
		{
			if (!socket.isEntrySocket())
			{
				getModelMgr().getMsgContainer().addMsg(this, "Node socket is not an entry socket.");
				success = false;
			}
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * Gets informational text about the object.
	 * The text can be used to e. g. display a tool tip that describes the object.
	 *
	 * @return An array of strings that make up the information text.<br>
	 * Each array element corresponds to a paragraph that should be displayed.
	 * A paragraph may contain newline ('\n') and tab ('\t') characters that
	 * should be interpreted by the user interface.
	 */
	public String [] getInfoText()
	{
		String headLine = getDisplayText();

		String dt = super.getDisplayText();
		if (dt != null && dt.equals(getName()))
			dt = null;

		return createInfoText(headLine, dt, getDescriptionText());
	}

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @return The display text (should usually not be null)
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text == null && jumpTarget != null)
		{
			text = "> " + jumpTarget;
		}
		else
		{
			text = super.getDisplayText();
		}
		return text;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the final node.
	 * @return {@link FinalNode#SCOPE_PUBLIC}/{@link FinalNode#SCOPE_PRIVATE}
	 */
	public int getExitScope()
	{
		return exitScope;
	}

	/**
	 * Sets the scope of the final node.
	 * @param exitScope {@link FinalNode#SCOPE_PUBLIC}/{@link FinalNode#SCOPE_PRIVATE}
	 */
	public void setExitScope(int exitScope)
	{
		this.exitScope = exitScope;
	}

	/**
	 * Gets the scope of the final node as string value.
	 * @nowarn
	 */
	public String getExitScopeStr()
	{
		String ret = null;
		switch (exitScope)
		{
		case SCOPE_PUBLIC:
			break;

		case SCOPE_PRIVATE:
			ret = STR_SCOPE_PRIVATE;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the scope of the final node as string value.
	 * @nowarn
	 */
	public void setExitScopeStr(String exitScopeStr)
	{
		exitScope = SCOPE_PUBLIC;

		if (exitScopeStr != null)
		{
			if (exitScopeStr.equals(STR_SCOPE_PRIVATE))
				exitScope = SCOPE_PRIVATE;
		}
	}

	/**
	 * Gets the name of the jump target of this final node, if any.
	 * @nowarn
	 */
	public String getJumpTarget()
	{
		return jumpTarget;
	}

	/**
	 * Sets the name of the jump target of this final node, if any.
	 * @nowarn
	 */
	public void setJumpTarget(String jumpTarget)
	{
		this.jumpTarget = jumpTarget;
	}
}
