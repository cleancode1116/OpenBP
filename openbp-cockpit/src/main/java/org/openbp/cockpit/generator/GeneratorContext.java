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
package org.openbp.cockpit.generator;

import java.util.HashMap;
import java.util.Map;

import org.openbp.common.template.TemplateEngineResult;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.guiclient.objectvalidators.ModelObjectValidator;

/**
 * This container object holds various informations that are modified by the generator wizard and used
 * by the generator.
 *
 * @author Heiko Erhardt
 */
public class GeneratorContext
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Status flag: The item is a new item. */
	private static final int STATUS_NEW_ITEM = (1 << 0);

	/** Status flag: The item is considered empty, i. e. name and structure should be initialized, possibly overwriting existing definitions. */
	private static final int STATUS_EMPTY_ITEM = (1 << 1);

	/** Status flag: The item has been saved during the generation process. */
	private static final int STATUS_ITEM_SAVED = (1 << 2);

	/** Status flag: A property/wizard page that may require (re)-generation has been modified. Propose this to the user. */
	private static final int STATUS_NEED_GENERATION = (1 << 3);

	/** Status flag: The requirements for the generator have not been met. Unable to perform the generation. */
	private static final int STATUS_INVALID_GENERATOR = (1 << 4);

	/** Status flag: The item has been edited in the visual view. */
	private static final int STATUS_CLASSNAME_SET = (1 << 5);

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * (Modified) item the generation process should be performed upon.
	 * This item may be modified by the wizard.
	 */
	private Item item;

	/**
	 * Original item the generation process should be performed upon.
	 * This is a copy of the item passed to the wizard.
	 * It is used to restore the original item state when using the 'Back' button of the wizard.
	 */
	private Item originalItem;

	/** Status flag */
	private int status;

	/**
	 * Table of properties that will be used in the generation process.
	 * Maps property names to arbitrary objects.
	 * This table contains the initial properties of the selected generator (see {@link Generator#addInitialProperty}),
	 * custom properties that have been defined by custom wizard pages and working properties of the generator.
	 */
	private Map properties;

	/**
	 * Root of the output directory.
	 * Usually, the generator will create files on a sub directory of the output root directory.
	 */
	private String outputRootDir;

	/** Template overwrite mode */
	private String overwriteMode;

	/** Selected generator */
	private Generator selectedGenerator;

	/** Generator-specific settings */
	private GeneratorSettings generatorSettings;

	/** Template engine result */
	private TemplateEngineResult templateEngineResult;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public GeneratorContext()
	{
		properties = new HashMap();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Resets the context status.
	 */
	public void reset()
	{
		item = null;
		originalItem = null;
		status = 0;
		outputRootDir = null;
		overwriteMode = null;
		selectedGenerator = null;
		generatorSettings = null;
		templateEngineResult = null;
		properties.clear();
	}

	/**
	 * Gets the (modified) tem the generation process should be performed upon.
	 * This item may be modified by the wizard.
	 * @nowarn
	 */
	public Item getItem()
	{
		return item;
	}

	/**
	 * Sets the (modified) tem the generation process should be performed upon.
	 * This item may be modified by the wizard.
	 * @nowarn
	 */
	public void setItem(Item item)
	{
		this.item = item;

		if (item != null)
		{
			Model model = item.getOwningModel();
			if (model != null)
			{
				outputRootDir = model.getModelPath();
			}
		}
		else
		{
			outputRootDir = null;
		}
	}

	/**
	 * Gets the original item the generation process should be performed upon.
	 * This is the item that has been passed to the wizard
	 * It is used to:<br>
	 * - restore the original item state when using the 'Back' button of the wizard<br>
	 * - serve as a reference for name uniqueness check of the {@link ModelObjectValidator}
	 * @nowarn
	 */
	public Item getOriginalItem()
	{
		return originalItem;
	}

	/**
	 * Sets the original item the generation process should be performed upon.
	 * This is the item that has been passed to the wizard.
	 * It is used to:<br>
	 * - restore the original item state when using the 'Back' button of the wizard<br>
	 * - serve as a reference for name uniqueness check of the {@link ModelObjectValidator}
	 * @nowarn
	 */
	public void setOriginalItem(Item originalItem)
	{
		this.originalItem = originalItem;
	}

	/**
	 * Checks if the specified status is set.
	 *
	 * @param statusFlag Status flag to check (see the constants of this class)
	 * @nowarn
	 */
	public boolean hasStatus(int statusFlag)
	{
		return (status & statusFlag) != 0;
	}

	/**
	 * Sets the specified status.
	 *
	 * @param statusFlag Status flag to set (see the constants of this class)
	 * @nowarn
	 */
	public void setStatus(int statusFlag)
	{
		status |= statusFlag;
	}

	/**
	 * Clears the specified status.
	 *
	 * @param statusFlag Status flag to clear (see the constants of this class)
	 * @nowarn
	 */
	public void clearStatus(int statusFlag)
	{
		status &= ~statusFlag;
	}

	/**
	 * Gets the root of the output directory.
	 * Usually, the generator will create files on a sub directory of the output root directory.
	 * @nowarn
	 */
	public String getOutputRootDir()
	{
		return outputRootDir;
	}

	/**
	 * Sets the root of the output directory.
	 * Usually, the generator will create files on a sub directory of the output root directory.
	 * @nowarn
	 */
	public void setOutputRootDir(String outputRootDir)
	{
		this.outputRootDir = outputRootDir;
	}

	/**
	 * Gets the template overwrite mode.
	 * @return "ask"/"merge"/"overwrite"
	 */
	public String getOverwriteMode()
	{
		return overwriteMode;
	}

	/**
	 * Sets the template overwrite mode.
	 * @param overwriteMode "ask"/"merge"/"overwrite"
	 */
	public void setOverwriteMode(String overwriteMode)
	{
		this.overwriteMode = overwriteMode;
	}

	/**
	 * Gets the selected generator.
	 * @nowarn
	 */
	public Generator getSelectedGenerator()
	{
		return selectedGenerator;
	}

	/**
	 * Sets the selected generator.
	 * @nowarn
	 */
	public void setSelectedGenerator(Generator selectedGenerator)
	{
		if (this.selectedGenerator != null)
		{
			this.selectedGenerator.removeInitialPropertiesToContext(this);
		}

		this.selectedGenerator = selectedGenerator;

		if (selectedGenerator != null)
		{
			selectedGenerator.copyInitialPropertiesToContext(this);
		}
	}

	/**
	 * Gets the generator-specific settings.
	 * @nowarn
	 */
	public GeneratorSettings getGeneratorSettings()
	{
		return generatorSettings;
	}

	/**
	 * Sets the generator-specific settings.
	 * @nowarn
	 */
	public void setGeneratorSettings(GeneratorSettings generatorSettings)
	{
		this.generatorSettings = generatorSettings;
	}

	/**
	 * Gets the template engine result.
	 * @nowarn
	 */
	public TemplateEngineResult getTemplateEngineResult()
	{
		return templateEngineResult;
	}

	/**
	 * Sets the template engine result.
	 * @nowarn
	 */
	public void setTemplateEngineResult(TemplateEngineResult templateEngineResult)
	{
		this.templateEngineResult = templateEngineResult;
	}

	/**
	 * Adds a generator property.
	 *
	 * @param key Property name
	 * @param value Property value
	 */
	public void setProperty(String key, Object value)
	{
		properties.put(key, value);
	}

	/**
	 * Removes a generator property.
	 *
	 * @param key Property name
	 */
	public void removeProperty(String key)
	{
		properties.remove(key);
	}

	/**
	 * Gets a generator property.
	 *
	 * @param key Property name
	 * @return Property value or null if no such property exists
	 */
	public Object getProperty(String key)
	{
		return properties.get(key);
	}

	/**
	 * Gets the table of generator properties that will be used in the template-based generation process.
	 * @nowarn
	 */
	public Map getProperties()
	{
		return properties;
	}

	//////////////////////////////////////////////////
	// @@ Status flags
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the item is a new item.
	 * @nowarn
	 */
	public boolean isNewItem()
	{
		return (status & STATUS_NEW_ITEM) != 0;
	}

	/**
	 * Sets the flag if the item is a new item.
	 * @nowarn
	 */
	public void setNewItem(boolean newItem)
	{
		if (newItem)
			status |= STATUS_NEW_ITEM;
		else
			status &= ~STATUS_NEW_ITEM;
	}

	/**
	 * Gets the flag if the item name and structure should be initialized, overwriting existing definitions.
	 * @nowarn
	 */
	public boolean isEmptyItem()
	{
		return (status & STATUS_EMPTY_ITEM) != 0;
	}

	/**
	 * Sets the flag if the item name and structure should be initialized, overwriting existing definitions.
	 * @nowarn
	 */
	public void setEmptyItem(boolean isEmpty)
	{
		if (isEmpty)
			status |= STATUS_EMPTY_ITEM;
		else
			status &= ~STATUS_EMPTY_ITEM;
	}

	/**
	 * Gets the flag if the item has been saved during the generation process.
	 * @nowarn
	 */
	public boolean isItemSaved()
	{
		return (status & STATUS_ITEM_SAVED) != 0;
	}

	/**
	 * Sets the flag if the item has been saved during the generation process.
	 * @nowarn
	 */
	public void setItemSaved(boolean itemSaved)
	{
		if (itemSaved)
			status |= STATUS_ITEM_SAVED;
		else
			status &= ~STATUS_ITEM_SAVED;
	}

	/**
	 * Gets the flag if a property/wizard page that may require (re)-generation has been modified. Propose this to the user.
	 * @nowarn
	 */
	public boolean isNeedGeneration()
	{
		return (status & STATUS_NEED_GENERATION) != 0;
	}

	/**
	 * Sets the flag if a property/wizard page that may require (re)-generation has been modified. Propose this to the user.
	 * @nowarn
	 */
	public void setNeedGeneration(boolean needGeneration)
	{
		if (needGeneration)
			status |= STATUS_NEED_GENERATION;
		else
			status &= ~STATUS_NEED_GENERATION;
	}

	/**
	 * Gets the flag if the requirements for the generator have not been met. Unable to perform the generation.
	 * @nowarn
	 */
	public boolean isInvalidGenerator()
	{
		return (status & STATUS_INVALID_GENERATOR) != 0;
	}

	/**
	 * Sets the flag if the requirements for the generator have not been met. Unable to perform the generation.
	 * @nowarn
	 */
	public void setInvalidGenerator(boolean invalidGenerator)
	{
		if (invalidGenerator)
			status |= STATUS_INVALID_GENERATOR;
		else
			status &= ~STATUS_INVALID_GENERATOR;
	}

	/**
	 * Gets the flag if the item has been edited in the visual view.
	 * @nowarn
	 */
	public boolean isClassnameSet()
	{
		return (status & STATUS_CLASSNAME_SET) != 0;
	}

	/**
	 * Sets the flag if the item has been edited in the visual view.
	 * @nowarn
	 */
	public void setClassnameSet(boolean classnameSet)
	{
		if (classnameSet)
			status |= STATUS_CLASSNAME_SET;
		else
			status &= ~STATUS_CLASSNAME_SET;
	}
}
