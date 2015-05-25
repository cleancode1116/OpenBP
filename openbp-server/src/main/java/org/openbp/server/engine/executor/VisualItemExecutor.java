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
package org.openbp.server.engine.executor;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.visual.VisualItem;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.engine.EngineExecutor;
import org.openbp.server.engine.ModelObjectExecutorBase;
import org.openbp.server.uiadapter.UIAdapterCache;

/**
 * Executor for visual items.
 *
 * @author Heiko Erhardt
 */
public class VisualItemExecutor extends ModelObjectExecutorBase
{
	/**
	 * Executes a process element.
	 *
	 * @param mo Process element to execute
	 * @param ee Engine executor that called this method
	 * @throws OpenBPException On error
	 */
	public void executeModelObject(ModelObject mo, EngineExecutor ee)
	{
		TokenContext context = ee.getTokenContext();
		NodeSocket entrySocket = context.getCurrentSocket();
		VisualItem visual = (VisualItem) mo;

		NodeSocket nextSocket = null;

		// Check, whether we've a dynamic visual we should execute instead of this visual.
		VisualItem dynamicVisual = determineDynamicVisual(visual, context, entrySocket);

		if (dynamicVisual != this)
		{
			// Delegate execution to the dynamic visual.
			visual = dynamicVisual;
		}

		// Execute the visual implementation
		try
		{
			String visualType = "external";
			String exitSocketName = UIAdapterCache.getInstance().executeVisual(visualType, visual, context, entrySocket);
			if (exitSocketName != null)
			{
				// Return value of activity execution is the name of the exit socket
				// Determine the corresponding exit socket using the usual search strategy
				// Note that visuals for asynchronous protocols like HTTP don't return an exit name.
				nextSocket = getEngine().resolveSocketRef(exitSocketName, entrySocket, context, true);
			}
		}
		catch (OpenBPException e)
		{
			// The exception can be rethrown as is.
			throw e;
		}
		catch (Throwable t)
		{
			String msg = LogUtil.error(getClass(), "Error executing visual $0. [{1}]", visual.getName(), t, context);
			throw new EngineException("VisualExecutionFailed", msg, t);
		}

		context.setCurrentSocket(nextSocket);
	}

	//////////////////////////////////////////////////
	// @@ Helper methods
	//////////////////////////////////////////////////

	/**
	 * This method determines the visual to be executed by looking up a special
	 * parameter named "VisualName".
	 *
	 * @param visual The visual that should be executed (originally)
	 * @param context The TokenContext
	 * @param entrySocket Socket used to enter the visual
	 * @return The dynamic visual to be executed or the original visual
	 */
	protected VisualItem determineDynamicVisual(VisualItem visual, TokenContext context, NodeSocket entrySocket)
	{
		VisualItem result;

		// Lookup the special parameter's value.
		Object dynamicVisualName = TokenContextUtil.getParamValue(context, entrySocket, CoreConstants.DYNAMIC_VISUAL_PARAM_NAME);
		if ((dynamicVisualName != null) && (dynamicVisualName instanceof String))
		{
			// Try to find dynamic visual.
			try
			{
				result = (VisualItem) context.getExecutingModel().resolveItemRef((String) dynamicVisualName,
					ItemTypes.VISUAL);

				LogUtil.debug(getClass(), "Invoking dynamically determined visual $0.", result.getQualifier());
			}
			catch (ModelException re)
			{
				// Just fall back to the original item.
				result = visual;
			}
		}
		else
		{
			result = visual;
		}

		return result;
	}
}
