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

import java.util.List;

import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.item.ItemTypes;

/**
 * A model manager is responsible for management and persistence of models and their items.
 * Usually, only one implementation of a model manager exists in a single OpenBP server or cockpit instance.
 *
 * There is a basic implementation, {@link ModelMgrBase} that does not define persistence functions.
 *
 * @author Heiko Erhardt
 */
public interface ModelMgr
	extends ModelNotificationObserver
{
	/**
	 * Gets the parent model manager.
	 * @nowarn
	 */
	public ModelMgr getParentModelMgr();

	/**
	 * Sets the parent model manager.
	 * @nowarn
	 */
	public void setParentModelMgr(ModelMgr parentModelMgr);

	/**
	 * Gets the item type registry.
	 * @nowarn
	 */
	public ItemTypeRegistry getItemTypeRegistry();

	/**
	 * Sets the item type registry.
	 * @nowarn
	 */
	public void setItemTypeRegistry(ItemTypeRegistry itemTypeRegistry);

	/**
	 * Gets the message container for validation error logging.
	 * @nowarn
	 */
	public StandardMsgContainer getMsgContainer();

	/**
	 * Sets the message container for validation error logging.
	 * @nowarn
	 */
	public void setMsgContainer(StandardMsgContainer msgContainer);

	/**
	 * Creates a class loader instance for the given model.
	 * The class loader will be used to load activity and data type classes for this model.
	 *
	 * The default implementation will return the standard class loader.
	 *
	 * @param model The model
	 * @return A new class loader for this type of model
	 * @throws Exception On any error that occurs while scanning the repositories of the model class loader
	 */
	public ClassLoader createModelClassLoader(Model model)
		throws Exception;

	//////////////////////////////////////////////////
	// @@ Model access
	//////////////////////////////////////////////////

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model
	 * @throws OpenBPException If the specified model does not exist
	 */
	public Model getModelByQualifier(ModelQualifier modelQualifier);

	/**
	 * Gets an (optional) model specified by its qualifier.
	 * Will not throw an exception if the model does not exist.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model getOptionalModelByQualifier(ModelQualifier modelQualifier);

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model internalGetModelByQualifier(ModelQualifier modelQualifier);

	/**
	 * Gets a list of all top level models.
	 *
	 * @return A list of model descriptors ({@link Model} objects) or null
	 */
	public List getModels();

	//////////////////////////////////////////////////
	// @@ Operations on models
	//////////////////////////////////////////////////

	/**
	 * Adds a new model.
	 *
	 * @param model Model to add<br>
	 * The name of the model must have been set already.
	 * @return
	 *		true	If the model manager was able to create the model.<br>
	 *		false	If the model manager is read-only or if a model of this type cannot be created by this manager.
	 * @throws OpenBPException If the model could not be created due to an error
	 */
	public boolean addModel(Model model);

	/**
	 * Updates the properties of the model.
	 *
	 * @param model Model to update
	 * @throws OpenBPException If the model could not be updated
	 */
	public void updateModel(Model model);

	/**
	 * Deletes a model including all it's sub models and the contents (processes, activities)
	 * of these models.
	 *
	 * @param model Model to delete
	 * @throws OpenBPException On error.
	 * Note that depending on the error condition, some or all of the processes and/or sub models
	 */
	public void removeModel(Model model);

	//////////////////////////////////////////////////
	// @@ Accessing model items
	//////////////////////////////////////////////////

	/**
	 * Gets a particular model item.
	 * Note that the qualifier must specify an existing item.
	 *
	 * @param qualifier Reference to the item
	 * @param required
	 *		true	Will throw an exception if the item does not exist.<br>
	 *		false	Will return null if the item does not exist.
	 * @return Item descriptors or null if the item does not exist.<br>
	 * Note that the appropriate subclass of the {@link Item} class will be returned.
	 * @throws OpenBPException On error
	 */
	public Item getItemByQualifier(ModelQualifier qualifier, boolean required);

	//////////////////////////////////////////////////
	// @@ Operations on model items (processes, activities, validators, fields etc.)
	//////////////////////////////////////////////////

	/**
	 * Adds an item.
	 * Note that name and type of the item must be set in order to add it to the model.
	 *
	 * @param model Model the item shall belong to
	 * @param item Item to add
	 * @param syncGlobalReferences
	 *		true	Updates the names of external references (e. g. to data types)<br>
	 *		false	Does not perform name updates
	 * @throws OpenBPException If the item could not be added
	 */
	public void addItem(Model model, Item item, boolean syncGlobalReferences);

	/**
	 * Updates the properties of an item.
	 * Note that the name of the item must not be changed. Use {@link #moveItem} for this.<br>
	 * The type of the item may never be changed once it was added to the model.
	 *
	 * @param item Item to update
	 * @throws OpenBPException If the item could not be updated
	 */
	public void updateItem(Item item);

	/**
	 * Deletes an item from a model.
	 *
	 * @param item Item to delete
	 * @throws OpenBPException On error
	 */
	public void removeItem(Item item);

	/**
	 * Moves an item.
	 * This can be used to simply rename an item within a model or to move an item to a new model.
	 *
	 * @param item Item to rename
	 * @param destinationQualifier New name of the item
	 * @throws OpenBPException On error. The item has not been renamed/moved in this case.
	 */
	public void moveItem(Item item, ModelQualifier destinationQualifier);

	//////////////////////////////////////////////////
	// @@ Item types
	//////////////////////////////////////////////////

	/**
	 * Gets a list of item types.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of strings (see the constants of the {@link ItemTypes} class)
	 */
	public String[] getItemTypes(int mode);

	/**
	 * Gets a list of item type descriptors.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of {@link ItemTypeDescriptor} objects or null
	 */
	public ItemTypeDescriptor[] getItemTypeDescriptors(int mode);

	/**
	 * Gets the item type descriptor of a particular item type.
	 *
	 * @param itemType Item type to look for
	 * @return The item type descriptor or null if the model does not support this item type
	 */
	public ItemTypeDescriptor getItemTypeDescriptor(String itemType);

	//////////////////////////////////////////////////
	// @@ Reading models and model items
	//////////////////////////////////////////////////

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize();

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 */
	public void requestModelReset();

	/**
	 * Read all models in the model root directory.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public void readModels();

	/**
	 * Initializes all models.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public void initializeModels();

	//////////////////////////////////////////////////
	// @@ ModdelMgr implemenation: Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if implementation classses etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public boolean isInstantiateItems();

	/**
	 * Sets the flag if implementation classses etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public void setInstantiateItems(boolean instantiateItems);
}
