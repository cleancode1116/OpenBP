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
package org.openbp.guiclient.model.item;

import java.util.HashMap;
import java.util.Map;

import org.openbp.core.model.item.ItemTypes;

/**
 * Registry that maps item types (see {@link ItemTypes})
 * to instances of item editors ({@link ItemEditor}) that can be used to edit the items.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class ItemEditorRegistry
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Table mapping item types to editor instances */
	private Map editors;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static ItemEditorRegistry singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ItemEditorRegistry getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ItemEditorRegistry();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ItemEditorRegistry()
	{
		editors = new HashMap();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Looks up an item editor.
	 *
	 * @param itemType Item type
	 * @return Editor to use for this type of item or null
	 */
	public ItemEditor lookupItemEditor(String itemType)
	{
		return (ItemEditor) editors.get(itemType);
	}

	/**
	 * Registers an item editor.
	 *
	 * @param itemType Item type
	 * @param editor Editor to use for this type of item
	 */
	public void registerItemEditor(String itemType, ItemEditor editor)
	{
		editors.put(itemType, editor);
	}
}
