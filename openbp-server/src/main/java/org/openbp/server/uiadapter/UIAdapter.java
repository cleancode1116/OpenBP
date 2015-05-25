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
package org.openbp.server.uiadapter;

import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.visual.VisualItem;
import org.openbp.server.context.TokenContext;

/**
 * This interface should be implemented by adapter classes that are intended to
 * wrap user interface adapters like JSP or Velocity.
 * In addition to the interface, the contract for user interfaces adapters includes
 * a public constructor that takes a single {@link Model} argument.
 *
 * All user interface adapters are kept in the user interface adapter registry
 * ({@link UIAdapterCache}).
 *
 * @author Heiko Erhardt
 */
public interface UIAdapter
{
	/**
	 * Executes a visual.
	 *
	 * @param visual The visual item that invoked the user interface adapter
	 * @param context Token context of the process
	 * @param entrySocket Entry socket of the visual
	 * @return The name of the visual item exit socket to continue processing with.<br>
	 * Most user interface adapters will return null here because visual processing is usually
	 * an asynchronous request/response process. However, for templates that support
	 * direct user interaction (i. e. a Swing adapter), the adapter might choose the exit
	 * right away.
	 * @throws Exception If the visual could not be executed
	 */
	public String executeVisual(VisualItem visual, TokenContext context, NodeSocket entrySocket)
		throws Exception;
}
