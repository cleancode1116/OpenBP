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
package org.openbp.jaspira.decoration;

/**
 * This class defines an abstract boolean decorator.
 * The decoration, as defined by {@link #doDecorate} is only performed if {@link #qualifies} returns true.
 *
 * @author Stephan Moritz
 */
public abstract class FilteredDecorator
	implements Decorator
{
	/**
	 * Performs the decoration if qualifies (owner) yields true.
	 * The actual decoration is delegated to doDecorate ().
	 *
	 * @param owner Owner of the object
	 * @param key Key under which the decorator is accessed
	 * @param value Object to decorate (also passed to the {@link DecorationMgr#decorate} method)
	 * @return The decorated object.
	 * The decorator may either modify the original object and return it
	 * or return a new object. The returned object will be passed to the
	 * next decorator.
	 */
	public final Object decorate(Object owner, String key, Object value)
	{
		if (qualifies(owner))
		{
			return doDecorate(owner, key, value);
		}

		return value;
	}

	/**
	 * Performs the actual decoration.
	 *
	 * @param owner Owner of the object
	 * @param key Key under which the decorator is accessed
	 * @param value Object to decorate (also passed to the {@link DecorationMgr#decorate} method)
	 * @return The decorated object.
	 * The decorator may either modify the original object and return it
	 * or return a new object. The returned object will be passed to the
	 * next decorator.
	 */
	public abstract Object doDecorate(Object owner, String key, Object value);

	/**
	 * The actual filter method.
	 *
	 * @param owner Owner of the object.
	 * This parameter is passed to the {@link DecorationMgr#decorate} method.
	 * @return
	 *		true	If owner qualifies as source for decoration.<br>
	 *		false	If the decoration should not be applied.
	 */
	public abstract boolean qualifies(Object owner);
}
