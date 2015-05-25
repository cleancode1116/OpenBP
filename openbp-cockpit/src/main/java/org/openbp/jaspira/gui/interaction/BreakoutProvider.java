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
package org.openbp.jaspira.gui.interaction;

import java.util.List;

/**
 * A break out provider is some object that is able to create a set of entries
 * for a break out box.
 *
 * @author Jens Ferchland
 */
public interface BreakoutProvider
{
	/**
	 * Creates a set of break out entries for the given data flavors.
	 *
	 * @param importers Flavors supported by the break out box (contains DataFlavor objects)
	 * @return The list of entries
	 */
	public BreakoutBoxEntry [] createBreakOutEntries(List importers);
}
