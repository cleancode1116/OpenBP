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
package org.openbp.core.model.item;

import org.openbp.common.generic.Modifiable;
import org.openbp.common.property.RuntimeAttributeContainer;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;

/**
 * Item object.
 * Describes the basic properties of an OpenBP item.
 *
 * @seec ItemImpl
 *
 * @author Heiko Erhardt
 */
public interface Item
	extends ModelObject, RuntimeAttributeContainer, Modifiable
{
	/**
	 * Creates a new configuration bean of the type specified by the {@link #setConfigurationClassName(String)} property.
	 *
	 * @return The new bean or null if no setting class name was given
	 */
	public ConfigurationBean createConfigurationBean();

	//////////////////////////////////////////////////
	// @@ Reference resolving
	//////////////////////////////////////////////////

	/**
	 * Resolves an item according to its reference.
	 * The resolving is done by the {@link Model#resolveItemRef} method of the model that owns the item.
	 *
	 * @param name Qualified, partially qualified or unqualified name of the item
	 * @param itemType Type of the item (see the constants of the {@link ItemTypes} class)
	 * @return The item descriptor or null if the item name is null or this item is not connected to a model
	 * @throws OpenBPException If the specified item does not exist
	 */
	public Item resolveItemRef(String name, String itemType);

	/**
	 * Determines the name of the type relative to the model that owns this item.
	 * The determination is done by the {@link Model#determineItemRef} method of
	 * the model that owns the item.
	 *
	 * @param item Item to determine the relative name for
	 * @return The relative name of the item or null if the item is null
	 */
	public String determineItemRef(Item item);

	/**
	 * Resolves an object according to its reference.
	 * The reference denotes either an item or a child object of an item.
	 * The item will be determined using the {@link #resolveItemRef} method.
	 *
	 * @param name Qualified or unqualified name of the object
	 * @param itemType Type of the item (see the constants of the {@link ItemTypes} class)
	 * @return The model object or null if the object name is null
	 * @throws OpenBPException If the specified object could not be found
	 */
	public ModelObject resolveObjectRef(String name, String itemType);

	/**
	 * Determines the name of the object relative to the model that owns the object.
	 * The item the object belongs to will be determined using the {@link #determineItemRef} method.
	 *
	 * @param object Object to determine the relative name for<br>
	 * The object must be a child object of an item.
	 * @return The relative name of the object or null if the object is null
	 */
	public String determineObjectRef(ModelObject object);

	/**
	 * Resolves a reference to a file.
	 * The resolving is done by the {@link Model#resolveFileRef} method of the model that owns the item.
	 *
	 * @param name Name (including sub path) of the file to resolve
	 * @return The full path name of the file
	 * @throws OpenBPException If the specified file could not be found
	 */
	public String resolveFileRef(String name);

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * Instantiates objects the item might reference.
	 * It usually instantiates classes that are referenced by activities, data types etc.
	 * Those classes might not be present on the client side, so this method
	 * should be called on the server side only.<br>
	 * Make sure you call this method \iafter\i calling the {@link ModelObject#maintainReferences} method,
	 * so any references needed for the instantiation process can be expected to be resolved.
	 *
	 * Any errors will be logged to the message container of the model manager that
	 * loaded the object.
	 */
	public void instantiate();

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the item.
	 * @return The item type (see the constants of the {@link ItemTypes} class)
	 */
	public String getItemType();

	/**
	 * Sets the type of the item.
	 * @param itemType The item type (ssee the constants of the {@link ItemTypes} class)
	 */
	public void setItemType(String itemType);

	/**
	 * Gets the functional group specification.
	 * The group specification can be assigned by the user as desired.
	 * @nowarn
	 */
	public String getFunctionalGroup();

	/**
	 * Sets the functional group specification.
	 * The group specification can be assigned by the user as desired.
	 * @nowarn
	 */
	public void setFunctionalGroup(String functionalGroup);

	/**
	 * Gets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public String getImagePath();

	/**
	 * Sets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public void setImagePath(String imagePath);

	/**
	 * Gets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public boolean isImageOnly();

	/**
	 * Sets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public void setImageOnly(boolean imageOnly);

	/**
	 * Gets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean isImageResize();

	/**
	 * Sets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public void setImageResize(boolean imageResize);

	/**
	 * Gets the read-only property.
	 * @nowarn
	 */
	public boolean isReadOnly();

	/**
	 * Sets the read-only property.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly);

	/**
	 * Gets the name of the class containing the settings for this activity.
	 * @nowarn
	 */
	public String getConfigurationClassName();

	/**
	 * Sets the name of the class containing the settings for this activity.
	 * @nowarn
	 */
	public void setConfigurationClassName(String configurationClassName);

	/**
	 * Gets the flag that determines that the settings bean should not be displayed in the parameter value wizard.
	 * @nowarn
	 */
	public boolean isHideSettingsInWizard();

	/**
	 * Sets the flag that determines that the settings bean should not be displayed in the parameter value wizard.
	 * @nowarn
	 */
	public void setHideSettingsInWizard(boolean hideSettingsInWizard);

	/**
	 * Gets the user who created the item.
	 * @return User id (email address)
	 */
	public String getCreatedBy();

	/**
	 * Sets the user who created the item.
	 * @param createdBy User id (email address)
	 */
	public void setCreatedBy(String createdBy);

	/**
	 * Gets the user who maintains the item.
	 * @return User id (email address)
	 */
	public String getMaintainedBy();

	/**
	 * Sets the user who maintains the item.
	 * @param maintainedBy User id (email address)
	 */
	public void setMaintainedBy(String maintainedBy);

	/**
	 * Gets the information created by the item generator/item wizard.
	 * Contains an xml string that hold information that has been serialized by the item generator
	 * of the Cockpit. The server does not need to understand this information, so it will treat this as raw data.
	 * The generator will deserialize the information again when the item is being edited.
	 * @nowarn
	 */
	public String getGeneratorInfo();

	/**
	 * Sets the information created by the item generator/item wizard.
	 * Contains an xml string that hold information that has been serialized by the item generator
	 * of the Cockpit. The server does not need to understand this information, so it will treat this as raw data.
	 * The generator will deserialize the information again when the item is being edited.
	 * @nowarn
	 */
	public void setGeneratorInfo(String generatorInfo);

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getModel();

	/**
	 * Sets the model the item belongs to.
	 * @nowarn
	 */
	public void setModel(Model model);
}
