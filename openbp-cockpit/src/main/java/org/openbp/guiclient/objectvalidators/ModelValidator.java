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
package org.openbp.guiclient.objectvalidators;

import java.util.ArrayList;
import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelImpl;
import org.openbp.core.model.ModelImportPBContainer;
import org.openbp.core.model.ModelQualifier;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * Object validator for the {@link Model} class.
 * Converts the model import container list to actual model imports and
 * resolves the references to the imported models.
 *
 * @author Heiko Erhardt
 */
public class ModelValidator extends ModelObjectValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 *
	 * @param editedObject Edited object that contains the property
	 * @param pb Property browser that edits the object
	 * @return
	 *		true	The object value is valid and can be saved.<br>
	 *		false	The object value is invalid. In this case, the save operation should be aborted and
	 *				the focus should not be set outside the property browser.
	 */
	public boolean validateObject(Object editedObject, PropertyBrowser pb)
	{
		boolean ret = true;

		ModelImpl model = (ModelImpl) editedObject;

		List importList = null;

		List containers = model.getImportPBContainerList();
		if (containers != null)
		{
			int n = containers.size();
			if (n > 0)
			{
				importList = new ArrayList();

				for (int i = 0; i < n; ++i)
				{
					ModelImportPBContainer container = (ModelImportPBContainer) containers.get(i);

					String name = container.getName();
					if (name == null)
					{
						// We ignore empty containers
						continue;
					}

					name = ModelQualifier.normalizeModelName(name);

					// First, check if the model can be resolved
					if (model.getModelMgr().getOptionalModelByQualifier(ModelQualifier.constructModelQualifier(name)) == null)
					{
						displayErrorMsg("Cannot resolve model '" + name + "'.");
						ret = false;
					}

					importList.add(name);
				}
			}
		}

		if (! ret)
			return false;

		model.setImportList(importList);
		model.setImportPBContainerList(null);
		model.setImportContainerListCreated(false);

		return super.validateObject(editedObject, pb);
	}
}
