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
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.jaspira.gui.wizard.JaspiraWizardPage;

/**
 * Base class of all wizard pages of the generator.
 *
 * @author Heiko Erhardt
 */
public class WizardCustomPage extends JaspiraWizardPage
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public WizardCustomPage(GeneratorWizard wizard)
	{
		super(wizard);
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
