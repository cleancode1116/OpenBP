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

import org.openbp.jaspira.propertybrowser.editor.PropertyValidator;

/**
 * Object validator interface.
 * Called after a property of an object has been edited or an object is about to be saved.
 * Checks the value of the object's properties and optionally modifies them.
 *
 * @author Heiko Erhardt
 */
public interface ObjectValidator
	extends PropertyValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 *
	 * @param editedObject Edited object that contains the property
	 * @param propertyBrowser Property browser that edits the object
	 * @return
	 *		true	The object value is valid and can be saved.<br>
	 *		false	The object value is invalid. In this case, the save operation should be aborted and
	 *				the focus should not be set outside the property browser.
	 */
	public boolean validateObject(Object editedObject, PropertyBrowser propertyBrowser);
}
