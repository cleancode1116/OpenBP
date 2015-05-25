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
package org.openbp.swing.components.treetable;

import javax.swing.event.TreeModelListener;

/**
 * Combines to listener and extends they with before change and after change methods.
 *
 * @author Andreas Putz
 */
public interface TreeTableModelListener
	extends TreeModelListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** If the tree structure changed event has been called */
	public static final int NO_TABLE_MODEL_EVENT_TYPE = -99;

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * It's called by the fire tree event methods.
	 *
	 * @param eventType The event types of TableModelEvent
	 */
	public void beforeChanges(int eventType);

	/**
	 * It's called by {@link SimpleTreeTableModel#delayedFireTableDataChanged}.
	 *
	 * @param eventType The event types of TableModelEvent
	 */
	public void afterChanges(int eventType);
}
