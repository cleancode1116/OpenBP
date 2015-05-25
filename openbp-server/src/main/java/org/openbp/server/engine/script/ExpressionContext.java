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

/**
 * An expression context is an environment an expression parser can operate on.
 * The context must contain a list of named objects that can be accessed in an expression.
 *
 * @author Heiko Erhardt
 */
public interface ExpressionContext
{
	/**
	 * Gets an object from the context.
	 *
	 * @param name Name of the object
	 * @return Value of the object or null if no such object exists
	 */
	public Object getObject(String name);

	/**
	 * Adds an object to the context.
	 *
	 * @param name Name of the object
	 * @param value Value of the object
	 */
	public void setObject(String name, Object value);
}
