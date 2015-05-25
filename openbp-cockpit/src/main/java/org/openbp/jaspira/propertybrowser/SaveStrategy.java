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

/**
 * Defines the behavior of an property browser regarding saving of an edited object.
 *
 * @author Andreas Putz
 */
public interface SaveStrategy
{
	/**
	 * Executes the save procedure for the current item of the specified property browser.
	 *
	 * @param editor The property browser
	 *
	 * @return
	 *		true	If object was saved successfully.<br>
	 *		false	There were errors during the save operations or the user
	 *				choosed to cancel the save operation.
	 *				The implementor of the strategy should issue an error message
	 *				if appropriate.
	 */
	public boolean executeSave(PropertyBrowser editor);
}
