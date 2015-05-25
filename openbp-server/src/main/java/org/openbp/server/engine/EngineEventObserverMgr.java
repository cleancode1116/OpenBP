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

import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;

/**
 * The engine event observer manager keeps track of clients that are interested in particular engine events.
 * This may include process debuggers and tracers, but also server-side extensions that automatically undeploy processes after execution etc.
 *
 * The OpenBP Cockpit Debugger may have problems with multiple process threads running in parallel.
 * This event observer manager is synchronized, ensuring that only one event at a time is being processed.
 * <b>Note that due to the synchronization, this class may create a bottleneck and should be used
 * as event observer manager for non-production environment that require debugging only.
 * Otherwise, the regular {@link EventObserverMgr} should be used.</b>
 *
 * @author Heiko Erhardt
 */
public class EngineEventObserverMgr extends EventObserverMgr
{
	/**
	 * Constructor.
	 */
	public EngineEventObserverMgr()
	{
	}

	/**
	 * Notifies all registered xc clients about an xc event.
	 *
	 * @param event Event descriptor describing the type of event
	 * @throws EngineTraceException To abort the running process
	 */
	public void fireEvent(EngineTraceEvent event)
		throws EngineTraceException
	{
		String eventType = event.getEventType();
		if (hasActiveObservers(eventType))
		{
			// Note that this block is synchronized, so only one event may be processed by a client at a time
			synchronized(this)
			{
				TokenContext context = ((EngineEvent) event).getContext();
				TokenContextUtil.checkTerminationRequest(context);

				super.fireEvent(event);

				TokenContextUtil.checkTerminationRequest(context);
			}
		}
	}
}
