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
package org.openbp.core;

import org.openbp.core.model.ModelQualifier;

/**
 * General system-wide constants and values.
 * Contains string constants only.
 *
 * @author Heiko Erhardt
 */
public interface CoreConstants
{
	//////////////////////////////////////////////////
	// @@ System properties etc.
	//////////////////////////////////////////////////

	/** OPENBP_HOME environment variable */
	public static final String SYSVAR_OPENBP_HOME = "OPENBP_HOME";

	/** System property: OPENBP_HOME property */
	public static final String SYSPROP_OPENBP_HOME = "openbp.home";

	/** System property: Reload the models on debugger reconnect command */
	public static final String SYSPROP_RELOAD_ON_MODEL_RESET = "openbp.reloadOnModelReset";

	/** RMI registry name */
	public static final String RMI_BINDING_NAME = "OpenBPRemoteConnector";

	//////////////////////////////////////////////////
	// @@ Folders and files
	//////////////////////////////////////////////////

	/** Component type folder */
	public static final String FOLDER_COMPONENTTYPE = "componenttype";

	/** Model sub directory */
	public static final String FOLDER_MODEL = "model";

	/** Model descriptor file */
	public static final String FILE_MODEL_DESCRIPTOR = "model.xml";

	//////////////////////////////////////////////////
	// @@ System model
	//////////////////////////////////////////////////

	/**
	 * Name of the 'System' top level model.
	 */
	public static final String SYSTEM_MODEL_NAME = "System";

	/**
	 * Name of the 'System' top level model.
	 */
	public static final ModelQualifier SYSTEM_MODEL_QUALIFIER = new ModelQualifier(SYSTEM_MODEL_NAME, null, null);

	//////////////////////////////////////////////////
	// @@ Error process
	//////////////////////////////////////////////////

	/**
	 * Name of the standard error socket of a node.
	 */
	public static final String ERROR_SOCKET_NAME = "Error";

	/**
	 * Name of the exception parameter to the error process.
	 */
	public static final String EXCEPTION_PARAM_NAME = "Exception";

	//////////////////////////////////////////////////
	// @@ Default nodes and sockets
	//////////////////////////////////////////////////

	/**
	 * Default name of initial nodes.
	 */
	public static final String DEFAULT_INITIAL_NODE_NAME = "In";

	/**
	 * Default name of initial nodes.
	 */
	public static final String DEFAULT_FINAL_NODE_NAME = "Out";

	/**
	 * Standard 'In' socket.
	 */
	public static final String SOCKET_IN = "In";

	/**
	 * Standard 'Out' socket.
	 */
	public static final String SOCKET_OUT = "Out";

	/**
	 * Standard 'Error' socket.
	 */
	public static final String SOCKET_ERROR = "Error";

	/**
	 * 'Yes' exit socket of a decision node.
	 */
	public static final String SOCKET_YES = "Yes";

	/**
	 * 'No' exit socket of a decision node.
	 */
	public static final String SOCKET_NO = "No";

	/**
	 * 'TaskPublished' exit socket of a workflow node.
	 */
	public static final String SOCKET_TASK_PUBLISHED = "TaskPublished";

	/** Name of the workflow task parameter of the 'TaskPublished' socket a workflow node */
	public static final String WORKFLOWTASK_PARAM_NAME = "WorkflowTask";

	/**
	 * 'TaskAccepted' exit socket of a workflow node.
	 */
	public static final String SOCKET_TASK_ACCEPTED = "TaskAccepted";

	//////////////////////////////////////////////////
	// @@ Flow control
	//////////////////////////////////////////////////

	/**
	 * Identifier for exit target nodes that denote the default entry
	 * of the default process of the executing model as jump target
	 */
	public static final String JUMPTARGET_DEFAULT_PROCESS = "<default process>";

	/**
	 * Resume exit socket of a fork node.
	 */
	public static final String FORK_RESUME = "Resume";

	/**
	 * Collection input parameter of a fork node.
	 */
	public static final String FORK_COLLECTION_PARAM = "Collection";

	/**
	 * Collection element output parameter of a fork node.
	 */
	public static final String FORK_COLLECTION_ELEMENT_PARAM = "CollectionElement";

	/** Name of the 'Incomplete' socket a join node */
	public static final String INCOMPLETE_SOCKET_NAME = "Incomplete";

	//////////////////////////////////////////////////
	// @@ Dynamic node settings
	//////////////////////////////////////////////////

	/**
	 * Name of the parameter of an final node entry socket to be interpreted
	 * as the jump target of the final node.
	 */
	public static final String DYNAMIC_JUMPTARGET_PARAM_NAME = "JumpTarget";

	/**
	 * Name of the parameter of a sub process node entry socket to be interpreted
	 * as sub process to call for dynamic sub processes.
	 */
	public static final String DYNAMIC_SUBPROCESS_PARAM_NAME = "ProcessName";

	/**
	 * Name of the parameter of a visual node entry socket to be interpreted
	 * as visual to be executed for dynamic visuals.
	 */
	public static final String DYNAMIC_VISUAL_PARAM_NAME = "VisualName";

	//////////////////////////////////////////////////
	// @@ Process variables
	//////////////////////////////////////////////////

	/** Indicator character for process variables */
	public static final String PROCESS_VARIABLE_INDICATOR = "_";

	/** Process variable _ThreadName contains the name that shall be assigned to the thread while executing this context */
	public static final String PROCESSVAR_THREAD_NAME = "_ThreadName";

	//////////////////////////////////////////////////
	// @@ Default packages
	//////////////////////////////////////////////////

	/** Activity sub package */
	public static final String PKG_ACTIVITY = "activity";

	/** Data type sub package */
	public static final String PKG_DATA = "data";

	//////////////////////////////////////////////////
	// @@ Resources
	//////////////////////////////////////////////////

	/** 'CoreModule' resource component */
	public static final String RC_CORE = "core";
}
