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
package org.openbp.jaspira.propertybrowser;

import java.util.HashMap;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;

/**
 * The node structure manager keeps track of object classes and the property browser
 * tree node structure that map to this type of object.
 * The tree node structures are generated based on the object descriptor
 * for the particular class.
 *
 * @author Erich Lauterbach
 */
public final class NodeStructureMgr
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Singleton instance. */
	private static NodeStructureMgr singletonInstance;

	/** Map for holding node structures for the different map tapes. */
	private Map nodeStructureMap;

	//////////////////////////////////////////////////
	// @@ Constructors
	//////////////////////////////////////////////////

	/**
	 * Default Constructor
	 */
	private NodeStructureMgr()
	{
	}

	/**
	 * Gets the singleton instance for this manager
	 * @nowarn
	 */
	public static synchronized NodeStructureMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new NodeStructureMgr();
		return singletonInstance;
	}

	//////////////////////////////////////////////////
	// @@ Publicly accessible methods
	//////////////////////////////////////////////////

	/**
	 * Determines the {@link ObjectNode} for a specfied class type and returns a copy of it.
	 * The structures are resolved from the cache if possible.
	 *
	 * @param classType The class type for which the {@link ObjectNode} and its children are to be returned
	 * @return The {@link ObjectNode} if it could be cloned, else null
	 */
	public ObjectNode createEditorStructureFor(Class classType)
	{
		ObjectNode node = obtainStructure(classType);

		// Clone the node and return it to the client.
		if (node != null)
		{
			try
			{
				return (ObjectNode) node.clone();
			}
			catch (CloneNotSupportedException ex)
			{
				ExceptionUtil.printTrace(ex);
			}
		}

		return null;
	}

	/**
	 * Loads the node structure for a specfied class type and stores it in the cache.
	 *
	 * @param classType The class type for which the {@link ObjectNode} and its children are to be loaded
	 */
	public void loadNodeStructureFor(Class classType)
	{
		obtainStructure(classType);
	}

	/**
	 * Clears the cache of the node structure manager.
	 */
	public void clearCache()
	{
		nodeStructureMap = null;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Obtains the node structure for a specified class type.
	 *
	 * @param classType The class type to be used to build the node structure
	 * @return The node structure that was retrieved from the structure cache or the
	 * newly built structure with a {@link ObjectNode} as the root node
	 */
	private ObjectNode obtainStructure(Class classType)
	{
		if (nodeStructureMap == null)
			nodeStructureMap = new HashMap();

		// Query cache
		ObjectNode node = (ObjectNode) nodeStructureMap.get(classType);
		if (node != null)
		{
			return node;
		}

		try
		{
			// Create from object description data
			ObjectDescriptor objectDescriptor = ObjectDescriptorMgr.getInstance().getDescriptor(classType, ObjectDescriptorMgr.ODM_THROW_ERROR);

			node = new ObjectNode(objectDescriptor);
			if (node != null)
			{
				// Save to cache
				nodeStructureMap.put(classType, node);
			}

			return node;
		}
		catch (XMLDriverException ex)
		{
			ExceptionUtil.printTrace(ex);
			return null;
		}
	}
}
