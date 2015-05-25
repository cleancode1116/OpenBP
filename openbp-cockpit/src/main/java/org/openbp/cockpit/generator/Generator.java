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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.common.template.TemplateEngine;
import org.openbp.common.template.TemplateEngineResult;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.jaspira.plugin.ConfigMgr;

/**
 * A template-based generation process for a particular item or item file.
 *
 * The generation process consists of<br>
 * 1. A check for the requirements of the generation (i. e. if particular item properties are set)<br>
 * 2. An option pre-processing step<br>
 * 3. The actual template-based generation<br>
 * 4. An option post-processing step
 *
 * @author Heiko Erhardt
 */
public class Generator extends DisplayObjectImpl
	implements Comparable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** A functional group specification that can be used as desired. */
	private String functionalGroup;

	/** Flag: Generator creates an item */
	private boolean itemGenerator;

	/** Sequence number that determines the order of displayed generator options in the wizard selection page */
	private int sequenceNr;

	/** Sequence number that determines the load order for the setting and depandant XML files */
	private int xmlLoaderSequenceNr;

	/** Item type this generation step is suitable for */
	private String itemType;

	/**
	 * Table of generator properties that will be used in the template-based generation process.
	 * Maps property names to arbitrary objects.
	 * This table contains the item the generation should be performed upon as well as
	 * custom properties that have been defined/edited by custom wizard page descriptors
	 * (see {@link GeneratorPageDescriptor#setObjectClassName}) and working properties of the generator.
	 */
	private Map initialProperties;

	/** List of required properties of the item (contains String objects) */
	private List requiredItemPropertyList;

	/** Custom page list (contains {@link GeneratorPageDescriptor} objects) */
	private List customPageList;

	/** Condition */
	private String condition;

	/** Name of the template class */
	private String templateName;

	/** Name of the default start page */
	private String defaultStartPageName;

	/** Name of the process customizer class (must implement the {@link GeneratorCustomizer} interface) */
	private String generatorCustomizerClassName;

	/** Flag if the 'Open generated file' check box in the result page should be displayed */
	private boolean showOpenResultCheckBox;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Generator customizer class */
	private transient GeneratorCustomizer customizer;

	/** Generator manager that loaded this generator */
	private GeneratorMgr generatorMgr;

	/** Class loader used to load template and setting classes */
	private ClassLoader classLoader;

	/** XML driver for generator settings */
	private XMLDriver xmlDriver;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Generator()
	{
		showOpenResultCheckBox = true;
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

		Generator src = (Generator) source;

		itemType = src.itemType;
		functionalGroup = src.functionalGroup;
		itemGenerator = src.itemGenerator;

		sequenceNr = src.sequenceNr;
		xmlLoaderSequenceNr = src.xmlLoaderSequenceNr;
		templateName = src.templateName;
		condition = src.condition;
		defaultStartPageName = src.defaultStartPageName;
		showOpenResultCheckBox = src.showOpenResultCheckBox;
		generatorCustomizerClassName = src.generatorCustomizerClassName;

		customizer = src.customizer;
		generatorMgr = src.generatorMgr;
		classLoader = src.classLoader;
		xmlDriver = src.xmlDriver;

		if (copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			initialProperties = CopyUtil.copyMap(src.initialProperties, CopyUtil.CLONE_VALUES);
			requiredItemPropertyList = (List) CopyUtil.copyCollection(src.requiredItemPropertyList, CopyUtil.CLONE_VALUES);
			customPageList = (List) CopyUtil.copyCollection(src.customPageList, CopyUtil.CLONE_VALUES);
		}
		else
		{
			// Shallow clone
			initialProperties = src.initialProperties;
			requiredItemPropertyList = src.requiredItemPropertyList;
			customPageList = src.customPageList;
		}
	}

	//////////////////////////////////////////////////
	// @@ Execution
	//////////////////////////////////////////////////

	/**
	 * Performs the generation pre-processing.
	 *
	 * @param context Generator context
	 */
	public void preProcess(GeneratorContext context)
		throws Exception
	{
		if (customizer != null)
		{
			// Let the customizer do this if it wants to
			customizer.preProcess(context);
		}
		else
		{
			runTemplateEngine(context);
		}
	}

	/**
	 * Performs the generation process.
	 *
	 * @param context Generator context
	 */
	public void performProcess(GeneratorContext context)
		throws Exception
	{
		if (customizer != null)
		{
			// Let the customizer do this if it wants to
			customizer.performProcess(context);
		}
	}

	/**
	 * Performs the generation post-process.
	 *
	 * @param context Generator context
	 */
	public void postProcess(GeneratorContext context)
		throws Exception
	{
		if (customizer != null)
		{
			// Let the customizer do this if it wants to
			customizer.postProcess(context);
		}
	}

	/**
	 * Runs the template engine.
	 *
	 * @param context Generator context
	 */
	public void runTemplateEngine(GeneratorContext context)
		throws Exception
	{
		// By default, use the template name from the XML file
		String tn = templateName;

		if (customizer != null)
		{
			tn = customizer.getTemplateName(context);
		}

		if (tn != null)
		{
			// Perform template-based generation step

			// Search the template file in the chosen template set directory and backwards
			Item item = context.getItem();
			Model model = item.getOwningModel();
			String modelPath = model.getModelPath();

			context.setProperty("modelPath", modelPath);
			context.setProperty("model", model);
			context.setProperty("item", item);
			context.setProperty("overwriteMode", context.getOverwriteMode());
			context.setProperty("settings", context.getGeneratorSettings());

			TemplateEngine engine = new TemplateEngine();
			engine.setOutputDir(context.getOutputRootDir());
			engine.setProperties(context.getProperties());
			engine.setClassLoader(getClassLoader());

			TemplateEngineResult result = engine.processTemplate(tn, null);
			if (result == null)
			{
				// No files generated, but also no error, so create a dummy
				result = new TemplateEngineResult();
			}
			context.setTemplateEngineResult(result);
		}
	}

	//////////////////////////////////////////////////
	// @@ Requirements check
	//////////////////////////////////////////////////

	/**
	 * Checks the preconditions of this generator.
	 *
	 * @return
	 * true: The preconditions are met, the generator can be displayed.<br>
	 * false: The generator should not appear in the list of generators.
	 */
	public boolean checkPrecondition()
	{
		return ConfigMgr.getInstance().evaluate(condition);
	}

	/**
	 * Checks the requirements of the generation process for the current item.
	 *
	 * @param context Generator context
	 * @throws GeneratorException If one of the necessary requirements is not met.
	 * The exception message should describe the problem.
	 */
	public void checkRequirements(GeneratorContext context)
	{
		Item item = context.getItem();
		if (requiredItemPropertyList != null && item != null)
		{
			StringBuffer errMsg = null;
			ObjectDescriptor od = null;

			int n = requiredItemPropertyList.size();
			for (int i = 0; i < n; ++i)
			{
				String property = (String) requiredItemPropertyList.get(i);

				Object value = null;
				try
				{
					value = PropertyAccessUtil.getProperty(item, property);
				}
				catch (PropertyException e)
				{
					System.err.println("Cannot access property '" + property + "': " + e);
				}

				if (value == null)
				{
					// Property not present
					if (errMsg == null)
					{
						// Initial text
						errMsg = new StringBuffer("The following required properties of the component have not been set:\n");
						try
						{
							// Get the object descriptor, but skip errors
							od = ObjectDescriptorMgr.getInstance().getDescriptor(item.getClass(), 0);
						}
						catch (XMLDriverException e)
						{
							System.err.println("Cannot access object descriptor for class '" + item.getClass().getName() + "': " + e);
						}
					}
					else
					{
						errMsg.append('\n');
					}

					String displayName = null;
					if (od != null)
					{
						// Try to get the display name of the property from the
						// object descriptor of the item
						PropertyDescriptor pd = od.getProperty(property);
						if (pd != null)
							displayName = pd.getDisplayName();
					}
					if (displayName == null)
						displayName = property;

					// Property not set, issue error message
					errMsg.append("    ");
					errMsg.append(displayName);
				}
			}

			if (errMsg != null)
			{
				throw new GeneratorException(errMsg.toString());
			}
		}

		if (customizer != null)
		{
			// Perform pre-process step
			customizer.checkRequirements(context);
		}
	}

	/**
	 * Creates a settings object for the customizer if it requires one.
	 *
	 * @param context Generator context
	 */
	public void createSettings(GeneratorContext context)
	{
		if (customizer != null)
		{
			// Perform pre-process step
			customizer.createSettings(context);
		}
	}

	/**
	 * Loads the settings from persistent storage into the newly created settings object.
	 * Does nothing by default.
	 *
	 * @param context Generator context
	 */
	public void loadSettings(GeneratorContext context)
	{
		if (customizer != null)
		{
			customizer.loadSettings(context);
		}
	}

	/**
	 * Saves the settings from the settings object to persistent storage.
	 * Does nothing by default.
	 *
	 * @param context Generator context
	 */
	public void saveSettings(GeneratorContext context)
	{
		if (customizer != null)
		{
			customizer.saveSettings(context);
		}
	}


	//////////////////////////////////////////////////
	// @@ Customizer
	//////////////////////////////////////////////////

	/**
	 * Gets the generator customizer class.
	 * @nowarn
	 */
	public GeneratorCustomizer getCustomizer()
	{
		return customizer;
	}

	/**
	 * Creates a new instance of the customizer class if specified.
	 *
	 * @throws GeneratorException On error
	 */
	protected void createCustomizer()
	{
		if (customizer != null || generatorCustomizerClassName == null)
		{
			// No customizer specified or already present
			return;
		}

		customizer = (GeneratorCustomizer) ReflectUtil.instantiate(generatorCustomizerClassName, getClassLoader(), GeneratorCustomizer.class, "generator customizer");
		customizer.setGenerator(this);
	}

	//////////////////////////////////////////////////
	// @@ Generator properties
	//////////////////////////////////////////////////

	/**
	 * Adds an initial property.
	 * @param initialProperty The initial property to add
	 */
	public void addInitialProperty(GeneratorProperty initialProperty)
	{
		if (initialProperties == null)
		{
			initialProperties = new HashMap();
		}
		initialProperties.put(initialProperty.getName(), initialProperty.getValue());
	}

	/**
	 * Copies the initial properties to the property table of the generator context.
	 *
	 * @param context Generator context
	 */
	public void copyInitialPropertiesToContext(GeneratorContext context)
	{
		if (initialProperties != null)
		{
			for (Iterator it = initialProperties.keySet().iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				String value = (String) initialProperties.get(key);
				context.setProperty(key, value);
			}
		}
	}

	/**
	 * Removes the initial property values from the property table of the generator context.
	 *
	 * @param context Generator context
	 */
	public void removeInitialPropertiesToContext(GeneratorContext context)
	{
		if (initialProperties != null)
		{
			for (Iterator it = initialProperties.keySet().iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				context.removeProperty(key);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the a functional group specification that can be used as desired..
	 * @nowarn
	 */
	public String getFunctionalGroup()
	{
		return functionalGroup;
	}

	/**
	 * Sets the a functional group specification that can be used as desired..
	 * @nowarn
	 */
	public void setFunctionalGroup(String functionalGroup)
	{
		this.functionalGroup = functionalGroup;
	}

	/**
	 * Gets the flag: Generator generators an item.
	 * @nowarn
	 */
	public boolean isItemGenerator()
	{
		return itemGenerator;
	}

	/**
	 * Sets the flag: Generator generators an item.
	 * @nowarn
	 */
	public void setItemGenerator(boolean itemGenerator)
	{
		this.itemGenerator = itemGenerator;
	}

	/**
	 * Gets the sequence number that determines the order of displayed generator options in the context menu.
	 * @nowarn
	 */
	public int getSequenceNr()
	{
		return sequenceNr;
	}

	/**
	 * Sets the sequence number that determines the order of displayed generator options in the context menu.
	 * @nowarn
	 */
	public void setSequenceNr(int sequenceNr)
	{
		this.sequenceNr = sequenceNr;
	}

	/**
	 * Gets the sequence number that determines the load order for the setting and depandant XML files.
	 * @nowarn
	 */
	public int getXmlLoaderSequenceNr()
	{
		return xmlLoaderSequenceNr;
	}

	/**
	 * Sets the sequence number that determines the load order for the setting and depandant XML files.
	 * @nowarn
	 */
	public void setXmlLoaderSequenceNr(int xmlLoaderSequenceNr)
	{
		this.xmlLoaderSequenceNr = xmlLoaderSequenceNr;
	}

	/**
	 * Gets the item type this generation step is suitable for.
	 * @nowarn
	 */
	public String getItemType()
	{
		return itemType;
	}

	/**
	 * Sets the item type this generation step is suitable for.
	 * @nowarn
	 */
	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}

	/**
	 * Gets the list of required properties of the item.
	 * @return An iterator of String objects
	 */
	public Iterator getRequiredItemPropertys()
	{
		if (requiredItemPropertyList == null)
			return EmptyIterator.getInstance();
		return requiredItemPropertyList.iterator();
	}

	/**
	 * Adds a required property.
	 * @param requiredItemProperty The required property to add
	 */
	public void addRequiredItemProperty(String requiredItemProperty)
	{
		if (requiredItemPropertyList == null)
			requiredItemPropertyList = new ArrayList();
		requiredItemPropertyList.add(requiredItemProperty);
	}

	/**
	 * Clears the list of required properties of the item.
	 */
	public void clearRequiredItemPropertys()
	{
		requiredItemPropertyList = null;
	}

	/**
	 * Gets the list of required properties of the item.
	 * @return A list of String objects
	 */
	public List getRequiredItemPropertyList()
	{
		return requiredItemPropertyList;
	}

	/**
	 * Sets the list of required properties of the item.
	 * @param requiredItemPropertyList A list of String objects
	 */
	public void setRequiredItemPropertyList(List requiredItemPropertyList)
	{
		this.requiredItemPropertyList = requiredItemPropertyList;
	}

	/**
	 * Gets the condition.
	 * @nowarn
	 */
	public String getCondition()
	{
		return condition;
	}

	/**
	 * Sets the condition.
	 * @nowarn
	 */
	public void setCondition(String condition)
	{
		this.condition = condition;
	}

	/**
	 * Gets the custom page list.
	 * @return An iterator of {@link GeneratorPageDescriptor} objects
	 */
	public Iterator getCustomPages()
	{
		if (customPageList == null)
			return EmptyIterator.getInstance();
		return customPageList.iterator();
	}

	/**
	 * Adds a custom page.
	 * @param customPage The custom page to add
	 */
	public void addCustomPage(GeneratorPageDescriptor customPage)
	{
		if (customPageList == null)
			customPageList = new ArrayList();
		customPageList.add(customPage);
	}

	/**
	 * Clears the custom page list.
	 */
	public void clearCustomPages()
	{
		customPageList = null;
	}

	/**
	 * Gets the custom page list.
	 * @return A list of {@link GeneratorPageDescriptor} objects
	 */
	public List getCustomPageList()
	{
		return customPageList;
	}

	/**
	 * Sets the custom page list.
	 * @param customPageList A list of {@link GeneratorPageDescriptor} objects
	 */
	public void setCustomPageList(List customPageList)
	{
		this.customPageList = customPageList;
	}

	/**
	 * Gets the name of the template class.
	 * @nowarn
	 */
	public String getTemplateName()
	{
		return templateName;
	}

	/**
	 * Sets the name of the template class.
	 * @nowarn
	 */
	public void setTemplateName(String templateName)
	{
		this.templateName = templateName;
	}

	/**
	 * Gets the name of the default start page.
	 * This page is displayed when an existing item is opened (needed for item generators only).
	 * @nowarn
	 */
	public String getDefaultStartPageName()
	{
		return defaultStartPageName;
	}

	/**
	 * Sets the name of the default start page.
	 * This page is displayed when an existing item is opened (needed for item generators only).
	 * @nowarn
	 */
	public void setDefaultStartPageName(String defaultStartPageName)
	{
		this.defaultStartPageName = defaultStartPageName;
	}

	/**
	 * Gets the flag if the 'Open generated file' check box in the result page should be displayed.
	 * @nowarn
	 */
	public boolean isShowOpenResultCheckBox()
	{
		return showOpenResultCheckBox;
	}

	/**
	 * Sets the flag if the 'Open generated file' check box in the result page should be displayed.
	 * @nowarn
	 */
	public void setShowOpenResultCheckBox(boolean showOpenResultCheckBox)
	{
		this.showOpenResultCheckBox = showOpenResultCheckBox;
	}

	/**
	 * Gets the name of the process customizer class (must implement the {@link GeneratorCustomizer} interface).
	 * @nowarn
	 */
	public String getGeneratorCustomizerClassName()
	{
		return generatorCustomizerClassName;
	}

	/**
	 * Sets the name of the process customizer class (must implement the {@link GeneratorCustomizer} interface).
	 * @nowarn
	 */
	public void setGeneratorCustomizerClassName(String generatorCustomizerClassName)
	{
		this.generatorCustomizerClassName = generatorCustomizerClassName;
	}

	/**
	 * Gets the generator manager that loaded this generator.
	 * @nowarn
	 */
	public GeneratorMgr getGeneratorMgr()
	{
		return generatorMgr;
	}

	/**
	 * Sets the generator manager that loaded this generator.
	 * @nowarn
	 */
	public void setGeneratorMgr(GeneratorMgr generatorMgr)
	{
		this.generatorMgr = generatorMgr;
	}

	/**
	 * Gets the class loader used to load template and setting classes.
	 * @nowarn
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * Sets the class loader used to load template and setting classes.
	 * @nowarn
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	/**
	 * Gets the xML driver for generator settings.
	 * @nowarn
	 */
	public XMLDriver getXmlDriver()
	{
		return xmlDriver;
	}

	/**
	 * Sets the xML driver for generator settings.
	 * @nowarn
	 */
	public void setXmlDriver(XMLDriver xmlDriver)
	{
		this.xmlDriver = xmlDriver;
	}

	//////////////////////////////////////////////////
	// @@ Comparable implementation
	//////////////////////////////////////////////////

	/**
	 * Compares this object to another object.
	 *
	 * @param o Object to compare to
	 * @return see the java.lang.Comparable interface
	 */
	public int compareTo(Object o)
	{
		Generator generator = (Generator) o;

		return this.getSequenceNr() - generator.getSequenceNr();
	}
}
