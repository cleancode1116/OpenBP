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
package org.openbp.jaspira.gui.wizard;

import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.openbp.jaspira.propertybrowser.ObjectChangeListener;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.jaspira.propertybrowser.SaveStrategy;
import org.openbp.swing.components.wizard.Wizard;
import org.openbp.swing.components.wizard.WizardEvent;

/**
 * Wizard property browser page.
 * Wizard page that contains an property browser.
 *
 * @author Heiko Erhardt
 */
public abstract class JaspiraWizardObjectPage extends JaspiraWizardPage
	implements ObjectChangeListener, SaveStrategy
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Property browser */
	private PropertyBrowserImpl propertyBrowser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public JaspiraWizardObjectPage(Wizard wizard)
	{
		super(wizard);

		// Construct the UI
		propertyBrowser = new PropertyBrowserImpl(this, null);
		propertyBrowser.setRootVisible(false);

		JScrollPane sp = new JScrollPane(propertyBrowser);
		sp.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPanel().add(sp);

		canMoveForward = true;
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		if (event.eventType == WizardEvent.SHOW)
		{
			propertyBrowser.setObjectModified(false);
		}
		else if (event.eventType == WizardEvent.BACK || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.FINISH)
		{
			// Save the object currently edited
			if (!propertyBrowser.saveObject())
			{
				if (event.eventType == WizardEvent.BACK)
				{
					propertyBrowser.setObjectModified(false);
				}
				else
				{
					event.cancel = true;
				}
			}
		}
	}

	/**
	 * Gets the property browser.
	 * @nowarn
	 */
	public PropertyBrowser getPropertyBrowser()
	{
		return propertyBrowser;
	}

	//////////////////////////////////////////////////
	// @@ Save strategy and object change listener
	//////////////////////////////////////////////////

	/**
	 * Override this method when the object needs to be saved.
	 * It will be called if the user presses the 'Finish', 'Next' or 'Back' button.<br>
	 * Call {@link #getPropertyBrowser} ().getModifiedObject () to retrieve the object.
	 *
	 * @param editor Property browser
	 * @return
	 *		true	Save successful. The wizard will continue to the next page.<br>
	 *		false	Save failed. The page will remain active.
	 */
	public abstract boolean executeSave(PropertyBrowser editor);

	/**
	 * Is performed if the object was changed.
	 *
	 * @param original The object without any changes
	 * @param modified The modified object
	 */
	public void objectChanged(Object original, Object modified)
	{
	}
}
