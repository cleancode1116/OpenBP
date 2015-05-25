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
package org.openbp.cockpit.modeler.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.TitleFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.skins.SkinMgr;
import org.openbp.cockpit.modeler.skins.SymbolDescriptor;
import org.openbp.common.CommonUtil;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeProvider;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessTypes;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;

/**
 * Figure utility methods.
 *
 * @author Heiko Erhardt
 */
public class FigureUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private FigureUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Position-related finder methods
	//////////////////////////////////////////////////

	/**
	 * Finds the figure that contains the given point within the given figure.
	 * If the given figure is composite figure, the method will return the direct or indirect
	 * child figure that contains the point and is the deepest figure in the figure hierarchy.
	 *
	 * @param figure Parent figure
	 * @param x X coordinate of the point
	 * @param y Y coordinate of the point
	 * @param figureClass Figure class if a child figure of a particular type should be retrieved
	 * or null for any type of child figure
	 * @return The figure or null if the point does not lie within the figure or its sub figures
	 */
	public static Figure getFigureByPoint(Figure figure, int x, int y, Class figureClass)
	{
		Figure child = getChildFigureByPoint(figure, x, y, figureClass);
		if (child != null)
		{
			// There is a direct child figure that contains the point, recurse down
			Figure cc = getFigureByPoint(child, x, y, figureClass);
			if (cc != null)
				return cc;
		}

		if (figure.containsPoint(x, y))
		{
			// The point hits this figure
			return figure;
		}

		// Point not within figure
		return null;
	}

	/**
	 * Finds a (direct) child figure within the given figure.
	 *
	 * @param figure Parent figure
	 * @param x X coordinate of the point
	 * @param y Y coordinate of the point
	 * @param figureClass Figure class if a child figure of a particular type should be retrieved
	 * or null for any type of child figure
	 * @return The child figure if the given figure is a composite figure and there is a child figure
	 * that contains the given point or null if no such child figure exists
	 */
	public static Figure getChildFigureByPoint(Figure figure, int x, int y, Class figureClass)
	{
		if (figure instanceof CompositeFigure)
		{
			for (FigureEnumeration fe = ((CompositeFigure) figure).figures(); fe.hasMoreElements();)
			{
				Figure child = fe.nextFigure();

				if (figureClass == null || figureClass.isInstance(child))
				{
					if (child.containsPoint(x, y))
					{
						return child;
					}
				}
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Creates a list of figures that match the given type.
	 *
	 * @param parentFigure Parent figure to enumerate
	 * @param figureClass Class of the figures to return
	 * @return The list or null if no appropiate figures have been found
	 */
	public static List getTypedFigureList(Figure parentFigure, Class figureClass)
	{
		List result = null;

		for (FigureEnumeration fe = parentFigure.figures(); fe.hasMoreElements();)
		{
			Object o = fe.nextElement();
			if (figureClass.isInstance(o))
			{
				// Found a matching element
				if (result == null)
					result = new ArrayList();
				result.add(o);
			}
		}

		return result;
	}

	/**
	 * Creates a shadow figure by cloning the given presentation figure.
	 *
	 * @param presentationFigure Presentation figure to clone
	 * @return The shadow figure<br>
	 * The fill and frame color attributes will be adjusted accordingly.
	 * The 'IsShadow' attribute will be set to true.
	 * If the figure is a child figure, it won't have a parent figure assigned.
	 */
	public static XFigure createShadowFigure(XFigure presentationFigure)
	{
		if (presentationFigure.getFillColor() == null && presentationFigure.getDefaultFillColor() == null)
		{
			// Figures that do not have a fill (non-solid figures) color may not have a shadow
			// or else the shadow would be visible within the figure.
			return null;
		}

		XFigure shadow = (XFigure) presentationFigure.clone();

		shadow.setParent(null);
		shadow.setFillColor(ModelerColors.SHADOW_FILL);
		shadow.setFillColor2(ModelerColors.SHADOW_FILL);
		shadow.setFrameColor(ModelerColors.SHADOW_BORDER);
		shadow.invalidate();

		return shadow;
	}

	/**
	 * Checks if two figures relate rather vertical or horizontal.
	 *
	 * @param f1 Event
	 * @param f2 Event
	 * @return
	 * true: The vertical distance between the figures is greater than the horizontal one.<br>
	 * false: Otherwise
	 */
	public static boolean isVerticalRelationship(Figure f1, Figure f2)
	{
		int f1X = f1.center().x;
		int f1Y = f1.center().y;
		int f2X = f2.center().x;
		int f2Y = f2.center().y;

		if (Math.abs(f1Y - f2Y) > Math.abs(f1X - f2X))
			return true;

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Text figures
	//////////////////////////////////////////////////

	/**
	 * Synchronizes a title figure with the text of its associated content object.
	 *
	 * @param figure Figure to updateFigure
	 * @return
	 * true: The text of the figure has changed.<br>
	 * false: No text change
	 */
	public static boolean synchronizeText(TitleFigure figure)
	{
		DisplayObject client = figure.getClient();

		if (client == null)
		{
			figure.setText("");
			return false;
		}

		String format = figure.getTitleFormat();
		if (format == null)
		{
			format = "$text";
		}

		StringBuffer sb = new StringBuffer();

		int n = format.length();
		for (int i = 0; i < n;)
		{
			char c = format.charAt(i);

			if (format.startsWith("$name", i))
			{
				sb.append(client.getName());
				i += 5;
			}
			else if (format.startsWith("$text", i))
			{
				String t = null;
				if (DisplayObjectPlugin.getInstance().isTitleModeText())
				{
					if (figure.isVerboseDisplay())
					{
						// Display text or name, whichever is defined
						t = client.getDisplayText();
					}
					else
					{
						// Display text only when explicitely defined
						t = client.getDisplayName();
					}
				}
				else
				{
					// Display object name only in verbose mode
					if (figure.isVerboseDisplay())
					{
						t = client.getName();
					}
				}
				if (t != null)
				{
					sb.append(t);
				}

				i += 5;
			}
			else
			{
				sb.append(c);
				++i;
			}
		}

		String oldText = figure.getText();
		String newText = sb.toString();

		figure.willChange();
		figure.setText(newText);
		figure.changed();

		return CommonUtil.equalsNull(oldText, newText);
	}

	//////////////////////////////////////////////////
	// @@ Images
	//////////////////////////////////////////////////

	/**
	 * Combines two images into a single one by overlaying the base image with the overlay image.
	 * If the specified position would cause the overlay image to appear out of the bounds
	 * of the base image, the position will be adjusted automatically.
	 *
	 * @param base Base image
	 * @param overlay Overlay image
	 * @param x X position of overlay image
	 * @param y Y position of overlay image
	 * @param overlayWidth Width of the overlay image; 0 for its regular size
	 * @param overlayHeight height of the overlay image; 0 for its regular size
	 * @return The new combined image or null on error
	 */
	public static Image combineImages(Image base, Image overlay, int x, int y, int overlayWidth, int overlayHeight)
	{
		Image result = null;

		try
		{
			// Adjust the overlay position
			int baseWidth = base.getWidth(null);
			int baseHeight = base.getHeight(null);

			if (overlayWidth == 0)
				overlayWidth = overlay.getWidth(null);
			if (overlayHeight == 0)
				overlayHeight = overlay.getHeight(null);

			if (x + overlayWidth > baseWidth)
				x = baseWidth - overlayWidth;
			if (x < 0)
				x = 0;
			if (y + overlayHeight > baseHeight)
				y = baseHeight - overlayHeight;
			if (y < 0)
				y = 0;

			// Create an image of the same size as the base image
			result = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_RGB);

			// Get a graphics to draw onto the new image
			Graphics2D g = (Graphics2D) result.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			// Make the image transparent
			g.setBackground(new Color(0xff, 0xff, 0xff, 0xff));
			g.clearRect(0, 0, baseWidth, baseHeight);

			// Draw the base image first into the new image
			g.drawImage(base, 0, 0, null);

			// Draw the overlay image into the new image
			g.drawImage(overlay, x, y, null);

			// g.drawImage (overlay, x, y, overlayWidth, overlayHeight, null);

			g.dispose();
		}
		catch (Exception e)
		{
			// Ignore
		}

		return result;
	}

	/**
	 * Copies an image.
	 *
	 * @param image Image to copy
	 * @return The copied image or null on error
	 */
	public static Image copyImage(Image image)
	{
		Image result = null;

		if (image != null)
		{
			try
			{
				int w = image.getWidth(null);
				int h = image.getHeight(null);

				int [] pixels = new int [w * h];
				PixelGrabber grabber = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);

				grabber.grabPixels();

				if ((grabber.getStatus() & ImageObserver.ABORT) != 0)
				{
					return null;
				}

				MemoryImageSource source = new MemoryImageSource(w, h, pixels, 0, w);
				result = Toolkit.getDefaultToolkit().createImage(source);
			}
			catch (Exception e)
			{
				// Ignore
			}
		}

		return result;
	}

	//////////////////////////////////////////////////
	// @@ Skins
	//////////////////////////////////////////////////

	/**
	 * Gets the skin to use for the given process.
	 *
	 * @param process Process
	 * @return the skin
	 */
	public static Skin determineProcessSkin(ProcessItem process)
	{
		Skin skin = null;

		String skinName = process.getSkinName();
		if (skinName != null)
		{
			// Skin specified
			skin = SkinMgr.getInstance().getSkin(skinName);

			if (skin == null)
			{
				// Skin not found, error message
				// TODO Fix 6: Error msg if process skin not found
			}
		}

		if (skin == null)
		{
			// Use default skin for this process type
			skin = SkinMgr.getInstance().getDefaultSkin();
		}

		return skin;
	}

	/**
	 * Updates the figure according to the currently selected skin.
	 *
	 * @param figure Figure to update
	 */
	public static void updateSkin(Figure figure)
	{
		if (figure instanceof VisualElement)
		{
			((VisualElement) figure).updatePresentationFigure();
		}

		if (figure instanceof CompositeFigure)
		{
			// Recurse down
			for (FigureEnumeration en = ((CompositeFigure) figure).figures(); en.hasMoreElements();)
			{
				Figure subFigure = en.nextFigure();
				updateSkin(subFigure);
			}
		}
	}

	/**
	 * Gets the symbol descriptor from the given skin that can be used to display the given model object.
	 *
	 * @param skin Process skin
	 * @param o Object to display
	 * @return The symbol descriptor
	 */
	public static SymbolDescriptor getSymbolDescriptorForModelObject(Skin skin, ModelObject o)
	{
		SymbolDescriptor desc = null;

		String nodeType = o.getModelObjectSymbolName();
		if (nodeType != null)
		{
			desc = skin.getSymbolDescriptor(nodeType);
		}
		if (desc == null)
		{
			desc = skin.getSymbolDescriptor(ModelObjectSymbolNames.NODE);
		}

		return desc;
	}

	/**
	 * Updates the geometry string of an item according to the number of sockets that are connected to the item.
	 *
	 * @param skin Process skin used for display or null for the default skin
	 * @param item Item to update
	 */
	public static void updateItemGeometry(Skin skin, Item item)
	{
		if (!(item instanceof NodeProvider))
		{
			// N/A
			return;
		}

		double factor = 1d;
		int additionalSockets = 0;

		if (item instanceof ProcessItem)
		{
			ProcessItem process = (ProcessItem) item;
			int size = ModelerGraphics.DEFAULT_NODE_SIZE;

			String processType = process.getProcessType();
			if (ProcessTypes.USECASE.equals(processType) || ProcessTypes.TOPLEVEL.equals(processType))
			{
				// Make use cases and top level processes appear larger
				factor = 2d;
			}

			// Count the number of sockets (i. e. entry and final nodes)
			for (Iterator it = process.getNodes(); it.hasNext();)
			{
				Node node = (Node) it.next();

				if (node instanceof InitialNode || node instanceof FinalNode)
					++additionalSockets;
			}

			// Normal are one entry and one final node, so subtract two sockets
			additionalSockets -= 2;

			// Adjust the size of the node according to the number of sockets
			if (additionalSockets > 0)
			{
				size += ModelerGraphics.DEFAULT_NODE_SIZE * additionalSockets / 2;
			}
			process.setNodeGeometry("size:" + size);
		}
		else if (item instanceof ActivityItem)
		{
			// Adjust the size of the node according to the number of sockets
			ActivityItem action = (ActivityItem) item;

			additionalSockets = action.getNumberOfSockets() - 2;
			if (additionalSockets > 0)
			{
				int size = ModelerGraphics.DEFAULT_NODE_SIZE;
				size += additionalSockets / 2 * size;
			}
		}
		else
		{
			// N/A
			return;
		}

		if (skin == null)
		{
			// Use default skin
			skin = SkinMgr.getInstance().getDefaultSkin();
		}

		// Get the node descriptor for this type of item node
		SymbolDescriptor desc = getSymbolDescriptorForModelObject(skin, item);

		// Adjust default width and height
		int width = desc.getSizeX();
		int height = desc.getSizeY();
		if (additionalSockets > 0)
		{
			factor += additionalSockets / 2;
		}
		width = (int) (width * factor);
		height = (int) (height * factor);

		// Build geometry string and assign to the item
		String geometry = "size:" + width + ":" + height;
		if (item instanceof ProcessItem)
		{
			((ProcessItem) item).setNodeGeometry(geometry);
		}
		else
		{
			((ActivityItem) item).setGeometry(geometry);
		}
	}

	//////////////////////////////////////////////////
	// @@ Geometry data encoding/decoding helpers
	//////////////////////////////////////////////////

	/**
	 * Prints a double value as an integer.
	 * Performs appropriate rounding.
	 * @nowarn
	 */
	public static String printInt(double d)
	{
		return Long.toString(Math.round(d));
	}
}
