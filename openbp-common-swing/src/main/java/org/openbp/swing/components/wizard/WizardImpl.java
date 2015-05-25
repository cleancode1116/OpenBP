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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.common.rc.ResourceCollection;

/**
 * Wizard component.
 *
 * @author Heiko Erhardt
 */
public class WizardImpl extends JPanel
	implements Wizard, ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Wizard resource file */
	private ResourceCollection wizardResourceCollection;

	/** Optional resource prefix for wizard page resources */
	private String pageResourcePrefix;

	/** Default wizard image */
	private ImageIcon defaultWizardImage;

	/** Default background image */
	private ImageIcon defaultBackgroundImage;

	/** Data collection model holding the data of the pages */
	private WizardDataModel dataModel;

	/** Page sequence manager */
	private SequenceManager manager;

	/** Wizard result page name */
	private String resultPageName;

	/** Wizard listeners */
	private List listeners;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Deck of wizard pages */
	private JPanel deckPanel;

	/** Table of pages (maps page names to {@link WizardPage} objects) */
	private Map pages;

	/** Layout manager */
	private WizardLayout layout;

	/** Name of the recently added page */
	private String lastAdded;

	/** Navigation bar */
	private WizardNavigator nav;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public WizardImpl()
	{
		this(null, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param wizardResourceCollection The resource that contains texts and images for the wizard's navigator
	 */
	public WizardImpl(ResourceCollection wizardResourceCollection)
	{
		this(wizardResourceCollection, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param wizardResourceCollection The resource that contains texts and images for the wizard's navigator
	 * @param defaultWizardImage Default wizard image
	 */
	public WizardImpl(ResourceCollection wizardResourceCollection, ImageIcon defaultWizardImage)
	{
		this(wizardResourceCollection, defaultWizardImage, null);
	}

	/**
	 * Constructor.
	 *
	 * @param wizardResourceCollection The resource that contains texts and images for the wizard's navigator
	 * @param defaultWizardImage Default wizard image
	 * @param defaultBackgroundImage Default background image
	 */
	public WizardImpl(ResourceCollection wizardResourceCollection, ImageIcon defaultWizardImage, ImageIcon defaultBackgroundImage)
	{
		this.wizardResourceCollection = wizardResourceCollection;
		this.defaultWizardImage = defaultWizardImage;
		this.defaultBackgroundImage = defaultBackgroundImage;

		pages = new HashMap();

		setManager(new SequenceManagerImpl());
		setDataModel(new WizardDataModelImpl());

		setLayout(new BorderLayout());

		layout = new WizardLayout();
		deckPanel = new JPanel();
		deckPanel.setLayout(layout);
		add("Center", deckPanel);

		nav = new WizardNavigator(this, wizardResourceCollection);
		add("South", nav);

		// Map ESC key to cancel
		ActionListener cancelListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		};

		registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		nav.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		// Don't show help button by default
		setShowHelp(false);
	}

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
	public void addAndLinkPage(String name, Component page)
	{
		if (pages.get(name) == null)
		{
			if (lastAdded == null)
			{
				manager.setFirst(name);
				manager.setCurrent(name);
			}
			else
			{
				manager.setPrevious(name, lastAdded);
				manager.setNext(lastAdded, name);
			}

			addPage(name, page);

			lastAdded = name;
		}
	}

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
	public void addPage(String name, Component page)
	{
		if (page instanceof WizardPage)
		{
			// Try to get the page title and description from the resource if not present yet
			WizardPage wp = (WizardPage) page;

			if (wp.getTitle() == null)
			{
				wp.setTitle(getPageResourceString(name, "title"));
			}

			if (wp.getDescription() == null)
			{
				wp.setDescription(getPageResourceString(name, "description"));
			}
		}

		page.setVisible(true);
		if (pages.get(name) == null)
		{
			pages.put(name, page);
			deckPanel.add(name, page);
		}
	}

	/**
	 * Removes a page from the wizard and the sequence manager.
	 *
	 * @param name Name of the page
	 */
	public void removePage(String name)
	{
		Component page = (Component) pages.remove(name);
		if (page != null)
		{
			deckPanel.remove(page);
		}

		manager.remove(name);
	}

	/**
	 * Gets a page by its name.
	 *
	 * @param name Name of the page or null for the current page
	 * @return The page component (usually an instance of {@link WizardPage})
	 */
	public Component getPage(String name)
	{
		return (Component) pages.get(name);
	}

	/**
	 * Gets the list of page names.
	 *
	 * @return An iterator of wizard page names (strings)
	 */
	public Iterator getPageNames()
	{
		return pages.keySet().iterator();
	}

	/**
	 * Gets the list of pages.
	 *
	 * @return An iterator of wizard pages (Component objects)
	 */
	public Iterator getPages()
	{
		return pages.values().iterator();
	}

	/**
	 * Gets the wizard result page name.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager. Note that the result page
	 * component must have been added using the {@link #addPage} method.
	 *
	 * @return The name of the result page or null if no result page is set
	 */
	public String getResultPageName()
	{
		return resultPageName;
	}

	/**
	 * Sets the wizard result page name.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager. Note that the result page
	 * component must have been added using the {@link #addPage} method.
	 *
	 * @param resultPageName The name of the result page or null for no result page
	 */
	public void setResultPageName(String resultPageName)
	{
		this.resultPageName = resultPageName;
	}

	/**
	 * Adds a result page to the wizard.
	 * The result page will be displayed after the user has pressed the 'Finish' button.
	 * It will not be considered by the sequence manager.<br>
	 * This method is a convenience method for {@link #addPage} + {@link #setResultPageName}
	 *
	 * @param resultPageName The name of the result page or null for no result page
	 * @param page Page component to add (usually a {@link WizardPage})
	 */
	public void setResultPage(String resultPageName, Component page)
	{
		addPage(resultPageName, page);
		setResultPageName(resultPageName);
	}

	/**
	 * Clears the wizard pages.
	 * Clears all pages and the sequence manager information
	 */
	public void clearPages()
	{
		pages.clear();
		manager.clear();
		deckPanel.removeAll();

		lastAdded = null;
	}

	//////////////////////////////////////////////////
	// @@ Navigation
	//////////////////////////////////////////////////

	/**
	 * Displays the first page in the sequence.
	 */
	public void displayFirst()
	{
		if (!fireWizardEvent(WizardEvent.FIRST))
			return;

		displayPage(manager.getFirst());
	}

	/**
	 * Displays the previous page.
	 * Also called if the user presses the 'Back' button.
	 */
	public void displayBack()
	{
		if (!fireWizardEvent(WizardEvent.BACK))
			return;

		displayPage(manager.getPrevious());
	}

	/**
	 * Displays the next page.
	 * Also called if the user presses the 'Next' button.
	 */
	public void displayNext()
	{
		if (!fireWizardEvent(WizardEvent.NEXT))
			return;

		displayPage(manager.getNext());
	}

	/**
	 * Finishes the wizard.
	 * Also called if the user presses the 'Finish' button.
	 */
	public void finish()
	{
		if (!fireWizardEvent(WizardEvent.FINISH))
			return;

		String currentPageName = manager.getCurrent();
		if (resultPageName != null && !resultPageName.equals(currentPageName) && getPage(resultPageName) != null)
		{
			// We need to display the result page

			// Make sure we can get back from there
			manager.setPrevious(resultPageName, currentPageName);

			displayPage(resultPageName);
		}
		else
		{
			hideWizard();
		}
	}

	/**
	 * Cancels the wizard.
	 * Also called if the user presses the 'Cancel' button.
	 */
	public void cancel()
	{
		if (!fireWizardEvent(WizardEvent.CANCEL))
			return;

		hideWizard();
	}

	/**
	 * Closes the wizard.
	 * Also called if the user presses the 'Close' button.
	 */
	public void close()
	{
		if (!fireWizardEvent(WizardEvent.CLOSE))
			return;

		hideWizard();
	}

	/**
	 * Determines if we can return to the previous page.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canMoveBackward()
	{
		WizardValidator validator = getCurrentValidator();
		if (validator != null)
		{
			if (!validator.canMoveBackward())
				return false;
		}
		return hasBackward();
	}

	/**
	 * Determines if there is a previous page.
	 * @nowarn
	 */
	public boolean hasBackward()
	{
		return manager.getPrevious() != null;
	}

	/**
	 * Determines if we can advance to the next page.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canMoveForward()
	{
		WizardValidator validator = getCurrentValidator();
		if (validator != null)
		{
			if (!validator.canMoveForward())
				return false;
		}
		return hasForward();
	}

	/**
	 * Determines if there is a next page.
	 * @nowarn
	 */
	public boolean hasForward()
	{
		return manager.getNext() != null;
	}

	/**
	 * Determines if we can finish the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canFinish()
	{
		WizardValidator validator = getCurrentValidator();
		if (validator != null)
		{
			return validator.canFinish();
		}

		if (hasForward())
			return false;

		return true;
	}

	/**
	 * Determines if we can cancel the wizard dialog at this point.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canCancel()
	{
		WizardValidator validator = getCurrentValidator();
		if (validator != null)
		{
			if (!validator.canCancel())
				return false;
		}
		return true;
	}

	/**
	 * Shows a particular page and updates the navigator.
	 *
	 * @param name Name of the page to show or null for the current one
	 */
	public void displayPage(String name)
	{
		if (name == null)
			name = manager.getCurrent();

		Component page = getPage(name);
		if (page == null)
			return;

		manager.setCurrent(name);

		layout.show(deckPanel, page);

		fireWizardEvent(WizardEvent.SHOW);

		nav.updateNavigator();

		// Set the focus to the page
		Component focusComponent = page;
		if (page instanceof WizardPage)
		{
			Component c = ((WizardPage) page).getFocusComponent();
			if (c != null)
				focusComponent = c;
		}
		focusComponent.requestFocus();
	}

	/**
	 * Updates the navigation bar.
	 * This method should be called if a page that does not make use of the wizard data model
	 * (see {@link #setDataModel(WizardDataModel)}) has changed its status and wants to update the state of the navigation
	 * bar buttons accordingly.
	 */
	public void updateNavigator()
	{
		nav.updateNavigator();
	}

	/**
	 * Hides the window the wizard is contained in.
	 */
	public void hideWizard()
	{
		Container root = getTopLevelAncestor();
		root.setVisible(false);
	}

	//////////////////////////////////////////////////
	// @@ Wizard events
	//////////////////////////////////////////////////

	/**
	 * Adds a wizard event listener.
	 * The listener will be notified each time a property value changes.
	 *
	 * @param listener Listener
	 */
	public void addWizardListener(WizardListener listener)
	{
		if (listeners == null)
			listeners = new Vector();
		listeners.add(listener);
	}

	/**
	 * Removes a wizard event listener.
	 *
	 * @param listener Listener
	 */
	public void removeWizardListener(WizardListener listener)
	{
		listeners.remove(listener);
		if (listeners.isEmpty())
			listeners = null;
	}

	/**
	 * Fires a wizard event using the given event code.
	 *
	 * @param eventType Event code to fire
	 * @return
	 *		true	Processing can be continued.<br>
	 *		false	The cancel flag of the event has been set by an event listener.
	 */
	public boolean fireWizardEvent(int eventType)
	{
		return fireWizardEvent(new WizardEvent(this, eventType));
	}

	/**
	 * Fires a wizard event.
	 *
	 * @param event Event to fire
	 * @return
	 *		true	Processing can be continued.<br>
	 *		false	The cancel flag of the event has been set by an event listener.
	 */
	protected boolean fireWizardEvent(WizardEvent event)
	{
		// Send the event also to the current wizard page if it implements the listener interface
		Component c = getPage(manager.getCurrent());
		if (c instanceof WizardListener)
		{
			((WizardListener) c).handleWizardEvent(event);
			if (event.cancel)
				return false;
		}

		// Call all registered listeners
		if (listeners != null)
		{
			for (int i = 0; i < listeners.size(); i++)
			{
				WizardListener listener = (WizardListener) listeners.get(i);
				listener.handleWizardEvent(event);
				if (event.cancel)
					return false;
			}
		}

		return true;
	}

	//////////////////////////////////////////////////
	// @@ Model change listener
	//////////////////////////////////////////////////

	/**
	 * Called if the state of the model changed.
	 * The navigator is updated to reflect any changes in the model
	 * that might lead to enable/disable the 'Next' or 'Finish' buttons.
	 *
	 * @param event Event
	 */
	public void stateChanged(ChangeEvent event)
	{
		nav.updateNavigator();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the data collection model holding the data of the pages.
	 * @nowarn
	 */
	public WizardDataModel getDataModel()
	{
		return dataModel;
	}

	/**
	 * Sets the data collection model holding the data of the pages.
	 * @nowarn
	 */
	public void setDataModel(WizardDataModel dataModel)
	{
		if (this.dataModel != dataModel)
		{
			if (this.dataModel != null)
				dataModel.removeChangeListener(this);
			this.dataModel = dataModel;
			dataModel.addChangeListener(this);
		}
	}

	/**
	 * Gets the page sequence manager.
	 * @nowarn
	 */
	public SequenceManager getManager()
	{
		return manager;
	}

	/**
	 * Sets the page sequence manager.
	 * @nowarn
	 */
	public void setManager(SequenceManager manager)
	{
		this.manager = manager;
	}

	/**
	 * Gets the wizard resource.
	 * @nowarn
	 */
	public ResourceCollection getResource()
	{
		return wizardResourceCollection;
	}

	/**
	 * Sets the wizard resource.
	 * @nowarn
	 */
	public void setWizardResource(ResourceCollection wizardResourceCollection)
	{
		this.wizardResourceCollection = wizardResourceCollection;
	}

	/**
	 * Gets the optional resource prefix for wizard page resources.
	 * @nowarn
	 */
	public String getPageResourcePrefix()
	{
		return pageResourcePrefix;
	}

	/**
	 * Sets the optional resource prefix for wizard page resources.
	 * @nowarn
	 */
	public void setPageResourcePrefix(String pageResourcePrefix)
	{
		this.pageResourcePrefix = pageResourcePrefix;
	}

	/**
	 * Sets the show help.
	 * @nowarn
	 */
	public void setShowHelp(boolean showHelp)
	{
		nav.setShowHelp(showHelp);
	}

	/**
	 * Gets the default wizard image.
	 * @nowarn
	 */
	public ImageIcon getDefaultWizardImage()
	{
		return defaultWizardImage;
	}

	/**
	 * Sets the default wizard image.
	 * @nowarn
	 */
	public void setDefaultWizardImage(ImageIcon defaultWizardImage)
	{
		this.defaultWizardImage = defaultWizardImage;
	}

	/**
	 * Gets the default background image.
	 * @nowarn
	 */
	public ImageIcon getDefaultBackgroundImage()
	{
		return defaultBackgroundImage;
	}

	/**
	 * Sets the default background image.
	 * @nowarn
	 */
	public void setDefaultBackgroundImage(ImageIcon defaultBackgroundImage)
	{
		this.defaultBackgroundImage = defaultBackgroundImage;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

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
	public String getPageResourceString(String pageName, String suffix)
	{
		String text = null;

		if (pageName != null)
		{
			if (pageResourcePrefix != null)
			{
				text = wizardResourceCollection.getOptionalString("wizard." + pageResourcePrefix + "." + pageName + "." + suffix);
			}

			if (text == null)
			{
				text = wizardResourceCollection.getOptionalString("wizard." + pageName + "." + suffix);
			}
		}

		if (text == null)
		{
			text = wizardResourceCollection.getOptionalString("wizard." + suffix);
		}

		return text;
	}

	/**
	 * Gets the validator of the current wizard page.
	 *
	 * @return The validator object or null if there is no validator
	 */
	private WizardValidator getCurrentValidator()
	{
		Component c = getPage(manager.getCurrent());
		if (c instanceof WizardValidator)
		{
			return ((WizardValidator) c);
		}
		return null;
	}
}
