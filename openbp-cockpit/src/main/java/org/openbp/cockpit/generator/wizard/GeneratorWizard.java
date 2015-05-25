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
package org.openbp.cockpit.generator.wizard;

import java.lang.reflect.Constructor;
import java.util.List;

import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorCustomizer;
import org.openbp.cockpit.generator.GeneratorException;
import org.openbp.cockpit.generator.GeneratorPageDescriptor;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.string.StringBufferOutputStream;
import org.openbp.common.string.StringUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.PlaceholderItem;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.wizard.SequenceManagerImpl;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.components.wizard.WizardImpl;
import org.openbp.swing.components.wizard.WizardPage;

/**
 * Wizard dialog that displays possible generator options and invokes the generation process.
 *
 * @author Heiko Erhardt
 */
public class GeneratorWizard extends WizardImpl
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Selection page of the wizard */
	public static final String SELECTION_PAGE = "selection";

	/** Settings page of the wizard */
	public static final String SETTINGS_PAGE = "settings";

	/** Process page of the wizard */
	public static final String RESULT_PAGE = "result";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Generator context */
	protected GeneratorContext context;

	/**
	 * Original sequence of the wizard pages of the generator wizard.
	 * The sequence contains the wizard selection page, but no generator-specific pages.
	 */
	private SequenceManagerImpl originalSequence;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param resource Generator wizard resource
	 */
	public GeneratorWizard(final ResourceCollection resource)
	{
		super(resource);
	}

	/**
	 * Default constructor.
	 *
	 * @param resource Generator wizard resource
	 * @param item Item the generation process should be performed upon
	 */
	public GeneratorWizard(final ResourceCollection resource, final Item item)
	{
		this(resource);

		context = new GeneratorContext();
		context.setItem(item);
	}

	//////////////////////////////////////////////////
	// @@ WizardImpl overrides
	//////////////////////////////////////////////////

	/**
	 * Clears the wizard pages.
	 * Clears all pages and the sequence manager information
	 * Also clears the basic page sequence; will be set by the first call to updateGeneratorPageSequence ().
	 */
	public void clearPages()
	{
		super.clearPages();

		originalSequence = null;
	}

	//////////////////////////////////////////////////
	// @@ WizardListener implementation
	//////////////////////////////////////////////////

	/**
	 * Fires a wizard event.
	 *
	 * @param event Event to fire
	 * @return
	 * true: Processing can be continued.<br>
	 * false: The cancel flag of the event has been set by an event listener.
	 */
	protected boolean fireWizardEvent(final WizardEvent event)
	{
		Generator selectedGenerator = context.getSelectedGenerator();

		// The super method will link and show the result page if set
		if (! super.fireWizardEvent(event))
			return false;

		// Invoke the generator's customizer to handle the event
		if (selectedGenerator != null)
		{
			GeneratorCustomizer customizer = selectedGenerator.getCustomizer();
			if (customizer != null)
			{
				customizer.processWizardEvent(context, event);
				if (event.cancel)
					return false;
			}
		}

		if (event.eventType == WizardEvent.FINISH)
		{
			// Save the item if appropriate
			boolean save = false;

			if (selectedGenerator == null)
			{
				// No generator, creating an item
				save = true;
			}
			else
			{
				if (selectedGenerator.isItemGenerator())
				{
					// Generator, but also creating an item
					save = true;
				}

				selectedGenerator.saveSettings(context);
			}

			if (save)
			{
				Item item = context.getItem();

				// First, serialize the generator settings and store them in the item
				String info = null;
				GeneratorSettings settings = context.getGeneratorSettings();
				if (settings != null && selectedGenerator != null)
				{
					settings.beforeSerialization();

					StringBuffer sb = new StringBuffer();
					StringBufferOutputStream os = new StringBufferOutputStream(sb);
					try
					{
						selectedGenerator.getXmlDriver().serialize(settings, os);
					}
					catch (XMLDriverException e)
					{
						ExceptionUtil.printTrace(e);
					}

					info = sb.toString().trim();
					if (info.equals(""))
					{
						info = null;
					}
					else
					{
						// Remove xml method tag (or else the XML parser will complain)
						info = StringUtil.substitute(info, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
						info = StringUtil.substitute(info, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", "");
					}
				}
				item.setGeneratorInfo(info);

				// Add the item if the item is new and has not been saved yet; otherwise update the item
				// Placeholders serve for the creation of placeholder nodes only and are never saved.
				if (! (item instanceof PlaceholderItem))
				{
					boolean add = context.isNewItem() && ! context.isItemSaved();
					if (! ModelConnector.getInstance().saveItem(item, add))
					{
						event.cancel = true;
						return false;
					}
				}

				// Mark the item as saved. Do not initialize the item any more after saving.
				context.setItemSaved(true);
				context.setEmptyItem(false);

				// Get a reference to the saved item that is managed by the model for name uniqueness check and restoration purposes
				Item original;
				if (item instanceof Model)
				{
					original = ModelConnector.getInstance().getModelByQualifier(item.getQualifier());
				}
				else
				{
					original = ModelConnector.getInstance().getItemByQualifier(item.getQualifier(), false);
				}
				context.setOriginalItem(original);

				// After the item has been saved, we won't let the user back to the generator selection page
				removePage(GeneratorWizard.SELECTION_PAGE);
			}

			// Link the result (= generation) page if our generator requires one
			if (selectedGenerator != null)
			{
				boolean hasResultPage = true;
				GeneratorCustomizer customizer = selectedGenerator.getCustomizer();
				if (customizer != null)
				{
					hasResultPage = customizer.hasResultPage(context);
				}

				// Set the generator result page if we should invoke a generator
				if (hasResultPage)
				{
					if (getPage(GeneratorWizard.RESULT_PAGE) == null)
					{
						// Create the result page if not created yet
						WizardResultPage resultPage = new WizardResultPage(this);
						addPage(GeneratorWizard.RESULT_PAGE, resultPage);
					}
					setResultPageName(GeneratorWizard.RESULT_PAGE);
				}
			}
		}

		if (event.eventType == WizardEvent.CANCEL)
		{
			// Indicate abort
			context.setItem(null);
		}

		return true;
	}

	//////////////////////////////////////////////////
	// @@ Page sequence
	//////////////////////////////////////////////////

	/**
	 * Updates the wizard pages according to the selected generator type.
	 */
	public void updateGeneratorPageSequence()
	{
		Generator generator = context.getSelectedGenerator();
		SequenceManagerImpl manager = (SequenceManagerImpl) getManager();

		if (originalSequence == null)
		{
			// First time: Back up the page sequence
			try
			{
				originalSequence = (SequenceManagerImpl) manager.clone();
			}
			catch (CloneNotSupportedException e)
			{
				// Never happens
			}
		}
		else
		{
			// Restore the original page sequence
			try
			{
				String current = manager.getCurrent();

				manager = (SequenceManagerImpl) originalSequence.clone();
				setManager(manager);

				manager.setCurrent(current);
			}
			catch (CloneNotSupportedException e)
			{
				// Never happens
			}
		}

		if (generator != null)
		{
			context.setInvalidGenerator(false);

			// A generator type was selected, we can continue.
			// Perform the necessary preparations
			try
			{
				// Create the generator customizer class for this type
				generator.checkRequirements(context);
				generator.createSettings(context);
				generator.loadSettings(context);

				GeneratorCustomizer customizer = generator.getCustomizer();
				if (customizer != null)
				{
					customizer.beforePageSequenceUpdate(context, this);
				}

				// Create and link the custom pages if defined
				List customPages = generator.getCustomPageList();
				if (customPages != null)
				{
					// Determine the first generator page and its successor
					String currentPageName;
					String next;
					if (getPage(GeneratorWizard.SELECTION_PAGE) != null)
					{
						currentPageName = GeneratorWizard.SELECTION_PAGE;
						next = manager.getNext(currentPageName);
					}
					else
					{
						currentPageName = null;
						next = manager.getFirst();
					}

					// Custom pages, link them into the sequence
					int n = customPages.size();
					for (int i = 0; i < n; ++i)
					{
						GeneratorPageDescriptor pd = (GeneratorPageDescriptor) customPages.get(i);
						String pageName = pd.getName();

						WizardPage page = (WizardPage) getPage(pageName);

						if (page == null)
						{
							// If the page does not yet exist, create it
							String pageClassName = pd.getPageClassName();
							String objectClassName = pd.getObjectClassName();

							if (objectClassName != null)
							{
								// Object page, get/create the object
								Object o = context.getProperty(pageName);
								if (o == null)
								{
									o = ReflectUtil.instantiate(objectClassName, null,
										"generator settings object class");
									context.setProperty(pageName, o);
								}

								// Create property browser page and set the object
								page = new WizardObjectPage(this, pd);
							}
							else if (pageClassName != null)
							{
								try
								{
									Class pageClass = Class.forName(pageClassName);

									// We retrieve the Constructor of the page that uses one single parameter
									// of the type Wizard.

									Constructor[] ctrs = pageClass.getDeclaredConstructors();
									for (int ic = 0; ic < ctrs.length; ++ic)
									{
										Class[] params = ctrs[ic].getParameterTypes();
										if (params.length == 1 && params[0] == GeneratorWizard.class)
										{
											page = (WizardPage) ctrs[ic].newInstance(new Object[]
											{
												this
											});
											break;
										}
									}

									if (page == null)
										throw new GeneratorException("Class '" + pageClassName
											+ "' does not have a (GeneratorWizard) constructor.");
								}
								catch (Exception e)
								{
									String msg = LogUtil.error(getClass(), "Cannot instantiate page class $0.",
										pageClassName, e);
									String exMsg = ExceptionUtil.getNestedMessage(e);
									throw new GeneratorException(exMsg != null ? exMsg : msg);
								}
							}

							if (page != null)
							{
								// Get the description of the page from the page descriptor
								String pageDescription = pd.getDescription();
								page.setDescription(pageDescription);

								// Add it to the wizard
								addPage(pageName, page);
							}
						}

						if (page != null)
						{
							// Get the title of the page from the page descriptor
							String pageTitle = pd.getDisplayName();
							if (pageTitle == null)
								pageTitle = generator.getDisplayName();
							page.setTitle(pageTitle);

							if (pd.isFinish())
							{
								page.canFinish = true;
							}
						}

						// Link the page
						manager.chain(currentPageName, pageName);
						currentPageName = pageName;
					}

					if (next != null)
					{
						// Link last inserted page with element after selection page
						manager.chain(currentPageName, next);
					}
				}

				if (customizer != null)
				{
					customizer.afterPageSequenceUpdate(context, this);
				}
			}
			catch (GeneratorException e)
			{
				// Cannot continue, stay here
				context.setInvalidGenerator(true);
				JMsgBox.show(this, e.getMessage(), JMsgBox.ICON_ERROR);
			}
		}

		updateNavigator();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the generator context.
	 * @nowarn
	 */
	public GeneratorContext getContext()
	{
		return context;
	}
}
