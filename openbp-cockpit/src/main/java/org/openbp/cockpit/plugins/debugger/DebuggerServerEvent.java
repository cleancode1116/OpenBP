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
package org.openbp.cockpit.plugins.debugger;

import org.openbp.core.engine.debugger.DebuggerEvent;
import org.openbp.core.model.ModelQualifier;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Client event that wraps a debugger server event.
 * The event information is contained in the server event class
 * {@link org.openbp.core.engine.debugger.DebuggerEvent}<br>
 * The event name is the same as defined in the
 * the EngineTraceEvent class.
 *
 * @author Stephan Moritz
 */
public class DebuggerServerEvent extends JaspiraEvent
{
	/** Basename for events */
	public static final String BASE = "debugger.server.";

	/** Event */
	private DebuggerEvent debuggerEvent;

	/**
	 * Constructor.
	 *
	 * @param source Source plugin
	 * @param debuggerEvent Debugger event from the server that initiated this event
	 */
	public DebuggerServerEvent(Plugin source, DebuggerEvent debuggerEvent)
	{
		super(source, BASE + debuggerEvent.getEventType());

		this.debuggerEvent = debuggerEvent;
	}

	/**
	 * Gets the underlying debugger event.
	 * @nowarn
	 */
	public DebuggerEvent getDebuggerEvent()
	{
		return debuggerEvent;
	}

	/**
	 * Gets the event type.
	 * @return The event type class)
	 */
	public String getEventType()
	{
		return debuggerEvent.getEventType();
	}

	/**
	 * Gets the current position of halted process.
	 * @nowarn
	 */
	public ModelQualifier getHaltedPosition()
	{
		return debuggerEvent.getHaltedPosition();
	}

	/**
	 * Gets the exception that has occurred.
	 * @nowarn
	 */
	public Throwable getException()
	{
		return debuggerEvent.getException();
	}

	/**
	 * Gets the executed data link.
	 * @nowarn
	 */
	public ModelQualifier getControlLinkQualifier()
	{
		return debuggerEvent.getControlLinkQualifier();
	}

	/**
	 * Gets the executed control link.
	 * @nowarn
	 */
	public ModelQualifier getDataLinkQualifier()
	{
		return debuggerEvent.getDataLinkQualifier();
	}

	/**
	 * Gets the source node socket.
	 * @nowarn
	 */
	public ModelQualifier getSourceSocketQualifier()
	{
		return debuggerEvent.getSourceSocketQualifier();
	}

	/**
	 * Gets the target node socket.
	 * @nowarn
	 */
	public ModelQualifier getTargetSocketQualifier()
	{
		return debuggerEvent.getTargetSocketQualifier();
	}

	/**
	 * Gets the source parameter.
	 * @nowarn
	 */
	public ModelQualifier getSourceParamName()
	{
		return debuggerEvent.getSourceParamName();
	}

	/**
	 * Gets the source parameter member path.
	 * @nowarn
	 */
	public String getSourceMemberPath()
	{
		return debuggerEvent.getSourceMemberPath();
	}

	/**
	 * Gets the target parameter.
	 * @nowarn
	 */
	public ModelQualifier getTargetParamName()
	{
		return debuggerEvent.getTargetParamName();
	}

	/**
	 * Gets the target parameter member path.
	 * @nowarn
	 */
	public String getTargetMemberPath()
	{
		return debuggerEvent.getTargetMemberPath();
	}

	/**
	 * Gets the param value.
	 * @nowarn
	 */
	public Object getParamValue()
	{
		return debuggerEvent.getParamValue();
	}

	/**
	 * Gets the string representation of the exception that has occurred.
	 * This method is used if the thorwable is not able to be transfered with
	 * RMI.
	 *
	 * @nowarn
	 */
	public String getExceptionString()
	{
		return debuggerEvent.getExceptionString();
	}

	/**
	 * Gets the param value as string.
	 * @nowarn
	 */
	public String getParamValueString()
	{
		return debuggerEvent.getParamValueString();
	}
}
