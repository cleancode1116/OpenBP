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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.openbp.common.CollectionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.classloader.XClassLoaderConfiguration;
import org.openbp.common.generic.Copyable;
import org.openbp.common.io.ExtensionFileNameFilter;
import org.openbp.common.io.FileUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelClassLoader;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelLocationUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.springframework.core.io.FileSystemResource;

/**
 * The file system model manager provides access to models that are located in the file system.
 *
 * Models are loaded from the $OPENBP_HOME/model directory. For each top-level model,
 * there is one sub directory, which contains the model.xml files and the models components and sub models
 * in further sub directories.
 *
 * In addition to the $OPENBP_HOME/model directory, top-level models may also be loaded from
 * one of the directories specified by the ModelMgr.AdditionalModelQualifier setting in the Server.properties file.
 * This settings contains a list of directories (separated by ';') that are also scanned for top-level
 * models. However, if a new top-level model is created, it will always be placed in the $OPENBP_HOME/server/model
 * directory.
 *
 * @author Heiko Erhardt
 */
public class FileSystemModelMgr extends ResourceBasedModelMgrBase
{
	/** Additional model paths */
	private String[] additionalModelRootPaths;

	/**
	 * Private constructor.
	 */
	public FileSystemModelMgr()
	{
	}

	/**
	 * Initializes the model manager.
	 * Called before reading the models.
	 */
	public void initialize()
	{
		super.initialize();

		// Check if there was a custom model root dir specified in Server.properties or Cockpit.properties
		String rootPath = SettingUtil.getStringSetting("openbp.FileSystemModelMgr.ModelPath");
		if (rootPath == null)
		{
			// Use the standard 
			if (Application.getRootDir() != null)
			{
				rootPath = Application.getRootDir() + StringUtil.FOLDER_SEP + CoreConstants.FOLDER_MODEL;
			}
		}
		if (rootPath == null)
		{
			String msg = LogUtil
				.error(
					getClass(),
					"Unable to determine the root path of the file system model manager from the setting 'openbp.FileSystemModelMgr.ModelPath' or the application root directory. Maybe the root directory was not specified using the '-DrootDir=<dir>' JVM option.");
			throw new ModelException("Init", msg);
		}
		rootPath = StringUtil.normalizePathName(rootPath);
		setModelRootPath(rootPath);

		// Issue a warning on non-existent model path, but continue anyway, directory might be created
		checkIfDirExists(rootPath);

		// Get and parse the additional model qualifier setting from the Server.properties or Cockpit.properties
		String additional = SettingUtil.getStringSetting("openbp.FileSystemModelMgr.AdditionalModelPath");
		if (additional != null)
		{
			ArrayList list = new ArrayList();
			for (StringTokenizer st = new StringTokenizer(additional, ";"); st.hasMoreTokens();)
			{
				String s = st.nextToken();
				s = StringUtil.absolutePathName(s);

				if (! list.contains(s))
				{
					// Additional model paths must exist, skip if not present
					if (checkIfDirExists(s))
					{
						list.add(s);
					}
				}
			}
			setAdditionalModelRootPaths(CollectionUtil.toStringArray(list));
		}
	}

	private boolean checkIfDirExists(String s)
	{
		File dir = new File(s);
		if (! dir.exists() || ! dir.isDirectory())
		{
			LogUtil.warn(getClass(), "Model path $0 does not exist or is not a directory.", s);
			return false;
		}
		return true;
	}

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
		// Use the class load that loaded the ModelImpl class as parent class loader of the model.
		ClassLoader parentClassLoader = model.getClass().getClassLoader();

		XClassLoaderConfiguration config = new XClassLoaderConfiguration();

		addClassesDir(config, model, ModelLocationUtil.DIR_CLASSES);
		addClassesDir(config, model, ModelLocationUtil.DIR_TARGET_CLASSES);
		addLibDir(config, model, ModelLocationUtil.DIR_LIB);
		addLibDir(config, model, ModelLocationUtil.DIR_EXTLIB);

		if (config.getRepositories().size() == 0)
		{
			// Nothing in repository, so use regular CL
			return parentClassLoader;
		}

		// Set the name of the class loader.
		String qualifier = model.getQualifier().toString();
		qualifier = qualifier.replace(ModelQualifier.PATH_DELIMITER_CHAR, '.');
		config.setName("Model" + qualifier);
		config.setParentClassLoader(parentClassLoader);

