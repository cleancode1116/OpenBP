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
package org.openbp.swing.layout.dock;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * The flexible border layout is a layout manager that is similar to the
 * BorderLayout, but allows dynamic resizing of the components and adjusting the
 * main orientation of the components.
 * Is also serves as base class for more advance layout managers like the {@link DockLayout}
 *
 * The flexible border layout lays out a container, arranging and resizing
 * its components to fit in five regions: North, south, east, west, and center.
 * Each component is optional, so layouts consisting of i. e. only a east, west and center
 * component are possible.
 *
 * The orientation (property {@link #setHorizontalOrder}) determines how the
 * components will be arranged in the container.
 *
 * horizontalOrder = true:<br>
 * The north and south components have the same width as the center component.
 * The component arrangement appears rather horizontally ordered.
 * @code 3
 * +------+------------+------+
 * |      |   north    |      |
 * |      +------------+      |
 * |      |            |      |
 * | west |   center   | east |
 * |      |            |      |
 * |      +------------+      |
 * |      |   south    |      |
 * +------+------------+------+
 * @code
 *
 * horizontalOrder = false:<br>
 * The north and south components have the same width as the center component.
 * The component arrangement appears vertically ordered.<br>
 * @code 3
 * +--------------------------+
 * |          north           |
 * +--------------------------+
 * |      |            |      |
 * | west |   center   | east |
 * |      |            |      |
 * +--------------------------+
 * |          south           |
 * +--------------------------+
 * @code
 *
 * Each region is identified by a corresponding constant:
 * <code>NORTH</code>, <code>SOUTH</code>, <code>EAST</code>,
 * <code>WEST</code>, and <code>CENTER</code>.
 * When adding a component to a container with a flexible border layout, use one of these
 * five constants.
 *
 * The components are laid out according to their preferred sizes and the
 * constraints of the container's size.
 * The north and south components may be stretched horizontally;
 * the east and west components may be stretched vertically;
 * the center component may stretch in both directions to fill any space left over.
 */
public class DockLayout
	implements LayoutManager2, java.io.Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Shorthand string constraing of center component (middle of container). */
	public static final String STR_CENTER = "Center";

	/** Shorthand string constraing of north component (top of container). */
	public static final String STR_NORTH = "North";

	/** Shorthand string constraing of south component (bottom of container). */
	public static final String STR_SOUTH = "South";

	/** Shorthand string constraing of east component (right side of container). */
	public static final String STR_EAST = "East";

	/** Shorthand string constraing of west component (left side of container). */
	public static final String STR_WEST = "West";

	/** Alternative shorthand string constraing of north component (top of container). */
	public static final String STR_TOP = "Top";

	/** Alternative shorthand string constraing of south component (bottom of container). */
	public static final String STR_BOTTOM = "Bottom";

	/** Alternative shorthand string constraing of east component (right side of container). */
	public static final String STR_RIGHT = "Right";

	/** Alternative shorthand string constraing of west component (left side of container). */
	public static final String STR_LEFT = "Left";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Main orientation of the components */
	private boolean horizontalOrder = true;

	/** Horizontal gap between the components. */
	private int horizontalGap = 2;

	/** Vertical gap between the components. */
	private int verticalGap = 2;

	/** Child component array index of center component (middle of container). */
	private static final int INDEX_CENTER = 0;

	/** Child component array index of north component (top of container). */
	private static final int INDEX_NORTH = 1;

	/** Child component array index of south component (bottom of container). */
	private static final int INDEX_SOUTH = 2;

	/** Child component array index of east component (right side of container). */
	private static final int INDEX_EAST = 3;

	/** Child component array index of west component (left side of container). */
	private static final int INDEX_WEST = 4;

	/** Child component array index of west component (left side of container). */
	private static final int NINDICES = 5;

	/** Child components of the container managed by this layout */
	private Component [] components = new Component [NINDICES];

	/** Working variable of the layoutContainer method:
	 * Top coordinate of the space left in the container */
	private int top;

	/** Working variable of the   method:
	 * Bottom coordinate of the space left in the container */
	private int bottom;

	/** Working variable of the   method:
	 * Left coordinate of the space left in the container */
	private int left;

	/** Working variable of the   method:
	 * Right coordinate of the space left in the container */
	private int right;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructs a new dock layout with
	 * no gaps between components.
	 */
	public DockLayout()
	{
		this(0, 0);
	}

	/**
	 * Constructs a dock layout with the specified gaps
	 * between components.
	 * The horizontal gap is specified by hgap
	 * and the vertical gap is specified by vgap.
	 * @param horizontalGap the horizontal gap
	 * @param verticalGap the vertical gap
	 */
	public DockLayout(int horizontalGap, int verticalGap)
	{
		this.horizontalGap = horizontalGap;
		this.verticalGap = verticalGap;
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the main order of the components.
	 * @return
	 *		true	The components appear horizontally ordered.<br>
	 *		false	The components appear vertically ordered.
	 */
	public boolean getHorizontalOrder()
	{
		return horizontalOrder;
	}

	/**
	 * Sets the main order of the components.
	 * @param horizontalOrder
	 *		true	The components appear horizontally ordered.<br>
	 *		false	The components appear vertically ordered.
	 */
	public void setHorizontalOrder(boolean horizontalOrder)
	{
		this.horizontalOrder = horizontalOrder;
	}

	/**
	 * Returns the horizontal gap between components.
	 * @nowarn
	 */
	public int getHgap()
	{
		return horizontalGap;
	}

	/**
	 * Sets the horizontal gap between components.
	 * @nowarn
	 */
	public void setHgap(int horizontalGap)
	{
		this.horizontalGap = horizontalGap;
	}

	/**
	 * Returns the vertical gap between components.
	 * @nowarn
	 */
	public int getVgap()
	{
		return verticalGap;
	}

	/**
	 * Sets the vertical gap between components.
	 * @nowarn
	 */
	public void setVgap(int verticalGap)
	{
		this.verticalGap = verticalGap;
	}

	//////////////////////////////////////////////////
	// @@ LayoutManager and LayoutManager2 implementation
	//////////////////////////////////////////////////

	/**
	 * Adds the specified component to the layout, using the specified
	 * constraint object.
	 *
	 * Most applications do not call this method directly. This method
	 * is called when a component is added to a container using the
	 * Container.add method with the same argument types.
	 *
	 * @param comp Component to be added
	 * @param constraints Object that specifies how and where
	 * the component is added to the layout.
	 * @exception IllegalArgumentException if the constraint object is not
	 * a string, or if it is not one of the five specified constants.
	 */
	public void addLayoutComponent(Component comp, Object constraints)
	{
		synchronized (comp.getTreeLock())
		{
			if (constraints == null || constraints instanceof String)
			{
				addLayoutComponent((String) constraints, comp);
			}
			else
			{
				throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
			}
		}
	}

	/**
	 * Adds the specified component with the specified constraint to the layout.
	 *
	 * @param constraint Component constraint
	 * @param comp Component to be added
	 * @deprecated replaced by {@link #addLayoutComponent(Component,Object)}
	 */
	public void addLayoutComponent(String constraint, Component comp)
	{
		synchronized (comp.getTreeLock())
		{
			int index = stringConstraintToIndex(constraint);
			if (index < 0)
			{
				throw new IllegalArgumentException("Unknown constraint '" + constraint + "' for DockLayout");
			}

			// Assign the component to one of the known regions of the layout
			components [index] = comp;
		}
	}

	/**
	 * Removes the specified component from this dock layout.
	 * This method is called when a container calls its remove or
	 * removeAll methods. Most applications do not call this
	 * method directly.
	 *
	 * @param comp Component to be removed
	 */
	public void removeLayoutComponent(Component comp)
	{
		synchronized (comp.getTreeLock())
		{
			for (int i = 0; i < NINDICES; ++i)
			{
				if (components [i] == comp)
				{
					components [i] = null;
					break;
				}
			}
		}
	}

	/**
	 * Lays out the container argument using this dock layout.
	 *
	 * This method actually reshapes the components in the specified
	 * container in order to satisfy the constraints of this
	 * DockLayout object.
	 *
	 * The NORTH and SOUTH components, if any, are placed at
	 * the top and bottom of the container, respectively. The
	 * WEST and EAST components are
	 * then placed on the left and right, respectively. Finally,
	 * the CENTER object is placed in any remaining
	 * space in the middle.
	 *
	 * Most applications do not call this method directly. This method
	 * is called when a container calls its doLayout method.
	 *
	 * @param target Container in which to do the layout
	 */
	public void layoutContainer(Container target)
	{
		synchronized (target.getTreeLock())
		{
			Insets insets = target.getInsets();
			top = insets.top;
			bottom = target.getHeight() - insets.bottom;
			left = insets.left;
			right = target.getWidth() - insets.right;

			if (horizontalOrder)
			{
				// Layout north/south first, so they have the full container width
				layoutNorthSouth();
				layoutEastWest();
			}
			else
			{
				// Layout east/west first, so they have the full container height
				layoutEastWest();
				layoutNorthSouth();
			}

			Component c = null;
			if ((c = getComponent(INDEX_CENTER)) != null)
			{
				c.setBounds(left, top, right - left, bottom - top);
			}
		}
	}

	/**
	 * Helper of the {@link #layoutContainer} method.
	 */
	private void layoutNorthSouth()
	{
		Component c = null;
		if ((c = getComponent(INDEX_NORTH)) != null)
		{
			c.setSize(right - left, c.getHeight());
			Dimension d = c.getPreferredSize();
			c.setBounds(left, top, right - left, d.height);
			top += d.height + verticalGap;
		}
		if ((c = getComponent(INDEX_SOUTH)) != null)
		{
			c.setSize(right - left, c.getHeight());
			Dimension d = c.getPreferredSize();
			c.setBounds(left, bottom - d.height, right - left, d.height);
			bottom -= d.height + verticalGap;
		}
	}

	/**
	 * Helper of the {@link #layoutContainer} method.
	 */
	private void layoutEastWest()
	{
		Component c = null;
		if ((c = getComponent(INDEX_EAST)) != null)
		{
			c.setSize(c.getWidth(), bottom - top);
			Dimension d = c.getPreferredSize();
			c.setBounds(right - d.width, top, d.width, bottom - top);
			right -= d.width + horizontalGap;
		}
		if ((c = getComponent(INDEX_WEST)) != null)
		{
			c.setSize(c.getWidth(), bottom - top);
			Dimension d = c.getPreferredSize();
			c.setBounds(left, top, d.width, bottom - top);
			left += d.width + horizontalGap;
		}
	}

	/**
	 * Determines the preferred size of the target
	 * container using this layout manager, based on the components
	 * in the container.
	 *
	 * Most applications do not call this method directly. This method
	 * is called when a container calls its getPreferredSize
	 * method.
	 *
	 * @param target Container in which to do the layout
	 * @return Preferred dimensions to lay out the subcomponents of the specified container
	 */
	public Dimension preferredLayoutSize(Container target)
	{
		return determineLayoutSize(target, true);
	}

	/**
	 * Determines the minimum size of the target container
	 * using this layout manager.
	 *
	 * This method is called when a container calls its
	 * getMinimumSize method. Most applications do not call
	 * this method directly.
	 *
	 * @param target Container in which to do the layout
	 * @return Minimum dimensions needed to lay out the subcomponents of the specified container
	 */
	public Dimension minimumLayoutSize(Container target)
	{
		return determineLayoutSize(target, false);
	}

	/**
	 * Returns the maximum dimensions for this layout given the components
	 * in the specified target container.
	 *
	 * This method is called when a container calls its
	 * getMaximumSize method. Most applications do not call
	 * this method directly.
	 *
	 * @param target Component which needs to be laid out
	 * @return Maximum dimensions needed to lay out the subcomponents of the specified container
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Determines the size of the target container using this layout manager.
	 *
	 * Helper methods of the above methods.
	 *
	 * @param target Container in which to do the layout
	 * @param preferedSize
	 *		true	Use the preferred size for the calculation.<br>
	 *		false	Use the minimum size for the calculation.
	 * @return Dimensions needed to lay out the subcomponents of the specified container
	 */
	private Dimension determineLayoutSize(Container target, boolean preferedSize)
	{
		synchronized (target.getTreeLock())
		{
			Dimension dim = new Dimension(0, 0);

			Component c = null;
			if ((c = getComponent(INDEX_EAST)) != null)
			{
				Dimension d = getSize(c, preferedSize);
				dim.width += d.width + horizontalGap;
				dim.height = Math.max(d.height, dim.height);
			}
			if ((c = getComponent(INDEX_WEST)) != null)
			{
				Dimension d = getSize(c, preferedSize);
				dim.width += d.width + horizontalGap;
				dim.height = Math.max(d.height, dim.height);
			}
			if ((c = getComponent(INDEX_CENTER)) != null)
			{
				Dimension d = getSize(c, preferedSize);
				dim.width += d.width;
				dim.height = Math.max(d.height, dim.height);
			}
			if ((c = getComponent(INDEX_NORTH)) != null)
			{
				Dimension d = getSize(c, preferedSize);
				dim.width = Math.max(d.width, dim.width);
				dim.height += d.height + verticalGap;
			}
			if ((c = getComponent(INDEX_SOUTH)) != null)
			{
				Dimension d = getSize(c, preferedSize);
				dim.width = Math.max(d.width, dim.width);
				dim.height += d.height + verticalGap;
			}

			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right;
			dim.height += insets.top + insets.bottom;

			return dim;
		}
	}

	/**
	 * Gets the size of a component.
	 *
	 * @param c Component
	 * @param preferedSize
	 *		true	Use the preferred size for the calculation.<br>
	 *		false	Use the minimum size for the calculation.
	 * @nowarn
	 */
	private Dimension getSize(Component c, boolean preferedSize)
	{
		return preferedSize ? c.getPreferredSize() : c.getMinimumSize();
	}

	/**
	 * Returns the alignment along the x axis.
	 * @param parent Container the layout has been applied to
	 * @return This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public float getLayoutAlignmentX(Container parent)
	{
		// Tells the caller that we prefer to be centered
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis.
	 * @param parent Container the layout has been applied to
	 * @return This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public float getLayoutAlignmentY(Container parent)
	{
		// Tells the caller that we prefer to be centered
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 * @param target Container the layout has been applied to
	 */
	public void invalidateLayout(Container target)
	{
		// No action
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Gets the component that corresponds to the given index if it is visible.
	 *
	 * @param index Index into component child array or null
	 * @return The component or null if the component is not visible
	 */
	protected Component getComponent(int index)
	{
		Component result = components [index];

		if (result != null && !result.isVisible())
		{
			result = null;
		}

		return result;
	}

	/**
	 * Gets the component array index that corresponds to a given constraint shortcut.
	 *
	 * @param constraint String constraing ("North", "Bottom", "Center" etc.)
	 * @return Index value (INDEX_CENTER/INDEX_NORTH/INDEX_SOUTH/INDEX_EAST/INDEX_WEST)
	 */
	protected int stringConstraintToIndex(String constraint)
	{
		int index = -1;

		if (constraint == null || constraint.equalsIgnoreCase(STR_CENTER))
		{
			index = INDEX_CENTER;
		}
		else if (constraint.equalsIgnoreCase(STR_NORTH) || constraint.equalsIgnoreCase(STR_TOP))
		{
			index = INDEX_NORTH;
		}
		else if (constraint.equalsIgnoreCase(STR_SOUTH) || constraint.equalsIgnoreCase(STR_BOTTOM))
		{
			index = INDEX_SOUTH;
		}
		else if (constraint.equalsIgnoreCase(STR_EAST) || constraint.equalsIgnoreCase(STR_RIGHT))
		{
			index = INDEX_EAST;
		}
		else if (constraint.equalsIgnoreCase(STR_WEST) || constraint.equalsIgnoreCase(STR_LEFT))
		{
			index = INDEX_WEST;
		}

		return index;
	}

	//////////////////////////////////////////////////
	// @@ Stuff
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of the state of this object.
	 * @nowarn
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[");
		sb.append("order=");
		sb.append(horizontalOrder ? "horizontal" : "vertical");
		sb.append(",horizontalGap=");
		sb.append(horizontalGap);
		sb.append(",verticalGap=");
		sb.append(verticalGap);
		sb.append("]");
		return sb.toString();
	}
}
