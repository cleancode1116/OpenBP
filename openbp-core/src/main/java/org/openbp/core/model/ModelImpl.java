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
package org.openbp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogLevel;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.iterator.CascadeIterator;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemImpl;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.modelmgr.ModelMgr;

/**
 * Implementation of a OpenBP model.
 * Note that the methods of this implementation do not perform any file system i/o.<br>
 * For this, use the {@link ModelMgr} class.
 *
 * @author Heiko Erhardt
 */
public class ModelImpl extends ItemImpl
	implements Model
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * List of names of models to import (contains String objects).
	 * Because a list of strings cannot be edited directly by the property browser, the methods
	 * {@link #getImportPBContainerList} and {@link #setImportPBContainerList}
	 * have been defined that use a container class that contains a single import model name.
	 */
	private List importList;

	/** Default package to use for source files of this model */
	private String defaultPackage;

	/** Directory the model resides in */
	private String modelPath;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Table of item types.
	 * Maps item types (Strings, see {@link ItemTypes}) to HashMaps of items ({@link ItemImpl})
	 * The item type list is lazy-loaded, i. e. it will be null after the object has been deserialized.
	 */
	private transient Map itemTable;

	/** List of imported models (contains {@link Model} objects) */
	private transient List importedModelList;

	/** Class loader that is used to load activity and bean classes */
	private transient ClassLoader classLoader;

	/**
	 * List of {@link ModelImportPBContainer} objects.
	 * This list resembles the imported models in a form that can be displayed and edited in the property browser.
	 */
	private transient List importPBContainerList;

	/** Flag: Import container list was created */
	private transient boolean importContainerListCreated;

	/** Reference helper */
	private transient final ModelReferenceHelper referenceHelper;

	/** Model manager that loaded this model */
	private transient ModelMgr modelMgr;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ModelImpl()
	{
		setItemType(ItemTypes.MODEL);
		internalResetItems();

		referenceHelper = new ModelReferenceHelper(this);
	}

	public void internalResetItems()
	{
		itemTable = new HashMap(ItemTypes.getValues().length);
	}

	/**
	 * Resets the model.
	 * Re-initializes the model classloader and reinitializes the components of the model.
	 */
	public void resetModel()
	{
		// Perform regular model shutdown
		shutdownModel();

		classLoader = null;
	}

	/**
	 * Shuts down the model.
	 * This method will be called before the OpenBP Server will exit or before a model is being unloaded.
	 * It will notify all observers about the model shutdown.
	 */
	public void shutdownModel()
	{
	}

	/**
	 * Gets the class loader that is used to load activity and bean classes.
	 * @nowarn
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * Sets the class loader that is used to load activity and bean classes.
	 * @nowarn
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

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

		ModelImpl src = (ModelImpl) source;

		modelPath = src.modelPath;
		defaultPackage = src.defaultPackage;
		modelMgr = src.modelMgr;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			importList = (List) CopyUtil.copyCollection(src.importList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
			importedModelList = (List) CopyUtil.copyCollection(src.importedModelList, CopyUtil.CLONE_NONE);
		}
		else
		{
			importList = src.importList;
			importedModelList = src.importedModelList;
		}

		if (copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of lists and maps; copy values only if deep copy, not keys
			itemTable = CopyUtil.copyMap(src.itemTable, CopyUtil.CLONE_VALUES);

			// Link dependent object to this object
			maintainReferences(0);
		}
		else
		{
			// Prevent values in the destination object from being overwritten by null values
			// caused by the transient attribute of the members if not doing a deep copy
			if (src.itemTable != null)
				itemTable = src.itemTable;
		}

		// Prevent values in the destination object from being overwritten by null values
		// caused by the transient attribute of the members if not doing a deep copy
		if (src.classLoader != null)
			classLoader = src.classLoader;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the model the item belongs to.
	 * A model always owns itself (though it may have a parent model),
	 * so this method will return this.
	 * @nowarn
	 */
	public Model getOwningModel()
	{
		return this;
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the sub models and all items of the model.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		List list = new ArrayList();

		// Add all items
		for (Iterator it = getItems(null); it.hasNext();)
		{
			Object o = it.next();
			list.add(o);
		}

		return list;
	}

	/**
	 * Gets the reference to the model.
	 * @return The qualified name ("modelA.modelB.thisModel")
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(this);
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if ((flag & INSTANTIATE_ITEM) != 0)
		{
			try
			{
				classLoader = modelMgr.createModelClassLoader(this);
			}
			catch (Exception e)
			{
				getModelMgr().getMsgContainer().addMsg(this, "Error instantiating model class loader.", new Object [] { e });
				return;
			}
		}

		if ((flag & RESOLVE_GLOBAL_REFS) != 0)
		{
			List models = null;

			for (Iterator it = getImports(); it.hasNext();)
			{
				String modelName = (String) it.next();
				modelName = ModelQualifier.normalizeModelName(modelName);

				Model model = getModelMgr().getOptionalModelByQualifier(ModelQualifier.constructModelQualifier(modelName));
				if (model == null)
				{
					getModelMgr().getMsgContainer().addMsg(LogLevel.WARN, this, "Cannot resolve imported model {0}.", new Object[]
					{
						modelName
					});
					continue;
				}

				if (models == null)
					models = new ArrayList();
				models.add(model);
			}

			setImportedModelList(models);
		}

		for (Iterator itTypes = itemTable.values().iterator(); itTypes.hasNext();)
		{
			Map items = (Map) itTypes.next();
			for (Iterator itItems = items.values().iterator(); itItems.hasNext();)
			{
				Item item = (Item) itItems.next();

				item.setModel(this);

				item.maintainReferences(flag);

				if ((flag & INSTANTIATE_ITEM) != 0)
				{
					// Instantiate classes referenced by the item
					item.instantiate();
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * Sets the model the item belongs to.
	 * @nowarn
	 */
	public void setModel(Model model)
	{
		if (getModel() != model)
		{
			super.setModel(model);
		}
	}

	/**
	 * Sets the name of the model.
	 * Also updates the qualfied name.
	 *
	 * @param name Model name
	 */
	public void setName(String name)
	{
		if (getName() != name)
		{
			super.setName(name);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the directory the model resides in.
	 * Actually, this is some arbitrary model storage specification, depending on the type of {@link ModelMgr} that read the model.
	 * @nowarn
	 */
	public String getModelPath()
	{
		return modelPath;
	}

	/**
	 * Sets the directory the model resides in.
	 * Actually, this is some arbitrary model storage specification, depending on the type of {@link ModelMgr} that read the model.
	 * @nowarn
	 */
	public void setModelPath(String modelPath)
	{
		this.modelPath = modelPath;
	}

	/**
	 * Gets the list of imported models.
	 * @return An iterator of {@link Model} objects
	 */
	public Iterator getImportedModels()
	{
		if (importedModelList == null)
			return EmptyIterator.getInstance();
		return importedModelList.iterator();
	}

	/**
	 * Adds an imported model.
	 * @param importedModel The imported model to add
	 */
	public void addImportedModel(Model importedModel)
	{
		if (importedModelList == null)
			importedModelList = new ArrayList();
		importedModelList.add(importedModel);
	}

	/**
	 * Gets the list of imported models.
	 * @return A list of {@link Model} objects
	 */
	public List getImportedModelList()
	{
		return importedModelList;
	}

	/**
	 * Sets the list of imported models.
	 * @param importedModelList A list of {@link Model} objects
	 */
	public void setImportedModelList(List importedModelList)
	{
		this.importedModelList = importedModelList;
	}

	//////////////////////////////////////////////////
	// @@ Object browsing support
	// The following methods provide support for object browsing and map to the properties of this class.
	//////////////////////////////////////////////////

	/**
	 * Gets the list of names of models to import.
	 * @return An iterator of String objects
	 */
	public Iterator getImports()
	{
		if (importList == null)
			return EmptyIterator.getInstance();
		return importList.iterator();
	}

	/**
	 * Adds an import.
	 * @param imp The import to add
	 */
	public void addImport(String imp)
	{
		if (importList == null)
			importList = new ArrayList();
		importList.add(imp);
	}

	/**
	 * Gets the list of names of models to import.
	 * @return A list of String objects
	 */
	public List getImportList()
	{
		return importList;
	}

	/**
	 * Sets the list of names of models to import.
	 * @param importList A list of String objects
	 */
	public void setImportList(List importList)
	{
		this.importList = importList;
	}

	/**
	 * Creates a new property browser import container list element.
	 * Used by the property browser if a new node is to be added.
	 *
	 * @return An empty import container
	 */
	public ModelImportPBContainer createImportPBContainerListElement()
	{
		return new ModelImportPBContainer();
	}

	/**
	 * Gets the property browser import container list.
	 * This list resembles the imported models in a form that can be displayed and edited
	 * in the property browser.
	 * @return A list of {@link ModelImportPBContainer} objects
	 */
	public List getImportPBContainerList()
	{
		if (! importContainerListCreated)
		{
		// Create the import container list from the model imports
		if (importedModelList != null)
		{
			importPBContainerList = new ArrayList();

			int n = importedModelList.size();
			for (int i = 0; i < n; ++i)
			{
				Model model = (Model) importedModelList.get(i);
				importPBContainerList.add(new ModelImportPBContainer(model));
			}
		}
		else
		{
			importPBContainerList = null;
			}

			importContainerListCreated = true;
		}

		return importPBContainerList;
	}

	/**
	 * Sets the property browser import container list.
	 * @param importPBContainerList A list of {@link ModelImportPBContainer} objects
	 */
	public void setImportPBContainerList(List importPBContainerList)
	{
		// The actual import list will be updated by the ModelImplValidator class
		this.importPBContainerList = importPBContainerList;
		importContainerListCreated = true;
	}

	/**
	 * Gets the flag: Import container list was created.
	 * @nowarn
	 */
	public boolean isImportContainerListCreated()
	{
		return importContainerListCreated;
	}

	/**
	 * Sets the flag: Import container list was created.
	 * @nowarn
	 */
	public void setImportContainerListCreated(boolean importContainerListCreated)
	{
		this.importContainerListCreated = importContainerListCreated;
	}

	/**
	 * Gets the default package to use for source files of this model.
	 * @nowarn
	 */
	public String getDefaultPackage()
	{
		return defaultPackage;
	}

	/**
	 * Sets the default package to use for source files of this model.
	 * @nowarn
	 */
	public void setDefaultPackage(String defaultPackage)
	{
		this.defaultPackage = defaultPackage;
	}

	/**
	 * Gets the model manager that loaded this model.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		return modelMgr;
	}

	/**
	 * Sets the model manager that loaded this model.
	 * @nowarn
	 */
	public void setModelMgr(ModelMgr modelMgr)
	{
		this.modelMgr = modelMgr;
	}

	//////////////////////////////////////////////////
	// @@ Accessing model items
	//////////////////////////////////////////////////

	/**
	 * Gets a particular model item.
	 * The item list is lazy-loaded, i. e. it will be null after the object has been deserialized.
	 *
	 * @param itemName Name of or referent to the item
	 * @param itemType Type of the item (see the constants of the {@link ItemTypes} class)
	 * @param required
	 *		true	Will throw an exception if the item does not exist.<br>
	 *		false	Will return null if the item does not exist.
	 * @return Item descriptors or null if the item does not exist.<br>
	 * Note that the appropriate subclass of the {@link Item} class will be returned.
	 * @throws OpenBPException If the item does not exist
	 */
	public Item getItem(String itemName, String itemType, boolean required)
	{
		Item item = null;

		Map items = (Map) itemTable.get(itemType);
		if (items != null)
			item = (Item) items.get(itemName);

		if (item == null && required)
			throw new ModelException("ObjectNotFound", "Component '" + itemName + "' does not exist in model '" + getQualifier() + "'");
		return item;
	}

	/**
	 * Gets the names of all items of the specified type of the specified model.
	 * The item list is lazy-loaded, i. e. it will be null after the object has been deserialized.
	 *
	 * @param itemType Type of the items (see the constants of the {@link ItemTypes} class)
	 * @return Iterator of item names (String objects)
	 */
	public Iterator getItemNames(String itemType)
	{
		Map items = (Map) itemTable.get(itemType);
		if (items != null)
			return items.keySet().iterator();
		return EmptyIterator.getInstance();
	}

	/**
	 * Gets a list of item descriptors of the specified type of the specified model.
	 * The item list is lazy-loaded, i. e. it will be null after the object has been deserialized.
	 *
	 * @param itemType Type of the items (see the constants of the {@link ItemTypes} class).
	 * If the item type is null, the method will return an iterator over all items, regardless of their type.
	 * @return Iterator of item descriptors ({@link Item} objects)
	 */
	public Iterator getItems(String itemType)
	{
		if (itemType == null)
			// Iterate over all items recursively; the cascade iterator will return
			// an iterator of all leaf nodes in a tree-like structure.
			return new CascadeIterator().iterator(itemTable);

		Map items = (Map) itemTable.get(itemType);
		if (items != null)
			return items.values().iterator();

		return EmptyIterator.getInstance();
	}

	/**
	 * Gets the default process of this model.
	 *
	 * @return The process or null if there is no process that has the default process flag
	 * ({@link ProcessItem#setDefaultProcess}) set
	 */
	public ProcessItem getDefaultProcess()
	{
		for (Iterator it = getItems(ItemTypes.PROCESS); it.hasNext();)
		{
			ProcessItem process = (ProcessItem) it.next();
			if (process.isDefaultProcess())
				return process;
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Item management
	//////////////////////////////////////////////////

	/**
	 * Adds a new item.
	 *
	 * @param item Item to add
	 * @throws OpenBPException If an object with this name already exists in this model
	 */
	public void addItem(Item item)
	{
		String type = item.getItemType();

		Map items = (Map) itemTable.get(type);
		if (items == null)
		{
			items = new HashMap();
			itemTable.put(type, items);
		}
		else
		{
			if (items.get(item.getName()) != null)
				throw new ModelException("Operation", "Component '" + item.getName() + "' already exists in model '" + getQualifier() + "'");
		}
		items.put(item.getName(), item);

		item.setModel(this);
	}

	/**
	 * Removes an item.
	 *
	 * @param item Item to remove
	 */
	public void removeItem(Item item)
	{
		String type = item.getItemType();

		Map items = (Map) itemTable.get(type);
		if (items != null)
		{
			items.remove(item.getName());
			if (items.isEmpty())
				itemTable.remove(type);
		}
		item.setModel(null);
	}

	//////////////////////////////////////////////////
	// @@ Reference resolving
	//////////////////////////////////////////////////

	/**
	 * Resolves an item according to its reference.
	 * The method will search for the item in the following models:<br>
	 * 1. Model root if the name is absolute (= fully qualified item)<br>
	 * 2. System model<br>
	 * 3. This model<br>
	 * 4. Imported models of this model<br>
	 *
	 * @param name Qualified or unqualified name of the item
	 * @param itemType Type of the item (see the constants of the {@link ItemTypes} class)
	 * @return The item descriptor or null if the item name is null or this item is not connected to a model
	 * @throws OpenBPException If the specified item could not be found
	 */
	public Item resolveItemRef(String name, String itemType)
	{
		return referenceHelper.resolveItemRef(name, itemType);
	}

	/**
	 * Determines the name of the type relative to the model that owns the item.
	 * The method will search the imports of the model and return the name of the
	 * item relative to an imported model if the item belongs to an imported model
	 * or a sub model of an imported model (or the System model, since the System
	 * model is imported automatically). If the item could not be found in the
	 * list of imported models, the method will return the fully qualified name
	 * of the item.
	 *
	 * @param item Item to determine the relative name for
	 * @return The relative name of the item or null if the item is null
	 */
	public String determineItemRef(Item item)
	{
		return referenceHelper.determineItemRef(item);
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
		return referenceHelper.resolveObjectRef(name, itemType);
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
		return referenceHelper.determineObjectRef(object);
	}

	/**
	 * Resolves a reference to a file.
	 * The method will search for the file in the following models:<br>
	 * 1. System model<br>
	 * 2. This model<br>
	 * 3. Imported models of this model
	 *
	 * @param fileName Name (including sub path) of the file to resolve
	 * @return The full path name of the file
	 * @throws OpenBPException If the specified file could not be found
	 */
	public String resolveFileRef(String fileName)
	{
		return referenceHelper.resolveFileRef(fileName);
	}

	/**
	 * Looks up a data type by its class name in the model's scope, including imported models.
	 *
	 * @param className Class to look up
	 * @return Data type associated with the class or null if no such class has been found
	 */
	public ComplexTypeItem lookupTypeByClassName(String className)
	{
		return referenceHelper.lookupTypeByClassName(className);
	}
}