		// Create a class loader for this model
		return new ModelClassLoader(config, model);
	}

	private void addClassesDir(XClassLoaderConfiguration config, Model model, String identifier)
	{
		String sDir = determineRepositoryDir(model, identifier);
		if (sDir == null)
			return;

		config.addRepository(sDir);
	}

	private void addLibDir(XClassLoaderConfiguration config, Model model, String identifier)
	{
		String sDir = determineRepositoryDir(model, identifier);
		if (sDir == null)
			return;

		File dir = new File(sDir);
		String[] fileNames = dir.list(new ExtensionFileNameFilter("jar"));
		if (fileNames == null)
			return;

		for (int i = 0; i < fileNames.length; ++i)
		{
			String jarFileName = sDir + StringUtil.FOLDER_SEP + fileNames[i];
			config.addRepository(jarFileName);
		}
	}

	private String determineRepositoryDir(Model model, String identifier)
	{
		String sDir = ModelLocationUtil.expandModelLocation(model, identifier);

		File dir = new File(sDir);
		if (! dir.exists() || ! dir.isDirectory())
			return null;
		return sDir;
	}

	/**
	 * Gets the additional model paths.
	 * @nowarn
	 */
	public String[] getAdditionalModelRootPaths()
	{
		return additionalModelRootPaths;
	}

	/**
	 * Sets the additional model paths.
	 * @nowarn
	 */
	public void setAdditionalModelRootPaths(String[] additionalModelRootPaths)
	{
		this.additionalModelRootPaths = additionalModelRootPaths;
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
		if (model.getModelPath() == null)
		{
			model.setModelPath(getModelRootPath() + ModelQualifier.PATH_DELIMITER_CHAR + model.getName());
		}

		saveModelToStore(model);
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

		String filePath = StringUtil.buildPath(model.getModelPath(), CoreConstants.FILE_MODEL_DESCRIPTOR);

		// Create the path on the file system
		File parentdir = FileUtil.getParent(new File(filePath), false);
		if (parentdir != null && ! parentdir.exists())
			parentdir.mkdirs();

		byte[] bytes = serializeModelToByteArray(model);

		// Write the string to the output file
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(filePath);
		}
		catch (FileNotFoundException e)
		{
			throw new ModelException("FileSystemOperation", "Cannot create file '" + filePath + "': " + e.getMessage());
		}

		try
		{
			out.write(bytes);
		}
		catch (IOException ioe)
		{
			throw new ModelException("FileSystemOperation", "Cannot write to file '" + filePath + "': " + ioe.getMessage());
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{
			}
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
		saveItemToStore(item);
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

		String filePath = getItemFilePath(item);

		// Create the path in the file system
		File parentdir = FileUtil.getParent(new File(filePath), false);
		if (parentdir != null && ! parentdir.exists())
			parentdir.mkdirs();

		// Serialize item descriptor file
		byte[] bytes = serializeItemToByteArray(item);

		// Write the string to the output file
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(filePath);
		}
		catch (FileNotFoundException e)
		{
			throw new ModelException("FileSystemOperation", "Cannot create file '" + filePath + "': " + e.getMessage());
		}

		try
		{
			out.write(bytes);
		}
		catch (IOException ioe)
		{
			throw new ModelException("FileSystemOperation", "Cannot write to file '" + filePath + "': " + ioe.getMessage());
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{
			}
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
		// Copy iterator data to list in order to prevent concurrent modification exception
		Collection coll = CollectionUtil.collection(model.getItems(null));
		for (Iterator it = coll.iterator(); it.hasNext();)
		{
			Item item = (Item) it.next();
			removeItemFromStore(item);
		}

		String modelPath = model.getModelPath();
		String filePath = StringUtil.buildPath(modelPath, CoreConstants.FILE_MODEL_DESCRIPTOR);

		try
		{
			new File(filePath).delete();
		}
		catch (SecurityException se)
		{
			throw new ModelException("FileSystemOperation", "Cannot remove model descriptor file '" + filePath + "': " + se.getMessage());
		}

		// Removes a model directory and all its files and subdirs.
		try
		{
			// Recursively delete the directory
			FileUtil.remove(new File(modelPath));
		}
		catch (IOException e)
		{
			throw new ModelException("FileSystemOperation", "Cannot remove model directory '" + modelPath + "': " + e.getMessage());
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
		String filePath = getItemFilePath(item);
		try
		{
			new File(filePath).delete();
		}
		catch (SecurityException se)
		{
			throw new ModelException("FileSystemOperation", "Cannot remove model descriptor file '" + filePath + "': " + se.getMessage());
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
		// Read all top-level models, their items and their sub models
		readModelsFromRootPath(getModelRootPath());

		// Read additional top-level models from other model root paths if specified
		if (additionalModelRootPaths != null)
		{
			for (int i = 0; i < additionalModelRootPaths.length; ++i)
			{
				String path = additionalModelRootPaths[i];
				readModelsFromRootPath(path);
			}
		}
	}

	/**
	 * Read all models in the specified root path.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param basePath Directory containing the model directories
	 */
	private void readModelsFromRootPath(String basePath)
	{
		File baseDir = new File(basePath);

		// Iterate all model directories
		String[] fileNames = baseDir.list();
		if (fileNames == null)
			return;

		for (int i = 0; i < fileNames.length; ++i)
		{
			String name = fileNames[i];

			if (shouldLoadModel(name))
			{
				readModelByPath(basePath + StringUtil.FOLDER_SEP + name);
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
		String modelPath = determineModelPath(modelQualifier);
		if (modelPath == null)
			// No model directory (may be e. g. CVS directory or so)
			return null;
		return readModelByPath(modelPath);
	}

	/**
	 * Reads and registers the model in the specified path.
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param modelPath Fully qualified name of the model to read
	 * @return The new model or null (error messages go to the message container)
	 */
	private Model readModelByPath(String modelPath)
	{
		Model model = readModelDescriptor(modelPath);
		if (model == null)
			return null;

		// Save the model manager reference and the custom model dir, if any.
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
		Model newModel = readModelDescriptor(model.getModelPath());
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

	/**
	 * Reloads the item after a model update.
	 *
	 * @param item Item to reload
	 */
	protected void reloadItemAfterModelUpdate(Item item)
	{
		String itemType = item.getItemType();
		ItemTypeDescriptor itd = getItemTypeDescriptor(itemType);

		Item newItem = readItemFromStore(item.getModel(), item.getName(), itd);
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

	/**
	 * Read the modeldescriptor in the specified directory and return it.
	 * Does not register the model and also does not set the model manager and custom model root directory (if any).
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param modelPath Directory containing the model
	 * @return The new model or null (error messages go to the message container)
	 */
	private Model readModelDescriptor(String modelPath)
	{
		FileSystemResource modelDescriptorResource = new FileSystemResource(modelPath + StringUtil.FOLDER_SEP + CoreConstants.FILE_MODEL_DESCRIPTOR);
		return readModelDescriptor(modelDescriptorResource);
	}

	/**
	 * Determines the model directory from the model qualifier.
	 *
	 * @param modelQualifier Fully qualified model name
	 * @return The model directory or null if no such directory exists
	 */
	private String determineModelPath(ModelQualifier modelQualifier)
	{
		String modelName = modelQualifier.getModel();
		String modelDescriptorFileName = StringUtil.FOLDER_SEP_CHAR + modelName + StringUtil.FOLDER_SEP + CoreConstants.FILE_MODEL_DESCRIPTOR;

		// Check regular model root path
		if (new File(getModelRootPath() + modelDescriptorFileName).exists())
			return getModelRootPath() + StringUtil.FOLDER_SEP_CHAR + modelName;

		// Check the addditional model root paths
		String[] additionalModelRootPaths = getAdditionalModelRootPaths();
		if (additionalModelRootPaths != null)
		{
			for (int i = 0; i < additionalModelRootPaths.length; ++i)
			{
				if (new File(additionalModelRootPaths[i] + modelDescriptorFileName).exists())
					return additionalModelRootPaths[i] + StringUtil.FOLDER_SEP_CHAR + modelName;
			}
		}

		return null;
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
				throw new ModelException("UnknownItemType", "Unknown item type '" + itemType + "'.");

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
		FileSystemResource itemDescriptorResource = new FileSystemResource(fileName);
		return readItemDescriptor(model, itd, itemDescriptorResource);
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

		File baseDir = new File(modelBasePath, itemType.toLowerCase());
		if (! baseDir.exists())
			// Directory does not exist.
			// This means there are no items of this type
			return;
		if (! baseDir.isDirectory())
			// Ignore non-directories
			return;

		// Iterate all model directories
		String[] fileNames = baseDir.list(ExtensionFileNameFilter.getXmlInstance());
		if (fileNames == null)
			// No items present
			return;

		for (int i = 0; i < fileNames.length; ++i)
		{
			String itemName = fileNames[i].substring(0, fileNames[i].lastIndexOf('.'));

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
						item.getName(), model.getName(), getClass().getName(), e
					});
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Gets the path name of the item descriptor file.
	 *
	 * @param item The item
	 * @return The path name of the item's xml file
	 */
	private String getItemFilePath(Item item)
	{
		return getItemFilePath(item.getModel().getModelPath(), item.getItemType(), item.getName());
	}
}
