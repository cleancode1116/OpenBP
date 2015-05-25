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
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * VFlowLayout is similar to FlowLayout except it lays out components
 * vertically. Extends FlowLayout because it mimics much of the
 * behavior of the FlowLayout class, except vertically. An additional
 * feature is that you can specify a fill to edge flag, which causes
 * the VerticalFlowLayout manager to resize all components to expand to the
 * column width Warning: This causes problems when the main panel
 * has less space that it needs and it seems to prohibit multi-column
 * output. Additionally there is a vertical fill flag, which fills the last
 * component to the remaining height of the container.
 *
 * @author Heiko Erhardt
 */
public class VerticalFlowLayout extends FlowLayout
	implements java.io.Serializable
{
	public static final int TOP = 0;

	public static final int MIDDLE = 1;

	public static final int BOTTOM = 2;

	//int align;
	int hgap;

	int vgap;

	boolean horFill;

	boolean vertFill;

	/**
	 * Construct a new VerticalFlowLayout with a middle alignemnt, and
	 * the fill to edge flag set.
	 */
	public VerticalFlowLayout()
	{
		this(TOP, 5, 5, true, false);
	}

	/**
	 * Construct a new VerticalFlowLayout with a middle alignemnt.
	 * @param horFill Horizontal fill to edge flag
	 * @param vertFill Vertical fill to edge flag
	 */
	public VerticalFlowLayout(boolean horFill, boolean vertFill)
	{
		this(TOP, 5, 5, horFill, vertFill);
	}

	/**
	 * Construct a new VerticalFlowLayout with a middle alignemnt.
	 * @param align the alignment value
	 */
	public VerticalFlowLayout(int align)
	{
		this(align, 5, 5, true, false);
	}

	/**
	 * Construct a new VerticalFlowLayout.
	 * @param align the alignment value
	 * @param horFill Horizontal fill to edge flag
	 * @param vertFill Vertical fill to edge flag
	 */
	public VerticalFlowLayout(int align, boolean horFill, boolean vertFill)
	{
		this(align, 5, 5, horFill, vertFill);
	}

	/**
	 * Construct a new VerticalFlowLayout.
	 * @param align the alignment value
	 * @param hgap the horizontal gap variable
	 * @param vgap the vertical gap variable
	 * @param horFill Horizontal fill to edge flag
	 * @param vertFill Vertical fill to edge flag
	 */
	public VerticalFlowLayout(int align, int hgap, int vgap, boolean horFill, boolean vertFill)
	{
		setAlignment(align);
		this.hgap = hgap;
		this.vgap = vgap;
		this.horFill = horFill;
		this.vertFill = vertFill;
	}

	/**
	 * Gets the horizontal gap between components.
	 * @nowarn
	 */
	public int getHgap()
	{
		return hgap;
	}

	/**
	 * Sets the horizontal gap between components.
	 * @nowarn
	 */
	public void setHgap(int hgap)
	{
		super.setHgap(hgap);
		this.hgap = hgap;
	}

	/**
	 * Gets the vertical gap between components.
	 * @nowarn
	 */
	public int getVgap()
	{
		return vgap;
	}

	/**
	 * Sets the vertical gap between components.
	 * @nowarn
	 */
	public void setVgap(int vgap)
	{
		super.setVgap(vgap);
		this.vgap = vgap;
	}

	/**
	 * Returns the preferred dimensions given the components
	 * in the target container.
	 * @param target the component to lay out
	 * @return The computed size
	 */
	public Dimension preferredLayoutSize(Container target)
	{
		Dimension tarsiz = new Dimension(0, 0);

		for (int i = 0; i < target.getComponentCount(); i++)
		{
			Component m = target.getComponent(i);
			if (m.isVisible())
			{
				Dimension d = m.getPreferredSize();
				tarsiz.width = Math.max(tarsiz.width, d.width);
				if (i > 0)
				{
					tarsiz.height += vgap;
				}
				tarsiz.height += d.height;
			}
		}
		Insets insets = target.getInsets();
		tarsiz.width += insets.left + insets.right + hgap * 2;
		tarsiz.height += insets.top + insets.bottom + vgap * 2;
		return tarsiz;
	}

	/**
	 * Returns the minimum size needed to layout the target container
	 * @param target the component to lay out
	 * @return The computed size
	 */
	public Dimension minimumLayoutSize(Container target)
	{
		Dimension tarsiz = new Dimension(0, 0);

		for (int i = 0; i < target.getComponentCount(); i++)
		{
			Component m = target.getComponent(i);
			if (m.isVisible())
			{
				Dimension d = m.getMinimumSize();
				tarsiz.width = Math.max(tarsiz.width, d.width);
				if (i > 0)
				{
					tarsiz.height += vgap;
				}
				tarsiz.height += d.height;
			}
		}
		Insets insets = target.getInsets();
		tarsiz.width += insets.left + insets.right + hgap * 2;
		tarsiz.height += insets.top + insets.bottom + vgap * 2;
		return tarsiz;
	}

	public void setVerticalFill(boolean vertFill)
	{
		this.vertFill = vertFill;
	}

	public boolean getVerticalFill()
	{
		return vertFill;
	}

	public void setHorizontalFill(boolean horFill)
	{
		this.horFill = horFill;
	}

	public boolean getHorizontalFill()
	{
		return horFill;
	}

	/**
	 * Aligns the components defined by first to last within the target
	 * container using the bounds box defined
	 * @param target the container
	 * @param x the x coordinate of the area
	 * @param y the y coordinate of the area
	 * @param width the width of the area
	 * @param height the height of the area
	 * @param first the first component of the container to align
	 * @param last the last component of the container to align
	 */
	private void alignComponents(Container target, int x, int y, int width, int height, int first, int last)
	{
		int align = getAlignment();

		if (align == MIDDLE)
			y += height / 2;
		if (align == BOTTOM)
			y += height;

		for (int i = first; i < last; i++)
		{
			Component m = target.getComponent(i);
			Dimension md = m.getSize();
			if (m.isVisible())
			{
				int px = x + (width - md.width) / 2;
				m.setLocation(px, y);
				y += vgap + md.height;
			}
		}
	}

	/**
	 * Lays out the container.
	 * @param target the container to lay out
	 */
	public void layoutContainer(Container target)
	{
		Insets insets = target.getInsets();
		int maxheight = target.getSize().height - (insets.top + insets.bottom + vgap * 2);
		int maxwidth = target.getSize().width - (insets.left + insets.right + hgap * 2);
		int numcomp = target.getComponentCount();
		int x = insets.left + hgap;
		int y = 0;
		int colw = 0, start = 0;

		for (int i = 0; i < numcomp; i++)
		{
			Component m = target.getComponent(i);
			if (m.isVisible())
			{
				Dimension d = m.getPreferredSize();

				// fit last component to remaining height
				if ((this.vertFill) && (i == (numcomp - 1)))
				{
					d.height = Math.max((maxheight - y), m.getPreferredSize().height);
				}

				// fit componenent size to container width
				if (this.horFill)
				{
					m.setSize(maxwidth, d.height);
					d.width = maxwidth;
				}
				else
				{
					m.setSize(d.width, d.height);
				}

				if (y + d.height > maxheight)
				{
					alignComponents(target, x, insets.top + vgap, colw, maxheight - y, start, i);
					y = d.height;
					x += hgap + colw;
					colw = d.width;
					start = i;
				}
				else
				{
					if (y > 0)
						y += vgap;
					y += d.height;
					colw = Math.max(colw, d.width);
				}
			}
		}
		alignComponents(target, x, insets.top + vgap, colw, maxheight - y, start, numcomp);
	}
}
