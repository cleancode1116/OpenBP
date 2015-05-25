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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectImpl;
import org.openbp.core.model.ModelQualifier;

/**
 * Default implementation of an item object.
 * Describes the basic properties of an OpenBP item.
 * An item is always a sub object of a model.
 *
 * @author Heiko Erhardt
 */
public class ItemImpl extends ModelObjectImpl
	implements Item
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type of the item. See the constants of the {@link ItemTypes} class */
	private String itemType;

	/** A functional group specification that can be used as desired. */
	private String functionalGroup;

	/** Image path */
	private String imagePath;

	/** Flag if only the image should be displayed instead of node drawing + image */
	private boolean imageOnly;

	/** Flag if the image size should be resized with the node */
	private boolean imageResize;

	/** Read-only property. */
	private boolean readOnly;

	/** Name of the class containing the configuration for this item */
	private String configurationClassName;

	/** Flag that determines that the settings bean should not be displayed in the parameter value wizard */
	private boolean hideSettingsInWizard;

	/**
	 * Information created by the item generator/item wizard.
	 * This member contains an xml string that hold information that has been serialized by the item generator
	 * of the Cockpit. The server does not need to understand this information, so it will treat this as raw data.
	 * The generator will deserialize the information again when the item is being edited.<br>
	 * The object serialized into this string is an instance of org.openbp.cockpit.generator.GeneratorSettings.
	 */
	private String generatorInfo;

	/** User who created the item. The user id is usually the email address of the user. */
	private String createdBy;

	/** User who maintains the item. The user id is usually the email address of the user. */
	private String maintainedBy;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Model the item belongs to */
	private transient Model model;

	/** Runtime attribute table */
	protected transient Hashtable runtimeAttributes;

	/** Modified flag for usage by editors */
	private transient boolean modified;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		ItemImpl src = (ItemImpl) source;

		itemType = src.itemType;
		functionalGroup = src.functionalGroup;
		imagePath = src.imagePath;
		imageOnly = src.imageOnly;
		imageResize = src.imageResize;
		readOnly = src.readOnly;
		configurationClassName = src.configurationClassName;
		hideSettingsInWizard = src.hideSettingsInWizard;
		generatorInfo = src.generatorInfo;
		createdBy = src.createdBy;
		maintainedBy = src.maintainedBy;

		// Prevent values in the destination object from being overwritten by null values
		// caused by the transient attribute of the members
		if (src.model != null)
			model = src.model;
		if (src.runtimeAttributes != null)
			runtimeAttributes = src.runtimeAttributes;
	}

	/**
	 * Creates a new configuration bean of the type specified by the {@link #setConfigurationClassName(String)} property.
	 *
	 * @return The new bean or null if no setting class name was given
	 */
	public ConfigurationBean createConfigurationBean()
	{
		if (configurationClassName == null)
			return null;

		ConfigurationBean configurationbean = null;
		try
		{
			configurationbean = (ConfigurationBean) ReflectUtil.instantiate(configurationClassName, getModel().getClassLoader(),
				ConfigurationBean.class, "configuration bean");
		}
		catch (ReflectException e)
		{
			LogUtil.error(getClass(), "The configuration bean class of component $0 could not be instantiated.", getQualifier(), e);
		}

		return configurationbean;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getOwningModel()
	{
		return model;
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return model;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 *
	 * @return An iterator over the items of the model of this item that have
	 * the same item type as this item
	 */
	public Iterator getContainerIterator()
	{
		if (model != null)
			return model.getItems(itemType);

		return null;
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name equals the {@link #setItemType} for an item
	 */
	public String getModelObjectSymbolName()
	{
		return itemType;
	}

	/**
	 * Gets the reference to the item.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(this);
	}

	//////////////////////////////////////////////////
	// @@ RuntimeAttributeContainer implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a runtime attribute value.
	 *
	 * @param key Name of the attribute
	 * @return The value of the attribute or null if no such attribute exists
	 */
	public Object getRuntimeAttribute(String key)
	{
		if (runtimeAttributes != null)
			return runtimeAttributes.get(key);
		return null;
	}

	/**
	 * Sets a runtime attribute value.
	 *
	 * @param key Name of the attribute
	 * @param value Value of the attribute
	 */
	public void setRuntimeAttribute(String key, Object value)
	{
		if (runtimeAttributes == null)
		{
			runtimeAttributes = new Hashtable();
		}
		runtimeAttributes.put(key, value);
	}

	/**
	 * Removes a runtime attribute.
	 *
	 * @param key Name of the attribute
	 */
	public void removeRuntimeAttribute(String key)
	{
		if (runtimeAttributes != null)
		{
			runtimeAttributes.remove(key);
			if (runtimeAttributes.isEmpty())
			{
				runtimeAttributes = null;
			}
		}
	}

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
	public Item resolveItemRef(String name, String itemType)
	{
		return model != null && name != null ? model.resolveItemRef(name, itemType) : null;
	}

	/**
	 * Determines the name of the type relative to the model that owns this item.
	 * The determination is done by the {@link Model#determineItemRef} method of
	 * the model that owns the item.
	 *
	 * @param item Item to determine the relative name for
	 * @return The relative name of the item or null if the item is null
	 */
	public String determineItemRef(Item item)
	{
		return model != null && item != null ? model.determineItemRef(item) : null;
	}

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
	public ModelObject resolveObjectRef(String name, String itemType)
	{
		return model != null && name != null ? model.resolveObjectRef(name, itemType) : null;
	}

	/**
	 * Determines the name of the object relative to the model that owns the object.
	 * The item the object belongs to will be determined using the {@link #determineItemRef} method.
	 *
	 * @param object Object to determine the relative name for<br>
	 * The object must be a child object of an item.
	 * @return The relative name of the object or null if the object is null
	 */
	public String determineObjectRef(ModelObject object)
	{
		return model != null && object != null ? model.determineObjectRef(object) : null;
	}

	/**
	 * Resolves a reference to a file.
	 * The resolving is done by the {@link Model#resolveFileRef} method of the model that owns the item.
	 *
	 * @param name Name (including sub path) of the file to resolve
	 * @return The full path name of the file
	 * @throws OpenBPException If the specified file could not be found
	 */
	public String resolveFileRef(String name)
	{
		return model != null && name != null ? model.resolveFileRef(name) : null;
	}

	/**
	 * Gets the qualifier of the owning model.
	 * Used in order to display this information in the UI.
	 * @nowarn
	 */
	public String getOwningModelQualifier()
	{
		if (getOwningModel() != null)
			return getOwningModel().getQualifier().toString();
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		return null;
	}

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
	public void instantiate()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the item.
	 * @return The item type (see the constants of the {@link ItemTypes} class)
	 */
	public String getItemType()
	{
		return itemType;
	}

	/**
	 * Sets the type of the item.
	 * @param itemType The item type (ssee the constants of the {@link ItemTypes} class)
	 */
	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}

	/**
	 * Gets the functional group specification.
	 * The group specification can be assigned by the user as desired.
	 * @nowarn
	 */
	public String getFunctionalGroup()
	{
		return functionalGroup;
	}

	/**
	 * Sets the functional group specification.
	 * The group specification can be assigned by the user as desired.
	 * @nowarn
	 */
	public void setFunctionalGroup(String functionalGroup)
	{
		this.functionalGroup = functionalGroup;
	}

	/**
	 * Gets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public String getImagePath()
	{
		return imagePath;
	}

	/**
	 * Sets the image path.
	 * The image path is either relative to the model directory (recommended) or absolute.
	 * @nowarn
	 */
	public void setImagePath(String imagePath)
	{
		this.imagePath = imagePath;
	}

	/**
	 * Checks the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean hasImageResize()
	{
		return imageResize;
	}

	/**
	 * Gets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public boolean isImageResize()
	{
		return imageResize;
	}

	/**
	 * Sets the flag if the image should be resized with the node.
	 * @nowarn
	 */
	public void setImageResize(boolean imageResize)
	{
		this.imageResize = imageResize;
	}

	/**
	 * Checks the flag if only the image should be displayed instead of node drawing + image is set.
	 * @nowarn
	 */
	public boolean hasImageOnly()
	{
		return imageOnly;
	}

	/**
	 * Gets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public boolean isImageOnly()
	{
		return imageOnly;
	}

	/**
	 * Sets the flag if only the image should be displayed instead of node drawing + image.
	 * @nowarn
	 */
	public void setImageOnly(boolean imageOnly)
	{
		this.imageOnly = imageOnly;
	}

	/**
	 * Gets the read-only property.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * Determines if the read-only property is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasReadOnly()
	{
		return readOnly;
	}

	/**
	 * Sets the read-only property.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	/**
	 * Gets the name of the class containing the settings for this item.
	 * @nowarn
	 */
	public String getConfigurationClassName()
	{
		return configurationClassName;
	}

	/**
	 * Sets the name of the class containing the settings for this item.
	 * @nowarn
	 */
	public void setConfigurationClassName(String configurationClassName)
	{
		this.configurationClassName = configurationClassName;
	}

	/**
	 * Checks if the flag that determines that the settings bean should not be displayed in the parameter value wizard is present.
	 * @nowarn
	 */
	public boolean hasHideSettingsInWizard()
	{
		return hideSettingsInWizard;
	}

	/**
	 * Gets the flag that determines that the settings bean should not be displayed in the parameter value wizard.
	 * @nowarn
	 */
	public boolean isHideSettingsInWizard()
	{
		return hideSettingsInWizard;
	}

	/**
	 * Sets the flag that determines that the settings bean should not be displayed in the parameter value wizard.
	 * @nowarn
	 */
	public void setHideSettingsInWizard(boolean hideSettingsInWizard)
	{
		this.hideSettingsInWizard = hideSettingsInWizard;
	}

	/**
	 * Gets the user who created the item.
	 * @return User id (email address)
	 */
	public String getCreatedBy()
	{
		return createdBy;
	}

	/**
	 * Sets the user who created the item.
	 * @param createdBy User id (email address)
	 */
	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	/**
	 * Gets the user who maintains the item.
	 * @return User id (email address)
	 */
	public String getMaintainedBy()
	{
		return maintainedBy;
	}

	/**
	 * Sets the user who maintains the item.
	 * @param maintainedBy User id (email address)
	 */
	public void setMaintainedBy(String maintainedBy)
	{
		this.maintainedBy = maintainedBy;
	}

	/**
	 * Gets the information created by the item generator/item wizard.
	 * Contains an xml string that hold information that has been serialized by the item generator
	 * of the Cockpit. The server does not need to understand this information, so it will treat this as raw data.
	 * The generator will deserialize the information again when the item is being edited.<br>
	 * The object serialized into this string is an instance of org.openbp.cockpit.generator.GeneratorSettings.
	 * @nowarn
	 */
	public String getGeneratorInfo()
	{
		return generatorInfo;
	}

	/**
	 * Sets the information created by the item generator/item wizard.
	 * Contains an xml string that hold information that has been serialized by the item generator
	 * of the Cockpit. The server does not need to understand this information, so it will treat this as raw data.
	 * The generator will deserialize the information again when the item is being edited.<br>
	 * The object serialized into this string is an instance of org.openbp.cockpit.generator.GeneratorSettings.
	 * @nowarn
	 */
	public void setGeneratorInfo(String generatorInfo)
	{
		this.generatorInfo = generatorInfo;
	}

	/**
	 * Gette method for generator/item wizard information to support raw serialization.
	 * In fact, this method is a dirty hack:<br>
	 * Castor doesn't support CDATA sections. So we write a dummy string and replace that
	 * dummy string for the actual CDATA-escaped string when writing the xml to the file.
	 * @nowarn
	 */
	public String getGeneratorInfoFilterHack()
	{
		return generatorInfo != null ? "$GENERATOR-INFO$" : null;
	}

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * Sets the model the item belongs to.
	 * @nowarn
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}

	/**
	 * Gets the modified flag for usage by editors.
	 * @nowarn
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * Sets the modified flag for usage by editors.
	 * @nowarn
	 */
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	/**
	 * Sets the modified flag.
	 * @nowarn
	 */
	public void setModified()
	{
		setModified(true);
	}

	/**
	 * Clears the modified flag.
	 * @nowarn
	 */
	public void clearModified()
	{
		setModified(false);
	}
}
