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

import java.util.Deque;
import java.util.LinkedList;

import org.openbp.server.context.TokenContext;

/**
 * Default implementation of the factory for script engines.
 * The class will cache script engine instances.
 *
 * @author Heiko Erhardt
 */
public class ScriptEngineFactoryImpl
	implements ScriptEngineFactory
{
	/** Script engine pool */
	private static final Deque<ScriptEngine> idleScriptEngines = new LinkedList<ScriptEngine>();
	
	/**
	 * Default constructor.
	 */
	public ScriptEngineFactoryImpl()
	{
	}

	/**
	 * Obtains a new instance of the script engine.
	 * Make sure to return each instance to the script engine pool using the method {@link #releaseScriptEngine(ScriptEngine)}.
	 *
	 * @return The engine instance
	 */
	public ScriptEngine obtainScriptEngine(TokenContext context)
	{
		ScriptEngine engine;
		synchronized (idleScriptEngines)
		{
			if (!idleScriptEngines.isEmpty())
			{
				engine = idleScriptEngines.removeLast();
			}
			else
			{
				engine = new ScriptEngineImpl();
			}
		}

		engine.setContext(new TokenContextToExpressionContextAdapter(context));

		return engine;
	}

	/**
	 * Releases a script engine instance back to the pool.
	 *
	 * @param scriptEngine Engine to release, must have been obtained by {@link #obtainScriptEngine(TokenContext)}
	 */
	public void releaseScriptEngine(ScriptEngine scriptEngine)
	{
		if (scriptEngine != null)
		{
			synchronized(idleScriptEngines)
			{
				idleScriptEngines.addLast(scriptEngine);
			}
		}
	}
}
