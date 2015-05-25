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
package org.openbp.cockpit.modeler.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.figures.generic.BasicFigure;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.decoration.Decorator;
import org.openbp.jaspira.decoration.FilteredDecorator;
import org.openbp.jaspira.gui.interaction.DragDropPane;

import CH.ifa.draw.standard.AbstractFigure;

/**
 * Tool that allows reordering of parameters on sockets.
 *
 * @author Stephan Pauxberger
 */
public class ParamReorderTracker extends ModelerTool
{
	/** The parameter to move */
	private ParamFigure paramFigure;

	/** The socket figure on which we want to move the param */
	private SocketFigure socketFigure;

	/** Old index of the param to move */
	private int oldIndex;

	/** Contains the regions of this socket in question */
	private Rectangle [] regions;

	/** Decorator used to paint the socket in question */
	private Decorator targetDecorator = new SingleFigureDecorator();

	/** Chaches the total size of the socket */
	private Rectangle socketDisplayBox;

	/** Cursor for acceptance */
	private Cursor accept;

	/** Cursor for rejection */
	private Cursor reject;

	public ParamReorderTracker(ModelerToolSupport toolSupport)
	{
		super(toolSupport);

		MultiIcon dragImage = ItemIconMgr.getMultiIcon(ItemIconMgr.getInstance().getIcon(ModelObjectSymbolNames.TYPE_ITEM, FlexibleSize.SMALL));
		accept = DragDropPane.acceptCursorPrototype.createCursor(dragImage);
		reject = DragDropPane.rejectCursorPrototype.createCursor(dragImage);
	}

	public void setAffectedObject(Object affectedObject)
	{
		super.setAffectedObject(affectedObject);

		if (affectedObject != null)
		{
			this.paramFigure = (ParamFigure) affectedObject;
			this.socketFigure = (SocketFigure) paramFigure.getParent();

			// Rebuild the parameter list, displaying all parameters
			socketFigure.reinitParams(true);

			socketDisplayBox = socketFigure.displayBox();

			// We retrieve the old index of the parameter.
			oldIndex = socketFigure.getNodeSocket().getParamList().indexOf(paramFigure.getNodeParam());

			regions = socketFigure.getParamRegions();
		}
		else
		{
			socketFigure = null;
			regions = null;
		}
	}

	public void activate()
	{
		super.activate();
		getEditor().startUndo("Reorder Parameters");

		DecorationMgr.addDecorator(getEditor(), BasicFigure.DECO_OVERLAY, targetDecorator);

		getView().redraw();
	}

	public void deactivate()
	{
		DecorationMgr.removeDecorator(getEditor(), BasicFigure.DECO_OVERLAY, targetDecorator);

		paramFigure = null;
		socketFigure = null;
		oldIndex = 0;
		regions = null;
		socketDisplayBox = null;

		super.deactivate();
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
		Cursor cursor;
		if (!socketDisplayBox.contains(x, y) || regions [oldIndex].contains(x, y) || (oldIndex < regions.length - 1 && regions [oldIndex + 1].contains(x, y)))
		{
			cursor = reject;
		}
		else
		{
			cursor = accept;
		}
		getView().setCursor(cursor);
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		if (socketDisplayBox.contains(x, y))
		{
			// Strategy: We determine the region over which we are right now
			for (int i = 0; i < regions.length; i++)
			{
				if (regions [i].contains(x, y))
				{
					socketFigure.moveParameter(oldIndex, i);

					getView().setCursor(null);

					break;
				}
			}
		}

		// Rebuild the parmeter list, displaying the visible parameters only
		socketFigure.reinitParams(false);

		getView().singleSelect(paramFigure);

		getEditor().endUndo();

		super.mouseUp(e, x, y);
	}

	//////////////////////////////////////////////////
	// @@ Decorator
	//////////////////////////////////////////////////

	/** Overlay used to show possible drop regions for the parameter */
	private TargetFigure socketOverlayFigure = new TargetFigure();

	/**
	 * Overlays the target figure with the ParamReorder.TargetFigure figure below.
	 */
	public class SingleFigureDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			return socketOverlayFigure;
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			return owner == socketFigure;
		}
	}

	/**
	 * Simple figure that displays possible drop regions for parameter reordering.
	 */
	public class TargetFigure extends AbstractFigure
	{
		/**
		 * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(Point origin, Point corner)
		 */
		public void basicDisplayBox(Point origin, Point corner)
		{
			// do nothing
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractFigure#basicMoveBy(int dx, int dy)
		 */
		protected void basicMoveBy(int dx, int dy)
		{
			// For display only, cannot be moved
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractFigure#displayBox()
		 */
		public Rectangle displayBox()
		{
			// Equals the size of the socket
			return socketDisplayBox;
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractFigure#draw(Graphics g)
		 */
		public void draw(Graphics g)
		{
			for (int i = 0; i < regions.length; i++)
			{
				if (i == oldIndex || i == oldIndex + 1)
				{
					continue;
				}

				g.setColor(ModelerColors.CONNECTION_ACCEPT);
				g.fillRoundRect(regions [i].x, regions [i].y, regions [i].width, regions [i].height, 8, 8);

				g.setColor(Color.BLACK);
				g.drawRoundRect(regions [i].x, regions [i].y, regions [i].width, regions [i].height, 8, 8);
			}
		}

		/**
		 * We do not want any handles, so we return an empty vector.
		 * @see CH.ifa.draw.standard.AbstractFigure#handles()
		 */
		public Vector handles()
		{
			return null;
		}
	}
}
