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
package org.openbp.cockpit.plugins.finder;

import java.util.Arrays;
import java.util.Comparator;

import org.openbp.cockpit.plugins.finder.treemodel.GenericNode;
import org.openbp.core.model.item.ItemTypes;

/**
 * Comparator for the tree.
 *
 * @author Baumgartner Michael
 */
public class RefComparator
	implements Comparator
{
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if (o1 == o2)
			return 0;

		GenericNode leftNode = (GenericNode) o1;
		GenericNode rightNode = (GenericNode) o2;

		// Get the priority of the nodes, 1 stands for model node,
		// 2 for an simple node, 3 for a leaf node
		int leftPrio = ((Integer) leftNode.getProperty(RefStrategy.PRIORITY_KEY)).intValue();
		int rightPrio = ((Integer) rightNode.getProperty(RefStrategy.PRIORITY_KEY)).intValue();
		
		if (leftPrio != rightPrio)
		{
		    return leftPrio - rightPrio;
		}

		return compareWithSamePrio(leftNode, rightNode, leftPrio);
	}

    private int compareWithSamePrio(GenericNode leftNode, GenericNode rightNode, int prio)
    {
        switch (prio)
        {
        case 1:
            return leftNode.toString().compareTo(rightNode.toString());

        case 2:
            return compareSimpleNodes(leftNode, rightNode);

        case 3:
            return leftNode.toString().compareTo(rightNode.toString());
        }
        
        return 0;
    }

    private int compareSimpleNodes(GenericNode leftNode, GenericNode rightNode)
    {
        String leftType = (String) leftNode.getProperty(RefStrategy.ITEMTYPE_KEY);
        String rightType = (String) rightNode.getProperty(RefStrategy.ITEMTYPE_KEY);
        
        if (leftType.equals(rightType))
            return leftNode.toString().compareTo(rightNode.toString());

        // Sort by the item type
        int index1 = Arrays.binarySearch(ItemTypes.getValues(), leftType);
        int index2 = Arrays.binarySearch(ItemTypes.getValues(), rightType);
        
        return index2 - index1;
    }
}
