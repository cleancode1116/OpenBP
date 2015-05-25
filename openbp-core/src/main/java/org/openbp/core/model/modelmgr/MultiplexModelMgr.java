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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;

/**
 * The multiplex model manager combines several model managers.
 *
 * @author Heiko Erhardt
 */
public class MultiplexModelMgr extends ModelMgrBase
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Supported model managers */
	private ModelMgr[] mgrs;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public MultiplexModelMgr()
	{
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].setParentModelMgr(this);
			mgrs[i].setMsgContainer(getMsgContainer());
			mgrs[i].setItemTypeRegistry(getItemTypeRegistry());
			mgrs[i].initialize();
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the supported model managers.
	 * @nowarn
	 */
	public ModelMgr[] getManagers()
	{
		return mgrs;
	}

	/**
	 * Sets the supported model managers.
	 * @nowarn
	 */
	public void setManagers(ModelMgr[] mgrs)
	{
		this.mgrs = mgrs;
	}

	/**
	 * Sets the supported model managers.
	 * @nowarn
	 */
	public void setManagers(List mgrList)
	{
		this.mgrs = (ModelMgr[]) CollectionUtil.toArray(mgrList, ModelMgr.class);
	}

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
	public Model getModelByQualifier(ModelQualifier modelQualifier)
	{
		Model ret = null;
		for (int i = 0; i < mgrs.length; ++i)
		{
			ret = mgrs[i].internalGetModelByQualifier(modelQualifier);
			if (ret != null)
				break;
		}
		if (ret == null)
			throw new ModelException("ObjectNotFound", "Model '" + modelQualifier + "' does not exist");
		return ret;
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
		Model ret = null;
		for (int i = 0; i < mgrs.length; ++i)
		{
			ret = mgrs[i].internalGetModelByQualifier(modelQualifier);
			if (ret != null)
				break;
		}
		return ret;
	}

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model internalGetModelByQualifier(ModelQualifier modelQualifier)
	{
		return null;
	}

	/**
	 * Gets a list of all top level models.
	 *
	 * @return A list of model descriptors ({@link Model} objects) or null
	 */
	public List getModels()
	{
		ArrayList ret = new ArrayList();
		for (int i = 0; i < mgrs.length; ++i)
		{
			List l = mgrs[i].getModels();
			if (l != null)
			{
				ret.addAll(l);
			}
		}
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Operations on models
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
	public boolean addModel(Model model)
	{
		for (int i = 0; i < mgrs.length; ++i)
		{
			if (mgrs[i].addModel(model))
				return true;
		}
		throw new ModelException("Operation", "Model '" + model.getName() + "' could not be created by any of the available model managers.");
	}

	/**
	 * Updates the properties of the model.
	 *
	 * @param model Model to update
	 * @throws OpenBPException If the model could not be updated
	 */
	public void updateModel(Model model)
	{
		getModelMgr(model).updateModel(model);
	}

	/**
	 * Deletes a model including all it's sub models and the contents (processes, activities)
	 * of these models.
	 *
	 * @param model Model to delete
	 * @throws OpenBPException On error.
	 * Note that depending on the error condition, some or all of the processes and/or sub models
	 */
	public void removeModel(Model model)
	{
		getModelMgr(model).removeModel(model);
	}

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
	public Item getItemByQualifier(ModelQualifier qualifier, boolean required)
	{
		if (qualifier.getModel() == null)
			throw new ModelException("Operation", "Missing model name");
		if (qualifier.getItem() == null)
			throw new ModelException("Operation", "Missing component name");

		Model model = getModelByQualifier(ModelQualifier.constructModelQualifier(qualifier.getModel()));
		return model.getItem(qualifier.getItem(), qualifier.getItemType(), required);
	}

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
	public void addItem(Model model, Item item, boolean syncGlobalReferences)
	{
		getModelMgr(model).addItem(model, item, syncGlobalReferences);
	}

	/**
	 * Updates the properties of an item.
	 * Note that the name of the item must not be changed. Use {@link #moveItem} for this.<br>
	 * The type of the item may never be changed once it was added to the model.
	 *
	 * @param item Item to update
	 * @throws OpenBPException If the item could not be updated
	 */
	public void updateItem(Item item)
	{
		getModelMgr(item).updateItem(item);
	}

	/**
	 * Deletes an item from a model.
	 *
	 * @param item Item to delete
	 * @throws OpenBPException On error
	 */
	public void removeItem(Item item)
	{
		getModelMgr(item).removeItem(item);
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
		getModelMgr(item).moveItem(item, destinationQualifier);
	}

	//////////////////////////////////////////////////
	// @@ Model updates
	//////////////////////////////////////////////////

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 */
	public void requestModelReset()
	{
		String errMsg = null;
		getMsgContainer().clearMsgs();

		if (SettingUtil.getBooleanSetting(CoreConstants.SYSPROP_RELOAD_ON_MODEL_RESET, false))
		{
			// Entirely reload the models
			for (int i = 0; i < mgrs.length; ++i)
			{
				mgrs[i].readModels();
			}
		}
		else
		{
			// Reset all model class loaders only
			for (Iterator it = getModels().iterator(); it.hasNext();)
			{
				Model model = (Model) it.next();
				model.resetModel();
			}
		}

		// Initialize the models
		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].initializeModels();
		}

		// This is an appropriate place to invoke the garbage collection
		System.gc();

		errMsg = getMsgContainer().toString();
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
		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].modelUpdated(qualifier, mode);
		}
	}

	//////////////////////////////////////////////////
	// @@ Reading models and model items
	//////////////////////////////////////////////////

	/**
	 * Read all models in the model root directory.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public void readModels()
	{
		initialize();

		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].readModels();
		}
	}

	/**
	 * Initializes all models.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	public void initializeModels()
	{
		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].initializeModels();
		}
	}

	//////////////////////////////////////////////////
	// @@ ModdelMgr implemenation: Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if implementation classes etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public boolean isInstantiateItems()
	{
		for (int i = 0; i < mgrs.length; ++i)
		{
			if (mgrs[i].isInstantiateItems())
				return true;
		}
		return false;
	}

	/**
	 * Sets the flag if implementation classses etc\. should be instantiated.
	 * This is done when running on the server side (default).
	 * The client usually doesn't have to instantiate the items.
	 * @nowarn
	 */
	public void setInstantiateItems(boolean instantiateItems)
	{
		for (int i = 0; i < mgrs.length; ++i)
		{
			mgrs[i].setInstantiateItems(instantiateItems);
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private ModelMgr getModelMgr(Model model)
	{
		ModelMgr mgr = model.getModelMgr();
		if (mgr == null)
			throw new ModelException("Internal", "Model '" + model.getQualifier() + "' has not model mgr reference.");
		return mgr;
	}

	private ModelMgr getModelMgr(Item item)
	{
		return getModelMgr(item.getModel());
	}
}
