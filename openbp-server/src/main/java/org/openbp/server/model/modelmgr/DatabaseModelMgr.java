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
package org.openbp.server.model.modelmgr;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Iterator;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelImpl;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemContainer;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.modelmgr.NonDelegatingModelMgrBase;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceContextProvider;
import org.openbp.server.persistence.PersistenceException;
import org.openbp.server.persistence.PersistenceQuery;
import org.openbp.server.persistence.TransactionGuard;

/**
 * The database model manager provides access to models that are located in the database.
 *
 * @author Heiko Erhardt
 */
public class DatabaseModelMgr extends NonDelegatingModelMgrBase
{
	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	/**
	 * Private constructor.
	 */
	public DatabaseModelMgr()
	{
	}

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Sets the persistence context provider.
	 * @nowarn
	 */
	public void setPersistenceContextProvider(PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
	}

	//////////////////////////////////////////////////
	// @@ ModelMgrBase overrides: Class loader management
	//////////////////////////////////////////////////

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
		throws Exception
	{
		/*
		XClassLoaderConfiguration config = new XClassLoaderConfiguration();

		// Set the name of the class loader.
		String qualifier = model.getQualifier().toString();
		qualifier = qualifier.replace(ModelQualifier.PATH_DELIMITER_CHAR, '.');
		config.setName("Model" + qualifier);

		config.addRepository(ModelLocationUtil.expandModelLocation(model, ModelLocationUtil.DIR_CLASSES));
		config.addRepository(ModelLocationUtil.expandModelLocation(model, ModelLocationUtil.DIR_TARGET_CLASSES));
		config.addRepository(ModelLocationUtil.expandModelLocation(model, ModelLocationUtil.DIR_LIB));
		config.addRepository(ModelLocationUtil.expandModelLocation(model, ModelLocationUtil.DIR_EXTLIB));

		// Use the class load that loaded the ModelImpl class as parent class loader of the model.
		config.setParentClassLoader(model.getClass().getClassLoader());

		// Create a class loader for this model
		return new ModelClassLoader(config, model);
		 */
		return super.createModelClassLoader(model);
	}

	//////////////////////////////////////////////////
	// @@ ModelMgrBase overrides: File operations and reloading
	//////////////////////////////////////////////////

