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
package org.openbp.core.model.modelmgr;

import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.item.ItemTypes;

/**
 * This class serves as a simple base class for all model managers.
 *
 * @author Heiko Erhardt
 */
public abstract class ModelMgrBase
	implements ModelMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Parent model manager */
	private ModelMgr parentModelMgr;

	/** Item type registry */
	private ItemTypeRegistry itemTypeRegistry;

	/** Message container for validation error logging */
	private StandardMsgContainer msgContainer = new StandardMsgContainer();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The constructor.
	 */
	public ModelMgrBase()
	{
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
	}

	/**
	 * Creates a custom class loader instance for the given model.
	 * The class loader will be used to load activity and data type classes for this model.
	 *
	 * The default implementation will return the standard class loader.
	 *
	 * @param model The model
	 * @return A new class loader for this type of model
	 * @throws Exception On any error that occurs while scanning the repositories of the model class loader
	 */
	public ClassLoader createModelClassLoader(Model model)
		throws Exception
	{
		return model.getClass().getClassLoader();
	}

	/**
	 * Gets the parent model manager.
	 * @nowarn
	 */
	public ModelMgr getParentModelMgr()
	{
		return parentModelMgr;
	}

	/**
	 * Sets the parent model manager.
	 * @nowarn
	 */
	public void setParentModelMgr(ModelMgr parentModelMgr)
	{
		this.parentModelMgr = parentModelMgr;
	}

	/**
	 * Gets the item type registry.
	 * @nowarn
	 */
	public ItemTypeRegistry getItemTypeRegistry()
	{
		return itemTypeRegistry;
	}

	/**
	 * Sets the item type registry.
	 * @nowarn
	 */
	public void setItemTypeRegistry(ItemTypeRegistry itemTypeRegistry)
	{
		this.itemTypeRegistry = itemTypeRegistry;
	}

	/**
	 * Gets the message container for validation error logging.
	 * @nowarn
	 */
	public StandardMsgContainer getMsgContainer()
	{
		return msgContainer;
	}

	/**
	 * Sets the message container for validation error logging.
	 * @nowarn
	 */
	public void setMsgContainer(StandardMsgContainer msgContainer)
	{
		this.msgContainer = msgContainer;
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Item types
	//////////////////////////////////////////////////

	/**
	 * Gets a list of item types.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of strings (see the constants of the {@link ItemTypes} class)
	 */
	public String[] getItemTypes(int mode)
	{
		return itemTypeRegistry.getItemTypes(mode);
	}

	/**
	 * Gets a list of item type descriptors.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of {@link ItemTypeDescriptor} objects or null
	 */
	public ItemTypeDescriptor[] getItemTypeDescriptors(int mode)
	{
		return itemTypeRegistry.getItemTypeDescriptors(mode);
	}

	/**
	 * Gets the item type descriptor of a particular item type.
	 *
	 * @param itemType Item type to look for
	 * @return The item type descriptor or null if the model does not support this item type
	 */
	public ItemTypeDescriptor getItemTypeDescriptor(String itemType)
	{
		return itemTypeRegistry.getItemTypeDescriptor(itemType);
	}
}
