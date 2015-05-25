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

import java.awt.Cursor;

import org.openbp.common.icon.MultiIcon;

/**
 * A CursorPrototype provides a template for generating custom cursors.
 * A custom cursor consists of the template part (for example an arrow or
 * a stop sign) and a variable part (in this case an icon) that is provided
 * by the client.
 *
 * @author Stephan Moritz
 */
public interface CursorPrototype
{
	/**
	 * Constructs a cursor object with the given icon.
	 * @param icon Variable part of the cursor
	 * @return The new cursor
	 */
	public Cursor createCursor(MultiIcon icon);
}
