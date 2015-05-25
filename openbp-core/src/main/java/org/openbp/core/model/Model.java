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

import java.util.Iterator;
import java.util.List;

import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.modelmgr.ModelMgr;

/**
 * OpenBP model.
 * A OpenBP model is a set of entities ({@link Item}) that comprise
 * an independent development model.
 *
 * @author Heiko Erhardt
 */
public interface Model
	extends Item
{
	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Shuts down the model.
	 * This method will be called before the OpenBP Server will exit or before a model is being unloaded.
	 * It will notify all observers about the model shutdown.
	 */
	public void shutdownModel();

	/**
	 * Resets the model.
	 * Re-initializes the model classloader and reinitializes the components of the model.
	 */
	public void resetModel();

	/**
	 * Gets the class loader that is used to load activity and bean classes.
	 * @nowarn
	 */
	public ClassLoader getClassLoader();

	/**
	 * Sets the class loader that is used to load activity and bean classes.
	 * @nowarn
	 */
	public void setClassLoader(ClassLoader classLoader);

	//////////////////////////////////////////////////
	// @@ Imported models
	//////////////////////////////////////////////////

	/**
	 * Gets the list of imported models.
	 * @return An iterator of {@link Model} objects
	 */
	public Iterator getImportedModels();

	/**
	 * Adds an imported model.
	 * @param importedModel The imported model to add
	 */
	public void addImportedModel(Model importedModel);

	/**
	 * Gets the list of imported models.
	 * @return A list of {@link Model} objects
	 */
	public List getImportedModelList();

	/**
	 * Sets the list of imported models.
	 * @param importedModelList A list of {@link Model} objects
	 */
	public void setImportedModelList(List importedModelList);

	//////////////////////////////////////////////////
	// @@ Accessing model items
	//////////////////////////////////////////////////

	/**
	 * Gets a particular model item.
	 *
	 * @param itemName Local or reference to the item
	 * @param itemType Type of the item (see the constants of the {@link ItemTypes} class)
	 * @param required
	 *		true	Will throw an exception if the item does not exist.<br>
	 *		false	Will return null if the item does not exist.
	 * @return Item descriptors or null if the item does not exist.<br>
	 * Note that the appropriate subclass of the {@link Item} class will be returned.
	 * @throws OpenBPException If the item does not exist
	 */
	public Item getItem(String itemName, String itemType, boolean required);

	/**
	 * Gets a list of item descriptors of the specified type of the specified model.
	 *
	 * @param itemType Type of the items (see the constants of the {@link ItemTypes} class)
	 * @return Iterator of item descriptors ({@link Item} objects)
	 */
	public Iterator getItems(String itemType);

	/**
	 * Gets the default process of this model.
	 *
	 * @return The process or null if there is no process that has the default process flag
	 * ({@link ProcessItem#setDefaultProcess}) set
	 */
	public ProcessItem getDefaultProcess();

	//////////////////////////////////////////////////
	// @@ Item management
	//////////////////////////////////////////////////

	/**
	 * Adds a new item.
	 *
	 * @param item Item to add
	 * @throws OpenBPException If an object with this name already exists in this model
	 */
	public void addItem(Item item);

	/**
	 * Removes an item.
	 *
	 * @param item Item to remove
	 */
	public void removeItem(Item item);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the directory the model resides in.
	 * Actually, this is some arbitrary model storage specification, depending on the type of {@link ModelMgr} that read the model.
	 * @nowarn
	 */
	public String getModelPath();

	/**
	 * Sets the directory the model resides in.
	 * Actually, this is some arbitrary model storage specification, depending on the type of {@link ModelMgr} that read the model.
	 * @nowarn
	 */
	public void setModelPath(String modelPath);

	/**
	 * Gets the default package to use for source files of this model.
	 * @nowarn
	 */
	public String getDefaultPackage();

	/**
	 * Sets the default package to use for source files of this model.
	 * @nowarn
	 */
	public void setDefaultPackage(String defaultPackage);

	/**
	 * Gets the model manager that loaded this model.
	 * @nowarn
	 */
	public ModelMgr getModelMgr();

	/**
	 * Sets the model manager that loaded this model.
	 * @nowarn
	 */
	public void setModelMgr(ModelMgr modelMgr);

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
	public Item resolveItemRef(String name, String itemType);

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
	public String determineItemRef(Item item);

	/**
	 * Resolves an object according to its reference.
	 * The reference denotes either an item or a child object of an item.
	 * The item will be determined using the {@link #resolveItemRef method}.
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
	 * The method will search for the file in the following models:<br>
	 * 1. System model<br>
	 * 2. This model<br>
	 * 3. Imported models of this model
	 *
	 * @param fileName Name (including sub path) of the file to resolve
	 * @return The full path name of the file delimited with the system separator
	 * @throws OpenBPException If the specified file could not be found
	 */
	public String resolveFileRef(String fileName);

	/**
	 * Looks up a data type by its class name in the model's scope, including imported models.
	 *
	 * @param className Class to look up
	 * @return Data type associated with the class or null if no such class has been found
	 */
	public ComplexTypeItem lookupTypeByClassName(String className);
}
