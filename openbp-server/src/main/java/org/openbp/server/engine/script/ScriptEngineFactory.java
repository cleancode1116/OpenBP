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
/*
 * Created on 13.09.2005
 * $Id$
 *
 * Copyright (c) 2005 Kassenaerztliche Vereinigung Bayerns.
 * All rights reserved. Use is subject to licence terms.
 *
 * Author: Heiko Erhardt (Heiko.Erhardt@gmx.net)
 */
package org.openbp.server.engine.script;

import org.openbp.server.context.TokenContext;

/**
 * Factory and manager for script engines.
 *
 * @author Heiko Erhardt
 */
public interface ScriptEngineFactory
{
	/**
	 * Obtains a new instance of the script engine.
	 * Make sure to return each instance to the script engine pool using the method {@link #releaseScriptEngine(ScriptEngine)}.
	 *
	 * @param context The token context the script engine shall be associated with
	 * @return The engine instance
	 */
	public ScriptEngine obtainScriptEngine(TokenContext context);

	/**
	 * Releases a script engine instance back to the pool.
	 *
	 * @param scriptEngine Engine to release, must have been obtained by {@link #obtainScriptEngine(TokenContext)}
	 */
	public void releaseScriptEngine(ScriptEngine scriptEngine);
}
