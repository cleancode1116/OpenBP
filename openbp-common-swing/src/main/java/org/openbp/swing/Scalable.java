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
package org.openbp.swing;

/**
 * Interface that defines a scalable object.
 *
 * @author Heiko Erhardt
 */
public interface Scalable
{
	/**
	 * Gets the scaling factor.
	 * @return 1.0 = 100 %
	 */
	public double getScaleFactor();

	/**
	 * Sets the scaling factor.
	 * @param scaleFactor 1.0 = 100 %
	 */
	public void setScaleFactor(double scaleFactor);
}
