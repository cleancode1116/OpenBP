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
package org.openbp.swing.layout.splitter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

/**
 * The splitter layout manager arranges several components that are separated by
 * {@link SplitterBar} components.
 * An intelligent resize algorithm determines the size of the remaining components
 * when one of the splitter bars is moved.<br>
 * For each component, its minimum and maximum sizes will be considered when giving
 * the user a visual feedback when moving the splitter bar, e. g. the splitter bar
 * won't move to a position that contradicts the constraints of the components.
 *
 * The splitter layout can be oriented horizontally or Vertically. Any SpliterBars
 * placed in the container will automatically be oriented.
 *
 * When adding components to the container, a constraint can be given.
 * The constraint can be either a {@link SplitterConstraint} object or a string.<br>
 * If the string contains the keyword "fill", or the {@link SplitterConstraint#setFiller}
 * property has been set, the component will be used as filler, e. g. it will be used
 * to fill up any remaining space when layout out the container. In a typical use case
 * of a graphical editor with tool windows at its sides, the editor workspace
 * would be defined as filler component. The layout manager will retain the sizes of
 * the non-filler components if possible.<br>
 * If there are no filler components at all, all non-SplitterBar components will be
 * used as fillers, e. g. their sizes will be changed as needed.
 */
public class SplitterLayout
	implements LayoutManager2, java.io.Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Aligns components vertically. SplitterBars will move up/down */
	public static final int VERTICAL = 0;

	/** Aligns components horizontally. SplitterBars will move left-right */
	public static final int HORIZONTAL = 1;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Orientation property */
	private int orientation = VERTICAL;

	/** Table mapping components to their layout constraints */
	private Map componentsToConstraints = new HashMap();

	/** Array of components in the container
	 * (working variable used during the layout process only). */
	private Component currentComps[];

	/** Array of splitter constraints that correspond to the components in the container
	 * (working variable used during the layout process only). */
	private SplitterConstraint currentConstraints[];

	/** Array of component size */
	private int currentSizes[];

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Default orientation is vertical.
	 */
	public SplitterLayout()
	{
		this(VERTICAL);
	}

	/**
	 * Constructor.
	 *
	 * @param orientation Orientation of the components in the container
	 * ({@link #VERTICAL} or {@link #HORIZONTAL})
	 */
	public SplitterLayout(int orientation)
	{
		setOrientation(orientation);
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the orientation of the layout.
	 * @return The orientation ({@link #VERTICAL} or {@link #HORIZONTAL})
	 */
	public int getOrientation()
	{
		/* Returns the orientation property value. */
		return orientation;
	}

	/**
	 * Sets the orientation of the layout.
	 * @param orientation The new orientation ({@link #VERTICAL} or {@link #HORIZONTAL})
	 */
	public void setOrientation(int orientation)
	{
		this.orientation = orientation;
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
	 * @exception IllegalArgumentException If the constraint object is not a number
	 */
	public final void addLayoutComponent(Component comp, Object constraints)
	{
		if (comp instanceof SplitterBar)
		{
			// Use the container's orientation for the splitter bar
			// Splitter bars don't have constraints
			((SplitterBar) comp).setOrientation(orientation);
			return;
		}

		SplitterConstraint sc = null;

		if (constraints instanceof SplitterConstraint)
		{
			// Constraint object provided directly
			sc = (SplitterConstraint) constraints;
		}
		else
		{
			// Provide default constraint object
			sc = new SplitterConstraint();

			if (constraints instanceof String)
			{
				// Parse string for constraint properties
				String s = (String) constraints;
				s = s.toLowerCase();
				if (s.indexOf("fill") != -1)
					sc.setFiller(true);
			}
		}

		// Save the constraint
		componentsToConstraints.put(comp, sc);
	}

	/**
	 * Adds the specified component with the specified constraint to the layout.
	 *
	 * @param constraint Component constraint
	 * @param comp Component to be added
	 * @deprecated replaced by {@link #addLayoutComponent(Component,Object)}
	 */
	public final void addLayoutComponent(String constraint, Component comp)
	{
		addLayoutComponent(comp, constraint);
	}

	/**
	 * Removes the specified component from this dock layout.
	 * This method is called when a container calls its remove or
	 * removeAll methods. Most applications do not call this
	 * method directly.
	 *
	 * @param comp Component to be removed
	 */
	public final void removeLayoutComponent(Component comp)
	{
		componentsToConstraints.remove(comp);
	}

	/**
	 * Lays out the container argument using this layout.
	 *
	 * This method actually reshapes the components in the specified
	 * container in order to satisfy the constraints of this
	 * DockLayout object.
	 *
	 * Most applications do not call this method directly. This method
	 * is called when a container calls its doLayout method.
	 *
	 * @param container Container in which to do the layout
	 */
	public final void layoutContainer(Container container)
	{
		// Initialize the layout process work variables and
		// ensure that there is at least one filler component
		if (!initLayoutProcess(container))
		{
			// The container does not contain any visible component
			return;
		}

		boolean isVertical = orientation == SplitterLayout.VERTICAL;

		Insets insets = container.getInsets();
		Dimension dim = container.getSize();

		SplitterConstraint sc;
		Component c;
		Dimension d;

		//
		// First, determine the spaces that will be considered in the layout computation.
		//

		// Total space in container we have for the layout
		int spaceInContainer;
		if (isVertical)
			spaceInContainer = dim.height - insets.top - insets.bottom;
		else
			spaceInContainer = dim.width - insets.left - insets.right;

		// Total actual size of the components
		int totalCurrentSize = 0;

		// Total preferred size of the components
		int totalPreferredSize = 0;

		// Total minumum size of the non-filler components
		int totalMinSize = 0;

		// Total minumum size of the filler components
		int totalFillerMinSize = 0;

		// Total size of the splitters
		int totalSplitterSize = 0;

		// Filler components
		List fillerIndices = new ArrayList(currentComps.length / 2 + 1);

		// Number of fill components
		int nFillers = 0;

		for (int i = 0; i < currentComps.length; i++)
		{
			Dimension dp = null;

			c = currentComps [i];
			if (!c.isVisible())
				continue;

			int size = isVertical ? c.getHeight() : c.getWidth();
			if (size == 0)
			{
				dp = c.getPreferredSize();
				size = isVertical ? dp.height : dp.width;
			}

			if (c instanceof SplitterBar)
			{
				// Splitter size never changes
				totalSplitterSize += size;
			}
			else
			{
				d = c.getMinimumSize();
				int minSize = isVertical ? d.height : d.width;

				sc = currentConstraints [i];
				if (sc.isAnyFiller())
				{
					fillerIndices.add(Integer.valueOf(i));
					++nFillers;

					// Fillers will take up at least their minimum size
					totalFillerMinSize += minSize;
				}
				else
				{
					totalCurrentSize += size;
					totalMinSize += minSize;

					// Non-fillers try to take up their preferred size
					if (dp == null)
						dp = c.getPreferredSize();
					if (dp != null)
					{
						totalPreferredSize += isVertical ? dp.height : dp.width;
					}
				}
			}
		}

		//
		// Now determine the component sizes to use as calculation basis
		//

		final int USE_CURR_SIZE = 0;
		final int USE_PREF_SIZE = 1;
		final int USE_MIN_SIZE = 2;
		int mode = USE_CURR_SIZE;

		// First see if we can fit in the current size of the components
		if (totalCurrentSize != 0 && totalCurrentSize + totalSplitterSize + totalFillerMinSize <= spaceInContainer)
		{
			mode = USE_CURR_SIZE;
		}

		// Now try the preferred size
		else if (totalPreferredSize + totalSplitterSize + totalFillerMinSize <= spaceInContainer)
		{
			mode = USE_PREF_SIZE;
		}

		// Minimum size is always our fallback
		else
		{
			mode = USE_MIN_SIZE;
		}

		//
		// Determine the space occupied by the non-fillers
		//

		int totalFillerSize = spaceInContainer;

		for (int i = 0; i < currentComps.length; i++)
		{
			c = currentComps [i];
			if (!c.isVisible())
				continue;

			if (c instanceof SplitterBar)
			{
				// Splitters do not have constraints
				d = c.getPreferredSize();
			}
			else
			{
				if (currentConstraints [i].isAnyFiller())
				{
					// Fillers have been processed already
					continue;
				}

				if (mode == USE_CURR_SIZE)
					d = c.getSize();
				else if (mode == USE_PREF_SIZE)
					d = c.getPreferredSize();
				else
					d = c.getMinimumSize();
			}

			int s = isVertical ? d.height : d.width;
			currentSizes [i] = s;
			totalFillerSize -= s;
		}

		//
		// Now that we have the size of the non-filler components,
		// determine the spacing of the fillers
		//

		// Make sure that the size for each filler does not exceed the maximum size
		int remainingSize = totalFillerSize;
		for (int fi = 0; fi < nFillers; ++fi)
		{
			int fillerIndex = ((Integer) fillerIndices.get(fi)).intValue();

			sc = currentConstraints [fillerIndex];
			if (sc.isAnyFiller())
			{
				d = currentComps [fillerIndex].getMinimumSize();
				int minSize = isVertical ? d.height : d.width;

				d = currentComps [fillerIndex].getMaximumSize();
				int maxSize = isVertical ? d.height : d.width;

				if (maxSize != 0 && maxSize != Integer.MAX_VALUE)
				{
					d = currentComps [fillerIndex].getPreferredSize();
					int prefSize = isVertical ? d.height : d.width;
					if (prefSize > maxSize)
						maxSize = prefSize;
				}
				maxSize = 10000; // TODO Minor: Due to Swing bug; remove later

				int fillerSize = remainingSize / (nFillers - fi);
				if ((minSize != 0 && fillerSize < minSize) || (maxSize != 0 && fillerSize > maxSize))
				{
					// We exceed the maximum size of this filler.
					currentSizes [fillerIndex] = (minSize != 0 && fillerSize < minSize) ? minSize : maxSize;
					totalFillerSize -= currentSizes [fillerIndex];

					// Ok, this component's size is finished
					fillerIndices.remove(fi);
					--nFillers;

					// Restart from the beginning since we need to (re-)adjust all other fillers
					remainingSize = totalFillerSize;
					fi = -1;
					continue;
				}

				currentSizes [fillerIndex] = fillerSize;
				remainingSize -= fillerSize;
			}
		}

		//
		// Finally adjust the component positions and sizes
		//
		adjustComponentPositions(dim, insets);

		// Reset layout process work variables
		resetLayoutProcess();
	}

	/**
	 * Processes a splitter bar drag event.
	 * This method is being called from inside the mouse handler methods of the splitter.
	 * This method will try to apply the given offset to the position of the specified
	 * splitter. If the offset causes layout changes that conflict with minimum or maximum
	 * sizes, the offset will be adjusted so that the conflict is resolved.
	 *
	 * @param splitter Splitter being dragged
	 * @param pDelta Offset to current splitter bar position.<br>
	 * This offset may be modified by the method if the delta would violate the minimum sizes
	 * of the components of the target container.
	 * @param updateLayout
	 *		true	Update the layout immediately.<br>
	 *		false	Check the delta only.
	 */
	public void processSplitterDrag(SplitterBar splitter, Point pDelta, boolean updateLayout)
	{
		Container container = splitter.getParent();

		// Initialize the layout process work variables and
		// ensure that there is at least one filler component
		if (!initLayoutProcess(container))
		{
			// The container does not contain any visible component
			return;
		}

		// Copy the current component sizes
		copyCurrentSizes();

		// Determine orientation and direction of splitter bar movement
		boolean isVertical = orientation == SplitterLayout.VERTICAL;
		int delta = isVertical ? pDelta.y : pDelta.x;
		int pos = isVertical ? splitter.getLocation().y : splitter.getLocation().x;
		int splitterSize = isVertical ? splitter.getHeight() : splitter.getWidth();
		boolean moveUpOrLeft = delta < 0;

		// Determine the minimum size and check if delta is still in range
		int minSizeTotal = determineMinimumSize(container, splitter, moveUpOrLeft);
		if (moveUpOrLeft)
		{
			if (pos + delta < minSizeTotal)
			{
				// Too small, correct to minimum value
				delta = minSizeTotal - pos;
			}
		}
		else
		{
			int iTotal = isVertical ? container.getHeight() : container.getWidth();
			if (pos + splitterSize + delta > iTotal - minSizeTotal)
			{
				// Too small, correct to minimum value
				delta = iTotal - minSizeTotal - pos - splitterSize;
			}
		}
		if (isVertical)
			pDelta.y = delta;
		else
			pDelta.x = delta;

		// Now update the component's position if we are in update mode.
		// Otherwise, we're done here (except the cleanup, which is done below)
		if (updateLayout)
		{
			// Determine which component "this" is
			int curr;
			for (curr = 0; curr < currentComps.length && currentComps [curr] != splitter; ++curr)
			{
				// Empty look
			}

			// The following algorithm combines four cases:
			// - vertical orientation & movement up
			// - vertical orientation & movement down
			// - horizontal orientation & movement left
			// - horizontal orientation & movement right
			// These cases are pretty much identical, except that
			// in vertical mode, we refer to y corrdinates, whereas in horizontal mode
			// we refer to x coordinates and when moving up/left, the direction of
			// processing the elements is from first to last and vice versa when moving
			// down/right.
			//
			// The comments below always refer to vertical orientation with movement up

			// Make positive
			if (delta < 0)
				delta = -delta;
			int deltaSav = delta;

			Component c;
			Dimension d;

			//
			// Make the components before the current one smaller
			//

			int i = moveUpOrLeft ? curr - 1 : curr + 1;
			while (moveUpOrLeft ? i >= 0 : i < currentComps.length)
			{
				if (delta <= 0)
					break;

				c = currentComps [i];
				if (!(c instanceof SplitterBar))
				{
					d = c.getMinimumSize();
					int minSize = orientation == VERTICAL ? d.height : d.width;

					if (currentSizes [i] - delta >= minSize)
					{
						// Make the component somewhat smaller
						currentSizes [i] -= delta;
						delta = 0;
					}
					else
					{
						// Reduce the component to minimum size
						delta -= currentSizes [i] - minSize;
						currentSizes [i] = minSize;
					}
				}

				if (moveUpOrLeft)
					--i;
				else
					++i;
			} // For each component before us

			//
			// Enlarge the components after the current one to fill up the new space
			//

			// Restore original delta value
			delta = deltaSav;

			i = moveUpOrLeft ? curr + 1 : curr - 1;
			while (moveUpOrLeft ? i < currentComps.length : i >= 0)
			{
				if (delta <= 0)
					break;

				c = currentComps [i];
				if (!(c instanceof SplitterBar))
				{
					d = c.getMaximumSize();
					int maxSize = orientation == VERTICAL ? d.height : d.width;
					maxSize = 10000; // TODO Minor: Due to Swing bug; remove later

					if (currentSizes [i] + delta <= maxSize)
					{
						// Make the component somewhat larger
						currentSizes [i] += delta;
						delta = 0;
					}
					else
					{
						// Enlarge the component to maximum size
						delta -= maxSize - currentSizes [i];
						currentSizes [i] = maxSize;
					}
				}

				if (moveUpOrLeft)
					++i;
				else
					--i;
			} // For each component after us

			// Finally adjust the component positions and sizes
			Insets insets = container.getInsets();
			Dimension dim = container.getSize();
			adjustComponentPositions(dim, insets);
		}

		// Reset layout process work variables
		resetLayoutProcess();
	}

	/**
	 * Adjust the component positions and sizes according to the computed component sizes.
	 * @param dim Dimensions of the container
	 * @param insets Insets of the container
	 */
	private void adjustComponentPositions(Dimension dim, Insets insets)
	{
		int x = insets.left;
		int y = insets.top;
		int width = dim.width - insets.left - insets.right;
		int height = dim.height - insets.top - insets.bottom;

		for (int i = 0; i < currentComps.length; i++)
		{
			Component c = currentComps [i];
			if (!c.isVisible())
				continue;

			if (orientation == VERTICAL)
				height = currentSizes [i];
			else
				width = currentSizes [i];

			c.setBounds(x, y, width, height);

			if (orientation == VERTICAL)
				y += height;
			else
				x += width;
			if (c instanceof JComponent)
				((JComponent) c).validate();
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
	public final Dimension preferredLayoutSize(Container target)
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
	public final Dimension minimumLayoutSize(Container target)
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
	public final Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
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
	// @@ Methods to be used by the Splitter
	//////////////////////////////////////////////////

	/**
	 * Determines the minmum size of the target container in respect to the current component.
	 * This method is used to compute the minimum size of the container above or below
	 * the current component when dragging a splitter (which is the current component).
	 *
	 * @param target Container in which to do the layout
	 * @param currentComponent Component to start the size determination from.
	 * The size of the current component itself will be excluded.
	 * @param before
	 *		false	The method will return the space from the current component
	 *				to the top/to the left of the container (before the component).<br>
	 *		true	The method will return the space from the current component
	 *				to the bottom/to the right of the container (after the component).<br>
	 * @return Dimensions needed to lay out the subcomponents of the specified container
	 */
	private int determineMinimumSize(Container target, Component currentComponent, boolean before)
	{
		Component comps[] = target.getComponents();

		int start = 0;
		int end = comps.length;

		if (currentComponent != null)
		{
			// Determine start/end point according to current component
			for (int i = 0; i < comps.length; i++)
			{
				if (currentComponent == comps [i])
				{
					if (before)
						end = i;
					else
						start = i + 1;
				}
			}
		}

		int space = 0;
		Insets insets = target.getInsets();
		if (before)
			space = orientation == VERTICAL ? insets.top : insets.left;
		else
			space = orientation == VERTICAL ? insets.bottom : insets.right;

		// Add the sizes of the components before/after the current one
		for (int i = start; i < end; i++)
		{
			Dimension d;

			Component c = comps [i];
			if (c.isVisible())
			{
				if (c instanceof SplitterBar)
					d = c.getPreferredSize();
				else
					d = c.getMinimumSize();
				space += orientation == VERTICAL ? d.height : d.width;
			}
		}

		return space;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines the size of the target container using this layout manager.
	 * If a current component is given, the size before (e. g. to the top or to the left)
	 * or after (e. g. from the component to the bottom or to the right) the component
	 * will be determined.
	 *
	 * Helper methods of the above methods.
	 *
	 * @param target Container in which to do the layout
	 * @param preferredSize
	 *		true	Use the preferred size for the calculation.<br>
	 *		false	Use the minimum size for the calculation.
	 * @return Dimensions needed to lay out the subcomponents of the specified container
	 */
	private Dimension determineLayoutSize(Container target, boolean preferredSize)
	{
		Dimension dim = new Dimension(0, 0);
		Dimension d;

		Component comps[] = target.getComponents();

		for (int i = 0; i < comps.length; i++)
		{
			Component c = comps [i];
			if (c.isVisible())
			{
				if (preferredSize || (c instanceof SplitterBar))
					d = c.getPreferredSize();
				else
					d = c.getMinimumSize();
				if (orientation == VERTICAL)
				{
					dim.width = Math.max(d.width, dim.width);
					dim.height += d.height;
				}
				else
				{
					dim.height = Math.max(d.height, dim.height);
					dim.width += d.width;
				}
			}
		}

		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}

	/**
	 * Initializes some working variables of the layout process.
	 * Also ensures that there is at least one filler component in the container.
	 *
	 * @param container Container the layout has been applied to
	 * @return
	 *		true	The container contains at least one component to layout<br>
	 *		false	The container does not contain any visible component
	 */
	private boolean initLayoutProcess(Container container)
	{
		// Initialize the caches
		currentComps = container.getComponents();
		currentConstraints = new SplitterConstraint [currentComps.length];
		currentSizes = new int [currentComps.length];

		// Ensure that:
		// 1. There are no overflous splitter bars visible
		// 2. There is at least one filler component that takes up the remaining space

		boolean foundFiller = false;
		boolean expectSplitter = false;
		for (int i = 0; i < currentComps.length; i++)
		{
			SplitterConstraint sc;

			// Constraints of splitters and invisible components will not be considered
			currentConstraints [i] = null;

			Component c = currentComps [i];
			if (!c.isVisible())
				continue;

			if (c instanceof SplitterBar)
			{
				// Copy the container's orientation to the splitter
				((SplitterBar) c).setOrientation(orientation);

				if (!expectSplitter || i == currentComps.length - 1)
				{
					// We expect a component or are at the bottom;
					// hide this splitter
					c.setVisible(false);
					continue;
				}
				expectSplitter = false;
			}
			else
			{
				// Save the splitter constraints
				sc = getSplitterConstraint(c);
				currentConstraints [i] = sc;

				// Clear makeshift filler flag
				sc.setMakeshiftFiller(false);

				// Check if the component is a defined filler
				if (sc.isFiller())
					foundFiller = true;

				expectSplitter = true;
			}
		}

		// Make sure we have at least one filler
		if (!foundFiller)
		{
			// No filler yet. Make them all fillers.
			for (int i = 0; i < currentComps.length; i++)
			{
				Component c = currentComps [i];
				if (!c.isVisible() || (c instanceof SplitterBar))
					continue;

				currentConstraints [i].setMakeshiftFiller(true);
			}
		}

		return true;
	}

	/**
	 * Initializes the size working variables with the current component sizes.
	 */
	private void copyCurrentSizes()
	{
		for (int i = 0; i < currentComps.length; i++)
		{
			int size;
			if (orientation == VERTICAL)
				size = currentComps [i].getHeight();
			else
				size = currentComps [i].getWidth();
			currentSizes [i] = size;
		}
	}

	/**
	 * Retrieves the constraints of a component.
	 * @param c Component to look at
	 * @return The component's constraints or null if the component does not have any constraints
	 * (this shouldn't be the case as long as the component was properly added to the container).
	 */
	protected SplitterConstraint getSplitterConstraint(Component c)
	{
		return (SplitterConstraint) componentsToConstraints.get(c);
	}

	/**
	 * Resets the working variables of the layout process.
	 */
	private void resetLayoutProcess()
	{
		currentComps = null;
		currentConstraints = null;
		currentSizes = null;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of the state of this object.
	 * @nowarn
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[");
		sb.append("orientation=");
		sb.append(orientation == VERTICAL ? "vertical" : "horizontal");
		sb.append("]");
		return sb.toString();
	}
}
