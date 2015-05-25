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
 * A decorator is a modifier for a single parameter (object).
 * For a detailed explanation, see the decorator pattern.
 *
 * @author Stephan Moritz
 */
public interface Decorator
{
	/**
	 * Modifies the given object and returns it.
	 *
	 * @param owner Owner of the object.
	 * The decorator may use this to decide if the object should be dorated.
	 * This parameter is passed from the {@link DecorationMgr#decorate} method.
	 * @param key Key under which the decorator is accessed
	 * @param value Object to decorate (also passed from the {@link DecorationMgr#decorate} method)
	 * @return The decorated object.
	 * The decorator may either modify the original object and return it
	 * or return a new object (or of course return the object unchanged).
	 * The returned object will be passed to the next decorator.
	 */
	public Object decorate(Object owner, String key, Object value);
}
