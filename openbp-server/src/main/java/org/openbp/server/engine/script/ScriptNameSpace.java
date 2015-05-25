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
package org.openbp.server.engine.script;

import org.openbp.server.context.TokenContextUtil;

import bsh.NameSpace;
import bsh.Primitive;
import bsh.UtilEvalError;

/**
 * Special bean shell name space.
 *
 * @author Heiko Erhardt
 */
public class ScriptNameSpace extends NameSpace
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Script engine that uses this namespace */
	private ScriptEngine scriptEngine;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param scriptEngine Script engine that uses this namespace
	 * @param parent Parent namespace or null
	 */
	public ScriptNameSpace(ScriptEngine scriptEngine, NameSpace parent)
	{
		super(parent, "context");
		this.scriptEngine = scriptEngine;
	}

	//////////////////////////////////////////////////
	// @@ NameSpace overrides
	//////////////////////////////////////////////////

	public void setVariable(String name, Object value, boolean strictJava)
		throws UtilEvalError
	{
		String contextName = determineContextName(name);
		if (contextName != null)
		{
			if (value != null)
			{
				value = Primitive.unwrap(value);
			}

			scriptEngine.getContext().setObject(contextName, value);
		}
		else
		{
			super.setVariable(name, value, strictJava);
		}
	}

	public Object getVariable(String name)
		throws UtilEvalError
	{
		Object value;

		String contextName = determineContextName(name);
		if (contextName != null)
		{
			value = scriptEngine.getContext().getObject(contextName);
			if (value == null)
				value = Primitive.NULL;
		}
		else
		{
			value = super.getVariable(name);
		}
		return value;
	}

	//////////////////////////////////////////////////
	// @@ Variable management
	//////////////////////////////////////////////////

	/**
	 * Removes a variable from the script namespace.
	 *
	 * @param name Variable name
	 */
	public void removeVariable(String name)
	{
		try
		{
			super.setVariable(name, null, false);
		}
		catch (UtilEvalError e)
		{
			// Never happens
		}
	}

	//////////////////////////////////////////////////
	// @@ Access to context objects
	//////////////////////////////////////////////////

	/**
	 * Checks if the name specifies an existing context object.
	 *
	 * @param name Name of the object<br>
	 * Process variables must be prefixed with the '_' character.
	 * @nowarn
	 */
	protected String determineContextName(String name)
	{
		if (TokenContextUtil.isProcessVariableIdentifier(name))
			return name;
		return null;
	}
}
