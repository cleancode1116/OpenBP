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

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;

/**
 * Menu that refers to a Jaspira action and supports multi icons.
 *
 * @author Stephan Moritz
 */
public class JaspiraMenu extends JMenu
	implements FlexibleSize, PropertyChangeListener, HierarchyListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Action represented by this menu */
	private JaspiraAction action;

	/** Menu icon */
	private MultiIcon icon;

	/** Current icon size for the menu */
	private int iconSize;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param action Action this menu represents
	 */
	public JaspiraMenu(JaspiraAction action)
	{
		super(action);

		this.action = action;
		copyFromAction(null);

		// Add ourself as property listener to the action so we are notified of state changes
		// like enabled, selected, text etc.
		// The link to the listener will be a weak link, so we don't need to care about removing
		// ourself from the action's listener list (however, we do it anyway using a hierarchy listener
		// in order to prevent zombie component updates when there is no garbage collection).
		action.addPropertyChangeListener(this);

		// The hierarchy listener will remove the property change listener
		// when the component is being removed from its parent
		// TODO Optimize 6: Remove property change listener when Jaspira component is not in use any more addHierarchyListener (this);

		// The actionPerformed method of this class will forward the menu activiation to the action
		addActionListener(action);

		icon = action.getIcon();
		if (icon != null)
		{
			// Actually, we should clone the icon here; however, since we use the same icon size everywhere, this is not necessary.
			setIcon(icon.getIcon(FlexibleSize.SMALL));
		}
	}

	/**
	 * Copies the values from the action to the menu.
	 *
	 * @param propertyName Name of the property that has changed and needs to copied.
	 * If this parameter is null, the method will copy all parameters of the action.
	 * Otherwise, it will only copy the given property, if applicable.
	 */
	private void copyFromAction(String propertyName)
	{
		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_NAME))
		{
			setActionCommand(action.getName());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DISPLAY_NAME))
		{
			setText(action.getDisplayName());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DESCRIPTION))
		{
			setToolTipText(action.getDescription());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_ENABLED))
		{
			setEnabled(action.isEnabled());
		}

		if (propertyName == null && action.isMnemonic())
		{
			setMnemonic(action.getMnemonicChar());
			setDisplayedMnemonicIndex(action.getMnemonicPos());
		}
	}

	/**
	 * Gets the action represented by this menu.
	 * @nowarn
	 */
	public JaspiraAction getJaspiraAction()
	{
		return action;
	}

	//////////////////////////////////////////////////
	// @@ HierarchyListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when the parent of the component changes.
	 * Removes or adds this component as property change listener from its action.
	 * @nowarn
	 */
	public void hierarchyChanged(HierarchyEvent e)
	{
		if (!isShowing())
		{
			action.removePropertyChangeListener(this);
		}
		else
		{
			action.addPropertyChangeListener(this);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Adding items
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a Jaspira action to the menu
	 *
	 * @param a Action to add
	 * @return The added menu item
	 */
	public JMenuItem add(JaspiraAction a)
	{
		return a != null ? add(a.toMenuItem()) : null;
	}

	/**
	 * Adds a Jaspira menu item to the menu.
	 *
	 * @param mi Menu item to add
	 * @return The menu item
	 */
	public JMenuItem add(JaspiraMenuItem mi)
	{
		if (mi == null)
			return null;

		mi.setIconSize(iconSize);

		return super.add(mi);
	}

	/**
	 * Adds a regular menu item to the menu.
	 *
	 * @param mi Menu item to add
	 * @return The menu item
	 */
	public JMenuItem add(JMenuItem mi)
	{
		return mi != null ? super.add(mi) : null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ FlexibleSize implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.common.icon.FlexibleSize#getIconSize()
	 */
	public int getIconSize()
	{
		return iconSize;
	}

	/**
	 * @see org.openbp.common.icon.FlexibleSize#setIconSize(int)
	 */
	public void setIconSize(int iconSize)
	{
		// Set our own size
		this.iconSize = iconSize;

		// Now set the sizes of all our children
		Component [] comps = getMenuComponents();
		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] instanceof FlexibleSize)
			{
				((FlexibleSize) comps [i]).setIconSize(iconSize);
			}
		}

		revalidate();
		repaint();
	}

	/**
	 * @see javax.swing.JMenu#addSeparator()
	 */
	public void addSeparator()
	{
		super.addSeparator();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property change listener implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		// Update the menu from the action
		copyFromAction(evt.getPropertyName());
	}
}
