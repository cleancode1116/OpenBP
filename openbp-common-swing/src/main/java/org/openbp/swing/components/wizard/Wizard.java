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
package org.openbp.swing.components.wizard;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.openbp.common.rc.ResourceCollection;

/**
 * Wizard component.
 *
 * @author Heiko Erhardt
 */
public interface Wizard
{
	//////////////////////////////////////////////////
	// @@ Page access
	//////////////////////////////////////////////////

	/**
	 * Adds a page to the wizard and links it into the sequence manager's list.
	 * The pages will be displayed in the sequence
	 * they have been added to the wizard using this method.
	 *
	 * @param name Name of the page. Use this name for any sequence
	 * manager operations (see {@link #getManager}).
	 * @param page page to add.
	 * The page should be a subclass of {@link WizardPage}
	 */
	public void addAndLinkPage(String name, Component page);

	/**
	 * Adds a page to the wizard, but does not link it into the sequence manager's list.
	 * You have to use the methods of the sequence manager (see {@link #getManager}) to
	 * define the order the pages will appear.
	 *
	 * @param name Name of the page. Use this name for any sequence
	 * manager operations (see {@link #getManager}).
	 * @param page page to add.
	 * The page should be a subclass of {@link WizardPage}
	 */
	public void addPage(String name, Component page);

	/**
	 * Removes a page from the wizard and the sequence manager.
	 *
	 * @param name Name of the page
	 */
	public void removePage(String name);

	/**
	 * Gets a page by its name.
	 *
	 * @param name Name of the page or null for the current page
	 * @return The page component (usually an instance of {@link WizardPage})
	 */
	public Component getPage(String name);

	/**
	 * Gets the list of page names.
	 *
	 * @return An iterator of wizard page names (strings)
	 */
	public Iterator getPageNames();

	/**
	 * Gets the list of pages.
	 *
	 * @return An iterator of wizard pages (Component objects)
	 */
	public Iterator getPages();

	/**
	 * Gets the wizard result page name.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager. Note that the result page
	 * component must have been added using the {@link #addPage} method.
	 *
	 * @return The name of the result page or null if no result page is set
	 */
	public String getResultPageName();

	/**
	 * Sets the wizard result page name.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager. Note that the result page
	 * component must have been added using the {@link #addPage} method.
	 *
	 * @param resultPageName The name of the result page or null for no result page
	 */
	public void setResultPageName(String resultPageName);

	/**
	 * Adds a result page to the wizard.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager.<br>
	 * This method is a convenience method for {@link #addPage} + {@link #setResultPageName}
	 *
	 * @param resultPageName The name of the result page or null for no result page
	 * @param page Page component to add (usually a {@link WizardPage})
	 */
	public void setResultPage(String resultPageName, Component page);

	/**
	 * Clears the wizard pages.
	 * Clears all pages and the sequence manager information
	 */
	public void clearPages();

	//////////////////////////////////////////////////
	// @@ Navigation
	//////////////////////////////////////////////////

	/**
	 * Displays the first page in the sequence.
	 */
	public void displayFirst();

	/**
	 * Displays the previous page.
	 * Also called if the user presses the 'Back' button.
	 */
	public void displayBack();

	/**
	 * Displays the next page.
	 * Also called if the user presses the 'Next' button.
	 */
	public void displayNext();

	/**
	 * Finishes the wizard.
	 * Also called if the user presses the 'Finish' button.
	 */
	public void finish();

	/**
	 * Cancels the wizard.
	 * Also called if the user presses the 'Cancel' button.
	 */
	public void cancel();

	/**
	 * Closes the wizard.
	 * Also called if the user presses the 'Close' button.
	 */
	public void close();

	/**
	 * Determines if we can return to the previous page.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canMoveBackward();

	/**
	 * Determines if there is a previous page.
	 * @nowarn
	 */
	public boolean hasBackward();

	/**
	 * Determines if we can advance to the next page.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canMoveForward();

	/**
	 * Determines if there is a next page.
	 * @nowarn
	 */
	public boolean hasForward();

	/**
	 * Determines if we can finish the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canFinish();

	/**
	 * Determines if we can cancel the wizard dialog at this point.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canCancel();

	/**
	 * Shows a particular page and updates the navigator.
	 *
	 * @param name Name of the page to show or null for the current one
	 */
	public void displayPage(String name);

	/**
	 * Updates the navigation bar.
	 * This method should be called if a page that does not make use of the wizard data model
	 * (see {@link #setDataModel(WizardDataModel)}) has changed its status and wants to update the state of the navigation
	 * bar buttons accordingly.
	 */
	public void updateNavigator();

	//////////////////////////////////////////////////
	// @@ Wizard events
	//////////////////////////////////////////////////

	/**
	 * Adds a wizard event listener.
	 * The listener will be notified each time a property value changes.
	 *
	 * @param listener Listener
	 */
	public void addWizardListener(WizardListener listener);

	/**
	 * Removes a wizard event listener.
	 *
	 * @param listener Listener
	 */
	public void removeWizardListener(WizardListener listener);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data collection model holding the data of the pages.
	 * @nowarn
	 */
	public WizardDataModel getDataModel();

	/**
	 * Sets the data collection model holding the data of the pages.
	 * @nowarn
	 */
	public void setDataModel(WizardDataModel model);

	/**
	 * Gets the page sequence manager.
	 * @nowarn
	 */
	public SequenceManager getManager();

	/**
	 * Sets the page sequence manager.
	 * @nowarn
	 */
	public void setManager(SequenceManager manager);

	/**
	 * Gets the wizard resource.
	 * @nowarn
	 */
	public ResourceCollection getResource();

	/**
	 * Sets the wizard resource.
	 * @nowarn
	 */
	public void setWizardResource(ResourceCollection wizardResourceCollection);

	/**
	 * Gets the optional resource prefix for wizard page resources.
	 * @nowarn
	 */
	public String getPageResourcePrefix();

	/**
	 * Sets the optional resource prefix for wizard page resources.
	 * @nowarn
	 */
	public void setPageResourcePrefix(String pageResourcePrefix);

	/**
	 * Sets the show help.
	 * @nowarn
	 */
	public void setShowHelp(boolean showHelp);

	/**
	 * Gets the default wizard image.
	 * @nowarn
	 */
	public ImageIcon getDefaultWizardImage();

	/**
	 * Sets the default wizard image.
	 * @nowarn
	 */
	public void setDefaultWizardImage(ImageIcon defaultWizardImage);

	/**
	 * Gets the default background image.
	 * @nowarn
	 */
	public ImageIcon getDefaultBackgroundImage();

	/**
	 * Sets the default background image.
	 * @nowarn
	 */
	public void setDefaultBackgroundImage(ImageIcon defaultBackgroundImage);

	/**
	 * Gets a text resource for the specified page from the wizard resource.
	 * The method will search the wizard resource for a string named "wizard.\i@lp pageResourcePrefix\i.\ipageName\i.\isuffix\i"
	 * if a page resource prefix has been set.
	 * If no such string can be found, the method will try "wizard.\ipageName\i.\isuffix\i" and "wizard.\isuffix\i".
	 *
	 * @param pageName Name of the wizard page
	 * @param suffix Suffix to use for the the resource item name generation
	 * @return The value of the resource item or null if no such resource item exists
	 */
	public String getPageResourceString(String pageName, String suffix);
}
