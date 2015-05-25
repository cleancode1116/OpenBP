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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.shellmatcher.ShellMatcher;
import org.openbp.common.util.ByteArrayUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelImpl;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemContainer;
import org.openbp.core.model.item.ItemImpl;
import org.openbp.core.model.item.ItemTypeDescriptor;

/**
 * This class serves as base class for model managers that do not just delegate requests.
 * I. e. these model managers do the actual model persistence work.
 * Examples are the FileSystemModelMgr and the ClassPathModelMgr.
 *
 * This class actually implements the model management logic, its base classes implement
 * the physical access layer.
 *
 * The ModelMgr.ModelPatterns setting in the Server.properties file may contain a list of comma-separated Unix-Shell
 * compatible patterns. If specified, only those models will be loaded that match one of these  patterns.
 * This feature can be used to load only a few of a variety of present models (e. g. for quick testing purposes).
 * Note that the System model will always be loaded.
 *
 * @author Heiko Erhardt
 */
public abstract class NonDelegatingModelMgrBase extends ModelMgrBase
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * List of all models.
	 * Maps model qualifiers ({@link ModelQualifier} objects) to model descriptors ({@link ModelImpl})
	 */
	protected Map allModels = new HashMap();

	/** Model patterns */
	private ShellMatcher[] modelPatterns;

	/** Flag if implementation classses etc\. should be instantiated */
	private boolean instantiateItems = true;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The constructor.
	 */
	public NonDelegatingModelMgrBase()
	{
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
		super.initialize();

		// Get and parse the model name pattern setting from the Server.properties
		String patterns = SettingUtil.getStringSetting("openbp.ModelMgr.ModelPatterns");
		if (patterns != null)
		{
			ArrayList list = new ArrayList();
			for (StringTokenizer st = new StringTokenizer(patterns, ";"); st.hasMoreTokens();)
			{
				list.add(new ShellMatcher(st.nextToken()));
			}
			modelPatterns = (ShellMatcher[]) CollectionUtil.toArray(list, ShellMatcher.class);
		}
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Model access
	//////////////////////////////////////////////////

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model
	 * @throws OpenBPException If the specified model does not exist
	 */
	public synchronized Model getModelByQualifier(ModelQualifier modelQualifier)
	{
		if (getParentModelMgr() != null)
		{
			return getParentModelMgr().getModelByQualifier(modelQualifier);
		}

		Model model = internalGetModelByQualifier(modelQualifier);
		if (model == null)
			throw new ModelException("ObjectNotFound", "Model '" + modelQualifier + "' does not exist");
		return model;
	}

	/**
	 * Gets an (optional) model specified by its qualifier.
	 * Will not throw an exception if the model does not exist.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model getOptionalModelByQualifier(ModelQualifier modelQualifier)
	{
		if (getParentModelMgr() != null)
		{
			return getParentModelMgr().getOptionalModelByQualifier(modelQualifier);
		}

		return internalGetModelByQualifier(modelQualifier);
	}

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model internalGetModelByQualifier(ModelQualifier modelQualifier)
	{
		return (Model) allModels.get(modelQualifier);
	}

	/**
	 * Gets a list of all top level models.
	 *
	 * @return A list of model descriptors ({@link Model} objects) or null
	 */
	public List getModels()
	{
		return CollectionUtil.iteratorToArrayList(allModels.values().iterator());
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Operations on models
	//////////////////////////////////////////////////

	/**
	 * Adds a new model
	 *
	 * @param model Model to add<br>
	 * The name of the model must have been set already.
	 * @return
	 *		true	If the model manager was able to create the model.<br>
	 *		false	If the model manager is read-only or if a model of this type cannot be created by this manager.
	 * @throws OpenBPException If the model could not be created due to an error
	 */
	public synchronized boolean addModel(Model model)
	{
		model.setModelMgr(this);

		// Check if model already exists
		ModelQualifier qualifier = model.getQualifier();
		if (allModels.get(qualifier) != null)
			throw new ModelException("Operation", "Model '" + qualifier + "' already exists");

		addModelToStore(model);

		registerModel(model);

		return true;
	}

	/**
	 * Updates the properties of the model.
	 *
	 * @param model Model to update
	 * @throws OpenBPException If the model could not be updated
	 */
	public void updateModel(Model model)
	{
		// The update is performed by copying the data from the update model
		// to the actual model in order not to loose relations to any dependent
		// objects that might be contained in or referenced by the item

		Model currentModel = getModelByQualifier(model.getQualifier());

		// Update the model item from the argument item
		// Perform a flat copy only, the contained objects will remain the same
		try
		{
			// copyFrom will overwrite the parent model information; save it
			Model parentModel = currentModel.getModel();

			// In contrast to updateItem, we will do a shallow copy here or else all
			// sub models and items will be copied
			currentModel.copyFrom(model, Copyable.COPY_SHALLOW);

			// Repair hiearchy and establish links
			currentModel.setModel(parentModel);
			currentModel.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);
		}
		catch (CloneNotSupportedException e)
		{
			throw new ModelException("Clone", "Cannot update model '" + currentModel.getQualifier() + "': " + e.getMessage());
		}

		// Save changes to the model persistence store
		saveModelToStore(currentModel);
	}

	/**
	 * Deletes a model including all its sub models and the contents (processes, activities)
	 * of these models.
	 *
	 * @param model Model to delete
	 * @throws OpenBPException On error.
	 * Note that depending on the error condition, some or all of the processes and/or sub models
	 * may already have been deleted when the error condition is raised.
	 */
	public synchronized void removeModel(Model model)
	{
		unregisterModel(model);
		removeModelFromStore(model);
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Item access
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
	public Item getItemByQualifier(ModelQualifier qualifier, boolean required)
	{
		if (qualifier.getModel() == null)
			throw new ModelException("Operation", "Missing model name");
		if (qualifier.getItem() == null)
			throw new ModelException("Operation", "Missing component name");

		Model model = null;
		if (required)
		{
			model = getModelByQualifier(ModelQualifier.constructModelQualifier(qualifier.getModel()));
		}
		else
		{
			model = getOptionalModelByQualifier(ModelQualifier.constructModelQualifier(qualifier.getModel()));
		}
		if (model != null)
			return model.getItem(qualifier.getItem(), qualifier.getItemType(), required);
		return null;
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Operations on items
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
	public synchronized void addItem(Model model, Item item, boolean syncGlobalReferences)
	{
		// Add the item to the model if not present already
		boolean exists = model.getItem(item.getName(), item.getItemType(), false) != null;
		if (! exists)
		{
			model.addItem(item);
		}

		// Repair hiearchy and establish links
		if (syncGlobalReferences)
		{
			item.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);

			// Save the item file
			addItemToStore(item);

			// Instantiate classes referenced by the item if we are expected to
			if (isInstantiateItems())
			{
				item.instantiate();
			}
		}
	}

	/**
	 * Updates the properties of an item.
	 * Note that the name of the item may not be changed. Use {@link #moveItem} for this.<br>
	 * The type of the item may never be changed once it was added to the model.
	 *
	 * @param argItem Item to update
	 * @throws OpenBPException If the item could not be updated
	 */
	public synchronized void updateItem(Item argItem)
	{
		// The update is performed by copying the data from the update item
		// to the item contained already in the model in order not to loose
		// relations to any dependent objects that might be contained in
		// or referenced by the item

		// Get the item that belongs to the model
		Item currentItem = getItemByQualifier(argItem.getQualifier(), true);

		// Update the model item from the argument item
		// Perform a flat copy only, the contained objects will remain the same
		try
		{
			// copyFrom will overwrite the parent model information; save it
			Model currentModel = currentItem.getModel();

			// Copy the item data
			currentItem.copyFrom(argItem, Copyable.COPY_DEEP);

			// Repair hiearchy and establish links
			currentItem.setModel(currentModel);
			currentItem.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);
		}
		catch (CloneNotSupportedException e)
		{
			throw new ModelException("Clone", "Cannot update component '" + currentItem.getQualifier() + "': " + e.getMessage());
		}

		// Save the item file
		saveItemToStore(currentItem);

		// Instantiate classes referenced by the item if we are expected to
		if (isInstantiateItems())
		{
			currentItem.instantiate();
		}
	}

	/**
	 * Deletes an item from a model.
	 *
	 * @param item Item to delete
	 * @throws OpenBPException On error
	 */
	public synchronized void removeItem(Item item)
	{
		// Remove item file
		removeItemFromStore(item);

		// Remove the item from the model finally
		Model model = item.getModel();

		model.removeItem(item);
	}

	/**
	 * Moves an item.
	 * This can be used to simply rename an item within a model or to move an item to a new model.
	 *
	 * @param item Item to rename
	 * @param destinationQualifier New name of the item
	 * @throws OpenBPException On error. The item has not been renamed/moved in this case.
	 */
	public void moveItem(Item item, ModelQualifier destinationQualifier)
	{
		String destinationItemName = destinationQualifier.getItem();

		String destinationModelName = destinationQualifier.getModel();
		Model destinationModel = item.getModel();
		if (destinationModelName != null)
			destinationModel = getModelByQualifier(ModelQualifier.constructModelQualifier(destinationModelName));

		if (CommonUtil.equalsNull(item.getName(), destinationItemName) && CommonUtil.equalsNull(item.getModel(), destinationModel))
			return;

		// TODO Fix 5: This does not account any artifacts (e. g. class files) that accompany the item.

		removeItemFromStore(item);

		Model sourceModel = item.getModel();
		sourceModel.removeItem(item);

		if (destinationItemName != null)
			item.setName(destinationItemName);

		destinationModel.addItem(item);

		addItemToStore(item);
	}

	//////////////////////////////////////////////////
	// @@ ModelNotificationObserver implementation
	//////////////////////////////////////////////////

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 */
	public void requestModelReset()
	{
		getMsgContainer().clearMsgs();

		if (SettingUtil.getBooleanSetting(CoreConstants.SYSPROP_RELOAD_ON_MODEL_RESET, false))
		{
			// Entirely reload the models
			readModels();
		}
		else
		{
			// Reset all model class loaders only
			for (Iterator it = allModels.values().iterator(); it.hasNext();)
			{
				Model model = (Model) it.next();
				model.resetModel();
			}
		}

		// Initialize the models
		initializeModels();

		// This is an appropriate place to invoke the garbage collection
		System.gc();

		String errMsg = getMsgContainer().toString();
		if (errMsg != null && errMsg.equals(""))
			errMsg = null;
		getMsgContainer().clearMsgs();

		if (errMsg != null)
			throw new ModelException("ModelValidationFailedAfterreload", "Model validation failed after reload:\n" + errMsg);
	}

	/**
	 * Notification method for model updates.
	 *
	 * @param qualifier Qualifier of the object that has been updated
	 * @param mode Type of model update ({@link ModelNotificationService#ADDED}/{@link ModelNotificationService#UPDATED}/{@link ModelNotificationService#REMOVED})
	 */
	public void modelUpdated(ModelQualifier qualifier, int mode)
	{
		String itemType = qualifier.getItemType();

		if (itemType == null)
		{
			Model model;
			switch (mode)
			{
			case ModelNotificationService.ADDED:
				// Read the model and repair hiearchy, establish links and instantiate the model
				model = readModelFromStore(qualifier);
				if (model != null)
				{
					model.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS | ModelObject.INSTANTIATE_ITEM);
				}
				break;

			case ModelNotificationService.UPDATED:
				model = getOptionalModelByQualifier(qualifier);
				if (model != null)
				{
					reloadModelAfterModelUpdate(model);
				}
				break;

			case ModelNotificationService.REMOVED:
				model = getOptionalModelByQualifier(qualifier);
				if (model != null)
				{
					unregisterModel(model);
				}
				break;
			}
		}
		else
		{
			Item item;
			Model model;
			switch (mode)
			{
			case ModelNotificationService.ADDED:
				item = readItemFromStore(qualifier);
				if (item != null)
				{
					model = item.getModel();
					model.addItem(item);
				}
				break;

			case ModelNotificationService.UPDATED:
				item = getItemByQualifier(qualifier, false);
				if (item != null)
				{
					reloadItemAfterModelUpdate(item);
				}
				break;

			case ModelNotificationService.REMOVED:
				item = getItemByQualifier(qualifier, false);
				if (item != null)
				{
					model = item.getModel();
					model.removeItem(item);
				}
				break;
			}
		}
	}

	/**
	 * Reads the specified model from the model peristence store.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param modelQualifier Fully qualified name of the model to read
	 * @return The new model or null (error messages go to the message container)
	 */
	protected abstract Model readModelFromStore(ModelQualifier modelQualifier);

	/**
	 * Reads the specified item from the model persistence store and adds it to its owning model.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param itemQualifier Fully qualified name of the item to read
	 * @return The new item or null (error messages go to the message container)
	 */
	protected abstract Item readItemFromStore(ModelQualifier itemQualifier);

	/**
	 * Reloads the model after a model update.
	 *
	 * @param model Model to reload
	 */
	protected abstract void reloadModelAfterModelUpdate(Model model);

	/**
	 * Reloads the item after a model update.
	 *
	 * @param item Item to reload
	 */
	protected abstract void reloadItemAfterModelUpdate(Item item);

	/**
	 * Read all models in the model root directory.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public synchronized void readModels()
	{
		allModels.clear();
		modelPatterns = null;

		initialize();

		readModelsFromStore();
	}

	/**
	 * Initializes all models.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public void initializeModels()
	{
		int flag = ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS | ModelObject.VALIDATE_BASIC | ModelObject.VALIDATE_RUNTIME;
		if (isInstantiateItems())
			flag |= ModelObject.INSTANTIATE_ITEM;

		for (Iterator it = allModels.values().iterator(); it.hasNext();)
		{
			Model model = (Model) it.next();

			// After a model has been read, the links to other objects must be established
			// and the objects instantiated if necessary
			model.maintainReferences(flag);

			// TODO Feature 4 Trigger unit event
		}
	}

	/**
	 * Reads all models from the model persistence store.
	 */
	protected abstract void readModelsFromStore();

	/**
	 * Checks if the given model should be loaded or skipped.
	 * A model should be loaded if it is the System model (which will always be loaded) or
	 * its name matchess the model pattern of the model manager.
	 *
	 * @param name Unqualified name of the model to match
	 * @return truf if the model name matches the pattern or no pattern has been defined.
	 */
	protected boolean shouldLoadModel(String name)
	{
		if (! name.equals("System"))
		{
			// Check the model name against the pattern list if defined.
			if (modelPatterns != null)
			{
				for (int ip = 0; ip < modelPatterns.length; ++ip)
				{
					if (modelPatterns[ip].match(name))
						return true;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Serializes a model to a byte array.
	 *
	 * @param model Model to serialize
	 * @return A byte array containing the CDATA element
	 */
	protected byte[] serializeModelToByteArray(Model model)
	{
		// Serialize to a byte array
		ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
		try
		{
			XMLDriver.getInstance().serialize(model, os);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
		}

		// This is the array we would like to copy
		byte[] bytes = os.toByteArray();
		return bytes;
	}

	/**
	 * Serializes an item to a byte array.
	 * This is some kind of hack:
	 * Castor doesn't support CDATA sections. So we write a dummy string and replace that
	 * dummy string for the actual CDATA-escaped string when writing the xml to the file.
	 * See {@link ItemImpl#getGeneratorInfoFilterHack}
	 *
	 * @param item Item to serialize
	 * @return A byte array containing the CDATA element
	 */
	protected byte[] serializeItemToByteArray(Item item)
	{
		Object obj = item;

		ItemTypeDescriptor itd = getItemTypeDescriptor(item.getItemType());
		if (itd.isContainedItem())
		{
			// Item is wrapped by a container class in its xml file
			obj = new ItemContainer(item);
		}

		// Dirty hack:
		// Castor doesn't support CDATA sections. So we write a dummy string and replace that
		// dummy string for the actual CDATA-escaped string when writing the xml to the file.
		// See {@link ItemImpl#getGeneratorInfoFilterHack}

		// Serialize to a byte array
		ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
		try
		{
			XMLDriver.getInstance().serialize(obj, os);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
			throw e;
		}

		// This is the array we would like to copy
		byte[] bytes = os.toByteArray();

		String generatorInfo = item.getGeneratorInfo();
		if (generatorInfo != null)
		{
			try
			{
				String encoding = XMLDriver.getInstance().getEncoding();

				byte[] pattern = new String("$GENERATOR-INFO$").getBytes(encoding);

				String cdata = "<![CDATA[\n" + generatorInfo.trim() + "\n]]>";
				byte[] replacement = cdata.getBytes(encoding);

				bytes = ByteArrayUtil.replaceBytes(bytes, pattern, replacement);
			}
			catch (UnsupportedEncodingException e)
			{
				System.err.println(e);
			}
			catch (XMLDriverException pe)
			{
				throw new ModelException("FileSystemOperation", "Error serializing component '" + item.getQualifier() + "'.", pe);
			}
		}

		// Replace the generator info tag in the string output
		return bytes;
	}

	//////////////////////////////////////////////////
	// @@ ModdelMgr implemenation: Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if implementation classses etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public boolean isInstantiateItems()
	{
		return instantiateItems;
	}

	/**
	 * Sets the flag if implementation classses etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public void setInstantiateItems(boolean instantiateItems)
	{
		this.instantiateItems = instantiateItems;
	}

	//////////////////////////////////////////////////
	// @@ Abstract methods: File operations
	//////////////////////////////////////////////////

	/**
	 * Adds a model to the model persistence store.
	 *
	 * @param model Model to add
	 * @throws OpenBPException On error
	 */
	protected abstract void addModelToStore(Model model);

	/**
	 * Saves a model to the model persistence store.
	 *
	 * @param model Model to save
	 * @throws OpenBPException On error
	 */
	protected abstract void saveModelToStore(Model model);

	/**
	 * Removes a model from the model persistence store.
	 *
	 * @param model Model to remove
	 * @throws OpenBPException On error
	 */
	protected abstract void removeModelFromStore(Model model);

	/**
	 * Adds an item to the model persistence store.
	 *
	 * @param item Item to add
	 * @throws OpenBPException On error
	 */
	protected abstract void addItemToStore(Item item);

	/**
	 * Saves an item to the model persistence store.
	 *
	 * @param item Item to save
	 * @throws OpenBPException On error
	 */
	protected abstract void saveItemToStore(Item item);

	/**
	 * Removes an item from the model persistence store.
	 *
	 * @param item Item to remove
	 * @throws OpenBPException On error
	 */
	protected abstract void removeItemFromStore(Item item);

	//////////////////////////////////////////////////
	// @@ Model registration
	//////////////////////////////////////////////////

	/**
	 * Registers a model.
	 *
	 * @param model Model to register
	 * @throws OpenBPException If the model could not be registered
	 */
	protected void registerModel(Model model)
	{
		model.setModelMgr(this);

		ModelQualifier qualifier = model.getQualifier();

		// Check if model already exists

		// First, check in current model manager
		if (allModels.get(qualifier) != null)
			throw new ModelException("Operation", "Model '" + qualifier + "' already exists");

		// Then, check in other model managers
		if (getParentModelMgr() != null)
		{
			Model existingModel = getParentModelMgr().getOptionalModelByQualifier(qualifier);;
			if (existingModel != null)
			{
				throw new ModelException("Operation", "Model '" + qualifier + "' already loaded by model manager '" + existingModel.getModelMgr().getClass().getName() + "'.");
			}
		}

		// Now that the model descriptor has been written, we can link the new model
		// into the top level/all models list.
		allModels.put(qualifier, model);
	}

	/**
	 * Unregisters a model.
	 *
	 * @param model Model to unregister
	 * @throws OpenBPException If the model could not be unregistered
	 */
	protected void unregisterModel(Model model)
	{
		ModelQualifier qualifier = model.getQualifier();
		allModels.remove(qualifier);
	}
}
