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
package org.openbp.cockpit.modeler.figures.layouter;

import java.awt.Insets;
import java.awt.Rectangle;

import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;

/**
 * Layout multiplexer that delegates layouting to a surrogate according to the direction of the tag.
 *
 * @author Stephan Moritz
 */
public class MultiplexLayouter extends AbstractTagLayouter
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The currently active layouter */
	private TagLayouter activeLayouter;

	/** List of sub layouters to use for each direction */
	private TagLayouter [] subLayouters;

	/** List of directions to use for each layouter */
	private int [] directionMapper;
	
	private static final int NUMBER_OF_LAYOUTERS_PER_CIRCLE = 8;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param tagFigure Tag to lay out
	 */
	public MultiplexLayouter(AbstractTagFigure tagFigure)
	{
		super(tagFigure);
	}

	/**
	 * Sets the surrogate layouter for a given direction range.
	 *
	 * @param begin Begin of the range (inclusive, one of the direction constants of the {@link CircleConstants} class)
	 * @param end End of the range (exclusive, one of the direction constants of the {@link CircleConstants} class)
	 * @param layouter Layouter to set for this range or null for the layouter that is already
	 * present at this range (i. e. the default {@link HorizontalLayouter}).
	 * @param mappedDir Direction to use for the specified range
	 */
	public void setLayouter(int begin, int end, TagLayouter layouter, int mappedDir)
	{
		if (end < begin)
		{
			setLayouter(begin, NUMBER_OF_LAYOUTERS_PER_CIRCLE, layouter, mappedDir);
			setLayouter(0, end, layouter, mappedDir);
		}
		else
		{
			for (int i = begin; i < end; i++)
			{
				setLayouter(i, layouter, mappedDir);
			}
		}
	}

	/**
	 * Sets the surrogate layouter for a given direction.
	 *
	 * @param dir Direction, one of the direction constants of the {@link CircleConstants} class
	 * @param layouter Layouter to set for this range or null for the layouter that is already
	 * present at this range (i. e. the default {@link HorizontalLayouter}).
	 * @param mappedDir Direction to use for the specified layouter
	 */
	public void setLayouter(int dir, TagLayouter layouter, int mappedDir)
	{
		if (layouter != null)
		{
			subLayouters [dir] = layouter;
		}
		directionMapper [dir] = mappedDir;
	}

	//////////////////////////////////////////////////
	// @@ AbstractLayouter overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#setupLayouter()
	 */
	public void setupLayouter()
	{
		subLayouters = new TagLayouter [NUMBER_OF_LAYOUTERS_PER_CIRCLE];
		directionMapper = new int [NUMBER_OF_LAYOUTERS_PER_CIRCLE];

		TagLayouter l = new HorizontalLayouter(tagFigure);

		for (int i = 0; i < NUMBER_OF_LAYOUTERS_PER_CIRCLE; i++)
		{
			// By default, use a horizontal layouter
			subLayouters [i] = l;

			// No direction remapping: map each direction to itself
			directionMapper [i] = i;
		}
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#applyDirection()
	 */
	public void applyDirection()
	{
		activeLayouter = subLayouters [direction];

		activeLayouter.setDirection(directionMapper [direction]);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#isVerticalLayouter()
	 */
	public boolean isVerticalLayouter()
	{
		// Delegate to the active layouter
		return activeLayouter.isVerticalLayouter();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.TagLayouter#calculateSize()
	 */
	public Rectangle calculateSize()
	{
		// Delegate to the active layouter
		return activeLayouter.calculateSize();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.TagLayouter#performLayout(Rectangle box)
	 */
	public void performLayout(Rectangle box)
	{
		// Delegate to the active layouter
		activeLayouter.performLayout(box);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#setInsets(Insets insets)
	 */
	public void setInsets(Insets insets)
	{
		for (int i = 0; i < NUMBER_OF_LAYOUTERS_PER_CIRCLE; i++)
		{
			subLayouters [i].setInsets(insets);
		}
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.layouter.AbstractTagLayouter#getInsets()
	 */
	public Insets getInsets()
	{
		// Delegate to the active layouter
		return activeLayouter.getInsets();
	}

	//////////////////////////////////////////////////
	// @@ Static generators
	//////////////////////////////////////////////////

	public static MultiplexLayouter getStraightInstance(AbstractTagFigure tag)
	{
		MultiplexLayouter result = new MultiplexLayouter(tag);

		// Default horizontal layouter
		result.setLayouter(CircleConstants.EIGHTH_NE, null, CircleConstants.EIGHTH_E);
		result.setLayouter(CircleConstants.EIGHTH_E, null, CircleConstants.EIGHTH_E);
		result.setLayouter(CircleConstants.EIGHTH_SW, null, CircleConstants.EIGHTH_W);
		result.setLayouter(CircleConstants.EIGHTH_W, null, CircleConstants.EIGHTH_W);

		TagLayouter vertical = new VerticalLayouter(tag);

		result.setLayouter(CircleConstants.EIGHTH_SE, vertical, CircleConstants.EIGHTH_S);
		result.setLayouter(CircleConstants.EIGHTH_S, vertical, CircleConstants.EIGHTH_S);
		result.setLayouter(CircleConstants.EIGHTH_NW, vertical, CircleConstants.EIGHTH_N);
		result.setLayouter(CircleConstants.EIGHTH_N, vertical, CircleConstants.EIGHTH_N);

		return result;
	}

	/**
	 * The left-right multiplex layouter will layout the tag contents horizontally (from left to right).
	 *
	 * @param tag Tag to layout
	 * @return A new layouter instance for the given tag
	 */
	public static MultiplexLayouter getLeftRightInstance(AbstractTagFigure tag)
	{
		MultiplexLayouter result = new MultiplexLayouter(tag);

		// By specifying null, we use the default horizontal layouter instance of the multiplex layouter

		// Right half of the circle: Layout to east
		result.setLayouter(CircleConstants.EIGHTH_N, CircleConstants.EIGHTH_S, null, CircleConstants.EIGHTH_E);

		// Left half of the circle: Layout to west
		result.setLayouter(CircleConstants.EIGHTH_S, CircleConstants.EIGHTH_N, null, CircleConstants.EIGHTH_W);

		return result;
	}

	/**
	 * The left-right multiplex layouter will layout the tag contents vertically (from top to bottom).
	 *
	 * @param tag Tag to layout
	 * @return A new layouter instance for the given tag
	 */
	public static MultiplexLayouter getUpDownInstance(AbstractTagFigure tag)
	{
		MultiplexLayouter result = new MultiplexLayouter(tag);

		TagLayouter vertical = new VerticalLayouter(tag);

		// Bottom half of the circle: Layout to south
		result.setLayouter(CircleConstants.EIGHTH_E, CircleConstants.EIGHTH_W, vertical, CircleConstants.EIGHTH_S);

		// Top half of the circle: Layout to north
		result.setLayouter(CircleConstants.EIGHTH_W, CircleConstants.EIGHTH_E, vertical, CircleConstants.EIGHTH_N);

		return result;
	}

	/**
	 * The radial multiplex layouter will layout the tag contents radially according to the tag direction.
	 *
	 * @param tag Tag to layout
	 * @return A new layouter instance for the given tag
	 */
	public static MultiplexLayouter getRadialInstance(AbstractTagFigure tag)
	{
		MultiplexLayouter result = new MultiplexLayouter(tag);

		TagLayouter vertical = new VerticalLayouter(tag);

		result.setLayouter(CircleConstants.EIGHTH_E, null, CircleConstants.EIGHTH_E);
		result.setLayouter(CircleConstants.EIGHTH_NE, vertical, CircleConstants.EIGHTH_N);
		result.setLayouter(CircleConstants.EIGHTH_N, vertical, CircleConstants.EIGHTH_N);
		result.setLayouter(CircleConstants.EIGHTH_NW, null, CircleConstants.EIGHTH_W);
		result.setLayouter(CircleConstants.EIGHTH_W, null, CircleConstants.EIGHTH_W);
		result.setLayouter(CircleConstants.EIGHTH_SW, vertical, CircleConstants.EIGHTH_S);
		result.setLayouter(CircleConstants.EIGHTH_S, vertical, CircleConstants.EIGHTH_S);
		result.setLayouter(CircleConstants.EIGHTH_SE, null, CircleConstants.EIGHTH_E);

		return result;
	}

	/**
	 * The inverse radial multiplex layouter will layout the tag contents radially according to the tag direction, but in the
	 * inverse direction of the layouter returned by {@link #getRadialInstance}.
	 *
	 * @param tag Tag to layout
	 * @return A new layouter instance for the given tag
	 */
	public static MultiplexLayouter getRadialInstance2(AbstractTagFigure tag)
	{
		MultiplexLayouter result = new MultiplexLayouter(tag);

		TagLayouter vertical = new VerticalLayouter(tag);

		result.setLayouter(CircleConstants.EIGHTH_E, vertical, CircleConstants.EIGHTH_N);
		result.setLayouter(CircleConstants.EIGHTH_NE, null, CircleConstants.EIGHTH_E);
		result.setLayouter(CircleConstants.EIGHTH_N, null, CircleConstants.EIGHTH_W);
		result.setLayouter(CircleConstants.EIGHTH_NW, vertical, CircleConstants.EIGHTH_N);
		result.setLayouter(CircleConstants.EIGHTH_W, vertical, CircleConstants.EIGHTH_S);
		result.setLayouter(CircleConstants.EIGHTH_SW, null, CircleConstants.EIGHTH_W);
		result.setLayouter(CircleConstants.EIGHTH_S, null, CircleConstants.EIGHTH_E);
		result.setLayouter(CircleConstants.EIGHTH_SE, vertical, CircleConstants.EIGHTH_S);

		return result;
	}
}
