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
package org.openbp.jaspira.event;

import java.util.ArrayList;
import java.util.List;

import org.openbp.jaspira.plugin.Plugin;

/**
 * A request event is used to broadcast a request to all plugins.
 * Each plugin that wants to respond to the request can add information to a list
 * held by the event. The originator of the event may retrieve the information from
 * the list after the event has been processed.
 *
 * @author Andreas Putz
 */
public class RequestEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** List of results containing arbitrary objects */
	private List resultList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source Source plugin
	 * @param eventName Name of the event
	 * @param object Event object
	 */
	public RequestEvent(Plugin source, String eventName, Object object)
	{
		super(source, eventName, object);
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Adds result information.
	 *
	 * @param object Result object to add
	 */
	public void addResult(Object object)
	{
		if (resultList == null)
			resultList = new ArrayList();

		resultList.add(object);
	}

	/**
	 * Gets the list of result information objects.
	 *
	 * @return The list of result objects or null if no plugin responded to the event
	 */
	public List getResultList()
	{
		return resultList;
	}
}
