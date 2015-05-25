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

import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;

/**
 * Object validator for the {@link DescriptionObject} class.
 * Checks if the name is a valid identifier.
 *
 * @author Heiko Erhardt
 */
public class DescriptionObjectValidator extends ValidatorBase
{
	/**
	 * Property validator method.
	 * Ensures that an object name is given and that the name is a valid identifier.
	 * @copy ValidatorBase.validateProperty
	 */
	public boolean validateProperty(String propertyName, Object propertyValue, Object editedObject, PropertyEditor propertyEditor, boolean complete)
	{
		if (complete)
		{
			if (propertyName.equals("Name"))
			{
				String ident = (String) propertyValue;

				if (ident == null)
				{
					displayErrorMsg("No object name specified");
					return false;
				}

				if (!ModelQualifier.isValidIdentifier(ident))
				{
					displayErrorMsg("'" + ident + "' must not contain one of the characters '.', '/', ':', ';'.");
					return false;
				}
			}
		}

		return true;
	}
}
