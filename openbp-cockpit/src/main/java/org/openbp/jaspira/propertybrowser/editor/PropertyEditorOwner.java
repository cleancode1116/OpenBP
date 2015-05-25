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

import java.awt.event.KeyEvent;

/**
 * This interface defines the methods that an object that 'owns' a property editor must support.
 *
 * @author Heiko Erhardt
 */
public interface PropertyEditorOwner
{
	/**
	 * Gets the the property validator used by this property editor..
	 * @nowarn
	 */
	public PropertyValidator getValidator();

	/**
	 * Forces a reload of a particular property from the modified object and redisplays the specified property.
	 * This method can be used by validators to redisplay properties they have changed.
	 *
	 * @param propertyName Property name
	 */
	public void reloadProperty(String propertyName);

	/**
	 * Handles a key event that is not consumed by the property editor.
	 *
	 * @param e The key event
	 */
	public void handleKeyEvent(KeyEvent e);
}
