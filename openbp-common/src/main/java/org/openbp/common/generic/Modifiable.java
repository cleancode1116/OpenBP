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
package org.openbp.common.generic;

/**
 * A modifiable object contains a flag that is set if the current state of the
 * object is different to a persistent version (i\.e\. a modify flag). It
 * provides methods to access this flag.
 *
 * @author Stephan Moritz
 */
public interface Modifiable
{
	/**
	 * Gets the modified flag.
	 * @nowarn
	 */
	public boolean isModified();

	/**
	 * Sets the modified flag.
	 * @nowarn
	 */
	public void setModified();

	/**
	 * Clears the modified flag.
	 * @nowarn
	 */
	public void clearModified();
}
