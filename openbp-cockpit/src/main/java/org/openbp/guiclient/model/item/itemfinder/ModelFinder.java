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
import java.util.Iterator;
import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;

/**
 * The model finder only searches for imported models.
 *
 * @author Baumgartner Michael
 */
public class ModelFinder extends FinderImpl
{
	/**
	 * @copy org.openbp.guiclient.model.item.itemfinder.Finder.findModelObjectInModel
	 */
	public List findModelObjectInModel(ModelObject mo, Model model)
	{
		List modelObjects = new ArrayList();
		if (mo instanceof Model)
		{
			Iterator iterator = model.getImportedModels();
			while (iterator.hasNext())
			{
				Model importedModel = (Model) iterator.next();
				addIfMatch(model, importedModel, mo, modelObjects);
			}
		}
		return modelObjects;
	}
}
