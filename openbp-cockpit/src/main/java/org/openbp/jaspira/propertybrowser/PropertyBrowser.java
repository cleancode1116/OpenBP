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

import java.awt.event.KeyEvent;

import javax.swing.JComponent;

import org.openbp.common.generic.Copyable;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;

/**
 * Defines the methods an editor for an object must implement.
 *
 * The {@link #setObject(Object, boolean)} method will be called by the framework when the object
 * is about to be displayed in the editor.<br>
 * If the user modifies a property of the object, the editor should create
 * a clone of the object. In this case, the {@link #getModifiedObject} method
 * should return the modified clone.
 *
 * @author Heiko Erhardt
 */
public interface PropertyBrowser
{
	//////////////////////////////////////////////////
	// @@ PropertyBrowser implementation
	//////////////////////////////////////////////////

	/**
	 * Sets the object to be displayed/edited.
	 *
	 * @param object The object to edit or null
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 * @throws CloneNotSupportedException If the object is not cloneable
	 */
	public void setObject(Object object, boolean isObjectNew)
		throws XMLDriverException, CloneNotSupportedException;

	/**
	 * Sets the object to be displayed/edited.
	 *
	 * @param object The object to edit or null
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootNode Root node of the property browser tree or null if the property browser
	 * should create an appropriate property browser tree based on the class of the object
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 * @throws CloneNotSupportedException If the object is not cloneable
	 */
	public void setObject(Object object, boolean isObjectNew, ObjectNode rootNode)
		throws XMLDriverException, CloneNotSupportedException;

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, boolean isObjectNew, MultiIcon rootIcon)
		throws XMLDriverException, CloneNotSupportedException;

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param modifiedObject The modified Object
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, Object modifiedObject, boolean isObjectNew, MultiIcon rootIcon)
		throws XMLDriverException, CloneNotSupportedException;

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param modifiedObject The modified Object
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 * @param rootNode Root node of the property browser tree or null if the property browser
	 * should create an appropriate property browser tree based on the class of the object
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, Object modifiedObject, boolean isObjectNew, MultiIcon rootIcon, ObjectNode rootNode)
		throws XMLDriverException, CloneNotSupportedException;

	/**
	 * Gets the object that is currently edited.
	 *
	 * @return The object that is currently edited or null
	 */
	public Object getObject();

	/**
	 * Gets the clone of the edited object that has been modified.
	 * The clone is usually a first-level clone (see the {@link Copyable} class).
	 *
	 * @return The modified object or null if nothing has been changed
	 */
	public Object getModifiedObject();

	/**
	 * Gets the optional object the 'object' is based upon.
	 * This is used for name uniqueness checks by the model object validator
	 * if the object passed to the property browser has been cloned.
	 * Since the property browser clones the given object once more (into the modifiedObject)
	 * we loose the reference to the original.
	 * originalObject will refer the non-cloned original.
	 *
	 * @return The modified object or null if there is no original
	 */
	public Object getOriginalObject();

	/**
	 * Sets the optional object the 'object' is based upon.
	 * This is used for name uniqueness checks by the model object validator
	 * if the object passed to the property browser has been cloned.
	 * Since the property browser clones the given object once more (into the modifiedObject)
	 * we loose the reference to the original.
	 * originalObject will refer the non-cloned original.
	 *
	 * @param originalObject The modified object or null if there is no original
	 */
	public void setOriginalObject(Object originalObject);

	/**
	 * Gets the flag used to determine if the original object has changed.
	 * @nowarn
	 */
	public boolean isObjectModified();

	/**
	 * Sets the flag used to determine if the original object has changed.
	 * @nowarn
	 */
	public void setObjectModified(boolean objectModified);

	/**
	 * Gets the flag that determines if the object has just been created.
	 * @nowarn
	 */
	public boolean isObjectNew();

	/**
	 * Sets the flag that determines if the object has just been created.
	 * @nowarn
	 */
	public void setObjectNew(boolean isObjectNew);

	/**
	 * Save the object.
	 *
	 * @return
	 *	true	The object has been successfully saved or the change was discarded<br>
	 *	false	The object has not been saved, return
	 */
	public boolean saveObject();

	/**
	 * @see org.openbp.jaspira.propertybrowser.PropertyBrowser#reset()
	 * @nowarn
	 */
	public boolean reset();

	//////////////////////////////////////////////////
	// @@ Property browser listener support
	//////////////////////////////////////////////////

	/**
	 * Adds a {@link PropertyBrowserListener} to the receiver which notifies the listener of any
	 * property change when the {@link #firePropertyBrowserEvent} method is called.
	 *
	 * @param listener Listener to add
	 */
	public void addPropertyBrowserListener(PropertyBrowserListener listener);

	/**
	 * Removes a {@link PropertyBrowserListener} from the receiver.
	 *
	 * @param listener Listener to remove
	 */
	public void removePropertyBrowserListener(PropertyBrowserListener listener);

	/**
	 * Notifies all registered {@link PropertyBrowserListener} 's registered with the receiver that
	 * the property editor has stopped editing the property.
	 *
	 * @param e Event
	 */
	public void firePropertyBrowserEvent(PropertyBrowserEvent e);

	//////////////////////////////////////////////////
	// @@ Methods used by property editor implementations
	//////////////////////////////////////////////////

	/**
	 * Processses a key event.
	 *
	 * @param e Event
	 */
	public void handleKeyEvent(KeyEvent e);

	/**
	 * Configures a property editor component for usage with this property browser.
	 *
	 * @param propertyEditorComponent Property editor component
	 */
	public void configureSubComponent(JComponent propertyEditorComponent);

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the propertybrowser readonly.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly);

	/**
	 * Gets wheter the object is read only.
	 * @nowarn
	 */
	public boolean isReadOnly();

	/**
	 * Gets the save after modifying property flag.
	 * @nowarn
	 */
	public boolean isSaveImmediately();

	/**
	 * Sets the save after modifying property flag.
	 * @nowarn
	 */
	public void setSaveImmediately(boolean saveImmediately);

	/**
	 * Gets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public String [] getVisibleMembers();

	/**
	 * Sets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public void setVisibleMembers(String [] visibleMembers);
}
