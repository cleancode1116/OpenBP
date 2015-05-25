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
package org.openbp.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;

/**
 * Layout manager for a container that contains a single object.
 * The object may be vertically and/or horizontally aligned/centered.
 *
 * @author Heiko Erhardt
 */
public class UnitLayout
	implements LayoutManager2, Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Align with top of container */
	public static final int TOP = 1;

	/** Align with left edge of container */
	public static final int LEFT = 1;

	/** Center in container */
	public static final int CENTER = 2;

	/** Align with right edge of container */
	public static final int RIGHT = 3;

	/** Align with bottom of container */
	public static final int BOTTOM = 3;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Horizontal alignment */
	protected int xpos = CENTER;

	/** Vertical alignment */
	protected int ypos = CENTER;

	/** Flag if a component has been added */
	private boolean haveComponent = false;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * Centers the component vertically and horizontally.
	 */
	public UnitLayout()
	{
		this(CENTER, CENTER);
	}

	/**
	 * Default constructor.
	 * @param xpos Horizontal alignment of the component ({@link #LEFT}/{@link #CENTER}/{@link #RIGHT})
	 * @param ypos Vertical alignment of the component ({@link #TOP}/{@link #CENTER}/{@link #BOTTOM})
	 */
	public UnitLayout(int xpos, int ypos)
	{
		super();
		this.xpos = xpos;
		this.ypos = ypos;
	}

	//////////////////////////////////////////////////
	// @@ LayoutManager2 implementation
	//////////////////////////////////////////////////

	/**
	 * Add the specified component from the layout.
	 * By default, we let the Container handle this directly.
	 *
	 * @param comp The component to be added
	 * @param constraints The constraints to apply when laying out
	 */
	public void addLayoutComponent(Component comp, Object constraints)
	{
		if (haveComponent)
			throw new IllegalArgumentException("Only one child component permitted");
		haveComponent = true;
	}

	/**
	 * Not used.
	 * @deprecated replaced by addLayoutComponent(Component, Object)
	 * @nowarn
	 */
	public void addLayoutComponent(String name, Component comp)
	{
		addLayoutComponent(comp, name);
	}

	/**
	 * Removes the specified component from the layout.
	 * By default, we let the Container handle this directly.
	 *
	 * @param comp the component to be removed
	 */
	public void removeLayoutComponent(Component comp)
	{
		haveComponent = false;
	}

	/**
	 * Calculates the minimum size for the specified page.
	 *
	 * @param container The name of the parent container
	 * @return minimum dimensions required to lay out the components
	 */
	public Dimension minimumLayoutSize(Container container)
	{
		Dimension size = haveComponent ? new Dimension(container.getComponent(0).getMinimumSize()) : new Dimension(10, 10);

		Insets insets = container.getInsets();
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;

		return size;
	}

	/**
	 * Returns the maximum dimensions for this layout given
	 * the component in the specified target container.
	 *
	 * @param target The component which needs to be laid out
	 * @nowarn
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Calculates the preferred size for the specified container.
	 *
	 * @param container The name of the parent container
	 * @return minimum dimensions required to lay out the components
	 */
	public Dimension preferredLayoutSize(Container container)
	{
		return minimumLayoutSize(container);
	}

	/**
	 * Lays out the specified container using this layout.
	 *
	 * @param container Container that owns the layout
	 */
	public void layoutContainer(Container container)
	{
		if (!haveComponent)
			return;

		Component component = container.getComponent(0);
		Dimension size = container.getSize();
		Insets insets = container.getInsets();
		int x = insets.left;
		int y = insets.top;
		int w = component.getPreferredSize().width;
		int h = component.getPreferredSize().height;

		if (xpos == CENTER)
			x += (size.width - w) / 2;
		if (xpos == RIGHT)
			x += size.width - w;

		if (ypos == CENTER)
			y += (size.height - h) / 2;
		if (ypos == BOTTOM)
			y += size.height - h;

		component.setBounds(x, y, w, h);
	}

	/**
	 * Returns the alignment along the x axis. This specifies how
	 * the component would like to be aligned relative to other
	 * components. The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 *
	 * @param parent Container that owns the layout
	 * @return The alignment
	 */
	public float getLayoutAlignmentX(Container parent)
	{
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis. This specifies how
	 * the component would like to be aligned relative to other
	 * components. The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 *
	 * @param parent Container that owns the layout
	 * @return The alignment
	 */
	public float getLayoutAlignmentY(Container parent)
	{
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout
	 * manager has cached information it should be discarded.
	 *
	 * @param target Container that owns the layout
	 */
	public void invalidateLayout(Container target)
	{
	}
}
