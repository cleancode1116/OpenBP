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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Helper class for model methods related to reference creation and resolving.
 *
 * @author Heiko Erhardt
 */
public class ModelReferenceHelper
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** The model we are associated to */
	private final ModelImpl model;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param model The model we are associated to
	 */
	ModelReferenceHelper(ModelImpl model)
	{
		this.model = model;
	}

	//////////////////////////////////////////////////
	// @@ Item references
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
	Item resolveItemRef(String name, String itemType)
	{
		if (name == null)
			// Nothing to resolve
			return null;

		Item item;

		if (ModelQualifier.isAbsolute(name))
		{
			// Check if it's an absolute item name
			ModelQualifier qualifier = (new ModelQualifier(name));
			qualifier.setItemType(itemType);
			item = checkItemRef(qualifier);
			if (item != null)
				return item;

			// Cannot resolve the item
			throw new ModelException("ObjectNotFound", "Component '" + name + "' not found in model '" + model.getQualifier() + "' or imported models.");
		}
		else
		{
			// Check if the item is a part of this model
			item = checkItemRef(new ModelQualifier(model.getName(), name, itemType));
			if (item != null)
				return item;

			// Check the imports
			for (Iterator it = model.getImports(); it.hasNext();)
			{
				String importModelName = (String) it.next();
				item = checkItemRef(new ModelQualifier(importModelName, name, itemType));
				if (item != null)
					return item;
			}

			// Finally, try the System model
			item = checkItemRef(new ModelQualifier(CoreConstants.SYSTEM_MODEL_NAME, name, itemType));
			if (item != null)
				return item;

			// Cannot resolve the item
			throw new ModelException("ObjectNotFound", "Component '" + name + "' not found in model '" + model.getQualifier() + "' or imported models.");
		}
	}

	/**
	 * Checks if an item specification can be resolved.
	 *
	 * @param qualifier Qualified name of the item
	 * @return The item or null if the item was not found
	 */
	private Item checkItemRef(ModelQualifier qualifier)
	{
		Item item = null;
		try
		{
			String itemType = qualifier.getItemType();
			if (itemType == null || itemType.equals(ItemTypes.MODEL))
			{
				item = model.getModelMgr().getOptionalModelByQualifier(qualifier);
			}
			else
			{
				item = model.getModelMgr().getItemByQualifier(qualifier, false);
			}
		}
		catch (ModelException e)
		{
			// Never thrown due to 'false' parameter
		}
		return item;
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
	String determineItemRef(Item item)
	{
		if (item == null)
			// Nothing to determine
			return null;

		ModelQualifier itemQualifier = item.getQualifier();
		String modelName = itemQualifier.getModel();
		String itemName = itemQualifier.getItem();

		if (modelName == null)
			// This is probably a component from the System model
			return itemName;

		// Check if the item is a part of this model
		if (modelName.equals(model.getName()))
			// The item is part of this model
			return itemName;

		// Check if the item name starts with the name of an imported model
		for (Iterator it = model.getImports(); it.hasNext();)
		{
			String importModelName = (String) it.next();

			if (modelName.equals(importModelName))
				return itemName;
		}

		// Finally, try the System model
		if (modelName.equals(CoreConstants.SYSTEM_MODEL_NAME))
			// The item is part of the System model or one of its sub models; use this
			return itemName;

		// Item not found in imports, return the entire item name
		return itemQualifier.toString();
	}

	//////////////////////////////////////////////////
	// @@ Model object references
	//////////////////////////////////////////////////

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
	ModelObject resolveObjectRef(String name, String itemType)
	{
		if (name == null)
			// Nothing to resolve
			return null;

		String itemName;
		String childName;

		int dotIndex = name.indexOf(ModelQualifier.OBJECT_DELIMITER_CHAR);
		if (dotIndex > 0)
		{
			itemName = name.substring(0, dotIndex);
			childName = name.substring(dotIndex + 1);
		}
		else
		{
			itemName = name;
			childName = null;
		}

		Item item = resolveItemRef(itemName, itemType);

		if (childName == null)
			return item;

		List children = item.getChildren();

		ModelObject object = (ModelObject) NamedObjectCollectionUtil.getByName(children, childName);

		if (object == null)
			// Cannot resolve the child
			throw new ModelException("ObjectNotFound", "Component member '" + childName + "' not found in component '"
				+ itemName + "'");

		return object;
	}

	/**
	 * Determines the name of the object relative to the model that owns the object.
	 * The item the object belongs to will be determined using the {@link #determineItemRef} method.
	 *
	 * @param object Object to determine the relative name for<br>
	 * The object must be a child object of an item.
	 * @return The relative name of the object or null if the object is null
	 */
	String determineObjectRef(ModelObject object)
	{
		if (object == null)
			// Nothing to determine
			return null;

		if (object instanceof Item)
			return determineItemRef((Item) object);

		ModelObject container = object.getContainer();
		String ref;

		if (container != null)
		{
			String containerRef = determineObjectRef(container);
			ref = containerRef + ModelQualifier.OBJECT_DELIMITER_CHAR + object.getName();
		}
		else
		{
			ref = model.getName();
		}

		return ref;
	}

	//////////////////////////////////////////////////
	// @@ File references
	//////////////////////////////////////////////////

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
	String resolveFileRef(String fileName)
	{
		if (fileName == null)
			// Nothing to resolve
			return null;

		String path;

		// Check if the file is a part of this model
		path = checkFileRef(model, fileName);
		if (path != null)
			return path;

		// Check the imports if not found in this model
		for (Iterator it = model.getImportedModels(); it.hasNext();)
		{
			Model importedModel = (Model) it.next();

			path = checkFileRef(importedModel, fileName);
			if (path != null)
				return path;
		}

		// Finally, try the System model
		path = checkFileRef(model.getModelMgr().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER), fileName);
		if (path != null)
			return path;

		// Cannot resolve the file
		throw new ModelException("ObjectNotFound", "File '" + fileName + "' not found in model '"
			+ model.getQualifier() + "' or imported models.");
	}

	/**
	 * Checks if a file specification can be resolved.
	 *
	 * @param baseModel Model that serves as a base for the file lookup
	 * @param fileName Name (including sub path) of the file to resolve
	 * @return The path to the file or null if the file was not found in the model directory
	 */
	private static String checkFileRef(Model baseModel, String fileName)
	{
		File file = new File(baseModel.getModelPath(), fileName);
		if (file.exists())
			return file.getPath();
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Type/class name resolving
	//////////////////////////////////////////////////

	/**
	 * Looks up a data type by its class name in the model's scope, including imported models.
	 *
	 * @param className Class to look up
	 * @return Data type associated with the class or null if no such class has been found
	 */
	public ComplexTypeItem lookupTypeByClassName(String className)
	{
		ComplexTypeItem type = lookupTypeByClassName(model, className);
		if (type != null)
			return type;

		for (Iterator it = model.getImportedModels(); it.hasNext();)
		{
			Model importedModel = (Model) it.next();
			type = lookupTypeByClassName(importedModel, className);
			if (type != null)
				return type;
		}

		return null;
	}

	private ComplexTypeItem lookupTypeByClassName(Model model, String className)
	{
		for (Iterator it = model.getItems(ItemTypes.TYPE); it.hasNext();)
		{
			DataTypeItem dataType = (DataTypeItem) it.next();
			if (dataType instanceof ComplexTypeItem)
			{
				if (className.equals(dataType.getClassName()))
				{
					return (ComplexTypeItem) dataType;
				}
			}
		}
		return null;
	}
}
