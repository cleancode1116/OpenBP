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

import javax.swing.JComponent;

import org.openbp.common.generic.Copyable;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * Property editor interface.
 *
 * @author Andreas Putz
 */
public interface PropertyEditor
	extends Copyable, Cloneable
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Sets the editor parameters and initializes the editor.
	 *
	 * @param params Editor parameters from the property descriptor or null
	 */
	public void initialize(String params);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Loads the property value from the object and displays it in the property component.
	 */
	public void loadProperty();

	/**
	 * Saves the changes made to the property value if the property was modified.
	 * The method will will validate the modified value, if a {@link PropertyValidator} has been set and then
	 * on success save the property value.
	 *
	 * @return
	 *		true	The property value has been successfully validated and saved.<br>
	 *		false	Validation or saving failed.
	 */
	public boolean saveProperty();

	/**
	 * Resets the property to its original value.
	 */
	public void resetProperty();

	/**
	 * When a change in the property value has occurred, then this method is to be called.
	 */
	public void propertyChanged();

	/**
	 * Get the editor for the property.
	 * This will also initialize the component (if not done yet) and load the current property value
	 * from the object to be displayed.
	 *
	 * @return The property editor or null if the property cannot be accessed
	 */
	public JComponent getPropertyComponent();

	/**
	 * Get the editor for the property.
	 * This will also initialize the component (if not done yet), however it will
	 * \bnot\b load the current property value from the object to be displayed.
	 *
	 * @return The property editor or null if the property cannot be accessed
	 */
	public JComponent getComponent();

	/**
	 * Resets the display of the editor.
	 * Shows the editor component in an 'unfocused' state.
	 */
	public void resetComponentDisplay();

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the name of the property that should be edited by this editor instance.
	 *
	 * @param propertyName The property name
	 */
	public void setPropertyName(String propertyName);

	/**
	 * Gets the property name.
	 * @nowarn
	 */
	public String getPropertyName();

	/**
	 * Sets the object that holds the property specified by {@link #setPropertyName}
	 *
	 * @param object The object to edit
	 */
	public void setObject(Object object);

	/**
	 * Gets the the object to edit (object that own the property).
	 * @nowarn
	 */
	public Object getObject();

	/**
	 * Gets the the actual property value.
	 * @nowarn
	 */
	public Object getValue();

	/**
	 * Sets the the actual property value.
	 * @nowarn
	 */
	public void setValue(Object value);

	/**
	 * Gets the read only flag.
	 * @nowarn
	 */
	public boolean isReadonly();

	/**
	 * Gets the editor parameter values.
	 * @nowarn
	 */
	public String getParams();

	/**
	 * Sets the editor parameter values.
	 * @nowarn
	 */
	public void setParams(String params);

	/**
	 * Sets the read only flag.
	 * @nowarn
	 */
	public void setReadonly(boolean readonly);

	/**
	 * Gets the the property validator used by this property editor.
	 * @nowarn
	 */
	public PropertyValidator getValidator();

	/**
	 * Sets the the property validator used by this property editor.
	 * @nowarn
	 */
	public void setValidator(PropertyValidator validator);

	/**
	 * Gets the the owner of the editor.
	 * This can be a component or a tree node if the pe is used by the property browser.
	 * @nowarn
	 */
	public PropertyEditorOwner getOwner();

	/**
	 * Sets the the owner of the editor.
	 * This can be a component or a tree node if the pe is used by the property browser.
	 * @nowarn
	 */
	public void setOwner(PropertyEditorOwner owner);

	/**
	 * Gets the property browser instance if this property is used within the context of an property browser.
	 * @nowarn
	 */
	public PropertyBrowser getPropertyBrowser();

	/**
	 * Sets the property browser instance if this property is used within the context of an property browser.
	 * @nowarn
	 */
	public void setPropertyBrowser(PropertyBrowser propertyBrowser);
}
