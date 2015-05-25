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
package org.openbp.model.system.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openbp.core.engine.EngineException;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Check for empty collection.
 * Implementation of the IsCollectionEmpty activity handler.
 * If the collection is empty, the 'Yes' exit will be chosen, otherwise the 'No' exit.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Collection': Collection
 * 
 * Output sockets/parameter:
 *   Socket 'Yes'
 *   Socket 'No'
 */
public class IsCollectionEmptyHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter Collection */
	private static final String PARAM_COLLECTION = "Collection";

	// {{*Custom constants*
	// }}*Custom constants*

	// {{*Custom members*
	// Note: If you define member variables, consider the fact that the same handler instance may be executed
	// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.
	// In general, member variables should be defined for global-like data only.
	// }}*Custom members*

	/**
	 * Executes the handler.
	 *
	 * @param hc Handler context that contains execution parameters
	 * @return true if the handler handled the event, false to apply the default handling to the event
	 * @throws Exception Any exception that may occur during execution of the handler will be
	 * propagated to an exception handler if defined or abort the process execution otherwise.
	 */
	public boolean execute(HandlerContext hc)
		throws Exception
	{
		// {{*Handler implementation*
		Object collection = hc.getParam(PARAM_COLLECTION);

		boolean empty = true;
		if (collection != null)
		{
			if (collection instanceof Iterator)
			{
				empty = !((Iterator) collection).hasNext();
			}
			else if (collection instanceof Collection)
			{
				empty = ((Collection) collection).isEmpty();
			}
			else if (collection.getClass().isArray())
			{
				empty = ((Object []) collection).length == 0;
			}
			else if (collection instanceof Map)
			{
				empty = ((Map) collection).isEmpty();
			}
			else
			{
				throw new EngineException("IteratableUnsupported", "IsCollectionEmptyActivity: Param of type '" + collection.getClass() + "' is not a supported iteratable object");
			}
		}

		if (empty)
			hc.chooseExitSocket(SOCKET_YES);
		else
			hc.chooseExitSocket(SOCKET_NO);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

