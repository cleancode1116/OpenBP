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
package org.openbp.cockpit.modeler.drawing;

import java.awt.Color;
import java.util.List;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.ChildFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.decoration.FilteredDecorator;
import org.openbp.jaspira.plugin.Plugin;

import CH.ifa.draw.framework.Figure;

/**
 * Decorators for selection elements. This is simply a collection of decorators
 * for frame and fill color as well as frame stroke.
 *
 * @author Stephan Moritz
 */
public class SelectionDecorator
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Drawing view this decorator is associated with */
	private WorkspaceDrawingView view;

	/** Owner of the decorator */
	private Plugin provider;

	/** Frame decorator */
	private FrameDecorator frameDecorator;

	/** Stroke decorator */
	private StrokeDecorator strokeDecorator;

	/** Fill decorator */
	private FillDecorator fillDecorator;

	/** Overlay decorator */
	private OverlayDecorator overlayDecorator;

	/** Content state decorator */
	private ContentStateDecorator contentStateDecorator;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param view View to decorate
	 * @param provider Owner of the decorator
	 */
	public SelectionDecorator(WorkspaceDrawingView view, Plugin provider)
	{
		this.view = view;
		this.provider = provider;

		frameDecorator = new FrameDecorator();
		strokeDecorator = new StrokeDecorator();
		fillDecorator = new FillDecorator();
		overlayDecorator = new OverlayDecorator();
		contentStateDecorator = new ContentStateDecorator();

		DecorationMgr.addDecorator(provider, XFigure.DECO_FRAMESTROKE, strokeDecorator);
		DecorationMgr.addDecorator(provider, XFigure.DECO_FRAMECOLOR, frameDecorator);
		DecorationMgr.addDecorator(provider, XFigure.DECO_FILLCOLOR, fillDecorator);
		DecorationMgr.addDecorator(provider, XFigure.DECO_OVERLAY, overlayDecorator);
		DecorationMgr.addDecorator(provider, AbstractTagFigure.DECO_TAGCONTENTTYPE, contentStateDecorator);
	}

	/**
	 * Unregisters the decorators with the decoration Manager.
	 */
	public void uninstall()
	{
		DecorationMgr.removeDecorator(provider, XFigure.DECO_FRAMESTROKE, strokeDecorator);
		DecorationMgr.removeDecorator(provider, XFigure.DECO_FRAMECOLOR, frameDecorator);
		DecorationMgr.removeDecorator(provider, XFigure.DECO_FILLCOLOR, fillDecorator);
		DecorationMgr.removeDecorator(provider, XFigure.DECO_OVERLAY, overlayDecorator);
		DecorationMgr.removeDecorator(provider, AbstractTagFigure.DECO_TAGCONTENTTYPE, contentStateDecorator);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Checks if the owner is a selectable figure.
	 * @nowarn
	 */
	boolean isSelectable(Object o)
	{
		return o instanceof ProcessElementContainer;
	}

	/**
	 * Checks if the argument is the figure currently under the cursor.
	 * @nowarn
	 */
	boolean isHovered(Object o)
	{
		return o == view.getFigureUnderCursor();
	}

	/**
	 * Checks if the argument is part of the current selection.
	 * @nowarn
	 */
	boolean isSelected(Object o)
	{
		return view.isFigureSelected((Figure) o);
	}

	/**
	 * Checks if either the argument or one of its parents is part of the current selection.
	 * @nowarn
	 */
	boolean isAncestorSelected(Object o)
	{
		if (isSelected(o))
			return true;

		while (o instanceof ChildFigure)
		{
			o = ((ChildFigure) o).getParent();

			if (view.isFigureSelected((Figure) o))
				return true;
		}

		return false;
	}

	/**
	 * Checks if either the argument or one of its parents is part of the current selection.
	 * @nowarn
	 */
	boolean isSocketRelativeSelected(SocketFigure socketFigure)
	{
		if (view.isFigureSelected(socketFigure))
			return true;

		if (view.isFigureSelected(socketFigure.getParent()))
			return true;

		int n = socketFigure.getNumberOfContents();
		for (int i = 0; i < n; ++i)
		{
			Figure c = socketFigure.getContentFigureAt(i);
			if (view.isFigureSelected(c))
				return true;
		}

		return false;
	}

	/**
	 * Checks if there is an expression assigned to this figure.
	 * This applies to node parameter figures only.
	 * @nowarn
	 */
	boolean hasExpression(Object o)
	{
		if (o instanceof ParamFigure)
		{
			ParamFigure paramFigure = (ParamFigure) o;
			NodeParam param = paramFigure.getNodeParam();
			String expression = param.getExpression();

			// We consider the object to have an expression if the expression contains something of interest (i. e. a non-stanard value)
			if (expression != null && !expression.equals("null") && !expression.equals("\"\"") && !expression.equals("false") && !expression.equals("0"))
			{
				return true;
			}
		}
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Decorator classes
	//////////////////////////////////////////////////

	/**
	 * Decorater for the tag content state.
	 * When a socket or a node is selected, we make sure that the flow connector is visible.
	 */
	public class ContentStateDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			Integer stateObject = (Integer) value;
			int state = stateObject.intValue();
			if ((state & AbstractTagFigure.CONTENT_FLOW) == 0)
			{
				// The flow access figure is currently excluded from the socket content.
				// We have to include it if the socket or some connecting figure is selected.

				// We are decorating if the socket or its node is selected or a drop target.
				boolean decorate = false;

				SocketFigure socketFigure = null;

				if (owner instanceof SocketFigure)
				{
					socketFigure = (SocketFigure) owner;
					if ((socketFigure.getVisualStatus() & VisualElement.VISUAL_DND_PARTICIPANT) != 0)
					{
						decorate = true;
					}
					else
					{
						// No drop target, try selection of the socket or its node
						decorate = isSocketRelativeSelected(socketFigure);
						if (!decorate)
						{
							// Not selected, check if control link pointing to that node is selected
							if (socketFigure != null)
							{
								List connections = socketFigure.getFlowConnections();
								if (connections != null)
								{
									int n = connections.size();
									for (int i = 0; i < n; ++i)
									{
										FlowConnection connection = (FlowConnection) connections.get(i);
										if (isSelected(connection))
										{
											decorate = true;
											break;
										}
									}
								}
							}
						}
					}
				}

				if (decorate)
				{
					// Display the content object
					return Integer.valueOf(state | AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_TEXT);
				}
			}

			// No change
			return value;
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			// We are interested in socket figures only
			return owner instanceof SocketFigure || owner instanceof ParamFigure;
		}
	}

	/**
	 * Decorater for a frame. A frame is decorated if itself is selected.
	 */
	public class FrameDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			if (isSelected(owner))
				return ModelerColors.SELECTED_BORDER;
			return ModelerColors.HOVERED_BORDER;
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			return isSelectable(owner) && (isHovered(owner) || isSelected(owner));
		}
	}

	/**
	 * Decorater for a stroke. The stroke is decorated if itself is selected.
	 */
	public class StrokeDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			if (isSelected(owner))
				return ModelerGraphics.selectionStroke;
			return ModelerGraphics.hoveredStroke;
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			return isSelectable(owner) && (isHovered(owner) || isSelected(owner));
		}
	}

	/**
	 * Decorater for a fill. The fill is decorated if itself is selected.
	 */
	public class FillDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			// brighter() on transparent results in black... bad
			return value != null ? ((Color) value).brighter() : null;
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			return isSelectable(owner) && isAncestorSelected(owner);
		}
	}

	/**
	 * Decorater for an overlay. The overlay is decorated if itself is selected.
	 */
	public class OverlayDecorator extends FilteredDecorator
	{
		/**
		 * @see FilteredDecorator#doDecorate(Object, String, Object)
		 */
		public Object doDecorate(Object owner, String key, Object value)
		{
			return FigureResources.getExpressionOverlay(owner);
		}

		/***
		 * @see FilteredDecorator#qualifies(Object)
		 */
		public boolean qualifies(Object owner)
		{
			return hasExpression(owner);
		}
	}
}
