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

import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.string.StringUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelImpl;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemContainer;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.springframework.core.io.Resource;

/**
 * This class serves as base class for model managers.
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
public abstract class ResourceBasedModelMgrBase extends NonDelegatingModelMgrBase
{
	/** Root path */
	private String modelRootPath;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * The constructor.
	 */
	public ResourceBasedModelMgrBase()
	{
	}

	/**
	 * Gets the model root path.
	 * The model root path is the path that specifies the base location of a model repository.
	 * Depending on the type of model manager, this may be e. g. the model base directory or a classpath resource prefix.
	 * @nowarn
	 */
	public String getModelRootPath()
	{
		return modelRootPath;
	}

	/**
	 * Sets the model root path.
	 * The model root path is the path that specifies the base location of a model repository.
	 * Depending on the type of model manager, this may be e. g. the model base directory or a classpath resource prefix.
	 * @nowarn
	 */
	public void setModelRootPath(String modelRootPath)
	{
		this.modelRootPath = modelRootPath;
	}

	/**
	 * Read the model descriptor in the specified directory and return it.
	 * Does not register the model and also does not set the model manager and custom model root directory (if any).
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param modelDescriptorResource Resource that identifies the model descriptor file
	 * @return The new model or null (error messages go to the message container)
	 */
	protected Model readModelDescriptor(Resource modelDescriptorResource)
	{
		XMLDriver driver = XMLDriver.getInstance();

		// Deserialize model descriptor file (model.xml)
		Model model = null;
		try
		{
			model = (Model) driver.deserializeResource(ModelImpl.class, modelDescriptorResource);
		}
		catch (XMLDriverException pe)
		{
			getMsgContainer().addMsg(model, "Error reading model descriptor file $0 in model manager $1.", new Object[]
			{
				modelDescriptorResource.getDescription(), getClass().getName(), pe
			});
			return null;
		}

		return model;
	}

	/**
	 * Reads the specified item and adds it to its owning model.
	 *
	 * Any errors will be logged to the message container of this class.
	 *
	 * @param model Owning model
	 * @param itd Type of the item
	 * @param itemDescriptorResource Resource that identifies the item descriptor file
	 * @return The new item or null (error messages go to the message container)
	 */
	protected Item readItemDescriptor(Model model, ItemTypeDescriptor itd, Resource itemDescriptorResource)
	{
		XMLDriver driver = XMLDriver.getInstance();

		Item item = null;

		// Deserialize item descriptor file
		try
		{
			if (itd.isContainedItem())
			{
				// Item is wrapped by a container class in its xml file
				ItemContainer container = (ItemContainer) driver.deserializeResource(ItemContainer.class, itemDescriptorResource);
				item = container.getItem();
			}
			else
			{
				item = (Item) driver.deserializeResource(itd.getItemClass(), itemDescriptorResource);
			}
		}
		catch (XMLDriverException pe)
		{
			getMsgContainer().addMsg(model, "Error reading component descriptor file $0 in model manager $1.", new Object[]
			{
				itemDescriptorResource.getDescription(), getClass().getName(), pe
			});
			return null;
		}

		// Explicitely make this item an item of the specified type
		item.setItemType(itd.getItemType());

		return item;
	}

	/**
	 * Gets the path name of the item descriptor file.
	 *
	 * @param modelPath Path to the model directory
	 * @param itemType Item sub directory
	 * @param itemName Name of the item
	 * @return The path name of the item's xml file
	 */
	protected String getItemFilePath(String modelPath, String itemType, String itemName)
	{
		StringBuffer sb = new StringBuffer(modelPath);
		sb.append(StringUtil.FOLDER_SEP_CHAR);
		sb.append(itemType.toLowerCase());
		sb.append(StringUtil.FOLDER_SEP_CHAR);
		sb.append(itemName);
		sb.append(".xml");
		return sb.toString();
	}
}
