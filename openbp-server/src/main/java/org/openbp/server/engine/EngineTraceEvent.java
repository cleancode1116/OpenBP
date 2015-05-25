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
package org.openbp.server.engine;

import org.openbp.common.ExceptionUtil;
import org.openbp.core.engine.debugger.DebuggerEvent;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.server.context.TokenContext;

/**
 * The engine trace manager may control the execution of a process.
 * Engine trace (xc) clients like ddebuggers and profilers may register
 * with this manager in order to be notified about the execution of processes.
 *
 * @author Heiko Erhardt
 */
public class EngineTraceEvent extends EngineEvent
{
	//////////////////////////////////////////////////
	// @@ Event type constants
	//////////////////////////////////////////////////

	// NOTE: constants are mapped to JaspiraEvents -> need to be LOWERCASE

	/** Event type: Node Entry */
	public static final String NODE_ENTRY = "nodeentry";

	/** Event type: Node Exit */
	public static final String NODE_EXIT = "nodeexit";

	/** Event type: Data Flow */
	public static final String CONTROL_FLOW = "controlflow";

	/** Event type: Data Flow */
	public static final String DATA_FLOW = "dataflow";

	/** Event type: Process exception */
	public static final String PROCESS_EXCEPTION = "processexception";

	/** Table of all possible values */
	public static final String[] SUPPORTED_EVENT_TYPES =
	{
		NODE_ENTRY, NODE_EXIT, CONTROL_FLOW, DATA_FLOW, PROCESS_EXCEPTION,
	};

	/**
	 * Returns a list of supported event types.
	 * @nowarn
	 */
	public static String[] getSupportedEventTypes()
	{
		return SUPPORTED_EVENT_TYPES;
	}

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Executing process */
	private ProcessItem process;

	/** Exception that has occurred (for {@link EngineTraceEvent#PROCESS_EXCEPTION} event only) */
	private Throwable exception;

	/** Current node */
	private Node node;

	/** Current node */
	private NodeSocket nodeSocket;

	/** Name of the executed data link (for {@link EngineTraceEvent#CONTROL_FLOW} events only) */
	private ControlLink controlLink;

	/** Name of the executed control link (for {@link EngineTraceEvent#DATA_FLOW} events only) */
	private DataLink dataLink;

	/** Source node socket (for {@link EngineTraceEvent#CONTROL_FLOW} and {@link EngineTraceEvent#DATA_FLOW} events only) */
	private NodeSocket sourceSocket;

	/** Target node socket (for {@link EngineTraceEvent#CONTROL_FLOW} and {@link EngineTraceEvent#DATA_FLOW} events only) */
	private NodeSocket targetSocket;

	/** Source parameter (for {@link EngineTraceEvent#DATA_FLOW} events only) */
	private Param sourceParam;

	/** Source parameter member path (for {@link EngineTraceEvent#DATA_FLOW} events only) */
	private String sourceMemberPath;

	/** Target parameter (for {@link EngineTraceEvent#DATA_FLOW} events only) */
	private Param targetParam;

	/** Target parameter member path (for {@link EngineTraceEvent#DATA_FLOW} events only) */
	private String targetMemberPath;

