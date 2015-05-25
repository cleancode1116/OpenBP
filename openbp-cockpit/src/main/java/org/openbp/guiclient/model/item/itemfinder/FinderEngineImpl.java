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
package org.openbp.guiclient.model.item.itemfinder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.guiclient.model.ModelConnector;

/**
 * Implementation of the finder engine interface.
 *
 * @author Baumgartner Michael
 */
public class FinderEngineImpl
	implements FinderEngine
{
	//////////////////////////////////////////////////
	// @@ Member
	//////////////////////////////////////////////////

	/** The mapping of the item types and the corresponding finder. */
	private final Hashtable finderMapp;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Constructor. All finder of the standard type are registered.
	 */
	public FinderEngineImpl()
	{
		finderMapp = new Hashtable();

		// Finder for item types
		registerFinder(ItemTypes.PROCESS, new ProcessFinder());
		registerFinder(ItemTypes.TYPE, new DataTypeFinder());
		registerFinder(ItemTypes.MODEL, new ModelFinder());

		// Finder for a special node type
		registerFinder(InitialNode.class, new ProcessEntryFinder());
	}

	//////////////////////////////////////////////////
	// @@ Finder registry
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.FinderEngine.registerFinder
	 */
	public void registerFinder(String itemType, Finder finder)
	{
		finderMapp.put(itemType, finder);
	}

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.FinderEngine.registerFinder
	 */
	public void registerFinder(ItemTypeDescriptor itemType, Finder finder)
	{
		registerFinder(itemType.getItemType(), finder);
	}

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.FinderEngine.registerFinder
	 */
	public void registerFinder(Class modelObjectClass, Finder finder)
	{
		finderMapp.put(modelObjectClass, finder);
	}

	//////////////////////////////////////////////////
	// @@ Finder methods
	//////////////////////////////////////////////////

	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.FinderEngine.createReferenceList
	 */
	public List createReferenceList(ModelObject core, List modelList)
	{
		// Create the model list that will be searched
		List modelsToSearch = new ArrayList();
		if (modelList == null)
			modelsToSearch = createTopLevelModelList();
		else
			modelsToSearch = modelList;

		Finder finder = getFinder(core);
		if (finder == null)
			return null;

		return findReferences(finder, core, modelsToSearch);
	}

	/**
	 * Get the finder to use with the model object. If the class
	 * is register with an finder, then this one is used. When
	 * no finder is found with the model object class, then
	 * the item type of the model object is used as a
	 * references for the finder.
	 * @param core The model object after that is seached
	 * @return the finder or null if no finder was found
	 */
	private Finder getFinder(ModelObject core)
	{
		Class coreClass = core.getClass();
		Enumeration e = finderMapp.keys();
		while (e.hasMoreElements())
		{
			Object obj = e.nextElement();
			if (obj instanceof Class)
			{
				Class finderClass = (Class) obj;
				if (finderClass.isAssignableFrom(coreClass))
					return (Finder) (finderMapp.get(finderClass));
			}
		}
		return (Finder) finderMapp.get(core.getQualifier().getItemType());
	}

	/**
	 * Find all references of the item in the given model list.
	 * @param finder The finder to use
	 * @param obj The object to search for
	 * @param modelList The list of models
	 * @return The list with references or null if none were found
	 */
	private List findReferences(Finder finder, ModelObject obj, List modelList)
	{
		if (modelList.size() == 0)
			return null;

		List modelObjects = new ArrayList();
		for (int i = 0; i < modelList.size(); i++)
		{
			Model model = (Model) modelList.get(i);
			modelObjects.addAll(finder.findModelObjectInModel(obj, model));
		}
		return modelObjects.size() == 0 ? null : modelObjects;
	}

	//////////////////////////////////////////////////
	// @@ Helper methods
	//////////////////////////////////////////////////

	/**
	 * Create a list of all top-level models.
	 * @return List with all models
	 */
	private List createTopLevelModelList()
	{
		return ModelConnector.getInstance().getModels();
	}
}
