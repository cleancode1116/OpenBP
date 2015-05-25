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

import javax.swing.Icon;

/**
 * Decorator for (multi-) icons.
 *
 * @author Stephan Moritz
 */
public interface IconDecorator
{
	/**
	 * Decorates the given icon for the object and returns a decorated version.
	 * @param object The object the icon belongs to
	 * @param old The original icon
	 * @return The decorated icon
	 */
	public Icon decorate(Object object, Icon old);
}
