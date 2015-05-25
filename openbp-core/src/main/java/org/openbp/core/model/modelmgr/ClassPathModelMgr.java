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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * The classpath model manager provides access to models that reside in the classpath.
 * Note that these models and their items are considered to be read-only.
 *
 * Models are loaded from the "model" folder in the classpath.
 * Similar to the FileSystemModelMgr, the model structure is expected to reside below this directory.
 *
 * @author Heiko Erhardt
 */
public class ClassPathModelMgr extends ResourceBasedModelMgrBase
{
	/**
	 * Private constructor.
	 */
	public ClassPathModelMgr()
	{
		super();
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
		super.initialize();

		// Check if there was a custom model root dir specified in Server.properties or Cockpit.properties
		String rootPath = SettingUtil.getStringSetting("openbp.ClassPathModelMgr.ModelPath", CoreConstants.FOLDER_MODEL);
		rootPath = StringUtil.normalizePathName(rootPath);
		setModelRootPath(rootPath);
	}

	//////////////////////////////////////////////////
	// @@ ModelMgrBase overrides: File operations and reloading
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
		return false;
	}

	/**
	 * Adds a model to the model persistence store.
	 *
	 * @param model Model to add
	 * @throws OpenBPException On error
	 */
	protected void addModelToStore(Model model)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
	}

	/**
	 * Saves a model to the model persistence store.
	 *
	 * @param model Model to save
	 * @throws OpenBPException On error
	 */
	protected void saveModelToStore(Model model)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
	}

	/**
	 * Adds an item to the model persistence store.
	 *
	 * @param item Item to add
	 * @throws OpenBPException On error
	 */
	protected void addItemToStore(Item item)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
	}

	/**
	 * Saves an item to the model persistence store.
	 *
	 * @param item Item to save
	 * @throws OpenBPException On error
	 */
	protected void saveItemToStore(Item item)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
	}

	/**
	 * Removes a model from the model persistence store.
	 *
	 * @param model Model to remove
	 * @throws OpenBPException On error
	 */
	protected void removeModelFromStore(Model model)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
	}

	/**
	 * Removes an item from the model persistence store.
	 *
	 * @param item Item to remove
	 * @throws OpenBPException On error
	 */
	protected void removeItemFromStore(Item item)
	{
		throw new ModelException("ClassPathOperation", "Class path model manager does not support modfying operations.");
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
		String basePath = getModelRootPath();

		String pattern = basePath + StringUtil.FOLDER_SEP + "*" + StringUtil.FOLDER_SEP + CoreConstants.FILE_MODEL_DESCRIPTOR;
		Resource[] resources = ResourceMgr.getDefaultInstance().findResources(pattern);

		for (int i = 0; i < resources.length; ++i)
		{
			Resource res = resources[i];

			// Extract the model name from the path
			String name = null;
			URL url = null;
			try
			{
				url = res.getURL();
			}
			catch (IOException e1)
			{
				throw new ModelException("ClassPathOperation", "Error accessing model URL '" + res.getDescription() + "'.");
			}
			String path = url.toString();
			int index = path.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
			if (index > 0)
			{
				path = path.substring(0, index);
				index = path.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
				if (index >= 0)
				{
					name = path.substring(index + 1);
				}
			}

			if (shouldLoadModel(name))
			{
				readModelByPath(res);
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
	protected Model readModelFromStore(ModelQualifier modelQualifier)
	{
		String modelPath = getModelRootPath() + StringUtil.FOLDER_SEP_CHAR + modelQualifier.getModel();
		ClassPathResource modelDescriptorResource = new ClassPathResource(modelPath + StringUtil.FOLDER_SEP + CoreConstants.FILE_MODEL_DESCRIPTOR);
		if (modelDescriptorResource.exists())
			return readModelByPath(modelDescriptorResource);
		return null;
	}

	/**
	 * Reads and registers the model in the specified path.
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param modelDescriptorResource Resource that identifies the model descriptor file
	 * @return The new model or null (error messages go to the message container)
	 */
	private Model readModelByPath(Resource modelDescriptorResource)
	{
		Model model = readModelDescriptor(modelDescriptorResource);
		if (model == null)
			return null;

		// Determine the model URL from the descriptor URL
		// TODO Cleanup 4: This is very rough, there should be some better way... In fact, the modelPath property should be a modelUrl property
		URL url = null;
		try
		{
			url = modelDescriptorResource.getURL();
		}
		catch (IOException e1)
		{
			throw new ModelException("ClassPathOperation", "Error accessing model URL '" + modelDescriptorResource.getDescription() + "'.");
		}
		String modelPath = url.toString();
		int index = modelPath.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
		if (index > 0)
		{
			modelPath = modelPath.substring(0, index);
		}
		model.setModelPath(modelPath);

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

		// Read the model items
		ItemTypeDescriptor[] itds = getItemTypeDescriptors(ItemTypeRegistry.SKIP_MODEL | ItemTypeRegistry.SKIP_INVISIBLE);
		for (int i = 0; i < itds.length; ++i)
		{
			readItems(model, modelPath, itds[i]);
		}

		LogUtil.info(getClass(), "Loaded model $0.", model.getQualifier());
		return model;
	}

	/**
	 * Reloads the model after a model update.
	 *
	 * @param model Model to reload
	 */
	protected void reloadModelAfterModelUpdate(Model model)
	{
		// N/A due to read-only models
	}

	/**
	 * Reloads the item after a model update.
	 *
	 * @param item Item to reload
	 */
	protected void reloadItemAfterModelUpdate(Item item)
	{
		// N/A due to read-only models
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
		ModelQualifier modelQualifier = ModelQualifier.constructModelQualifier(itemQualifier.getModel());
		Model model = getOptionalModelByQualifier(modelQualifier);
		if (model != null)
		{
			String itemType = itemQualifier.getItemType();
			ItemTypeDescriptor itd = getItemTypeDescriptor(itemType);
			if (itd == null)
			{
				throw new ModelException("UnknownItemType", "Unknown item type '" + itemType + "'.");
			}

			return readItemFromStore(model, itemQualifier.getItem(), itd);
		}
		return null;
	}

	/**
	 * Reads the specified item from the model persistence store and adds it to its owning model.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param model Owning model
	 * @param itemName Name of the item
	 * @param itd Item type descriptor
	 * @return The new item or null (error messages go to the message container)
	 */
	private Item readItemFromStore(Model model, String itemName, ItemTypeDescriptor itd)
	{
		String fileName = getItemFilePath(model.getModelPath(), itd.getItemType(), itemName);
		try
		{
			// ClassPathResource itemDescriptorResource = new ClassPathResource(fileName);
			UrlResource itemDescriptorResource = new UrlResource(fileName);
			return readItemDescriptor(model, itd, itemDescriptorResource);
		}
		catch (MalformedURLException e)
		{
			throw new ModelException("ClassPathOperation", "Error accessing item URL '" + fileName + "'.");
		}
	}

	/**
	 * Reads all model items in the specified directory and adds them to the model.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param model Model the item shall be added to
	 * @param modelBasePath Base directory of the model
	 * @param itd Item type descriptor
	 */
	private void readItems(Model model, String modelBasePath, ItemTypeDescriptor itd)
	{
		String itemType = itd.getItemType();

		String pattern = modelBasePath + StringUtil.FOLDER_SEP + itemType.toLowerCase() + StringUtil.FOLDER_SEP + "*.xml";
		Resource[] resources = null;
		try
		{
			resources = ResourceMgr.getDefaultInstance().findResources(pattern);
		}
		catch (ResourceMgrException e)
		{
			// Obviously item type not present, ignore
		}

		if (resources != null)
		{
			for (int i = 0; i < resources.length; ++i)
			{
				Resource res = resources[i];

				// TODO Cleanup 4: This is very rough, there should be some better way... In fact, the modelPath property should be a modelUrl property
				URL url = null;
				try
				{
					url = res.getURL();
				}
				catch (IOException e1)
				{
					throw new ModelException("ClassPathOperation", "Error accessing item URL '" + res.getDescription() + "'.");
				}
				String itemPath = url.toString();
				int folderIndex = itemPath.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
				int dotIndex = itemPath.lastIndexOf(".");
				if (folderIndex > 0 && dotIndex > 0)
				{
					String itemName = itemPath.substring(folderIndex + 1, dotIndex);

					Item item = readItemFromStore(model, itemName, itd);
					if (item != null)
					{
						// Add the item to the model
						try
						{
							model.addItem(item);
						}
						catch (ModelException e)
						{
							getMsgContainer().addMsg(model, "Error adding component $0 to model $1 in model manager $2.", new Object[]
							{
								item.getName(), model.getName(), getClass().getName(), e,
							});
						}
					}
				}
			}
		}
	}
}
