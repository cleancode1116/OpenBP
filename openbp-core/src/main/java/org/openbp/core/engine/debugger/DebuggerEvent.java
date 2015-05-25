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
 * A debugger event contains the position and status settings of a halted process.
 *
 * @author Heiko Erhardt
 */
public class DebuggerEvent
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Event type */
	protected String eventType;

	/** Current position of halted process */
	private ModelQualifier haltedPosition;

	/** Exception that has occurred */
	private transient Throwable exception;

	/** String representation of the exception that has occurred */
	private String exceptionString;

	/** Executed data link */
	private ModelQualifier controlLinkQualifier;

	/** Executed control link */
	private ModelQualifier dataLinkQualifier;

	/** Source node socket */
	private ModelQualifier sourceSocketQualifier;

	/** Target node socket */
	private ModelQualifier targetSocketQualifier;

	/** Source parameter */
	private ModelQualifier sourceParamName;

	/** Source parameter member path */
	private String sourceMemberPath;

	/** Target parameter */
	private ModelQualifier targetParamName;

	/** Target parameter member path */
	private String targetMemberPath;

	/** Param value */
	private transient Object paramValue;

	/** String representation of the parameter value that has occurred */
	private String paramValueString;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DebuggerEvent()
	{
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return The multi-line string includes the event type and all information that is
	 * present in respect to this type.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		String eventType = getEventType();

		sb.append("Debugger: Event = " + eventType + "\r\n");
		sb.append("\tPosition = " + haltedPosition);

		if (getException() != null)
		{
			sb.append("\tException = " + getException() + "\r\n");
		}

		if (getControlLinkQualifier() != null)
		{
			sb.append("\tControl link = " + getControlLinkQualifier());
		}
		if (getDataLinkQualifier() != null)
		{
			sb.append("\tData link = " + getDataLinkQualifier());
		}

		if (getSourceParamName() != null)
		{
			sb.append("\tSource = " + getSourceParamName());
			if (getSourceMemberPath() != null)
				sb.append(ModelQualifier.OBJECT_DELIMITER + getSourceMemberPath());
		}
		else if (getSourceSocketQualifier() != null)
		{
			sb.append("\tSource = " + getSourceSocketQualifier());
		}

		if (getTargetParamName() != null)
		{
			sb.append("\tTarget = " + getTargetParamName());
			if (getTargetMemberPath() != null)
				sb.append(ModelQualifier.OBJECT_DELIMITER + getTargetMemberPath());
		}
		else if (getTargetSocketQualifier() != null)
		{
			sb.append("\tTarget = " + getTargetSocketQualifier());
		}

		if (getParamValue() != null)
		{
			sb.append("\tValue = " + getParamValue() + "\r\n");
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the event type 
	 * @nowarn
	 */
	public String getEventType()
	{
		return eventType;
	}

	/**
	 * Sets the event type 
	 * @nowarn
	 */
	public void setEventType(String eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * Gets the current position of halted process.
	 * @nowarn
	 */
	public ModelQualifier getHaltedPosition()
	{
		return haltedPosition;
	}

	/**
	 * Sets the current position of halted process.
	 * @nowarn
	 */
	public void setHaltedPosition(ModelQualifier haltedPosition)
	{
		this.haltedPosition = haltedPosition;
	}

	/**
	 * Gets the exception that has occurred 
	 * @nowarn
	 */
	public Throwable getException()
	{
		return exception;
	}

	/**
	 * Sets the exception that has occurred 
	 * @nowarn
	 */
	public void setException(Throwable exception)
	{
		this.exception = exception;
	}

	/**
	 * Gets the string representation of the exception that has occurred 
	 * @nowarn
	 */
	public String getExceptionString()
	{
		return exceptionString;
	}

	/**
	 * Sets the string representation of the exception that has occurred 
	 * @nowarn
	 */
	public void setExceptionString(String exceptionString)
	{
		this.exceptionString = exceptionString;
	}

	/**
	 * Gets the executed data link 
	 * @nowarn
	 */
	public ModelQualifier getControlLinkQualifier()
	{
		return controlLinkQualifier;
	}

	/**
	 * Sets the executed data link 
	 * @nowarn
	 */
	public void setControlLinkQualifier(ModelQualifier controlLinkQualifier)
	{
		this.controlLinkQualifier = controlLinkQualifier;
	}

	/**
	 * Gets the executed control link 
	 * @nowarn
	 */
	public ModelQualifier getDataLinkQualifier()
	{
		return dataLinkQualifier;
	}

	/**
	 * Sets the executed control link 
	 * @nowarn
	 */
	public void setDataLinkQualifier(ModelQualifier dataLinkQualifier)
	{
		this.dataLinkQualifier = dataLinkQualifier;
	}

	/**
	 * Gets the source node socket 
	 * @nowarn
	 */
	public ModelQualifier getSourceSocketQualifier()
	{
		return sourceSocketQualifier;
	}

	/**
	 * Sets the source node socket 
	 * @nowarn
	 */
	public void setSourceSocketQualifier(ModelQualifier sourceSocketQualifier)
	{
		this.sourceSocketQualifier = sourceSocketQualifier;
	}

	/**
	 * Gets the target node socket 
	 * @nowarn
	 */
	public ModelQualifier getTargetSocketQualifier()
	{
		return targetSocketQualifier;
	}

	/**
	 * Sets the target node socket 
	 * @nowarn
	 */
	public void setTargetSocketQualifier(ModelQualifier targetSocketQualifier)
	{
		this.targetSocketQualifier = targetSocketQualifier;
	}

	/**
	 * Gets the source parameter 
	 * @nowarn
	 */
	public ModelQualifier getSourceParamName()
	{
		return sourceParamName;
	}

	/**
	 * Sets the source parameter 
	 * @nowarn
	 */
	public void setSourceParamName(ModelQualifier sourceParamName)
	{
		this.sourceParamName = sourceParamName;
	}

	/**
	 * Gets the source parameter member path 
	 * @nowarn
	 */
	public String getSourceMemberPath()
	{
		return sourceMemberPath;
	}

	/**
	 * Sets the source parameter member path 
	 * @nowarn
	 */
	public void setSourceMemberPath(String sourceMemberPath)
	{
		this.sourceMemberPath = sourceMemberPath;
	}

	/**
	 * Gets the target parameter 
	 * @nowarn
	 */
	public ModelQualifier getTargetParamName()
	{
		return targetParamName;
	}

	/**
	 * Sets the target parameter 
	 * @nowarn
	 */
	public void setTargetParamName(ModelQualifier targetParamName)
	{
		this.targetParamName = targetParamName;
	}

	/**
	 * Gets the target parameter member path 
	 * @nowarn
	 */
	public String getTargetMemberPath()
	{
		return targetMemberPath;
	}

	/**
	 * Sets the target parameter member path 
	 * @nowarn
	 */
	public void setTargetMemberPath(String targetMemberPath)
	{
		this.targetMemberPath = targetMemberPath;
	}

	/**
	 * Gets the param value 
	 * @nowarn
	 */
	public Object getParamValue()
	{
		return paramValue;
	}

	/**
	 * Sets the param value 
	 * @nowarn
	 */
	public void setParamValue(Object paramValue)
	{
		this.paramValue = paramValue;
	}

	/**
	 * Gets the string representation of the parameter value that has occurred 
	 * @nowarn
	 */
	public String getParamValueString()
	{
		return paramValueString;
	}

	/**
	 * Sets the string representation of the parameter value that has occurred 
	 * @nowarn
	 */
	public void setParamValueString(String paramValueString)
	{
		this.paramValueString = paramValueString;
	}
}
