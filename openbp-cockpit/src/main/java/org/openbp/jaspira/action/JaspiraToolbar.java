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

import javax.swing.JToolBar;

import org.openbp.common.icon.FlexibleSize;

/**
 * Toolbar that supports multi size components.
 *
 * @author Stephan Moritz
 */
public class JaspiraToolbar extends JToolBar
	implements FlexibleSize, ContainerListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** The current size of the underlying icons. */
	private int iconSize;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public JaspiraToolbar()
	{
		this(SMALL, HORIZONTAL);
	}

	/**
	 * Constructor.
	 * @param size The icon size descriptor (see {@link FlexibleSize})
	 * @param orientation Orientation of the toolbar (see JToolBar)
	 */
	public JaspiraToolbar(int size, int orientation)
	{
		super(orientation);

		iconSize = size;

		this.addContainerListener(this);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property access
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
		this.iconSize = size;

		// Notify children
		setChildrenSizes();

		revalidate();
		repaint();
	}

	/**
	 * Set the iconSizes of all FlexibleSize children to our own iconsize.
	 */
	private void setChildrenSizes()
	{
		Component [] comps = getComponents();

		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] instanceof FlexibleSize)
			{
				((FlexibleSize) comps [i]).setIconSize(iconSize);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Adding of components
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a {@link JaspiraAction} to the toolbar.
	 * @param action Action to add
	 * @return The added toobar component (usually a {@link JaspiraToolbarButton})
	 */
	public Component add(JaspiraAction action)
	{
		if (action == null)
			return null;

		return add(action.toToolBarComponent());
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ContainerListener
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Is called when a new component is added. If the new component is a flexible size
	 * component, sets its size accordingly.
	 * @nowarn
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
	 * Does nothing.
	 * @nowarn
	 */
	public void componentRemoved(ContainerEvent e)
	{
	}
}
