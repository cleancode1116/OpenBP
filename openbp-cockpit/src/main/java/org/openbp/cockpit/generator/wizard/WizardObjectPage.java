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

import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorPageDescriptor;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.swing.components.wizard.Wizard;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Wizard property browser page.
 * This page will be constructed based on a custom wizard page descriptor (class {@link GeneratorPageDescriptor})
 * that defines an object to be edited.
 *
 * @author Heiko Erhardt
 */
public class WizardObjectPage extends JaspiraWizardObjectPage
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Page descriptor of the generator for this page */
	private GeneratorPageDescriptor pageDescriptor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 * @param pageDescriptor Page descriptor of the generator for this page
	 */
	public WizardObjectPage(Wizard wizard, GeneratorPageDescriptor pageDescriptor)
	{
		super(wizard);

		this.pageDescriptor = pageDescriptor;

		if (!((GeneratorWizard) wizard).getContext().isNewItem())
		{
			canFinish = true;
		}
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		GeneratorContext context = getContext();

		if (event.eventType == WizardEvent.SHOW)
		{
			Object o = context.getProperty(pageDescriptor.getName());

			try
			{
				getPropertyBrowser().setObject(o, true);
				if (context.isNewItem())
				{
					// For new items, we assume any edited objects to be modified in order to invoke the validator
					getPropertyBrowser().setObjectModified(true);
				}
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}
		}
		else if (event.eventType == WizardEvent.BACK || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.FINISH)
		{
			if (getPropertyBrowser().isObjectModified())
			{
				// Remember that a (re-)generation should be performed when finishing the wizard after changing settings
				context.setNeedGeneration(true);
			}
		}
		else if (event.eventType == WizardEvent.CANCEL)
		{
			// Reset the property browser in order to prevent validation error messages
			getPropertyBrowser().reset();
		}

		// This will save the edited object
		super.handleWizardEvent(event);
	}

	//////////////////////////////////////////////////
	// @@ Save strategy and object change listener
	//////////////////////////////////////////////////

	/**
	 * Called when the object needs to be saved.
	 *
	 * @param pb Property browser
	 * @return
	 * true: Save successful.<br>
	 * false: Save failed.
	 */
	public boolean executeSave(PropertyBrowser pb)
	{
		Object o = pb.getModifiedObject();

		GeneratorContext context = getContext();
		context.setProperty(pageDescriptor.getName(), o);

		if (o instanceof GeneratorSettings)
		{
			// If we are editing a settings page, save the settings also to the context
			context.setGeneratorSettings((GeneratorSettings) o);
		}

		return true;
	}

	//////////////////////////////////////////////////
	// @@ Convenience methods
	//////////////////////////////////////////////////

	/**
	 * Gets the generator context.
	 * @nowarn
	 */
	public GeneratorContext getContext()
	{
		return ((GeneratorWizard) getWizard()).getContext();
	}

	/**
	 * Gets the currently selected generator.
	 *
	 * @return The generator or null if no generator has been selected yet
	 */
	public Generator getGenerator()
	{
		return ((GeneratorWizard) getWizard()).getContext().getSelectedGenerator();
	}

	/**
	 * Gets the generator settings.
	 * @nowarn
	 */
	public GeneratorSettings getGeneratorSettings()
	{
		return ((GeneratorWizard) getWizard()).getContext().getGeneratorSettings();
	}
}
