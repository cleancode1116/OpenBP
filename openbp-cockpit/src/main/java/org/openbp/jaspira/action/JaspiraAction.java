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
package org.openbp.jaspira.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.listener.BeanListenerSupport;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.gui.plugin.PluginFocusMgr;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Base class for actions within the Jaspria framework.
 * Each action has an optional name, display name and a description.
 * The display will be show as menu item text, the description will be used as tool tip
 * of a component that activates the action.
 *
 * if the {@link #actionPerformed} method is not overriden, it will fire an {@link JaspiraActionEvent}
 * using the name of the action as event name. The source plugin of the event will be the
 * currently focused plugin or (if none) the owner plugin of the action.
 *
 * Actions can contain several icons and descriptions. The small icon is
 * used in Menus. The large icon is used in toolbars.
 *
 * Jaspira actions are also used to build menu and toolbar structures.
 * The type of the action determines what the action represents:<br>
 * {@link #TYPE_ACTION} or null: Regular action<br>
 * {@link #TYPE_MENU}: Menu (used to hold sub menu items; it doesn't have a real action functionality)<br>
 * {@link #TYPE_GROUP}: Menu or toolbar group
 *
 * An action can have a menu parent and/or a toolbar parent, which determines where to place
 * the action in the main menu or the main toolbar.<br>
 * Group or menu actions may also have menu or toolbar children.
 *
 * @author Stephan Moritz
 */
public class JaspiraAction extends DisplayObjectImpl
	implements Action
{
	// TODO Refactor 6: We should split this class in different sub classes e. g. for actions, groups, combos etc.

	/////////////////////////////////////////////////////////////////////////
	// @@ Resource/property Constants
	/////////////////////////////////////////////////////////////////////////

	/** Property id for name */
	public static final String PROPERTY_NAME = "name";

	/** Property id for display name */
	public static final String PROPERTY_DISPLAY_NAME = "displayName";

	/** Property id for description */
	public static final String PROPERTY_DESCRIPTION = "description";

	/** Property id for normal icon */
	public static final String PROPERTY_ICON = "icon";

	/** Property id for disabled icon */
	public static final String PROPERTY_DISABLED_ICON = "disabledicon";

	/** Property id for key sequences */
	public static final String PROPERTY_SEQUENCE = "sequence";

	/** Property id for enabled option */
	public static final String PROPERTY_ENABLED = "enabled";

	/** Propertyid for selected option */
	public static final String PROPERTY_SELECTED = "selected";

	/** Property id for priority of the action */
	public static final String PROPERTY_PRIO = "prio";

	/** Property id for action scope */
	public static final String PROPERTY_SCOPE = "scope";

	/** Property id for the type of action */
	public static final String PROPERTY_TYPE = "type";

	/** Property id for menu parent if action has to be displayed in the menu */
	public static final String PROPERTY_MENU_PARENT = "menuparent";

	/** Property id for toolbar parent if action has to be displayed in teh toolbar */
	public static final String PROPERTY_TOOLBAR_PARENT = "toolbarparent";

	/** Property id for the names of the Jaspira pages that should display this action in the page menu or toolbar */
	public static final String PROPERTY_PAGE_NAMES = "pagenames";

	/** Property id  for the conditional expression */
	public static final String PROPERTY_CONDITION = "condition";

	/** Id for the menu root - top level of a menu*/
	public static final String MENU_ROOT = "menuroot";

	/** Id for the toolbar root - top level of a toolbar */
	public static final String TOOLBAR_ROOT = "toolbarroot";

	/** Regular action */
	public static final String TYPE_ACTION = "action";

	/** Combo box action */
	public static final String TYPE_COMBO = "combo";

	/** Menu (used to hold sub menu items; it doesn't have a real action functionality) */
	public static final String TYPE_MENU = "menu";

	/** Menu or toolbar group */
	public static final String TYPE_GROUP = "group";

	/** Delimiter for multiple key sequences */
	public static final String KEY_SEQUENCE_DELIM = ",";

	/** Default priority of an action */
	public static final int DFLT_PRIORITY = 50;

	/** Delimiter to show the mnemonic key - key after delimiter gets mnemonic */
	public static final String MNEMONIC_DELIMITER = "_";

	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Resource containing the action properties */
	private ResourceCollection actionResourceCollection;

	/** Toolbar/menu icon */
	private MultiIcon icon;

	/** Disabled version of the icon */
	private MultiIcon disabledIcon;

	/** The position of the mnemonic underscore in the title */
	private int mnemonicpos;

	/** Key sequence for action activation */
	private KeySequence [] keySequences;

	/** Enabled status */
	private boolean enabled = true;

	/** Selected status. */
	private boolean selected;

	/** Priority */
	private int priority;

	/** Conditional expression that determines if the plugin should be active */
	private String condition;

	/** Type of the action ({@link #TYPE_ACTION}/{@link #TYPE_COMBO}/{@link #TYPE_MENU}/{@link #TYPE_GROUP}/null) */
	private String type;

	/** Names of the Jaspira pages that should display this action in the page menu or toolbar */
	private String [] pageNames;

	/** Children of the action in the menu mode : contains JaspiraActions */
	private transient SortedSet menuchildren;

	/** Children of the action in the toolbar mode : contains JaspiraActions */
	private transient SortedSet toolbarchildren;

	/** Action values */
	private transient Map values;

	/** Scope of the event generated by this action (i\.e\. the level to which this event is propagated) */
	private int scope;

	/** Used by the actionmanager to count instances if an action. */
	private int counter;

	/////////////////////////////////////////////////////////////////////////
	// @@ Static comparator
	/////////////////////////////////////////////////////////////////////////

	/** Comparator used to order menu entries. */
	private static Comparator priorityComparator = new PriorityComparator();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * The new action retrieves its properties from the resource of its owner plugin using the given action name.
	 *
	 * @param owner Owner plugin of the action
	 * @param name Action name
	 */
	public JaspiraAction(Plugin owner, String name)
	{
		this(owner.getPluginResourceCollection(), name);
	}

	/**
	 * Constructor.
	 * The new action retrieves its properties from the given resource using the given action name.
	 *
	 * @param res Action resource
	 * @param name Action name
	 */
	public JaspiraAction(ResourceCollection res, String name)
	{
		super(name);
		this.actionResourceCollection = res;

		// Load display name and description from the resource and check for mnemonic
		ResourceCollectionUtil.loadDisplayObjectFromResource(this, res, name);
		checkForMnemonic();

		// Load the enabled icon
		this.icon = (MultiIcon) getActionProperty(PROPERTY_ICON);

		// Load the disabled icon
		this.disabledIcon = (MultiIcon) getActionProperty(PROPERTY_DISABLED_ICON);

		// Load the enabled option
		this.enabled = getActionPropertyBoolean(PROPERTY_ENABLED, true);

		// Load the selected option
		this.selected = getActionPropertyBoolean(PROPERTY_SELECTED, false);

		// Load the type (group, action, ...; default: regular action)
		this.type = getActionPropertyString(PROPERTY_TYPE, TYPE_ACTION);

		// Load the priority
		this.priority = getActionPropertyInt(PROPERTY_PRIO, DFLT_PRIORITY);

		// Load the condition
		this.condition = getActionPropertyString(PROPERTY_CONDITION);

		// Load the scope of the event to generate from the action
		String scopeStr = getActionPropertyString(PROPERTY_SCOPE);
		this.scope = Plugin.LEVEL_APPLICATION;
		if (scopeStr != null)
		{
			if (scopeStr.equals("application"))
			{
				this.scope = Plugin.LEVEL_APPLICATION;
			}
			else if (scopeStr.equals("frame"))
			{
				this.scope = Plugin.LEVEL_FRAME;
			}
			else if (scopeStr.equals("page"))
			{
				this.scope = Plugin.LEVEL_PAGE;
			}
			else if (scopeStr.equals("plugin"))
			{
				this.scope = Plugin.LEVEL_PLUGIN;
			}
			else
			{
				// Log as warning
				LogUtil.warn(getClass(), "Invalid value for scope of action $0 in resource $1.", name, res.getErrorName());
			}
		}

		// Load multiple sequences
		String keys = getActionPropertyString(PROPERTY_SEQUENCE);
		if (keys != null)
		{
			StringTokenizer sto = new StringTokenizer(keys, KEY_SEQUENCE_DELIM);
			keySequences = new KeySequence [sto.countTokens()];

			for (int i = 0; sto.hasMoreTokens(); i++)
			{
				keySequences [i] = new KeySequence(sto.nextToken());
			}
		}

		// Parse the names of the Jaspira pages that should display this action in the page menu or toolbar
		String pageNameStr = getActionPropertyString(PROPERTY_PAGE_NAMES);
		if (pageNameStr != null)
		{
			ArrayList list = new ArrayList();

			// Use ',' or ' ' as page name separator
			StringTokenizer st = new StringTokenizer(pageNameStr, ", ", false);
			while (st.hasMoreTokens())
			{
				String pageName = st.nextToken();
				list.add(pageName);
			}

			pageNames = CollectionUtil.toStringArray(list);
		}
	}

	/**
	 * Explicit constructor.
	 * Usually it is preferable to use the resource based constructor.
	 *
	 * @param name The internal name of the object
	 * @param displayName Display name of this object
	 * @param description Description of this object
	 * @param icon Toolbar/menu icon
	 * @param keySequences Key sequences for action activation
	 * @param priority Priority
	 * @param type Type of the action ({@link #TYPE_ACTION}/{@link #TYPE_COMBO}/{@link #TYPE_MENU}/{@link #TYPE_GROUP}/null)
	 */
	public JaspiraAction(String name, String displayName, String description, MultiIcon icon, KeySequence [] keySequences, int priority, String type)
	{
		super(name, displayName, description);

		this.icon = icon;
		this.keySequences = keySequences;
		this.priority = priority;
		this.type = type;

		checkForMnemonic();
	}

	/**
	 * Constructor using the given display object.
	 *
	 * @param obj Display object to copy name, display name and description from
	 */
	public JaspiraAction(DisplayObject obj)
	{
		try
		{
			copyFrom(obj, Copyable.COPY_DEEP);
		}
		catch (CloneNotSupportedException e)
		{
			// Never happens
		}

		this.priority = DFLT_PRIORITY;
		this.type = TYPE_ACTION;

		checkForMnemonic();
	}

	//////////////////////////////////////////////////
	// @@ Action execution
	//////////////////////////////////////////////////

	/**
	 * Called if the action has been activated.
	 * For actions of the TYPE_ACTION or TYPE_COMBO, the method fires a {@link JaspiraActionEvent} that has the
	 * same name as the action either to the currently focused plugin (if there is one) or
	 * to the global application plugin.
	 *
	 * @param ae Action event
	 */
	public void actionPerformed(ActionEvent ae)
	{
		if (type.equals(TYPE_ACTION) || type.equals(TYPE_COMBO))
		{
			Plugin focusedPlugin = PluginFocusMgr.getInstance().getFocusedPlugin();

			if (focusedPlugin != null)
			{
				focusedPlugin.fireEvent(new JaspiraActionEvent(focusedPlugin, getName(), ae, scope));
			}
			else
			{
				JaspiraEventMgr.fireGlobalEvent(new JaspiraActionEvent(null, getName(), ae, scope));
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ component generation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a component from this action.
	 * @return Null
	 */
	public JComponent toComponent()
	{
		return null;
	}

	/**
	 * Constructs a tool bar component from this action.
	 *
	 * @return A {@link JaspiraToolbarButton} if the action is a regular action,
	 * a {@link JaspiraToolbar} if the action has toolbar children, null otherwise
	 */
	public JComponent toToolBarComponent()
	{
		return toToolBarComponent(null);
	}

	/**
	 * Constructs a tool bar component from this action, considering the given page name.
	 * The toolbar will contain only those children that match the
	 * given page name (see {@link #matchesPageName}).
	 *
	 * @param currentPageName Name of the Jaspira page to check against or null
	 * @return A {@link JaspiraToolbarButton} if the action is a regular action,
	 * a {@link JaspiraToolbar} if the action has toolbar children, null otherwise
	 */
	public JComponent toToolBarComponent(String currentPageName)
	{
		if (type.equals(TYPE_ACTION))
		{
			// we are a simple action so return a toolbar button
			return new JaspiraToolbarButton(this);
		}
		else if (type.equals(TYPE_COMBO))
		{
			// Return the selection field
			return new JaspiraToolbarCombo(this);
		}
		else
		{
			if (toolbarchildren == null || toolbarchildren.isEmpty())
			{
				// We have no children, so we do not want the menu itself to be shown
				return null;
			}

			// we are a toolbar group - create a subtoolbar and return it.
			JaspiraToolbar bar = null;

			for (Iterator it = toolbarchildren.iterator(); it.hasNext();)
			{
				JaspiraAction childAction = (JaspiraAction) it.next();
				if (childAction == null)
					continue;

				if (currentPageName != null && !childAction.matchesPageName(currentPageName))
					continue;

				if (bar == null)
				{
					bar = new JaspiraToolbar();
				}
				bar.add(childAction.toToolBarComponent(currentPageName));
			}

			return bar;
		}
	}

	/**
	 * Constructs a menu item from this action.
	 *
	 * @return A {@link JaspiraMenuItem} if the action is a regular action,
	 * a {@link JaspiraMenu} if the action is a menu action and has menu children, null otherwise
	 */
	public JMenuItem toMenuItem()
	{
		return toMenuItem(null);
	}

	/**
	 * Constructs a menu item from this action, considering the given page name.
	 * If the result is a menu, it will contain only those children that match the
	 * given page name (see {@link #matchesPageName}).
	 *
	 * @param currentPageName Name of the Jaspira page to check against or null
	 * @return A {@link JaspiraMenuItem} if the action is a regular action,
	 * a {@link JaspiraMenu} if the action is a menu action and has menu children, null otherwise
	 */
	public JMenuItem toMenuItem(String currentPageName)
	{
		if (type.equals(TYPE_ACTION))
		{
			// we are a simple action so return a array with only one entry.
			return new JaspiraMenuItem(this);
		}
		else if (type.equals(TYPE_MENU))
		{
			if (menuchildren == null || menuchildren.isEmpty())
			{
				// We have no children, so we do not want the menu itself to be shown
				return null;
			}

			// we are a menu with subentries - create the menu and return it.

			JaspiraMenu menu = null;

			for (Iterator it = menuchildren.iterator(); it.hasNext();)
			{
				JaspiraAction childAction = (JaspiraAction) it.next();
				if (childAction == null)
					continue;

				if (currentPageName != null && !childAction.matchesPageName(currentPageName))
					continue;

				if (childAction.getType().equals(TYPE_GROUP))
				{
					if (childAction.getMenuchildren() == null)
					{
						// The group is empty, ignore it.
						continue;
					}

					for (Iterator it2 = childAction.getMenuchildren().iterator(); it2.hasNext();)
					{
						JaspiraAction childAction2 = (JaspiraAction) it2.next();
						if (currentPageName != null && !childAction2.matchesPageName(currentPageName))
							continue;

						JMenuItem menuItem = childAction2.toMenuItem(currentPageName);
						if (menu == null)
						{
							menu = new JaspiraMenu(this);
						}
						menu.add(menuItem);
					}
				}
				else
				{
					JMenuItem menuItem = childAction.toMenuItem(currentPageName);
					if (menu == null)
					{
						menu = new JaspiraMenu(this);
					}
					menu.add(menuItem);
				}

				if (it.hasNext())
				{
					if (menu != null)
					{
						menu.addSeparator();
					}
				}
			}

			return menu;
		}

		// Action groups and combos are not represented in a menu should return null...
		return null;
	}

	/**
	 * Checks if an action should be added to the menu or toolbar of this page.
	 * This is the case if the page name ({@link Plugin#getName}) matches the page names
	 * defined for the action ({@link #getPageNames}), in detail if:<br>
	 * - The action page name list is empty<br>
	 * - The page name equals an entry in the action page name list
	 *
	 * @param currentPageName Name of the Jaspira page to check against
	 * @return
	 *		true	The action should be added to this page.
	 *		false	The action should not appear on this page.
	 */
	public boolean matchesPageName(String currentPageName)
	{
		if (pageNames == null)
			return true;

		if (currentPageName != null)
		{
			for (int i = 0; i < pageNames.length; ++i)
			{
				if (currentPageName.equals(pageNames [i]))
					return true;
			}
		}

		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Mnemonic settings
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Checks the Title of the Menu for Mnemonic.
	 */
	public void checkForMnemonic()
	{
		String title = getDisplayName();
		if (title == null)
		{
			mnemonicpos = -1;
			return;
		}

		mnemonicpos = title.indexOf(MNEMONIC_DELIMITER);
		if (mnemonicpos != -1)
		{
			// Cut the delimiter from the display name
			setDisplayName(title.substring(0, mnemonicpos) + title.substring(mnemonicpos + 1));
		}
	}

	/**
	 * Returns the position of the mnemonic underscore in the display name of
	 * the action. The method returns -1 if no mnemonic is available.
	 *
	 * @return int the position in the display name string or -1
	 */
	public int getMnemonicPos()
	{
		return mnemonicpos;
	}

	/**
	 * Returns true if the Action has mnemonic.
	 *
	 * @return boolean true if mnemonic is available, false otherwise
	 */
	public boolean isMnemonic()
	{
		return mnemonicpos != -1;
	}

	/**
	 * Returns the mnemonic char of this action.
	 *
	 * @return char mnemonic char
	 */
	public char getMnemonicChar()
	{
		if (isMnemonic())
		{
			return getDisplayName().charAt(mnemonicpos);
		}

		return '\0';
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the resource containing the action properties.
	 * @nowarn
	 */
	public ResourceCollection getActionResource()
	{
		return actionResourceCollection;
	}

	/**
	 * Returns the menu children.
	 * @return The set of child actions or null
	 */
	public SortedSet getMenuchildren()
	{
		return menuchildren;
	}

	/**
	 * Registers an action at this parent.
	 *
	 * @param action the childaction that wants to register
	 */
	public void addMenuChild(JaspiraAction action)
	{
		if (menuchildren == null)
		{
			menuchildren = new TreeSet(priorityComparator);
		}
		menuchildren.add(action);
	}

	/**
	 * Unregisters an action from this parent.
	 *
	 * @param action the child action that wants to unregister
	 */
	protected void removeMenuChild(JaspiraAction action)
	{
		if (menuchildren != null)
		{
			menuchildren.remove(action);
		}
	}

	/**
	 * Returns the toolbarchildren.
	 * @return Set of toolbar children or null
	 */
	public SortedSet getToolbarchildren()
	{
		return toolbarchildren;
	}

	/**
	 * Registers an action at this parent.
	 *
	 * @param action the childaction that wants to register
	 */
	public void addToolbarChild(JaspiraAction action)
	{
		if (toolbarchildren == null)
		{
			toolbarchildren = new TreeSet(priorityComparator);
		}
		toolbarchildren.add(action);
	}

	/**
	 * Unregisters an action from this parent.
	 *
	 * @param action the child action that wants to unregister
	 */
	protected void removeToolbarChild(JaspiraAction action)
	{
		if (toolbarchildren != null)
		{
			toolbarchildren.remove(action);
		}
	}

	/**
	 * Gets the names of the Jaspira pages that should display this action in the page menu or toolbar.
	 * @return The list of page names (see JaspiraPage.getName) or null if the action
	 * should appear for all pages
	 */
	public String [] getPageNames()
	{
		return pageNames;
	}

	/**
	 * Sets the names of the Jaspira pages that should display this action in the page menu or toolbar.
	 * @param pageNames The list of page names (see JaspiraPage.getName) or null if the action
	 * should appear for all pages
	 */
	public void setPageNames(String [] pageNames)
	{
		this.pageNames = pageNames;
	}

	/**
	 * Returns the type.
	 * @return The type of the action ({@link #TYPE_ACTION}/{@link #TYPE_COMBO}/{@link #TYPE_MENU}/{@link #TYPE_GROUP}/null)
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Gets the toolbar/menu icon.
	 * @nowarn
	 */
	public MultiIcon getIcon()
	{
		return icon;
	}

	/**
	 * Sets the toolbar/menu icon.
	 * @nowarn
	 */
	public void setIcon(MultiIcon icon)
	{
		if (this.icon != icon)
		{
			Object old = this.icon;
			this.icon = icon;
			firePropertyChange(PROPERTY_ICON, old, icon);
		}
	}

	/**
	 * Gets the disabled version of the icon.
	 * @nowarn
	 */
	public MultiIcon getDisabledIcon()
	{
		return disabledIcon;
	}

	/**
	 * Sets the disabled version of the icon.
	 * @nowarn
	 */
	public void setDisabledIcon(MultiIcon disabledIcon)
	{
		if (this.disabledIcon != disabledIcon)
		{
			Object old = this.disabledIcon;
			this.disabledIcon = disabledIcon;
			firePropertyChange(PROPERTY_DISABLED_ICON, old, icon);
		}
	}

	/**
	 * Gets the key sequences for action activation.
	 * @nowarn
	 */
	public KeySequence [] getKeySequences()
	{
		return keySequences;
	}

	/**
	 * Gets the enabled status.
	 * @nowarn
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Sets the enabled status.
	 * @nowarn
	 */
	public void setEnabled(boolean enabled)
	{
		if (this.enabled != enabled)
		{
			Boolean old = Boolean.valueOf(this.enabled);
			this.enabled = enabled;
			firePropertyChange(PROPERTY_ENABLED, old, Boolean.valueOf(enabled));
		}
	}

	/**
	 * Returns the selected status.
	 * @nowarn
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Sets a new displayname for the action.
	 * @see org.openbp.common.generic.description.DisplayObjectImpl#setDisplayName(String)
	 */
	public void setDisplayName(String displayName)
	{
		// Cache the old name
		String oldValue = getDisplayName();

		if (!CommonUtil.equalsNull(displayName, oldValue))
		{
			// Set the displayname in the DisplayObject
			super.setDisplayName(displayName);

			// Notify all components
			firePropertyChange(PROPERTY_DISPLAY_NAME, oldValue, displayName);
		}
	}

	/**
	 * Sets a new description for the action.
	 * @see org.openbp.common.generic.description.DescriptionObjectImpl#setDescription(String)
	 */
	public void setDescription(String description)
	{
		// Cache the old name
		String oldValue = getDescription();

		if (!CommonUtil.equalsNull(description, oldValue))
		{
			// Set the description in the DisplayObject
			super.setDescription(description);

			// Notify all components
			firePropertyChange(PROPERTY_DESCRIPTION, oldValue, description);
		}
	}

	/**
	 * Sets the selected status.
	 * @nowarn
	 */
	public void setSelected(boolean selected)
	{
		if (this.selected != selected)
		{
			Boolean old = Boolean.valueOf(this.selected);
			this.selected = selected;
			firePropertyChange(PROPERTY_SELECTED, old, Boolean.valueOf(selected));
		}
		this.selected = selected;
	}

	/**
	 * Gets the priority.
	 * @nowarn
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority.
	 * @nowarn
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	/**
	 * Gets the conditional expression that determines if the plugin should be active.
	 * @nowarn
	 */
	public String getCondition()
	{
		return condition;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Reference counter
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the current value of the reference counter.
	 * @return The reference counter determines how many actions of this type
	 * have been created using the {@link ActionMgr}
	 */
	public int getCounter()
	{
		return counter;
	}

	/**
	 * Increases the reference counter of the action.
	 */
	public void increaseCounter()
	{
		counter++;
	}

	/**
	 * Decrease the reference counter of the action.
	 * @return
	 *		true	There are still references left to this action.<br>
	 *		false	The reference counter decreased to 0.
	 */
	public boolean decreaseCounter()
	{
		return --counter > 0;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Action implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see javax.swing.Action#getValue(String)
	 */
	public Object getValue(String key)
	{
		if (values != null)
		{
			return values.get(key);
		}
		return null;
	}

	/**
	 * @see javax.swing.Action#putValue(String, Object)
	 */
	public void putValue(String key, Object value)
	{
		if (values == null)
		{
			values = new HashMap();
		}
		Object oldValue = values.put(key, value);

		// Pass to property change listener so toolbar/menu components may react on this
		firePropertyChange(key, oldValue, value);
	}

	/**
	 * Clears all action values.
	 */
	public void clearValues()
	{
		if (values != null)
		{
			values.clear();
		}
	}

	//////////////////////////////////////////////////
	// @@ Action property acccess
	//////////////////////////////////////////////////

	/**
	 * Gets an action property, either from the action's property map or from the resource file.
	 * For resource file access, the name of the action and "." will be prepended to the resource key.
	 *
	 * @param key Key of the property
	 * @return The property value or null if not found
	 */
	public Object getActionProperty(String key)
	{
		Object value = getValue(key);

		if (value == null && actionResourceCollection != null)
		{
			// Try to resolve the value from the resource file
			StringBuffer sb = new StringBuffer();
			if (getName() != null)
				sb.append(getName());
			sb.append('.');
			sb.append(key);
			String propKey = sb.toString();
			value = actionResourceCollection.getOptionalObject(propKey);
		}

		return value;
	}

	/**
	 * Gets an action property of type String.
	 *
	 * @param key Key of the property
	 * @return The property value or null if not found
	 */
	public String getActionPropertyString(String key)
	{
		Object value = getActionProperty(key);

		if (value != null && !(value instanceof String))
		{
			System.err.println("Action property " + getName() + "." + key + " is not a string.");
			return null;
		}

		return (String) value;
	}

	/**
	 * Gets an action property of type String.
	 *
	 * @param key Key of the property
	 * @param dflt Default value
	 * @return The property value or null if not found
	 */
	public String getActionPropertyString(String key, String dflt)
	{
		String value = getActionPropertyString(key);

		if (value == null)
			value = dflt;

		return value;
	}

	/**
	 * Gets an action property of type boolean.
	 *
	 * @param key Key of the property
	 * @param dflt Default value
	 * @return The property value or null if not found
	 */
	public boolean getActionPropertyBoolean(String key, boolean dflt)
	{
		Object value = getActionProperty(key);

		boolean ret = dflt;
		if (value != null)
		{
			if (value instanceof Boolean)
			{
				ret = ((Boolean) value).booleanValue();
			}
			else if (value instanceof String)
			{
				String s = (String) value;
				if ("false".equals(s))
				{
					ret = false;
				}
				else if ("true".equals(s))
				{
					ret = false;
				}
				else
				{
					System.err.println("Action property " + getName() + "." + key + " is not a boolean.");
				}
			}
			else
			{
				System.err.println("Action property " + getName() + "." + key + " is not a boolean.");
			}
		}

		return ret;
	}

	/**
	 * Gets an action property of type int.
	 *
	 * @param key Key of the property
	 * @param dflt Default value
	 * @return The property value or null if not found
	 */
	public int getActionPropertyInt(String key, int dflt)
	{
		Object value = getActionProperty(key);

		int ret = dflt;
		if (value != null)
		{
			if (value instanceof Integer)
			{
				ret = ((Integer) value).intValue();
			}
			else if (value instanceof String)
			{
				try
				{
					ret = Integer.parseInt((String) value);
				}
				catch (NumberFormatException e)
				{
					System.err.println("Action property " + getName() + "." + key + " is not an integer.");
				}
			}
			else
			{
				System.err.println("Action property " + getName() + "." + key + " is not an integer.");
			}
		}

		return ret;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property change support
	/////////////////////////////////////////////////////////////////////////

	/** Listener support object holding the listeners */
	private BeanListenerSupport listenerSupport;

	/**
	 * Supports reporting bound property changes.
	 * This method can be called when a bound property has changed and
	 * will send the appropriate PropertyChangeEvent to any registered
	 * property change listeners.
	 *
	 * @param propertyName Name of the bound property
	 * @param oldValue Old value of the property
	 * @param newValue New value of the property
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		if (listenerSupport != null && listenerSupport.containsListeners(PropertyChangeListener.class) && !CommonUtil.equalsNull(oldValue, newValue))
		{
			listenerSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
		}
	}

	/**
	 * Adds a property change listener to the listener list.
	 * A PropertyChangeEvent will get fired in response to setting a bound property.
	 * The listener is registered for all properties as a WEAK listener, i. e. it may
	 * be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new PropertyChangeListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new BeanListenerSupport();
		}
		listenerSupport.addWeakListener(PropertyChangeListener.class, listener);
	}

	/**
	 * Removes a property change listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(PropertyChangeListener.class, listener);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Inner classes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Comparator used to sort menus and menu entries according to their
	 * priority and displayname.
	 */
	private static class PriorityComparator
		implements Comparator
	{
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2)
		{
			JaspiraAction a1 = (JaspiraAction) o1;
			JaspiraAction a2 = (JaspiraAction) o2;

			int result = a1.getPriority() - a2.getPriority();

			if (result != 0)
			{
				return result;
			}

			String n1 = a1.getDisplayName();
			if (n1 == null)
			{
				n1 = a1.getName();
			}
			String n2 = a2.getDisplayName();
			if (n2 == null)
			{
				n2 = a2.getName();
			}

			return n1.compareTo(n2);
		}
	}
}
