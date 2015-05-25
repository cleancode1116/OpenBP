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

import org.openbp.server.context.TokenContext;

/**
 * An engine context denotes stateful engine objects.
 * These objects hold a reference to the engine as well as to a token context.
 * This usually means that their methods are not thread-safe.
 *
 * @author Heiko Erhardt
 */
public interface EngineContext
{
	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public Engine getEngine();

	/**
	 * Gets the token context.
	 * @nowarn
	 */
	public TokenContext getTokenContext();

	/**
	 * Sets the token context.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext context);
}
