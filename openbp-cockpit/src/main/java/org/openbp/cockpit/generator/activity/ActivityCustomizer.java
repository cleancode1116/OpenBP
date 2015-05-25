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
package org.openbp.cockpit.generator.activity;

import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorCustomizer;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.guiclient.util.ClassNameBuilderUtil;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Customizer class for visual item generation.
 *
 * @author Heiko Erhardt
 */
public class ActivityCustomizer extends GeneratorCustomizer
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ActivityCustomizer()
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
		return GeneratorSettings.class;
	}

	/**
	 * Gets template name.
	 *
	 * @param context Generator context
	 * @return By default, the method returns the template name read from the generator XML file
	 */
	public String getTemplateName(GeneratorContext context)
	{
		JavaActivityItem activity = (JavaActivityItem) context.getItem();

		if (activity.getHandlerDefinition().getHandlerClassName() != null)
		{
			// Implementation class name specified, generate the activity source
			return super.getTemplateName(context);
		}

		// No implementation class name, no activity source
		return null;
	}

	/**
	 * Determines if the generator should display a result page.
	 * @param context Generator context
	 * @return
	 * true: If an implementation class name was specified.<br>
	 * false: Otherwise
	 */
	public boolean hasResultPage(GeneratorContext context)
	{
		return getTemplateName(context) != null;
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

		// After the selection page, update the implementation class name of the activity
		if (pageName.equals("property"))
		{
			if (event.eventType == WizardEvent.SHOW)
			{
				if (context.isEmptyItem() && !context.isClassnameSet())
				{
					JavaActivityItem activity = (JavaActivityItem) context.getItem();

					if (activity.getHandlerDefinition().getHandlerClassName() == null)
					{
						activity.getHandlerDefinition().setHandlerClassName(ClassNameBuilderUtil.constructActivityClassName(activity));

						// Redisplay the activity in the property browser
						// We have to do this because the event was already handled by the page *before* we get called.
						JaspiraWizardObjectPage page = (JaspiraWizardObjectPage) event.wizard.getPage(pageName);
						page.handleWizardEvent(event);
					}

					context.setClassnameSet(true);
				}
			}
		}

		super.processWizardEvent(context, event);
	}
}
