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
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openbp.common.icon.FlexibleSize;

/**
 * Popup menu that supports multi icons.
 *
 * @author Stephan Moritz
 */
public class JaspiraPopupMenu extends JPopupMenu
	implements FlexibleSize, ContainerListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** The current size of the underlying icons. */
	private int iconSize = SMALL;

	/////////////////////////////////////////////////////////////////////////
	// @@ COnstruction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public JaspiraPopupMenu()
	{
		super();

		addContainerListener(this);
	}

	/**
	 * Constructor.
	 *
	 * @param label Menu label
	 */
	public JaspiraPopupMenu(String label)
	{
		super(label);

		addContainerListener(this);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Adding of entries
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a Jaspira action to the menu
	 *
	 * @param a Action to add
	 * @return The added menu item
	 */
	public JMenuItem add(JaspiraAction a)
	{
		return add(a.toMenuItem());
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
	public void setIconSize(int size)
	{
		// Set our own size
		iconSize = size;

		// Now set the sizes of all our children
		Component comps[] = getComponents();
		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] instanceof FlexibleSize)
			{
				((FlexibleSize) comps [i]).setIconSize(size);
			}
		}

		revalidate();
		repaint();
	}

	//////////////////////////////////////////////////
	// @@ ContainerListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.event.ContainerListener#componentAdded(ContainerEvent)
	 */
	public void componentAdded(ContainerEvent e)
	{
		Component comp = e.getChild();

		if (comp instanceof FlexibleSize)
		{
			((FlexibleSize) comp).setIconSize(iconSize);
		}
	}

	/**
	 * @see java.awt.event.ContainerListener#componentRemoved(ContainerEvent)
	 */
	public void componentRemoved(ContainerEvent e)
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Determines the popup menu from a given menu component.
	 *
	 * @param component The component within a popup menu
	 * @return The popup menu or null if the parent menu cannot be determined
	 */
	public static JaspiraPopupMenu getMenuByMenuComponent(Object component)
	{
		if (component == null)
			return null;

		if (component instanceof JaspiraPopupMenu)
			return (JaspiraPopupMenu) component;

		if (component instanceof JComponent)
			return getMenuByMenuComponent(((JComponent) component).getParent());

		return null;
	}
}
