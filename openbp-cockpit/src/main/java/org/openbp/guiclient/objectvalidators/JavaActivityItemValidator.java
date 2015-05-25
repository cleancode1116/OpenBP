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

import org.openbp.common.string.StringUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;

/**
 * Object validator for the {@link JavaActivityItem} class.
 * Generates an implementation class name from the object name.
 *
 * @author Heiko Erhardt
 */
public class JavaActivityItemValidator extends ModelObjectValidator
{
	/**
	 * Property validator method.
	 * Generates the name of the activity implementation Java source from the object name
	 * if a default package is defined for the model the type belongs to.
	 * @copy DisplayObjectValidator.validateProperty
	 */
	public boolean validateProperty(String propertyName, Object propertyValue, Object editedObject, PropertyEditor propertyEditor, boolean complete)
	{
		if (!super.validateProperty(propertyName, propertyValue, editedObject, propertyEditor, complete))
		{
			return false;
		}

		if (complete)
		{
			if (propertyName.equals("Name"))
			{
				JavaActivityItem activity = (JavaActivityItem) editedObject;

				Model model = activity.getModel();
				if (model != null)
				{
					// If there is an implementation class name specified...
					String oldName = activity.getName();
					String oldClassName = activity.getHandlerDefinition().getHandlerClassName();
					if (oldClassName != null && oldName != null)
					{
						// ...substitute ".OldName" to ".NewName"
						String newName = (String) propertyValue;
						String className = StringUtil.substitute(oldClassName, "." + oldName, "." + newName);

						// Update the object
						activity.getHandlerDefinition().setHandlerClassName(className);
						propertyEditor.getOwner().reloadProperty("HandlerClassName");
					}
				}
			}
		}

		return true;
	}
}
