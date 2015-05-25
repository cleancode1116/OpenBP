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
package org.openbp.model.system.flow;

import java.util.Iterator;

import org.openbp.common.CollectionUtil;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Loop.
 * Implementation of the Loop activity handler.
 * Implements a program loop. This activity takes a set
 * of objects containing arbitrary data as input parameter of
 * the 'in' socket. The activity will iterate each
 * element of the set and provide it as output parameter of the
 * 'loop' socket. The actions connected to the
 * 'loop' socket form the body of the loop, i. e.
 * perform the loop operation on each element. In order to
 * access the next element, control needs to be transferred to
 * the 'continue' socket after execution of the loop
 * body. After all elements have been processed, the activity
 * will end with the 'out' socket.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Collection': Object set to operate on
 *   Socket 'Continue'
 * 
 * Output sockets/parameter:
 *   Socket 'Loop'
 *     Parameter 'Element': Element to process
 *   Socket 'Out'
 */
public class LoopHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Output socket Loop */
	private static final String SOCKET_LOOP = "Loop";

	/** Parameter Collection */
	private static final String PARAM_COLLECTION = "Collection";

	/** Parameter Element */
	private static final String PARAM_ELEMENT = "Element";

	// {{*Custom constants*

	/** Delimiter for parameter names (separates node, socket and parameter name, i\. e\. "node.socket.param") */
	public static final String PARAM_DELIMITER = ModelQualifier.OBJECT_DELIMITER;

	/** Temporary context variable to store the iterator */
	public static final String STORE_ITERATOR = "Element";

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
		String nodePath = hc.getCurrentSocket().getNode().getQualifier().toString();

		Iterator it;
		if (hc.getCurrentSocket().getName().equals(SOCKET_IN))
		{
			// Start of loop, get the iterator as parameter of the current node
			Object o = hc.getParam(PARAM_COLLECTION);

			if (o == null)
			{
				hc.chooseExitSocket(SOCKET_OUT);
				return true;
			}

			it = CollectionUtil.iterator(o);

			// Save the iterator in the context for later access
			hc.getTokenContext().setParamValue(nodePath + PARAM_DELIMITER + STORE_ITERATOR, it);
		}
		else
		{
			// Get the iterator we saved at the start of the node
			it = (Iterator) hc.getTokenContext().getParamValue(nodePath + PARAM_DELIMITER + STORE_ITERATOR);

			if (it == null)
			{
				throw new EngineException("UninitializedLoop", "LoopActivity: Socket '" + SOCKET_IN + "' socket must be executed before socket '" + SOCKET_LOOP + "'");
			}
		}

		if (it.hasNext())
		{
			// There are still more elements, provide it to the 'continue' socket
			Object element = it.next();
			hc.chooseExitSocket(SOCKET_LOOP);
			hc.setResult(PARAM_ELEMENT, element);
		}
		else
		{
			// End of loop, remove the iterator from the context
			hc.getTokenContext().removeParamValue(nodePath + PARAM_DELIMITER + STORE_ITERATOR);
			hc.chooseExitSocket(SOCKET_OUT);
		}

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

