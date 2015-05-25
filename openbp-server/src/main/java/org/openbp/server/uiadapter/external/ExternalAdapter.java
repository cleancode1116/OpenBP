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
package org.openbp.server.uiadapter.external;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.visual.VisualItem;
import org.openbp.server.context.TokenContext;

/**
 * User interface adapter implementation for external UI renderers.
 *
 * @author Heiko Erhardt
 */
public class ExternalAdapter
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param model The model for which the adapter should be created
	 */
	public ExternalAdapter(Model model)
		throws Exception
	{
	}

	//////////////////////////////////////////////////
	// @@ Implementation
	//////////////////////////////////////////////////

	/**
	 * Executes an external visual.
	 *
	 * @param visual The visual to be executed
	 * @param context The token context
	 * @param entrySocket Entry socket of the visual
	 * @return null
	 */
	public String executeVisual(VisualItem visual, TokenContext context, NodeSocket entrySocket)
		throws Exception
	{
		// TODO Fix 4 Save visual name to session context

		// Log that we're delegating to the driver.
		LogUtil.debug(getClass(), "Setting current visual for external ui adapter to $0.", visual.getQualifier());

		// Returning null will cause the process to stop
		return null;
	}
}
