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

import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;

/**
 * Object validator for the {@link DataMember} class.
 * Generates a xml name from the object name.
 *
 * @author Heiko Erhardt
 */
public class DataMemberValidator extends ModelObjectValidator
{
	/**
	 * Property validator method.
	 * Automatically generates an xml name from the object name.
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
			if (propertyName.equals("TypeName"))
			{
				DataMember memberObject = (DataMember) editedObject;

				String typeName = (String) propertyValue;

				try
				{
					// Try to determin the new data type and perform a default configuration
					// of the member according to its new type
					DataTypeItem dataType = (DataTypeItem) memberObject.getParentDataType().resolveItemRef(typeName, ItemTypes.TYPE);
					dataType.performDefaultDataMemberConfiguration(memberObject);

					// The default configuration may have changed some properties, so reload them
					propertyEditor.getOwner().reloadProperty("Length");
					propertyEditor.getOwner().reloadProperty("Precision");
				}
				catch (OpenBPException e)
				{
					// Ignore any errors
				}
			}
		}

		return true;
	}
}
