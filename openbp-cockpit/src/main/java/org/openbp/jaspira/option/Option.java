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
package org.openbp.jaspira.option;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.common.setting.SettingUtil;

/**
 * This is the abstract class for all options. A option
 * has viewable Components - the OptionWidgets. If the
 * option has to be able to shown in a tollbar or somthing
 * else it has to return diverent Widges!
 *
 * Every Option has a name and a displayname, where the displayname
 * is used to name the option in diferent languages.
 *
 * The option value kan read by the OptionMgr. If you need the
 * value of a option you have to ask vor it at the OptionMgr.
 *
 * If the Option value has changed a OptionChangedEvent will signal
 * the change. to recognize this you have to install a Listener at the
 * JaspiraEventMgr.
 *
 * @author Jens Ferchland
 */
public abstract class Option extends DisplayObjectImpl
	implements TreeNode
{
	//////////////////////////////////////////////////
	// @@ static members
	//////////////////////////////////////////////////

	/** Comperator for option ans optiongroups */
	public static final Comparator priorityComparator = new PriorityComparator();

	/** Resource id for the icon */
	public static final String PROPERTY_ICON = "icon";

	/** Resource id for the option type */
	public static final String PROPERTY_TYPE = "type";

	/** Resource id for the option priority */
	public static final String PROPERTY_PRIO = "prio";

	/** Resource id for the option heading */
	public static final String PROPERTY_HEADING = "heading";

	/** Resource id for the option parent */
	public static final String PROPERTY_OPTION_PARENT = "optionparent";

	/** Property id  for the conditional expression */
	public static final String PROPERTY_CONDITION = "condition";

	/** Option type: Regular option */
	public static final String TYPE_OPTION = "option";

	/** Option type: Option group */
	public static final String TYPE_GROUP = "group";

	/** Option type: Group of options which will displayed in a page and not as own option tree node */
	public static final String TYPE_SUB_GROUP = "subgroup";

	/** Default priority = 50 */
	public static final int DFLT_PRIORITY = 50;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Value of the option */
	private Object value;

	/** Default value of the option */
	private Object defaultValue;

	/** Type of option */
	private String type;

	/** Option heading (title of the option widget) */
	private String heading;

	/** Priority of the option */
	private int prio;

	/** Conditional expression that determines if the plugin should be active */
	private String condition;

	/** All real options of a option group */
	private SortedSet optionchildren;

	/** All options that are groups */
	private SortedSet groupchildren;

	/** Parent of this option */
	private Option parent;

	/** Cached option widget for usage within the {@link OptionDialog} */
	private OptionWidget cachedWidget;

	/////////////////////////////////////
	// @@ Constructor
	/////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 * @param defaultValue Default Value of the option
	 */
	public Option(ResourceCollection res, String optionName, Object defaultValue)
	{
		super(optionName);

		optionchildren = new TreeSet(priorityComparator);
		groupchildren = new TreeSet(priorityComparator);

		// Load display name and description from the resource
		ResourceCollectionUtil.loadDisplayObjectFromResource(this, res, optionName);

		String prefix = optionName != null ? optionName + "." : "";

		// Load the type (default: regular option)
		type = res.getOptionalString(prefix + PROPERTY_TYPE, TYPE_OPTION);

		// Load the priority
		prio = ResourceCollectionUtil.getOptionalInt(res, prefix + PROPERTY_PRIO, DFLT_PRIORITY);

		// Load the condition
		condition = res.getOptionalString(prefix + PROPERTY_CONDITION);

		// Load the option heading
		heading = res.getOptionalString(prefix + PROPERTY_HEADING);

		// Get the parent name (defaults to "optionroot")
		String optionParentName = res.getOptionalString(prefix + PROPERTY_OPTION_PARENT, OptionMgr.OPTIONROOT);

		// Check if the parent has already been registered
		OptionMgr optionMgr = OptionMgr.getInstance();

		parent = optionMgr.getOption(optionParentName);
		if (parent == null)
		{
			// If no parent exists, create a new one
			parent = new GroupOption(res, optionParentName);
			optionMgr.addOption(parent);
		}

		// Register this option at its parent
		parent.addOptionChild(this);

		this.defaultValue = defaultValue;
	}

	/**
	 * Value constructor.
	 *
	 * @param optionName Name of the option
	 * @param displayName Display name of the option
	 * @param description Description of the option
	 * @param defaultValue Default Value of the option
	 * @param type Option type ({@link #TYPE_OPTION}/{@link #TYPE_GROUP})
	 * @param parent Option parent or null
	 * @param prio Priority of the option
	 */
	public Option(String optionName, String displayName, String description, Object defaultValue, String type, Option parent, int prio)
	{
		super(optionName, displayName, description);

		optionchildren = new TreeSet(priorityComparator);
		groupchildren = new TreeSet(priorityComparator);

		this.defaultValue = defaultValue;

		this.prio = prio;
		this.type = type != null ? type : TYPE_GROUP;

		this.parent = parent;
		if (parent != null)
		{
			parent.addOptionChild(this);
		}
	}

	/**
	 * Installs the option width the option manager.
	 */
	public void install()
	{
		OptionMgr.getInstance().addOption(this);

		// Try to load the option (again)
		loadOptionValue();
	}

	/**
	 * Uninstalls the option from the option manager.
	 */
	public void uninstall()
	{
		OptionMgr.getInstance().removeOption(this);

		// save the option again
		saveOptionValue();
	}

	/**
	 * Gets the display name so it can be displayed directly by the default JTree.
	 */
	public String toString()
	{
		return getDisplayName();
	}

	//////////////////////////////////////////////////
	// @@ Abstract methods
	//////////////////////////////////////////////////

	/**
	 * Returns the widget for this option.
	 *
	 * @return The widget
	 */
	public abstract OptionWidget createOptionWidget();

	/**
	 * Serializes the option value to a string.
	 * This string value will be used to save the option using the {@link SettingUtil} class.
	 *
	 * @return A string representing the option value<br>
	 * This value corresponds to the value expected by {@link #loadFromString}.
	 */
	public abstract String saveToString();

	/**
	 * Deserializes the option value from a string.
	 * This string value will be used to save the option using the {@link SettingUtil} class.
	 *
	 * @param cryptString A string representing the option value or null
	 * This value corresponds to the value returned by {@link #saveToString}.
	 * @return The option value
	 */
	public abstract Object loadFromString(String cryptString);

	//////////////////////////////////////////////////
	// @@ Option widget
	//////////////////////////////////////////////////

	/**
	 * Returns the cached widget for this option.
	 * If no widget is currently cached, a new one will be created
	 * and initialized using the current option value.
	 *
	 * @return The widget
	 */
	public OptionWidget getCachedOptionWidget()
	{
		if (cachedWidget == null)
		{
			cachedWidget = createOptionWidget();

			// Initially set the option value
			cachedWidget.setValue(getValue());
		}
		return cachedWidget;
	}

	/**
	 * Resets the cached widget for this option.
	 * The next call to {@link #getCachedOptionWidget} will create a new widget.
	 */
	public void resetCachedOptionWidget()
	{
		cachedWidget = null;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Returns icon of the otion.
	 * @return If the icon is null, no icon will be displayed
	 */
	public Icon getIcon()
	{
		return null;
	}

	/**
	 * Returns the type of the option.
	 *
	 * @return {@link #TYPE_OPTION}/{@link #TYPE_GROUP}
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Returns the priority of the option.
	 *
	 * @return The priority (default: 50)
	 */
	public int getPriority()
	{
		return prio;
	}

	/**
	 * Gets the conditional expression that determines if the plugin should be active.
	 * @nowarn
	 */
	public String getCondition()
	{
		return condition;
	}

	/**
	 * Gets the option heading (title of the option widget).
	 * @nowarn
	 */
	public String getHeading()
	{
		return heading;
	}

	/**
	 * Returns the default value of the option.
	 * @return The default value or null
	 */
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Gets the value of the Option.
	 *
	 * @return The option value or the default value if not set
	 */
	public Object getValue()
	{
		return value != null ? value : getDefaultValue();
	}

	/**
	 * Sets the value of this option.
	 *
	 * @param o The option value
	 */
	public void setValue(Object o)
	{
		value = o;

		// save the optionvalue!
		saveOptionValue();
	}

	/**
	 * Returns the child options of this option.
	 *
	 * @return A sorted set of all option children (contains {@link Option} objects)
	 */
	public SortedSet getOptionChildren()
	{
		return optionchildren;
	}

	/////////////////////////////////////
	// @@ Serialisation
	/////////////////////////////////////

	/**
	 * Saves the option value.
	 */
	protected void saveOptionValue()
	{
		String saveString = saveToString();

		SettingUtil.setStringSetting(getName(), saveString);
	}

	/**
	 * Loads the option value from the setting manager.
	 * @return The option value or the default value if no such settings has been found
	 */
	protected Object loadOptionValue()
	{
		// try to load a saved value.
		String s = SettingUtil.getStringSetting(getName());

		if (s != null)
		{
			value = loadFromString(s);
		}

		// return the loaded value or the default value.
		return value == null ? getDefaultValue() : value;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Adds a child option.
	 *
	 * @param opt the new child option
	 */
	protected void addOptionChild(Option opt)
	{
		if (opt.getType().equals(TYPE_GROUP))
		{
			groupchildren.add(opt);
		}
		else
		{
			optionchildren.add(opt);
		}
	}

	/**
	 * Removes a child option.
	 *
	 * @param opt the option to remove
	 */
	protected void removeOptionChild(Option opt)
	{
		if (opt.getType().equals(TYPE_GROUP))
		{
			groupchildren.remove(opt);
		}
		else
		{
			optionchildren.remove(opt);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeNode implementation
	//////////////////////////////////////////////////

	/**
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int childIndex)
	{
		Iterator it = groupchildren.iterator();
		TreeNode node = (TreeNode) it.next();

		for (int i = 0; i < childIndex; i++)
		{
			node = (TreeNode) it.next();
		}

		return node;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	public int getChildCount()
	{
		return groupchildren.size();
	}

	/**
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	public TreeNode getParent()
	{
		return parent;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	public int getIndex(TreeNode node)
	{
		int count = 0;

		for (Iterator it = groupchildren.iterator(); it.hasNext();)
		{
			if (node == it.next())
			{
				return count;
			}

			count++;
		}

		return count;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren()
	{
		return true;
	}

	/**
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf()
	{
		return groupchildren.isEmpty();
	}

	/**
	 * @see javax.swing.tree.TreeNode#children()
	 */
	public Enumeration children()
	{
		return Collections.enumeration(groupchildren);
	}

	//////////////////////////////////////////////////
	// @@ inner class
	//////////////////////////////////////////////////

	/**
	 * Comparator used to sort menus and menu entries according to their
	 * priority and display name.
	 */
	private static class PriorityComparator
		implements Comparator
	{
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2)
		{
			Option a1 = (Option) o1;
			Option a2 = (Option) o2;

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
