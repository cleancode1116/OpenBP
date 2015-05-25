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
package org.openbp.core.engine.debugger;

import java.io.Serializable;

import org.openbp.core.model.ModelQualifier;

/**
 * A breakpoint of a process object.
 * Breakpoints can be set at nodes, a node sockets or parameters.
 *
 * @author Heiko Erhardt
 */
public class Breakpoint
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Breakoint state: Disabled */
	public static final int STATE_DISABLED = (1 << 0);

	/** Breakoint state: Global */
	public static final int STATE_GLOBAL = (1 << 1);

	/** Breakoint state: Temporary breakpoint (will be cleared when reached) */
	public static final int STATE_TEMPORARY = (1 << 2);

	//////////////////////////////////////////////////
	// @@ Properties members
	//////////////////////////////////////////////////

	/** Qualified name of the object that owns the breakpoint */
	private ModelQualifier qualifier;

	/** Breakpoint state */
	private int state;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Breakpoint()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param qualifier Qualified name of the object that owns the breakpoint
	 * @param state Any combination of {@link #STATE_DISABLED} | {@link #STATE_GLOBAL} or 0
	 */
	public Breakpoint(ModelQualifier qualifier, int state)
	{
		this.qualifier = qualifier;
		this.state = state;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the qualified name of the object that owns the breakpoint.
	 * This can be a node, a node socket or a parameter
	 * @nowarn
	 */
	public ModelQualifier getQualifier()
	{
		return qualifier;
	}

	/**
	 * Sets the qualified name of the object that owns the breakpoint.
	 * This can be a node, a node socket or a parameter
	 * @nowarn
	 */
	public void setQualifier(ModelQualifier qualifier)
	{
		this.qualifier = qualifier;
	}

	/**
	 * Gets the enabled static.
	 * @nowarn
	 */
	public boolean isEnabled()
	{
		return (state & STATE_DISABLED) == 0;
	}

	/**
	 * Gets the breakpoint state.
	 * @return Any combination of {@link #STATE_DISABLED} | {@link #STATE_GLOBAL} | {@link #STATE_TEMPORARY} or 0
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * Sets the breakpoint state.
	 * @param state Any combination of {@link #STATE_DISABLED} | {@link #STATE_GLOBAL} | {@link #STATE_TEMPORARY} or 0
	 */
	public void setState(int state)
	{
		this.state = state;
	}

	/**
	 * Updates the breakpoint state.
	 * @param state Any combination of {@link #STATE_DISABLED} | {@link #STATE_GLOBAL} | {@link #STATE_TEMPORARY} or 0
	 * @param set
	 *		true	Sets the specified state flags.<br>
	 *		false	Clears the specified state flags.
	 */
	public void updateState(int state, boolean set)
	{
		if (set)
		{
			this.state |= state;
		}
		else
		{
			this.state &= ~state;
		}
	}
}
