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

import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;

/**
 * Adapter from the token context to the expression context interface.
 *
 * @author Heiko Erhardt
 */
public class TokenContextToExpressionContextAdapter
	implements ExpressionContext
{
	/** Token context */
	private TokenContext tokenContext;

	/**
	 * Default constructor.
	 *
	 * @param tokenContext Token context
	 */
	public TokenContextToExpressionContextAdapter(TokenContext tokenContext)
	{
		this.tokenContext = tokenContext;
	}

	/**
	 * Gets an object from the context.
	 *
	 * @param name Name of the object
	 * @return Value of the object or null if no such object exists
	 */
	public Object getObject(String name)
	{
		if (TokenContextUtil.isProcessVariableIdentifier(name))
		{
			return tokenContext.getProcessVariableValue(name.substring(1));
		}
		return tokenContext.getParamValue(name);
	}

	/**
	 * Adds an object to the context.
	 *
	 * @param name Name of the object
	 * @param value Value of the object
	 */
	public void setObject(String name, Object value)
	{
		if (TokenContextUtil.isProcessVariableIdentifier(name))
		{
			tokenContext.setProcessVariableValue(name.substring(1), value);
		}
		tokenContext.setParamValue(name, value);
	}
}
