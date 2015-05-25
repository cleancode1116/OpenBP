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
package org.openbp.core.model.item;

/**
 * Class containing constants for standard item types.
 * If you add an item type, be sure to add it also to the item type registry of the server.
 * @seey Item.itemType
 *
 * @author Heiko Erhardt
 */
public final class ItemTypes
{
	//////////////////////////////////////////////////
	// @@ Type constants
	//////////////////////////////////////////////////

	/** Model item type */
	public static final String MODEL = "Model";

	/** Process item type */
	public static final String PROCESS = "Process";

	/** Activity item type */
	public static final String ACTIVITY = "Activity";

	/** Web Service activity type */
	public static final String WEBSERVICE = "Webservice";

	/** Visual item type */
	public static final String VISUAL = "Visual";

	/** Data type type */
	public static final String TYPE = "Type";

	/** Actor type */
	public static final String ACTOR = "Actor";

	/** Pseudo item type for placeholder dummy items */
	public static final String PSEUDO_PLACEHOLDER = "Placeholder";

	//////////////////////////////////////////////////
	// @@ Type descriptors
	//////////////////////////////////////////////////

	/** Table of all possible values */
	private static String [] allValues = { MODEL, PROCESS, ACTIVITY, WEBSERVICE, VISUAL, TYPE, ACTOR, };

	/**
	 * Returns an array of possible values of this class.
	 * @nowarn
	 */
	public static String [] getValues()
	{
		return allValues;
	}

	/**
	 * Private constructor prevents instantiation.
	 */
	private ItemTypes()
	{
	}
}
