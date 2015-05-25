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

import java.util.Comparator;

/**
 * Tree table node comparator.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class TreeTableNodeComparator
	implements Comparator
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static TreeTableNodeComparator singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized TreeTableNodeComparator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new TreeTableNodeComparator();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private TreeTableNodeComparator()
	{
	}

	//////////////////////////////////////////////////
	// @@ Comparator method
	//////////////////////////////////////////////////

	/**
	 * Conmpares two elements.
	 * @nowarn
	 */
	public int compare(Object o1, Object o2)
	{
		if (o1 == o2)
			return 0;

		TreeTableNode node1 = (TreeTableNode) o1;
		TreeTableNode node2 = (TreeTableNode) o2;

		return node1.getNodeText().compareTo(node2.getNodeText());
	}
}
