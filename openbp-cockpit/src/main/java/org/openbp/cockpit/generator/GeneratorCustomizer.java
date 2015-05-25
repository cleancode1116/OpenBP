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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.common.CollectionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Customizer class used to customize a template generation process described by the
 * {@link Generator} class.
 * The customizer class can access the item the generation process is to be performed upon
 * using the context.getItem() method. It may also add or modify generation properties
 * (see {@link GeneratorContext#setProperty}) that will be used as property table for the
 * template-based generation process.
 *
 * @author Heiko Erhardt
 */
public abstract class GeneratorCustomizer
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Generator that owns this customizer instance */
	private Generator generator;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public GeneratorCustomizer()
	{
	}

	/**
	 * Creates a settings object for this customizer if it requires one.
	 *
	 * @param context Generator context
	 * @return The new settings object or null if a settings class is not defined by the customizer
	 */
	public Object createSettings(final GeneratorContext context)
	{
		Class cls = getSettingsClass();

		boolean create = false;

		// Create new settings only if the current settings are different from what we need
		GeneratorSettings settings = context.getGeneratorSettings();
		if (settings != null)
		{
			if (! settings.getClass().equals(cls))
			{
				create = true;
			}
		}
		else
		{
			if (cls != null)
				create = true;
		}

		if (create)
		{
			if (cls != null)
			{
				settings = (GeneratorSettings) ReflectUtil.instantiate(cls, GeneratorSettings.class,
					"generator settings");
				settings.setGeneratorName(generator.getName());
			}

			context.setGeneratorSettings(settings);
			context.setProperty(GeneratorWizard.SETTINGS_PAGE, settings);
		}

		if (settings != null)
		{
			// Set the model the context will need to resolve item references
			settings.setModel(context.getItem().getModel());
		}

		return settings;
	}

	/**
	 * Loads the settings from persistent storage into the newly created settings object.
	 * Does nothing by default.
	 *
	 * @param context Generator context
	 */
	public void loadSettings(final GeneratorContext context)
	{
	}

	/**
	 * Saves the settings from the settings object to persistent storage.
	 * Does nothing by default.
	 *
	 * @param context Generator context
	 */
	public void saveSettings(final GeneratorContext context)
	{
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Returns the class of the generator settings used by this generator.
	 *
	 * @return The class or null if the generator does not require a settings object<br>
	 * The default implementation returns null.
	 */
	public Class getSettingsClass()
	{
		return null;
	}

	/**
	 * Returns the class of any dependent objects of the generator settings class
	 * that must be serialized to the generator info of an edited item.
	 *
	 * @return An array of classes or null if there are not dependent classes.<br>
	 * The default implementation returns null.
	 */
	public Class[] getDependentSettingsClasses()
	{
		return null;
	}

	/**
	 * Gets template name.
	 *
	 * @param context Generator context
	 * @return By default, the method returns the template name read from the generator XML file
	 */
	public String getTemplateName(final GeneratorContext context)
	{
		return generator.getTemplateName();
	}

	/**
	 * Determines if the generator should display a result page.
	 * @param context Generator context
	 * @return true by default
	 */
	public boolean hasResultPage(final GeneratorContext context)
	{
		return true;
	}

	/**
	 * This method is executed after the generation process has been selected
	 * in order to check any conditions that need to be fullfilled for the process to work.
	 * The method will throw an exception describing the error if a precondition is not met.
	 * This method can also be used to initialize generator properties.
	 *
	 * @param context Generator context
	 * @throws GeneratorException On error
	 */
	public void checkRequirements(final GeneratorContext context)
	{
	}

	/**
	 * Template method that is called before the page sequence has been updated.
	 *
	 * @param context Context
	 * @param wizard Generator wizard
	 */
	public void beforePageSequenceUpdate(final GeneratorContext context, final GeneratorWizard wizard)
	{
	}

	/**
	 * Template method that is called after the page sequence has been updated.
	 *
	 * @param context Context
	 * @param wizard Generator wizard
	 */
	public void afterPageSequenceUpdate(final GeneratorContext context, final GeneratorWizard wizard)
	{
	}

	/**
	 * Pre-process step that is executed before the actual generation step.
	 * The default implementation runs the template engine.
	 *
	 * @param context Generator context
	 * @throws GeneratorException On error
	 */
	public void preProcess(final GeneratorContext context)
		throws Exception
	{
		generator.runTemplateEngine(context);
	}

	/**
	 * Main processing step.
	 *
	 * @param context Generator context
	 * @throws GeneratorException On error
	 */
	public void performProcess(final GeneratorContext context)
		throws Exception
	{
	}

	/**
	 * Post-process step that is executed after the actual generation step.
	 *
	 * @param context Generator context
	 * @throws GeneratorException On error
	 */
	public void postProcess(final GeneratorContext context)
		throws Exception
	{
	}

	/**
	 * Processes a wizard event caused by a generator wizard page.
	 *
	 * @param context Generator context
	 * @param event Event to handle
	 */
	public void processWizardEvent(final GeneratorContext context, final WizardEvent event)
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the generator that owns this customizer instance.
	 * @nowarn
	 */
	public Generator getGenerator()
	{
		return generator;
	}

	/**
	 * Sets the generator that owns this customizer instance.
	 * @nowarn
	 */
	public void setGenerator(final Generator generator)
	{
		this.generator = generator;
	}

	//////////////////////////////////////////////////
	// @@ Helpers for name generation etc.
	//////////////////////////////////////////////////

	/**
	 * Evaluates a format descrition.
	 *
	 * @param format Format<br>
	 * The format consists of arbitrary text, which may contain "$identifier$" placeholders.
	 * These placeholders will be substituted against generator properties. If the property
	 * does not exist, nothing will be inserted.
	 * @param context Generator context
	 * @return The evaluated string or null if the format is null, the trimmed result
	 * of the evaluation is empty or if a placeholder doesn't exist.
	 */
	public String evaluateFormat(final GeneratorContext context, final String format)
	{
		if (format == null)
			return null;

		StringBuffer sb = new StringBuffer();

		StringTokenizer st = new StringTokenizer(format, "$", true);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (token.equals("$"))
			{
				if (st.hasMoreTokens())
				{
					String name = st.nextToken();

					Object value = context.getProperty(name);
					if (value == null)
						return null;

					if (value != null)
					{
						sb.append(value.toString());
					}

					if (st.hasMoreTokens())
					{
						// Eat the terminating '$'
						st.nextToken();
					}
				}
				continue;
			}

			sb.append(token);
		}

		String ret = sb.toString().trim();
		if (ret.length() == 0)
			ret = null;
		return ret;
	}

	/**
	 * Determines a unique suffix for the given name that will make
	 * the item name unique.
	 *
	 * @param item Item to generate a name for
	 * @param name Name to use as base for the new name
	 * @return The suffix ("1", "2", ... according to names like "name", "name2", ...)
	 * or null if the supplied name is already unique
	 */
	public String determineUniqueSuffix(final Item item, final String name)
	{
		Model model = item.getModel();
		String itemType = item.getItemType();

		Iterator itemIterator = model.getItems(itemType);
		List items = new ArrayList();
		CollectionUtil.addAll(items, itemIterator);

		String result = NamedObjectCollectionUtil.createUniqueId(items, name);
		if (result.equals(name))
			return null;
		return result.substring(name.length());
	}
}
