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
package org.openbp.jaspira.propertybrowser.editor;

/**
 * Property validator interface.
 * Called after a property has been edited.
 * Checks the value of an edited property.
 *
 * @author Andreas Putz
 */
public interface PropertyValidator
{
	/**
	 * Validates an edited property.
	 *
	 * @param propertyName Name of the edited property
	 * @param propertyValue Current value of the property
	 * @param editedObject Edited object that contains the property
	 * @param propertyEditor Property editor that edits the property value
	 * @param complete
	 *		true	The value has been completely entered. This is the case if the user wishes to leave the field.<br>
	 *		false	The value is being typed/edited.
	 * @return
	 *		true	The property value is valid.<br>
	 *		false	The property value is invalid. In this case, the focus should not be set
	 *				to the next component.
	 */
	public boolean validateProperty(String propertyName, Object propertyValue, Object editedObject, PropertyEditor propertyEditor, boolean complete);
}
