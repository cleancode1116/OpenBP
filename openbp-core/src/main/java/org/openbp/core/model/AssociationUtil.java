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
package org.openbp.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities for building model object associations util.
 *
 * @author Erich Lauterbach
 */
public final class AssociationUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private AssociationUtil()
	{
	}


	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Adds a new association at the begin of the list of associations.
	 *
	 * @param associations List of associations ({@link Association} objects) or null
	 * @param pos The new association will be inserted before this position in the list.<br>
	 * Specify -1 to append the new association to the list.
	 * @param displayName Display name of this association (appears in the context menu)
	 * @param associatedObject Associated object
	 * @param underlyingObject Underlying object
	 * @param associationTypes Association types; must contain at least one type
	 * @param associationPriority Association priority
	 * @param hintMsg Hint msg if no actual association is present
	 * @return The list of associations (this equals the first argument of the method
	 * or a new ArrayList if this is null)
	 */
	public static List addAssociation(List associations, int pos, String displayName, Object associatedObject, ModelObject underlyingObject, String [] associationTypes, int associationPriority, String hintMsg)
	{
		Association association = new Association(displayName, associatedObject, underlyingObject, associationTypes, associationPriority, hintMsg);

		if (associations == null)
		{
			associations = new ArrayList(5);
		}

		if (!associations.contains(association))
		{
			if (associationPriority == Association.PRIMARY)
			{
				// This is the default element; make sure that it's the only one
				int l = associations.size();
				for (int i = 0; i < l; ++i)
				{
					Association a = (Association) associations.get(i);
					if (a.getAssociationPriority() == associationPriority)
					{
						a.setAssociationPriority(Association.NORMAL);
					}
				}
			}

			if (pos >= 0 && pos < associations.size())
			{
				// Insert the element at the given position
				associations.add(pos, association);
			}
			else
			{
				// Append the element
				associations.add(association);
			}
		}

		return associations;
	}

	/**
	 * Adds a list of associations to the given list of associations.
	 *
	 * @param associations List of associations ({@link Association} objects) or null
	 * @param pos The new associations will be inserted before this position in the list.<br>
	 * Specify -1 to append the new associations to the list.
	 * @param otherAssociations List of associations ({@link Association} objects) to insert
	 * @return The list of associations (this equals the first argument of the method
	 * or a new ArrayList if this is null)
	 */
	public static List addAssociations(List associations, int pos, List otherAssociations)
	{
		if (otherAssociations != null)
		{
			if (associations == null)
			{
				associations = new ArrayList(5);
			}
			else
			{
				// Check if the other association list contains the default element
				boolean addNewPrimary = false;

				int l = otherAssociations.size();
				for (int i = 0; i < l; ++i)
				{
					Association a = (Association) otherAssociations.get(i);

					if (a.getAssociationPriority() == Association.PRIMARY)
						addNewPrimary = true;
				}

				if (addNewPrimary)
				{
					// This is the default element; make sure that it's the only one
					l = associations.size();
					for (int i = 0; i < l; ++i)
					{
						Association a = (Association) associations.get(i);

						if (addNewPrimary)
						{
							if (a.getAssociationPriority() == Association.PRIMARY)
							{
								a.setAssociationPriority(Association.NORMAL);
							}
						}
					}
				}
			}

			if (pos >= 0)
			{
				if (pos < associations.size())
					pos = -1;
			}

			// Insert the other list elements, but don't change their order
			int l = otherAssociations.size();
			for (int i = 0; i < l; ++i)
			{
				Association a = (Association) otherAssociations.get(i);

				if (associations.contains(a))
					continue;

				if (pos >= 0)
				{
					// Insert the element at the given position
					associations.add(pos++, a);
				}
				else
				{
					// Append the element
					associations.add(a);
				}
			}
		}

		return associations;
	}
}

