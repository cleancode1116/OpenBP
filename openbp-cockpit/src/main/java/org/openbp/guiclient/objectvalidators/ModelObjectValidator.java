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

import java.util.Iterator;

import org.openbp.core.model.ModelObject;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * Model object validator.
 *
 * @author Heiko Erhardt
 */
public class ModelObjectValidator extends DisplayObjectValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 * Ensures uniqueness of the object name based on the original object
	 * that has been set with the property browser (i. e. this object will be skipped
	 * when checking for objects with the same name).
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
		if (! (editedObject instanceof ModelObject))
			// Just for safety
			return true;

		ModelObject mo = (ModelObject) editedObject;

		mo.maintainReferences(ModelObject.VALIDATE_BASIC | ModelObject.RESOLVE_GLOBAL_REFS);

		checkNameUniqueness(mo, pb);

		// Print errors and return if any have been found
		if (! displayMsgContainer())
			return false;

		return super.validateObject(editedObject, pb);
	}

	/**
	 * Checks if the name of this object is unique in the collection it iscontain in - if any.
	 * If the contains a member with the same name as this object that is not the original object,
	 * an error message will be written to the message container of the object.
	 *
	 * @param editedObject Edited object that contains the property
	 * @param pb Property browser that edits the object
	 */
	public static void checkNameUniqueness(ModelObject editedObject, PropertyBrowser pb)
	{
		ModelObject object = (ModelObject) pb.getObject();
		ModelObject originalObject = (ModelObject) pb.getOriginalObject();

		Iterator it = object.getContainerIterator();
		if (it == null)
			// Object is not contained in a collection
			return;

		String name = editedObject.getName();

		while (it.hasNext())
		{
			ModelObject element = (ModelObject) it.next();

			if (element == editedObject || element == object || element == originalObject)
			{
				// Do not consider ourself or the original or the reference object of the object we are editing
				continue;
			}

			if (name.equals(element.getName()))
			{
				element.getModelMgr().getMsgContainer().addMsg(editedObject, "An element having this name already exists.");
				return;
			}
		}
	}
}
