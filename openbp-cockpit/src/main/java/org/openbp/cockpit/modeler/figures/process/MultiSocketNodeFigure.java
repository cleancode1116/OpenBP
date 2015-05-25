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
package org.openbp.cockpit.modeler.figures.process;

import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.generic.SimpleImageFigure;
import org.openbp.cockpit.modeler.skins.SymbolDescriptor;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessUtil;
import org.openbp.core.model.item.process.SubprocessNode;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * A Node Figure is the graphical representation of a multi socket node.
 *
 * @author Stephan Moritz
 */
public class MultiSocketNodeFigure extends NodeFigure
	implements Colorizable
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public MultiSocketNodeFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Initializes the icons of the figure.
	 */
	protected void initIcon()
	{
		super.initIcon();

		initOverlayIcon();
	}

	/**
	 * Initializes the icon in the center of the node.
	 */
	protected Icon determineCenterIcon()
	{
		Icon icon = null;

		// Try the underlying item first
		Item item = null;
		if (node instanceof SubprocessNode)
		{
			item = ((SubprocessNode) node).getSubprocess();
		}

		String skinName = getDrawing().getProcessSkin().getName();

		if (item != null)
		{
			// Get its icon
			icon = ItemIconMgr.getInstance().getIcon(skinName, item, FlexibleSize.HUGE);
		}

		// Try the default for this object
		if (icon == null && node.getModelObjectSymbolName() != null)
		{
			icon = ItemIconMgr.getInstance().getIcon(skinName, node.getModelObjectSymbolName(), FlexibleSize.HUGE);
		}

		return icon;
	}

	/**
	 * Initializes the overlay icon in the lower right corner of the node.
	 */
	protected void initOverlayIcon()
	{
		// Check if there is a default overlay figure in the figure descriptor
		ImageIcon overlayImage = ((SymbolDescriptor) presentationFigure.getDescriptor()).getOverlayIcon();

		SimpleImageFigure overlayFigure = null;
		if (overlayImage != null)
		{
			overlayFigure = new SimpleImageFigure(overlayImage);
		}
		setIconOverlayFigure(overlayFigure);
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.NodeFigure#initTextFigure(String textPosition)
	 */
	protected void initTextFigure(String textPosition)
	{
		MultiSocketNode msNode = (MultiSocketNode) node;
		String customImagePath = msNode.getImagePath();
		if (customImagePath != null)
		{
			// Position the text to the east if it is in the center and we have a custom icon
			if (textPosition == null || textPosition.equals("c"))
				textPosition = "e";
		}

		super.initTextFigure(textPosition);
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Removes the socket from this node.
	 *
	 * @param socketFigure Socket to remove
	 */
	public void removeSocket(SocketFigure socketFigure)
	{
		willChange();

		((MultiSocketNode) node).removeSocket(socketFigure.getNodeSocket());

		remove(socketFigure);

		changed();
	}

	//////////////////////////////////////////////////
	// @@ Colorizable implementation
	//////////////////////////////////////////////////

	/**
	 * Returns the default color of the underlying activity node.
	 * @see org.openbp.cockpit.modeler.figures.generic.Colorizable#getDefaultFillColor()
	 */
	public Color getDefaultFillColor()
	{
		return presentationFigure.getDefaultFillColor();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		List ret = super.getDropRegions(flavors, data, mouseEvent);
		if (ret != null)
			return ret;

		String regionId = getDropRegionIdForFlavor(flavors, data, mouseEvent);
		if (regionId != null)
			return Collections.singletonList(createNodeDropRegion(regionId, flavors));

		return null;
	}

	/**
	 * Determines if none of the flavors in the list is accepted by DnD actions.
	 *
	 * @param flavors Flavors to check
	 * @param data Transferred object
	 * @param mouseEvent Mouse event of the DnD drop action
	 * @return The region id that should be used for the accepted flavor or null if the flavor is not accepted
	 */
	protected String getDropRegionIdForFlavor(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		boolean accept = false;

		String regionId = node.getQualifier().toString();

		if (flavors.contains(ModelerFlavors.COLOR))
		{
			accept = true;

			// If the CTRL key is pressed, add "-All" to the region id (for CTRL+drop color)
			if (mouseEvent != null && mouseEvent.isControlDown())
			{
				regionId += "-All";
			}
		}
		else if (flavors.contains(ClientFlavors.NODE_SOCKET))
		{
			accept = true;
		}

		if (accept)
			return regionId;

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (super.importData(regionId, data, p))
			return true;

		p = SwingUtil.convertFromGlassCoords(p, getDrawing().getView());

		MultiSocketNode msNode = (MultiSocketNode) node;

		try
		{
			if (data.isDataFlavorSupported(ClientFlavors.NODE_SOCKET))
			{
				getDrawing().getEditor().startUndo("Add Socket");

				NodeSocket other = (NodeSocket) data.getTransferData(ClientFlavors.NODE_SOCKET);

				String name = other.isEntrySocket() ? CoreConstants.SOCKET_IN : CoreConstants.SOCKET_OUT;
				NodeSocket toAdd = msNode.createSocket(name);

				// Make sure there is a workflow task parameter
				if (msNode instanceof WorkflowNode)
				{
					ProcessUtil.ensureWorkflowTaskParameter(toAdd);
				}

				toAdd.setEntrySocket(other.isEntrySocket());
				boolean hasDefault = msNode.getDefaultSocket(other.isEntrySocket()) != null;
				if (! hasDefault)
					toAdd.setDefaultSocket(true);

				// add the socket to the node
				msNode.addSocket(toAdd);

				// Add a new socketFigure to the node.
				SocketFigure socketFigure = this.addSocket(toAdd);

				// Set the angle according to the drop position.
				socketFigure.setAngle(Math.atan2(p.getY() - center().getY(), p.getX() - center().getX()));

				getDrawing().getEditor().endUndo();

				((WorkspaceDrawingView) getDrawing().getEditor().view()).singleSelect(socketFigure);

				// Will invoke the autoconnector
				getDrawing().getEditor().fireEvent("modeler.drawing.socketadded", socketFigure);

				return true;
			}
			else if (data.isDataFlavorSupported(ModelerFlavors.COLOR))
			{
				Color color = (Color) data.getTransferData(ModelerFlavors.COLOR);

				getDrawing().getEditor().startUndo("Set Color");

				setFillColor(color);

				if (regionId instanceof String && ((String) regionId).endsWith("-All"))
				{
					// CTRL+drop color means colorize the sockets, too
					for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
					{
						Figure f = fe.nextFigure();
						if (f instanceof SocketFigure)
						{
							SocketFigure socketFigure = (SocketFigure) f;

							socketFigure.setFillColor(color);
							socketFigure.invalidate();
						}
					}
				}

				invalidate();
				getDrawing().getEditor().endUndo();
				getDrawing().getEditor().focusPlugin();

				return true;
			}
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

		return false;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		WorkspaceDrawingView view = getDrawing().getView();

		Point docPoint = SwingUtil.convertFromGlassCoords(p, view);
		if (presentationFigure.containsPoint(docPoint.x, docPoint.y))
			return Collections.singletonList(new Importer(node.getQualifier(), this, new DataFlavor[]
			{
				ClientFlavors.NODE_SOCKET, ModelerFlavors.COLOR
			}));

		return null;
	}
}
