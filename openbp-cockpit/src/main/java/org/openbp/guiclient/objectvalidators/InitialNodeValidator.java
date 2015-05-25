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

import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * Object validator for the {@link InitialNode} class.
 * If the 'default entry' flag has been set for the given initial node, the validator will
 * clear the flag from the other initial nodes of the process.
 * This ensures that there is always only one default initial node.
 *
 * @author Heiko Erhardt
 */
public class InitialNodeValidator extends ModelObjectValidator
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
		if (!super.validateObject(editedObject, pb))
		{
			return false;
		}

		InitialNode editedNode = (InitialNode) editedObject;
		if (editedNode.isDefaultEntry())
		{
			ProcessItem process = editedNode.getProcess();

			InitialNode currentDefaultInitialNode = process.getDefaultInitialNode();
			if (currentDefaultInitialNode != editedNode && currentDefaultInitialNode != pb.getObject() && currentDefaultInitialNode != pb.getOriginalObject())
			{
				currentDefaultInitialNode.setDefaultEntry(false);
			}
		}

		return true;
	}
}
