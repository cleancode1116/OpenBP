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
package org.openbp.core.model;

import org.openbp.core.model.item.ItemTypes;

/**
 * Class containing constants for symbols that represent model objects.
 *
 * @author Heiko Erhardt
 */
public class ModelObjectSymbolNames
{
	//////////////////////////////////////////////////
	// @@ Item symbols
	//////////////////////////////////////////////////

	/** Model item */
	public static final String MODEL = ItemTypes.MODEL;

	/** Process item */
	public static final String PROCESS_ITEM = ItemTypes.PROCESS;

	/** Activity item */
	public static final String ACTIVITY_ITEM = ItemTypes.ACTIVITY;

	/** Web Service */
	public static final String WEBSERVICE_ITEM = ItemTypes.WEBSERVICE;

	/** Visual item */
	public static final String VISUAL_ITEM = ItemTypes.VISUAL;

	/** Data type */
	public static final String TYPE_ITEM = ItemTypes.TYPE;

	/** Actor */
	public static final String ACTOR_ITEM = ItemTypes.ACTOR;

	//////////////////////////////////////////////////
	// @@ Node symbols
	//////////////////////////////////////////////////

	/** Activity node */
	public static final String ACTIVITY_NODE = ACTIVITY_ITEM;

	/** Actor node */
	public static final String ACTOR_NODE = ACTOR_ITEM;

	/** Decision node */
	public static final String DECISION_NODE = "Decision";

	/** Final node */
	public static final String FINAL_NODE = "Final";

	/** Fork node */
	public static final String FORK_NODE = "Fork";

	/** Initial node */
	public static final String INITIAL_NODE = "Initial";

	/** Join node */
	public static final String JOIN_NODE = "Join";

	/** Jump node */
	public static final String JUMP_NODE = "Jump";

	/** Merge node */
	public static final String MERGE_NODE = "Merge";

	/** Generic node */
	public static final String NODE = "Node";

	/** Placeholder node */
	public static final String PLACEHOLDER_NODE = "Placeholder";

	/** Process node */
	public static final String PROCESS_NODE = PROCESS_ITEM;

	/** Text element node */
	public static final String TEXT_ELEMENT_NODE = "TextElement";

	/** Visual node */
	public static final String VISUAL_NODE = VISUAL_ITEM;

	/** Web node */
	public static final String WEBSERVICE_NODE = WEBSERVICE_ITEM;

	/** Wait state node */
	public static final String WAIT_STATE_NODE = "WaitState";

	/** Workflow node */
	public static final String WORKFLOW_NODE = "Workflow";

	/** Workflow end node */
	public static final String WORKFLOW_END_NODE = "WorkflowEnd";

	//////////////////////////////////////////////////
	// @@ Other symbols
	//////////////////////////////////////////////////

	/** Node socket */
	public static final String NODE_SOCKET = "Socket";

	/** Node socket (entry) */
	public static final String NODE_SOCKET_IN = "SocketIn";

	/** Node socket (exit) */
	public static final String NODE_SOCKET_OUT = "SocketOut";

	/** Node parameter */
	public static final String NODE_PARAM = "Param";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	public ModelObjectSymbolNames()
	{
	}
}
