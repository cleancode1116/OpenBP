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
package org.openbp.guiclient.model.item.itemtree;

import java.util.ArrayList;
import java.util.List;

import org.openbp.core.model.ModelQualifier;

/**
 * Contains the current state of an item tree.
 * State state includes the qualifiers of the expanded tree nodes
 * and the qualifiers of the current selection.
 *
 * @author Heiko Erhardt
 */
public class ItemTreeState
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Qualifiers of expanded tree nodes (contains {@link ModelQualifier} objects) */
	private List expandedQualifiers;

	/** Qualifiers of selected tree nodes (contains {@link ModelQualifier} objects) */
	private List selectedQualifiers;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemTreeState()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Adds an expanded qualifier.
	 * @param expandedQualifier The expanded qualifier to add
	 */
	public void addExpandedQualifier(ModelQualifier expandedQualifier)
	{
		if (expandedQualifiers == null)
			expandedQualifiers = new ArrayList();
		expandedQualifiers.add(expandedQualifier);
	}

	/**
	 * Gets the jOIs of expanded tree nodes.
	 * @return A list of {@link ModelQualifier} objects
	 */
	public List getExpandedQualifiers()
	{
		return expandedQualifiers;
	}

	/**
	 * Sets the jOIs of expanded tree nodes.
	 * @param expandedQualifiers A list of {@link ModelQualifier} objects
	 */
	public void setExpandedQualifiers(List expandedQualifiers)
	{
		this.expandedQualifiers = expandedQualifiers;
	}

	/**
	 * Adds a selected qualifier.
	 * @param selectedQualifier The selected qualifier to add
	 */
	public void addSelectedQualifier(ModelQualifier selectedQualifier)
	{
		if (selectedQualifiers == null)
			selectedQualifiers = new ArrayList();
		selectedQualifiers.add(selectedQualifier);
	}

	/**
	 * Gets the jOIs of expanded tree nodes.
	 * @return A list of {@link ModelQualifier} objects
	 */
	public List getSelectedQualifiers()
	{
		return selectedQualifiers;
	}

	/**
	 * Sets the jOIs of expanded tree nodes.
	 * @param selectedQualifiers A list of {@link ModelQualifier} objects
	 */
	public void setSelectedQualifiers(List selectedQualifiers)
	{
		this.selectedQualifiers = selectedQualifiers;
	}
}
