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
package org.openbp.jaspira.gui.interaction;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RectangularShape;

import javax.swing.JScrollPane;

import org.openbp.swing.SwingUtil;

/**
 * Drop region that takes works on a view component.
 * Takes into account the particularities of JScrollPane wrapping the view.
 *
 * @author Stephan Moritz
 */
public class ViewDropRegion extends BasicDropRegion
{
	/** Clip rectangle */
	protected Rectangle clipRect;

	/**
	 * Convenience constructor.
	 * @copy BasicDropRegion.BasicDropRegion (Object,InteractionClient,RectangularShape,Component)
	 * @nowarn
	 */
	public ViewDropRegion(Object id, InteractionClient parent, RectangularShape shape, Component origin)
	{
		super(id, parent, shape, origin);

		JScrollPane scrollPane = SwingUtil.getScrollPaneAncestor(origin);
		if (scrollPane != null)
		{
			clipRect = SwingUtil.convertBoundsToGlassCoords(scrollPane.getViewport());
		}
	}

	/**
	 * Checks if the given coordinates are within the shapes region.
	 * @param x Position in screen coordinates
	 * @param y Position in screen coordinates
	 */
	public boolean reactsOn(int x, int y)
	{
		if (clipRect != null && !clipRect.contains(x, y))
			return false;
		return super.reactsOn(x, y);
	}

	/**
	 * Draws the region with the given attributes.
	 * @param g Graphics context
	 */
	public void draw(Graphics2D g)
	{
		Shape oldClip = g.getClip();
		if (clipRect != null)
		{
			g.setClip(clipRect);
		}

		super.draw(g);

		g.setClip(oldClip);
	}
}
