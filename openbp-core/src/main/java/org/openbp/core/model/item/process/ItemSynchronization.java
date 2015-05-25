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
package org.openbp.core.model.item.process;

import org.openbp.common.generic.description.DisplayObject;

/**
 * Item synchronization.
 * Node/item synchronization utility methods.
 *
 * @author Heiko Erhardt
 */
public final class ItemSynchronization
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Syncronization flag: Clear all information from the target before synchronizing */
	public static final int SYNC_CLEAR_TARGET = (1 << 0);

	/** Syncronization flag: Always copy description and display name */
	public static final int SYNC_DESCRIPTION_ALWAYS = (1 << 1);

	/** Syncronization flag: Copy description and display name if not yet present in target */
	public static final int SYNC_DESCRIPTION_NOT_YET_PRESENT = (1 << 2);

	/** Syncronization flag: Add new sockets */
	public static final int SYNC_ADD_SOCKETS = (1 << 3);

	/** Syncronization flag: Remove deleted sockets */
	public static final int SYNC_REMOVE_SOCKETS = (1 << 4);

	/** Syncronization flag: Add new parameters */
	public static final int SYNC_ADD_PARAMS = (1 << 5);

	/** Syncronization flag: Remove deleted parameters */
	public static final int SYNC_REMOVE_PARAMS = (1 << 6);

	/** Syncronization flag: Hide private initial nodes */
	public static final int SYNC_HIDE_PRIVATE_ENTRIES = (1 << 7);

	/** Syncronization flag shortcut: Synchronize all properties */
	public static final int SYNC_ALL_EXCEPT_DESCRIPTION = (SYNC_ADD_SOCKETS | SYNC_REMOVE_SOCKETS | SYNC_ADD_PARAMS | SYNC_REMOVE_PARAMS);

	/** Syncronization flag shortcut: Synchronize all properties except descriptions */
	public static final int SYNC_ALL = (SYNC_ALL_EXCEPT_DESCRIPTION | SYNC_DESCRIPTION_ALWAYS);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ItemSynchronization()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Synchronizes the display name and description of two objects.
	 *
	 * @param target Object to copy to
	 * @param source Object to copy from
	 * @param syncFlags Synchronization flags (see the constants of the {@link ItemSynchronization} class)
	 */
	public static void syncDisplayObjects(DisplayObject target, DisplayObject source, int syncFlags)
	{
		if ((syncFlags & SYNC_DESCRIPTION_ALWAYS) != 0)
		{
			// Copy display name and description regardless if present in the target or not
			target.setDisplayName(source.getDisplayName());
			target.setDescription(source.getDescription());
		}
		else if ((syncFlags & SYNC_DESCRIPTION_NOT_YET_PRESENT) != 0)
		{
			// Copy display name and description only if not present yet in the target
			if (target.getDisplayName() == null)
				target.setDisplayName(source.getDisplayName());
			if (target.getDescription() == null)
				target.setDescription(source.getDescription());
		}

		// Otherwise do not copy description and display name
	}
}