	/**
	 * Adds a model to the model persistence store.
	 *
	 * @param model Model to add
	 * @throws OpenBPException On error
	 */
	protected void addModelToStore(Model model)
	{
		// Create reference names for persistent storage
		model.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModel dbModel = (DbModel) pc.createObject(DbModel.class);

			modelToDbModel(model, dbModel);

			pc.saveObject(dbModel);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error saving descriptor of model '" + model.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Saves a model to the model persistence store.
	 *
	 * @param model Model to save
	 * @throws OpenBPException On error
	 */
	protected void saveModelToStore(Model model)
	{
		// Create reference names for persistent storage
		model.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModel dbModel = findDbModel(pc, model.getName());

			modelToDbModel(model, dbModel);

			pc.saveObject(dbModel);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error saving descriptor of model '" + model.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Adds an item to the model persistence store.
	 *
	 * @param item Item to add
	 * @throws OpenBPException On error
	 */
	protected void addItemToStore(Item item)
	{
		// Create reference names for persistent storage
		item.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModelItem dbModelItem = (DbModelItem) pc.createObject(DbModelItem.class);

			itemToDbModelItem(item, dbModelItem);

			pc.saveObject(dbModelItem);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error saving descriptor of component '" + item.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Saves an item to the model persistence store.
	 *
	 * @param item Item to save
	 * @throws OpenBPException On error
	 */
	protected void saveItemToStore(Item item)
	{
		// Create reference names for persistent storage
		item.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModelItem dbModelItem = findDbModelItem(pc, item);

			itemToDbModelItem(item, dbModelItem);

			pc.saveObject(dbModelItem);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error saving descriptor of component '" + item.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Removes a model from the model persistence store.
	 *
	 * @param model Model to remove
	 * @throws OpenBPException On error
	 */
	protected void removeModelFromStore(Model model)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			String sql = "DELETE FROM OPENBPMODELITEM WHERE MI_MODEL_NAME = '" + model.getName() + "'";
			pc.executeUpdateOrDelete(sql);

			sql = "DELETE FROM OPENBPMODEL WHERE MO_NAME = '" + model.getName() + "'";
			pc.executeUpdateOrDelete(sql);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error removing model '" + model.getQualifier() + "' from the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Removes an item from the model persistence store.
	 *
	 * @param item Item to remove
	 * @throws OpenBPException On error
	 */
	protected void removeItemFromStore(Item item)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModelItem dbModelItem = findDbModelItem(pc, item);

			pc.deleteObject(dbModelItem);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error saving descriptor of component '" + item.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ Reading models and model items
	//////////////////////////////////////////////////

	/**
	 * Reads all models from the model persistence store.
	 *
	 * Any errors will be logged to the message container of this class.
	 */
	protected void readModelsFromStore()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			readModelsFromDatabase(pc);
			readItemsFromDatabase(pc);
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error loading models from the database: " + e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	private void readModelsFromDatabase(PersistenceContext pc)
	{
		PersistenceQuery query = pc.createQuery(DbModel.class);
		Collection result = pc.runQuery(query);
		for (Iterator it = CollectionUtil.iterator(result); it.hasNext();)
		{
			DbModel dbModel = (DbModel) it.next();

			if (! shouldLoadModel(dbModel.getName()))
				continue;

			Model model = dbModelToModel(dbModel);

			// Register the model
			try
			{
				registerModel(model);
			}
			catch (ModelException e)
			{
				getMsgContainer().addMsg(null, "Error registering model $0 in model manager $1", new Object[]
				{
					model.getName(), getClass().getName(), e
				});
				continue;
			}

			LogUtil.info(getClass(), "Loaded model $0.", model.getQualifier());
		}
	}

	private void readItemsFromDatabase(PersistenceContext pc)
	{
		PersistenceQuery query = pc.createQuery(DbModelItem.class);
		Collection result = pc.runQuery(query);
		for (Iterator it = CollectionUtil.iterator(result); it.hasNext();)
		{
			DbModelItem dbModelItem = (DbModelItem) it.next();

			if (! shouldLoadModel(dbModelItem.getModelName()))
				continue;

			registerDbModelItem(dbModelItem, null);
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
	protected Model readModelFromStore(ModelQualifier modelQualifier)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModel dbModel = findDbModel(pc, modelQualifier.getModel());

			Model model = dbModelToModel(dbModel);

			// Register the model
			try
			{
				registerModel(model);
			}
			catch (ModelException e)
			{
				getMsgContainer().addMsg(null, "Error registering model $0 in model manager $1", new Object[]
				{
					model.getName(), getClass().getName(), e
				});
				return null;
			}

			PersistenceQuery query = pc.createQuery(DbModelItem.class);
			query.eq("modelName", model.getName());
			Collection result = pc.runQuery(query);
			for (Iterator it = CollectionUtil.iterator(result); it.hasNext();)
			{
				DbModelItem dbModelItem = (DbModelItem) it.next();

				registerDbModelItem(dbModelItem, model);
			}

			return model;
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error loading model from the database: " + e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Reloads the model after a model update.
	 *
	 * @param model Model to reload
	 */
	protected void reloadModelAfterModelUpdate(Model model)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		try
		{
			DbModel dbModel = findDbModel(pc, model.getName());
			pc.refreshObject(dbModel);

			Model newModel = dbModelToModel(dbModel);
			try
			{
				model.copyFrom(newModel, Copyable.COPY_SHALLOW);
			}
			catch (CloneNotSupportedException e)
			{
				// Doesn't happen
			}

			// Repair hiearchy, establish links and instantiate the model
			model.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS | ModelObject.INSTANTIATE_ITEM);
		}
		catch (PersistenceException e)
		{
			pc.rollbackTransaction();
			pc.release();
			throw new ModelException("DatabaseOperation", "Error reloading descriptor of model '" + model.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
	}

	/**
	 * Reloads the item after a model update.
	 *
	 * @param item Item to reload
	 */
	protected void reloadItemAfterModelUpdate(Item item)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		try
		{
			DbModelItem dbModelItem = findDbModelItem(pc, item);
			pc.refreshObject(dbModelItem);

			Item newItem = dbModelItemToItem(dbModelItem);
			try
			{
				item.copyFrom(newItem, Copyable.COPY_SHALLOW);
			}
			catch (CloneNotSupportedException e)
			{
				// Doesn't happen
			}

			// Repair hiearchy, establish links and instantiate the model
			item.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS | ModelObject.INSTANTIATE_ITEM);
		}
		catch (PersistenceException e)
		{
			pc.rollbackTransaction();
			pc.release();
			throw new ModelException("DatabaseOperation", "Error reloading descriptor of component '" + item.getQualifier() + "' to the database: "
				+ e.getMessage(), e);
		}
	}

	/**
	 * Reads the specified item from the model persistence store and adds it to its owning model.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param itemQualifier Fully qualified name of the item to read
	 * @return The new item or null (error messages go to the message container)
	 */
	protected Item readItemFromStore(ModelQualifier itemQualifier)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		TransactionGuard tg = new TransactionGuard(pc);
		try
		{
			DbModelItem dbModelItem = findDbModelItem(pc, itemQualifier.getModel(), itemQualifier.getItem(), itemQualifier.getItemType());

			Item item = registerDbModelItem(dbModelItem, null);

			return item;
		}
		catch (PersistenceException e)
		{
			tg.doCatch();
			throw new ModelException("DatabaseOperation", "Error loading models from the database: " + e.getMessage(), e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private Model dbModelToModel(DbModel dbModel)
	{
		ByteArrayInputStream in = new ByteArrayInputStream(dbModel.getXml().getBytes());
		ModelImpl model = (ModelImpl) XMLDriver.getInstance().deserializeStream(ModelImpl.class, in);
		return model;
	}

	private void modelToDbModel(Model model, DbModel dbModel)
	{
		dbModel.setName(model.getName());

		byte[] bytes = serializeModelToByteArray(model);
		String xml = new String(bytes);
		dbModel.setXml(xml);
	}

	private Item dbModelItemToItem(DbModelItem dbModelItem)
	{
		ItemTypeDescriptor itd = getItemTypeDescriptor(dbModelItem.getItemType());
		ByteArrayInputStream in = new ByteArrayInputStream(dbModelItem.getXml().getBytes());

		Item item = null;

		// Deserialize item descriptor file
		try
		{
			if (itd.isContainedItem())
			{
				// Item is wrapped by a container class in its xml file
				ItemContainer container = (ItemContainer) XMLDriver.getInstance().deserializeStream(ItemContainer.class, in);
				item = container.getItem();
			}
			else
			{
				item = (Item) XMLDriver.getInstance().deserializeStream(itd.getItemClass(), in);
			}
		}
		catch (XMLDriverException e)
		{
			getMsgContainer().addMsg(item, "Error reading component descriptor from database for component $0.", new Object[]
			{
				new ModelQualifier(dbModelItem.getModelName(), dbModelItem.getItemName(), dbModelItem.getItemType()).toUntypedString(), e
			});
			return null;
		}

		// Explicitely make this item an item of the specified type
		item.setItemType(itd.getItemType());

		return item;
	}

	private void itemToDbModelItem(Item item, DbModelItem dbModelItem)
	{
		dbModelItem.setModelName(item.getModel().getName());
		dbModelItem.setItemName(item.getName());
		dbModelItem.setItemType(item.getItemType());

		byte[] bytes = serializeItemToByteArray(item);
		String xml = new String(bytes);
		dbModelItem.setXml(xml);
	}

	private Item registerDbModelItem(DbModelItem dbModelItem, Model owningModel)
	{
		Item item = dbModelItemToItem(dbModelItem);
		if (owningModel == null)
		{
			owningModel = internalGetModelByQualifier(new ModelQualifier(dbModelItem.getModelName(), null, null));
		}

		// Register the model
		try
		{
			owningModel.addItem(item);
		}
		catch (ModelException e)
		{
			getMsgContainer().addMsg(owningModel, "Error adding component $0 to model $1 in model manager $2.", new Object[]
			{
				item.getName(), owningModel.getName(), getClass().getName(), e
			});
		}

		return item;
	}

	private DbModel findDbModel(PersistenceContext pc, String name)
	{
		PersistenceQuery query = pc.createQuery(DbModel.class);
		query.eq("name", name);
		Collection result = pc.runQuery(query);
		Iterator it = result.iterator();
		if (! it.hasNext())
			throw new ModelException("DatabaseOperation", "Model '" + name + "' not found. Maybe the model has been deleted from the database.");
		DbModel dbModel = (DbModel) it.next();
		return dbModel;
	}

	private DbModelItem findDbModelItem(PersistenceContext pc, Item item)
	{
		return findDbModelItem(pc, item.getModel().getName(), item.getName(), item.getItemType());
	}

	private DbModelItem findDbModelItem(PersistenceContext pc, String modelName, String itemName, String itemType)
	{
		PersistenceQuery query = pc.createQuery(DbModelItem.class);
		query.eq("modelName", modelName);
		query.eq("itemName", itemName);
		query.eq("itemType", itemType);
		Collection result = pc.runQuery(query);
		Iterator it = result.iterator();
		if (! it.hasNext())
			throw new ModelException("DatabaseOperation", "Component '" + new ModelQualifier(modelName, itemName, itemType).toUntypedString()
				+ "' not found. Maybe the component has been deleted from the database.");
		DbModelItem dbModelItem = (DbModelItem) it.next();
		return dbModelItem;
	}
}