	/** Param value (for {@link EngineTraceEvent#DATA_FLOW} event only) */
	private Object paramValue;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, Engine engine)
	{
		super(eventType, context, engine);
	}

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param process Executing process
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, ProcessItem process, Engine engine)
	{
		this(eventType, context, engine);
		this.process = process;
	}

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param process Executing process
	 * @param nodeSocket Current node
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, ProcessItem process, NodeSocket nodeSocket, Engine engine)
	{
		this(eventType, context, engine);
		this.process = process;
		this.nodeSocket = nodeSocket;
		updateInfo();
	}

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param exception Exception that has occurred (for {@link EngineTraceEvent#PROCESS_EXCEPTION} event only)
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, Throwable exception, Engine engine)
	{
		this(eventType, context, engine);
		this.exception = exception;
		updateInfo();
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param context Token context
	 * @param nodeSocket Current node
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, NodeSocket nodeSocket, Engine engine)
	{
		this(eventType, context, engine);
		this.nodeSocket = nodeSocket;
		updateInfo();
	}

	/**
	 * Value constructor.
	 *
	 * @param context Token context
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param node Current node
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, Node node, Engine engine)
	{
		this(eventType, context, engine);
		this.node = node;
		updateInfo();
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param context Token context
	 * @param controlLink Name of the executed data link (for {@link EngineTraceEvent#CONTROL_FLOW} events only)
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, ControlLink controlLink, Engine engine)
	{
		this(eventType, context, engine);
		this.controlLink = controlLink;
		updateInfo();
	}

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type (see {@link EngineTraceEvent} class)
	 * @param context Token context
	 * @param dataLink Name of the executed control link (for {@link EngineTraceEvent#DATA_FLOW} events only)
	 * @param paramValue Param value (for {@link EngineTraceEvent#DATA_FLOW} events only)
	 * @param engine Engine
	 */
	public EngineTraceEvent(String eventType, TokenContext context, DataLink dataLink, Object paramValue, Engine engine)
	{
		this(eventType, context, engine);
		this.paramValue = paramValue;
		this.dataLink = dataLink;
		updateInfo();
	}

	/**
	 * Updates the event information based on the values provided by the constructor.
	 */
	private void updateInfo()
	{
		if (nodeSocket == null)
			nodeSocket = getContext().getCurrentSocket();
		if (nodeSocket != null && node == null)
			node = nodeSocket.getNode();
		if (node != null && process == null)
			process = node.getProcess();

		if (controlLink != null)
		{
			if (sourceSocket == null)
				sourceSocket = controlLink.getSourceSocket();
			if (targetSocket == null)
				targetSocket = controlLink.getTargetSocket();
		}

		if (dataLink != null)
		{
			if (sourceParam == null)
				sourceParam = dataLink.getSourceParam();
			if (targetParam == null)
				targetParam = dataLink.getTargetParam();
			if (sourceMemberPath == null)
				sourceMemberPath = dataLink.getSourceMemberPath();
			if (targetMemberPath == null)
				targetMemberPath = dataLink.getTargetMemberPath();
		}

		if (sourceParam != null && sourceSocket == null && (sourceParam instanceof NodeParam))
			sourceSocket = ((NodeParam) sourceParam).getSocket();
		if (targetParam != null && targetSocket == null && (targetParam instanceof NodeParam))
			targetSocket = ((NodeParam) targetParam).getSocket();
		if (sourceSocket != null && process == null)
			process = sourceSocket.getNode().getProcess();
		if (targetSocket != null && process == null)
			process = targetSocket.getNode().getProcess();
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

		sb.append("Trace: Event = " + eventType + "\n");

		if (getNodeSocket() != null)
		{
			sb.append("\tSocket = " + getNodeSocket().getQualifier() + "\n");
		}
		else if (getNode() != null)
		{
			sb.append("\tNode = " + getNode().getQualifier() + "\n");
		}
		else if (getProcess() != null)
		{
			sb.append("\tProcess = " + getProcess().getQualifier() + "\n");
		}

		if (getException() != null)
		{
			sb.append("\tException = " + getException() + "\n");
		}

		if (getControlLink() != null)
		{
			sb.append("\tControl link = " + getControlLink().getQualifier());
		}
		if (getDataLink() != null)
		{
			sb.append("\tData link = " + getDataLink().getQualifier());
		}

		if (getSourceParam() != null)
		{
			sb.append("\tSource parameter = " + getSourceParam().getQualifier());
			if (getSourceMemberPath() != null)
				sb.append(ModelQualifier.OBJECT_DELIMITER + getSourceMemberPath());
		}
		if (getTargetParam() != null)
		{
			sb.append("\tTarget parameter = " + getTargetParam().getQualifier());
			if (getTargetMemberPath() != null)
				sb.append(ModelQualifier.OBJECT_DELIMITER + getTargetMemberPath());
		}
		if (getParamValue() != null)
		{
			sb.append("\tParameter value = " + getParamValue() + "\n");
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the executing process.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Gets the exception that has occurred (for {@link EngineTraceEvent#PROCESS_EXCEPTION} event only).
	 * @nowarn
	 */
	public Throwable getException()
	{
		return exception;
	}

	/**
	 * Gets the current node.
	 * @nowarn
	 */
	public Node getNode()
	{
		return node;
	}

	/**
	 * Gets the current node.
	 * @nowarn
	 */
	public NodeSocket getNodeSocket()
	{
		return nodeSocket;
	}

	/**
	 * Gets the name of the executed data link (for {@link EngineTraceEvent#CONTROL_FLOW} events only).
	 * @nowarn
	 */
	public ControlLink getControlLink()
	{
		return controlLink;
	}

	/**
	 * Sets the name of the executed data link (for {@link EngineTraceEvent#CONTROL_FLOW} events only).
	 * @nowarn
	 */
	public void setControlLink(ControlLink controlLink)
	{
		this.controlLink = controlLink;
	}

	/**
	 * Gets the name of the executed control link (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public DataLink getDataLink()
	{
		return dataLink;
	}

	/**
	 * Sets the name of the executed control link (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public void setDataLink(DataLink dataLink)
	{
		this.dataLink = dataLink;
	}

	/**
	 * Gets the source node socket (for {@link EngineTraceEvent#CONTROL_FLOW} and {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public NodeSocket getSourceSocket()
	{
		return sourceSocket;
	}

	/**
	 * Gets the source parameter member path (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public String getSourceMemberPath()
	{
		return sourceMemberPath;
	}

	/**
	 * Gets the target node socket (for {@link EngineTraceEvent#CONTROL_FLOW} and {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public NodeSocket getTargetSocket()
	{
		return targetSocket;
	}

	/**
	 * Gets the target parameter member path (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public String getTargetMemberPath()
	{
		return targetMemberPath;
	}

	/**
	 * Gets the source node socket parameter (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public Param getSourceParam()
	{
		return sourceParam;
	}

	/**
	 * Gets the target node socket parameter (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public Param getTargetParam()
	{
		return targetParam;
	}

	/**
	 * Gets the parameter value (for {@link EngineTraceEvent#DATA_FLOW} events only).
	 * @nowarn
	 */
	public Object getParamValue()
	{
		return paramValue;
	}

	//////////////////////////////////////////////////
	// @@ Debugger event conversion
	//////////////////////////////////////////////////

	/**
	 * Debugger event factory method.
	 *
	 * @param haltedPosition Current position of halted process
	 * @return The new event object
	 */
	public DebuggerEvent createDebuggerEvent(ModelQualifier haltedPosition)
	{
		DebuggerEvent de = new DebuggerEvent();

		de.setEventType(eventType);

		de.setException(exception);
		if (exception != null)
		{
			de.setExceptionString(ExceptionUtil.getNestedTrace(exception));
		}

		if (controlLink != null)
			de.setControlLinkQualifier(controlLink.getQualifier());
		if (dataLink != null)
			de.setDataLinkQualifier(dataLink.getQualifier());

		if (sourceSocket != null)
			de.setSourceSocketQualifier(sourceSocket.getQualifier());
		if (targetSocket != null)
			de.setTargetSocketQualifier(targetSocket.getQualifier());

		if (sourceParam != null)
			de.setSourceParamName(sourceParam.getQualifier());
		de.setSourceMemberPath(sourceMemberPath);
		if (targetParam != null)
			de.setTargetParamName(targetParam.getQualifier());
		de.setTargetMemberPath(targetMemberPath);

		if (paramValue != null)
		{
			de.setParamValueString(paramValue.toString());
		}

		de.setHaltedPosition(haltedPosition);

		return de;
	}
}
