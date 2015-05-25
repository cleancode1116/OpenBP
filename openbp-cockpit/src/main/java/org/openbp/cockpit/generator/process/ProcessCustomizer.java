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
package org.openbp.cockpit.generator.process;

import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorCustomizer;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.cockpit.itemeditor.ItemCreationUtil;
import org.openbp.cockpit.itemeditor.StandardItemEditor;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Customizer class for process item generation.
 *
 * @author Heiko Erhardt
 */
public class ProcessCustomizer extends GeneratorCustomizer
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Generator property: Process type of the process */
	public static final String PROCESS_TYPE = "ProcessType";

	/** Generator property: Skin name of the process */
	public static final String SKIN_NAME = "SkinName";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ProcessCustomizer()
	{
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Returns the class of the generator settings used by this generator.
	 *
	 * @return The class or null if the generator does not require a settings object
	 */
	public Class getSettingsClass()
	{
		// We have no special properties, we just need to remember the name of the generator.
		// So we return a plain generator settings class.
		return GeneratorSettings.class;
	}

	/**
	 * Determines if the generator should display a result page.
	 * @param context Generator context
	 * @return false by default
	 */
	public boolean hasResultPage(GeneratorContext context)
	{
		// Process generators don't use templates, so they don't have a result page.
		return false;
	}

	/**
	 * Processes a wizard event caused by a wizard page.
	 *
	 * @param context Generator context
	 * @param event Event to handle
	 */
	public void processWizardEvent(GeneratorContext context, WizardEvent event)
	{
		String pageName = event.wizard.getManager().getCurrent();

		if (pageName.equals(GeneratorWizard.SELECTION_PAGE))
		{
			if (event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.FINISH)
			{
				// Initialize the process according to the selected process type
				initializeProcess(context);
			}
		}

		else if (pageName.equals(StandardItemEditor.PROPERTY_PAGE))
		{
			if (event.eventType == WizardEvent.SHOW)
			{
				// If we do not have a type selection page...
				if (!GeneratorWizard.SELECTION_PAGE.equals(event.wizard.getManager().getFirst()))
				{
					// ... we need to initialize the process according to its process type now.
					initializeProcess(context);

					// Redisplay the process in the property browser
					// We have to do this because the event was already handled by the page *before* we get called.
					JaspiraWizardObjectPage page = (JaspiraWizardObjectPage) event.wizard.getPage(pageName);
					page.handleWizardEvent(event);
				}

				// Save the original item for later restore after a back button
				if (context.getOriginalItem() == null)
				{
					try
					{
						context.setOriginalItem((Item) context.getItem().clone());
					}
					catch (CloneNotSupportedException e)
					{
						// Never happens
					}
				}
			}

			else if (event.eventType == WizardEvent.BACK)
			{
				// Restore the original item after a back button
				try
				{
					context.setItem((Item) context.getOriginalItem().clone());
				}
				catch (CloneNotSupportedException e)
				{
					// Never happens
				}
			}
		}

		super.processWizardEvent(context, event);
	}

	/**
	 * Initializes the process.
	 *
	 * @param context Generator context
	 */
	private void initializeProcess(GeneratorContext context)
	{
		// Initialize the process according to the process type
		ProcessItem process = (ProcessItem) context.getItem();

		String processType = (String) context.getProperty(PROCESS_TYPE);
		process.setProcessType(processType);

		String skinName = (String) context.getProperty(SKIN_NAME);
		if (skinName != null)
		{
			process.setSkinName(skinName);
		}

		if (context.isEmptyItem() && context.isNewItem())
		{
			// Modify new processes
			updateItemName(context);
			updateItemStructure(context);
		}
	}

	/**
	 * Updates the name of the visual item.
	 *
	 * @param context Generator context
	 */
	public void updateItemName(GeneratorContext context)
	{
		ProcessItem process = (ProcessItem) context.getItem();

		// For new items, assign a default name/display name/description
		// based on the process type specified in the generator xml descriptor if they don't have a valid name already

		if (process.getName() != null)
		{
			ModelQualifier itemQualifier = process.getQualifier();
			if (ModelConnector.getInstance().getItemByQualifier(itemQualifier, false) == null)
			{
				// Name not present yet, continue
				return;
			}
		}

		// Generate a name for the item if a name format has been specified in the generator properties
		String processType = process.getProcessType();
		if (processType != null)
		{
			String name = processType;

			String nameSuffix = determineUniqueSuffix(process, name);
			if (nameSuffix != null)
			{
				name += nameSuffix;
			}
			process.setName(name);
		}
	}

	/**
	 * Updates the visual item before saving.
	 *
	 * @param context Generator context
	 */
	public void updateItemStructure(GeneratorContext context)
	{
		// For new items, set up default entry and final nodes according to the process type
		ProcessItem process = (ProcessItem) context.getItem();

		if (! process.getNodes().hasNext())
		{
			// Initialize empty processes
			// Determine which skin the process should use and initialize some process properties from the skin settings
			Skin processSkin = FigureUtil.determineProcessSkin(process);
			processSkin.initalizeNewProcess(process);

			// Re-setup the standard configuration of the process now that we have obtained the process type
			ItemCreationUtil.setupStandardConfiguration(process);

			// Update the geometry (i. e. the size) or the item node according to the default skin
			FigureUtil.updateItemGeometry(null, process);
		}
	}
}
