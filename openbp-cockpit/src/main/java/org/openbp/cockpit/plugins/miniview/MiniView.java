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
package org.openbp.cockpit.plugins.miniview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;

import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.standard.StandardDrawingView;

/**
 * Overview display for a modeler.
 *
 * @author Jens Ferchland
 */
public class MiniView extends StandardDrawingView
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Mini view scale factor */
	private double scale = 0.2;

	/** The tool of this view */
	private Tool miniViewTool;

	/** Current tracker rectangle */
	private Rectangle trackRect;

	/** Plugin the mini view belongs to */
	private MiniViewPlugin parent;

	/** Modeler that is mapped to this mini view */
	private Modeler modeler;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param modeler Modeler this mini view referes to
	 * @param parent Mini view plugin that holds the view
	 */
	public MiniView(Modeler modeler, MiniViewPlugin parent)
	{
		super(modeler);

		this.modeler = modeler;
		this.parent = parent;

		miniViewTool = new MiniViewZoomTool(this);

		setBackground(ModelerColors.MINIVIEW);
	}

	/**
	 * Unregisters the miniview.
	 */
	public void unregister()
	{
		setEditor(null);

		// setDrawing(null);

		drawing().removeDrawingChangeListener(this);

		parent = null;
		modeler = null;
		miniViewTool.deactivate();
		miniViewTool = null;
	}

	//////////////////////////////////////////////////
	// @@ Painting
	//////////////////////////////////////////////////

	/**
	 * Redraws the view.
	 * Note that this is much faster than calling repaint().
	 */
	public void redraw()
	{
		repaint();
	}

	/**
	 * @see CH.ifa.draw.standard.StandardDrawingView#drawAll(Graphics g)
	 */
	public void drawAll(Graphics g)
	{
		revalidateScale();

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawBackground(g2);

		// Scale and paint content
		g2.scale(scale, scale);

		drawDrawing(g2);
		drawViews(g2);
		drawTrack(g2);

		g2.scale(1 / scale, 1 / scale);
	}

	/**
	 * Draws rectangles of all views of the modeler.
	 * @param g Graphics object to paint on
	 */
	protected void drawViews(Graphics g)
	{
		g.setColor(ModelerColors.MINIVIEW_TRACKER);
		Rectangle r = modeler.getVisibleArea();
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	/**
	 * Paints the tracker rectangle of the miniview.
	 * @param g Graphics object to paint on
	 */
	protected void drawTrack(Graphics g)
	{
		if (trackRect != null)
		{
			g.setColor(Color.GRAY);
			g.drawRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
		}
	}

	/**
	 * @see javax.swing.JComponent#paintImmediately(int, int, int, int)
	 */
	public void repaint(int x, int y, int w, int h)
	{
		super.repaint();
	}

	//////////////////////////////////////////////////
	// @@ Scale
	//////////////////////////////////////////////////

	/**
	 * Revalidates the scale factor of the mini view so that the entire drawing will be displayed.
	 */
	private void revalidateScale()
	{
		revalidate();

		Dimension rectDim = getParent().getParent().getSize();
		rectDim.width /= parent.getNumberOfMiniViews();

		Rectangle drawingRect = ((ProcessDrawing) drawing()).displayBox();
		int drawingWidth = (int) drawingRect.getMaxX();
		int drawingHeight = (int) drawingRect.getMaxY();

		drawingHeight = Math.max(drawingHeight, 500);
		drawingWidth = Math.max(drawingWidth, 500);

		Insets insets = getParent().getParent().getInsets();

		scale = Math.min((double) (rectDim.width - insets.left - insets.right - 5) / (double) drawingWidth, (double) (rectDim.height - insets.top - insets.bottom - 5) / (double) drawingHeight);
	}

	/**
	 * Gets the current scale.
	 * Performs a revalidation of the scale.
	 *
	 * @return The scale
	 */
	public double getScale()
	{
		revalidateScale();
		return scale;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * If this rectangle is set, it will be painted in the overview as gray rectangle.
	 * @param r The rectangle or null
	 */
	public void setTrackRect(Rectangle r)
	{
		trackRect = r;
		redraw();
	}

	/**
	 * Gets the modeler that is mapped to this mini view.
	 * @nowarn
	 */
	public Modeler getModeler()
	{
		return modeler;
	}

	private static final Dimension MIN_DIM = new Dimension(50, 50);

	/**
	 * @see CH.ifa.draw.standard.StandardDrawingView#getMinimumSize()
	 */
	public Dimension getMinimumSize()
	{
		return MIN_DIM;
	}

	/**
	 * @see CH.ifa.draw.standard.StandardDrawingView#tool()
	 */
	public Tool tool()
	{
		return miniViewTool;
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		redraw();
	}
}
