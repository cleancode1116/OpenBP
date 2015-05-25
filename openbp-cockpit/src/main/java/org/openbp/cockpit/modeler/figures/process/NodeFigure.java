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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.generic.Expandable;
import org.openbp.cockpit.modeler.figures.generic.FixedTitleFigure;
import org.openbp.cockpit.modeler.figures.generic.GeometryUtil;
import org.openbp.cockpit.modeler.figures.generic.MoveableTitleFigure;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.ShadowDropper;
import org.openbp.cockpit.modeler.figures.generic.SimpleImageFigure;
import org.openbp.cockpit.modeler.figures.generic.TitleFigure;
import org.openbp.cockpit.modeler.figures.generic.UpdatableFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.figures.tag.SimpleTextTagFigure;
import org.openbp.cockpit.modeler.skins.SymbolDescriptor;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.CollectionUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessUtil;
import org.openbp.core.model.item.process.SingleSocketNode;
import org.openbp.core.model.item.process.WorkflowNode;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.TextHolder;

/**
 * A Node Figure is the graphical representation of a process node.
 *
 * @author Stephan Moritz
 */
public abstract class NodeFigure extends CompositeFigure
	implements FigureChangeListener, ShadowDropper, Expandable, ProcessElementContainer, TextHolder
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag for {@link #changeOrientation}: Rotate clockwise */
	public static final int ROTATE_CW = (1 << 0);

	/** Flag for {@link #changeOrientation}: Rotate counterclockwise */
	public static final int ROTATE_CCW = (1 << 1);

	/** Region id: Data type */
	public static final String REGION_TYPE = "DataTypeRegionId";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Node that this figure represents */
	protected Node node;

	/** Graphical Representation of the node itself (e\.g\. a circle) */
	protected XFigure presentationFigure;

	/** Icon of the node */
	protected Figure iconFigure;

	/** Overlay icon of the node */
	protected Figure iconOverlayFigure;

	/** Figure representing the shadow of this node */
	protected XFigure shadowFigure;

	/** Text figure that displays the title of this node in the center of the figure */
	private FixedTitleFigure fixedTitleFigure;

	/** Rotating text tag figure containing the title of this figure */
	private SimpleTextTagFigure rotatingTitleFigure;

	/** Since calculating the displayBox can be rather expensive, we store it here */
	protected Rectangle displayBox;

	/** The visual status as defined in org.openbp.cockpit.modeler.figures.VisualElement */
	private int visualStatus = VisualElement.VISUAL_VISIBLE;

	/** Flag that denotes that this figure has been created by the user using the toolbox and is not based on an item */
	private boolean createdFromScratch;

	/** Process drawing that owns the figure */
	private ProcessDrawing drawing;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public NodeFigure()
	{
		super();
	}

	/**
	 * Creates a clone of this object.
	 * @nowarn
	 */
	public Object clone()
	{
		NodeFigure copy = null;
		try
		{
			copy = getClass().newInstance();
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException("Cannot clone object of class '" + getClass() + ".");
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("Cannot clone object of class '" + getClass() + ".");
		}

		if (getNode() != null && getDrawing() != null)
		{
			copy.connect(getNode(), getDrawing());
		}

		return copy;
	}

	/**
	 * Connects the figure to a process object.
	 *
	 * @param node Node to refer to
	 * @param drawing Process drawing that owns the figure
	 */
	public void connect(Node node, ProcessDrawing drawing)
	{
		this.node = node;
		this.drawing = drawing;

		node.setRepresentation(this);

		initialize();
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "node");
	}

	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Initializes the NodeFigure by calling various init-functions.
	 * if you overwrite this, be sure to call super.initialize (). Generally it
	 * makes more sense to overwrite the sub-init functions.
	 */
	protected void initialize()
	{
		initPresentationFigure();
		initSockets();

		// Get the size from the presentation figure
		Rectangle box = presentationFigure.displayBox();
		int w = box.width;
		if (w == 0)
			w = presentationFigure.getDescriptor().getSizeX();
		int h = box.height;
		if (h == 0)
			h = presentationFigure.getDescriptor().getSizeX();
		setSize(w, h);

		decodeGeometry();

		updateFigure();

		init(displayBox());
	}

	/**
	 * Initializes the presentation figure and its shadow.
	 */
	protected void initPresentationFigure()
	{
		SymbolDescriptor desc = FigureUtil.getSymbolDescriptorForModelObject(getDrawing().getProcessSkin(), node);
		presentationFigure = desc.createFigure();

		presentationFigure.setParent(this);
		initShadow();

		String textPosition = ((SymbolDescriptor) presentationFigure.getDescriptor()).getTextPosition();
		initTextFigure(textPosition);
	}

	/**
	 * Sets up the name tag of this node. Standard implementation is a
	 * QuarterTag with the displayText of the node.
	 * @param textPosition Text position ("c"/"s"/"n"/"e"/"w")
	 */
	protected void initTextFigure(String textPosition)
	{
		if (fixedTitleFigure != null)
		{
			fixedTitleFigure = null;
		}
		if (rotatingTitleFigure != null)
		{
			remove(rotatingTitleFigure);
			rotatingTitleFigure = null;
		}

		if (textPosition == null || textPosition.equals("c"))
		{
			// Text centered in figure
			fixedTitleFigure = new FixedTitleFigure(node);
			fixedTitleFigure.setVerboseDisplay(true);
			fixedTitleFigure.setParent(this);
		}
		else
		{
			rotatingTitleFigure = new SimpleTextTagFigure(this, null);

			TitleFigure titleFigure = new MoveableTitleFigure(node);
			titleFigure.setVerboseDisplay(true);

			// Text floating around figure, add as name figure
			rotatingTitleFigure.addContent(titleFigure, AbstractTagFigure.CONTENT_TEXT);
			rotatingTitleFigure.setContentState(AbstractTagFigure.CONTENT_TEXT);

			add(rotatingTitleFigure);

			// Position the name tag according to the given orientation
			double angle = ProcessUtil.determineAngle(textPosition);
			rotatingTitleFigure.setAngle(angle);
		}
	}

	/**
	 * Initializes the icons of the figure.
	 */
	protected void initIcon()
	{
		Icon icon = determineCustomIcon();

		if (icon == null)
		{
			if (! ((SymbolDescriptor) presentationFigure.getDescriptor()).isImageDisabled())
			{
				icon = determineCenterIcon();
			}
		}

		if (icon != null)
		{
			setIconFigure(new SimpleImageFigure(icon));
		}
		else
		{
			setIconFigure(null);
		}
	}

	protected Icon determineCustomIcon()
	{
		Icon icon = null;

		String customImagePath = determineCustomIconPath();
		if (customImagePath != null)
		{
			String id = node.getOwningModel().getQualifier().toString() + "." + customImagePath;
			icon = ItemIconMgr.getInstance().getIcon(id, FlexibleSize.HUGE);

			if (icon == null)
			{
				// TODO FIX 2 Model-local figure icons must be changed from file to resource based
				String pathName = StringUtil.absolutePathName(StringUtil.buildPath(node.getOwningModel().getModelPath(), customImagePath));
				if (new File(pathName).exists())
				{
					icon = new ImageIcon(pathName, null);
					ItemIconMgr.getInstance().registerIcon(id, icon);
				}
				else
				{
					LogUtil.error(getClass(), "Image file '" + pathName + "' for node '" + node.getQualifier() + "' does not exist.");
				}
			}
		}

		return icon;
	}

	protected String determineCustomIconPath()
	{
		String customImagePath = null;

		if (node instanceof MultiSocketNode)
		{
			MultiSocketNode msNode = (MultiSocketNode) node;

			// Try the underlying item first

			if (msNode.getImagePath() != null)
			{
				customImagePath = msNode.getImagePath();
			}
		}

		return customImagePath;
	}

	/**
	 * Initializes the icon in the center of the node.
	 */
	protected Icon determineCenterIcon()
	{
		return null;
	}

	/**
	 * Sets up the sockets of the node.
	 * Standard implementation is an EntrySocketTag per EntrySocket and an ExitSocketTag per ExitSocket.
	 * You should not need to overwrite this.
	 */
	protected void initSockets()
	{
		for (Iterator it = node.getSockets(); it.hasNext();)
		{
			addSocket((NodeSocket) it.next());
		}
		layoutUnarrangedSockets();
	}

	/**
	 * Creates the shadow figure of this tag figure.
	 */
	protected void initShadow()
	{
		shadowFigure = FigureUtil.createShadowFigure(presentationFigure);
	}

	/**
	 * Adds a figure for the given socket to the node figure.
	 *
	 * @param socket Socket to add
	 * @return The new socket figure
	 */
	public SocketFigure addSocket(NodeSocket socket)
	{
		SocketFigure socketFigure = new SocketFigure(this, socket);
		add(socketFigure);
		return socketFigure;
	}

	//////////////////////////////////////////////////
	// @@ Geometry support
	//////////////////////////////////////////////////

	/**
	 * Layouts any unarranged sockets.
	 *
	 * Arranges all sockets evenly on their respective side.
	 */
	protected void layoutUnarrangedSockets()
	{
		// Strategy: We search for all Sockets that have no geometry information
		// attached and arrange them. In order to prevent complete overlaying in case
		// of ProcessNodes, we add an offset based on the number of sockets already
		// present

		List entriesToLayout = null;
		List exitsToLayout = null;
		List othersToLayout = null;

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof SocketFigure)
			{
				SocketFigure socketFigure = (SocketFigure) f;
				NodeSocket socket = socketFigure.getNodeSocket();

				String geometry = socket.getGeometry();
				if (geometry != null && geometry.indexOf("angle") >= 0)
				{
					// Alreayd layed out
					continue;
				}

				if (socket.getNode() instanceof WorkflowNode && socket.isExitSocket() && socket.getName().equals(CoreConstants.SOCKET_TASK_PUBLISHED))
				{
					if (othersToLayout == null)
						othersToLayout = new LinkedList();
					othersToLayout.add(socketFigure);
				}
				else if (socketFigure.isEntrySocket())
				{
					if (entriesToLayout == null)
						entriesToLayout = new LinkedList();
					entriesToLayout.add(socketFigure);
				}
				else
				{
					if (exitsToLayout == null)
						exitsToLayout = new LinkedList();
					exitsToLayout.add(socketFigure);
				}
			}
		}

		// Right now, entriesToLayout contains all unlayouted entry sockets,
		// exitsToLayout all unlayouted exit socket figures.
		// othersToLayout contains special sockets that are oriented to the right.

		layoutSockets(entriesToLayout, "n");
		layoutSockets(exitsToLayout, "s");
		layoutSockets(othersToLayout, "e");
	}

	/**
	 * Layouts sockets.
	 *
	 * @param socketsToLayout List of sockets to layout (contains {@link SocketFigure} objects)
	 * @param orientation Orientation of the sockets ("e"/"n"/"w"/"s")
	 */
	protected void layoutSockets(List socketsToLayout, String orientation)
	{
		if (socketsToLayout == null)
			return;

		int xFrom;
		int xTo;
		int yFrom;
		int yTo;

		Rectangle nodeRect = compactDisplayBox();

		if ("n".equals(orientation))
		{
			xFrom = nodeRect.x;
			xTo = nodeRect.x + nodeRect.width;
			yFrom = nodeRect.y;
			yTo = nodeRect.y;
		}
		else if ("s".equals(orientation))
		{
			xFrom = nodeRect.x;
			xTo = nodeRect.x + nodeRect.width;
			yFrom = nodeRect.y + nodeRect.height;
			yTo = nodeRect.y + nodeRect.height;
		}
		else if ("w".equals(orientation))
		{
			yFrom = nodeRect.y;
			yTo = nodeRect.y + nodeRect.height;
			xFrom = nodeRect.x;
			xTo = nodeRect.x;
		}
		else
		{
			yFrom = nodeRect.y;
			yTo = nodeRect.y + nodeRect.height;
			xFrom = nodeRect.x + nodeRect.width;
			xTo = nodeRect.x + nodeRect.width;
		}

		int n = socketsToLayout.size();

		int xDist = (xTo - xFrom) / (n * 2);
		int yDist = (yTo - yFrom) / (n * 2);
		int x = xFrom + xDist;
		int y = yFrom + yDist;

		Point center = center();

		for (int i = 0; i < n; ++i)
		{
			SocketFigure socketFigure = (SocketFigure) socketsToLayout.get(i);

			// Compute the angle from the coordinates
			int xRelative = x - center.x;
			int yRelative = y - center.y;
			double angle = Math.atan2(yRelative, xRelative);

			// Take care not to overlap existing sockets
			angle = findAvailableAngle(angle);

			socketFigure.setAngle(angle);

			x += 2 * xDist;
			y += 2 * yDist;
		}
	}

	/**
	 * Returns a socket angle that doesn't overlap other sockets.
	 *
	 * @param angle Angle to check
	 * @return The checked angle
	 */
	private double findAvailableAngle(double angle)
	{
		boolean loop = true;
		while (loop)
		{
			loop = false;

			for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
			{
				Figure f = fe.nextFigure();
				if (f instanceof SocketFigure)
				{
					SocketFigure socketFigure = (SocketFigure) f;

					String geometry = socketFigure.getNodeSocket().getGeometry();

					boolean arranged = geometry != null && geometry.indexOf("angle") >= 0;
					if (arranged)
					{
						double sa = socketFigure.getAngle();
						if (angle >= sa - 0.011d && angle <= sa + 0.011d)
						{
							// Overlap
							angle += Math.PI / 10;
							loop = true;
							break;
						}
					}
				}
			}
		}

		return angle;
	}

	/**
	 * Checks if the node figure is vertically or horizontally oriented.
	 *
	 * @return
	 * true: The node's orientation is top-down.<br>
	 * false: The node's orientation is left-right.
	 */
	public boolean isVerticallyOriented()
	{
		NodeSocket socket = null;

		if (node instanceof MultiSocketNode)
		{
			// Multi socket nodes are vertically oriented if their entry socket lies in the top quadrant of the circle.
			socket = ((MultiSocketNode) node).getDefaultEntrySocket();
		}
		else if (node instanceof SingleSocketNode)
		{
			socket = ((SingleSocketNode) node).getSocket();
		}
		if (socket != null)
		{
			SocketFigure socketFigure = getSocket(socket);
			Orientation orientation = CircleConstants.determineOrientation(socketFigure.getAngle(), displayBox());
			if (orientation == Orientation.TOP || orientation == Orientation.BOTTOM)
				return true;
		}

		// All other nodes have a horizontal orientation by default
		return false;
	}

	/**
	 * Mirrors all tags at the vertical axis. (I.e. swaps left and right).
	 */
	public void flipOrientation()
	{
		boolean vertical = isVerticallyOriented();

		willChange();
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof AbstractTagFigure)
			{
				AbstractTagFigure tagFigure = (AbstractTagFigure) f;
				double angle = tagFigure.getAngle();

				if (vertical)
				{
					angle = 2 * Math.PI - angle;
				}
				else
				{
					angle = Math.PI - angle;
				}

				angle = CircleConstants.normalizeAngle(angle);
				tagFigure.setAngle(angle);
			}
		}
		changed();
	}

	/**
	 * Rotates the orientation of the node by 90 degree.
	 * @param mode Rotation mode ({@link #ROTATE_CW}/{@link #ROTATE_CCW})
	 */
	public void changeOrientation(int mode)
	{
		willChange();
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof AbstractTagFigure)
			{
				AbstractTagFigure tagFigure = (AbstractTagFigure) f;
				double angle = tagFigure.getAngle();

				if ((mode & ROTATE_CW) != 0)
				{
					angle += Math.PI / 2;
				}
				else
				{
					angle -= Math.PI / 2;
				}

				angle = CircleConstants.normalizeAngle(angle);
				tagFigure.setAngle(angle);
			}
		}
		changed();
	}

	//////////////////////////////////////////////////
	// @@ Related objects
	//////////////////////////////////////////////////

	public Node getNode()
	{
		return node;
	}

	/**
	 * Returns the socket figure for a given node socket.
	 *
	 * @param socket Socket to search
	 * @return The socket figure or null if not found
	 */
	public SocketFigure getSocket(NodeSocket socket)
	{
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof SocketFigure)
			{
				SocketFigure socketFigure = (SocketFigure) f;

				if (socketFigure.getNodeSocket().equals(socket))
					return socketFigure;
			}
		}

		return null;
	}

	/**
	 * Returns the a socket figure of this node we can connect to
	 * @param isEntry
	 * true: Get an entry socket<br>
	 * false: Get an exit socket
	 * @return The point figure or null
	 */
	public SocketFigure getConnectableSocket(boolean isEntry)
	{
		NodeSocket socket = node.getConnectableSocket(isEntry);

		if (socket != null)
			return (SocketFigure) socket.getRepresentation();

		return null;
	}

	/**
	 * Removes all connections from/to this figure.
	 */
	protected void removeConnections()
	{
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof SocketFigure)
			{
				((SocketFigure) f).removeConnections();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Decodes enclosed geometry information by breaking it into single
	 * parameters and handing them to decodeParameter ().
	 */
	public void decodeGeometry()
	{
		if (node.getGeometry() == null)
			return;

		StringTokenizer st = new StringTokenizer(node.getGeometry(), "|");
		while (st.hasMoreTokens())
		{
			decodeParameter(st.nextToken());
		}
	}

	protected void decodeParameter(String parameter)
	{
		StringTokenizer st = new StringTokenizer(parameter, ":");

		try
		{
			String errIdent = node.getQualifier().toUntypedString();
			String ident = st.nextToken();

			if (ident.equals("origin"))
			{
				int x = GeometryUtil.parseInt(st, ident, errIdent);
				int y = GeometryUtil.parseInt(st, ident, errIdent);

				Point p = new Point(x, y);

				displayBox(p, p);
			}
			else if (ident.equals("size"))
			{
				int x = GeometryUtil.parseInt(st, ident, errIdent);
				int y = x;
				if (st.hasMoreTokens())
				{
					y = GeometryUtil.parseInt(st, ident, errIdent);
				}
				setSize(x, y);
			}
			else if (ident.equals("nameangle"))
			{
				if (rotatingTitleFigure != null)
				{
					rotatingTitleFigure.setAngle(GeometryUtil.parseAngle(st, ident, errIdent));
				}
			}
			else if (ident.equals("fillcolor"))
			{
				setFillColor(GeometryUtil.parseColor(st, ident, errIdent));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void encodeGeometry()
	{
		Rectangle box = presentationFigure.displayBox();

		String geometry = "origin:" + center().x + ":" + center().y + "|size:" + box.width;
		if (box.height != box.width)
		{
			geometry += ":" + box.height;
		}

		Color c = getFillColor();
		if (c != null)
		{
			geometry += "|fillcolor:" + c.getRed() + ":" + c.getGreen() + ":" + c.getBlue();
		}

		if (rotatingTitleFigure != null)
		{
			geometry += "|nameangle:" + GeometryUtil.printAngle(rotatingTitleFigure.getAngle());
		}

		node.setGeometry(geometry);

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof SocketFigure)
			{
				((SocketFigure) f).encodeGeometry();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractFigure overrides
	//////////////////////////////////////////////////

	/**
	 * The display box of this node figure consists of the
	 * display boxes of the presentation figure and all tags.
	 * @see CH.ifa.draw.standard.CompositeFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		Rectangle db = presentationFigure.displayBox();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			Rectangle kind = f.displayBox();
			db = db.union(kind);
		}

		return db;
	}

	/**
	 * Sets the position and size of the presentationFigure of this NodeFigure.
	 * The origin is set to the center of the given coordinates, while the size is
	 * set to the lower of width and heigth.
	 * @see CH.ifa.draw.standard.CompositeFigure#basicDisplayBox(Point newOrigin, Point newCorner)
	 */
	public void basicDisplayBox(Point newOrigin, Point newCorner)
	{
		Rectangle rect = new Rectangle(newOrigin);
		rect.add(newCorner);

		basicMoveBy((int) rect.getCenterX() - center().x, (int) rect.getCenterY() - center().y);
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#basicMoveBy(int dx, int dy)
	 */
	protected void basicMoveBy(int dx, int dy)
	{
		presentationFigure.moveBy(dx, dy);

		if (iconFigure != null)
		{
			iconFigure.moveBy(dx, dy);
		}

		if (iconOverlayFigure != null)
		{
			iconOverlayFigure.moveBy(dx, dy);
		}

		if (fixedTitleFigure != null)
		{
			fixedTitleFigure.moveBy(dx, dy);
		}

		translateChildren(dx, dy);

		if (shadowFigure != null)
		{
			shadowFigure.moveBy(dx, dy);
		}
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#handles()
	 */
	public Vector handles()
	{
		Vector v = null;

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			Vector subHandles = f.handles();
			if (subHandles.size() > 0)
			{
				if (v == null)
					v = new Vector();
				v.addAll(subHandles);
			}
		}

		return v != null ? v : CollectionUtil.EMPTY_VECTOR;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		// if we are not visible, we don't do anything at all
		if (isVisible())
		{
			super.draw(g);

			if (isPresentationFigureVisible())
			{
				presentationFigure.draw(g);
			}
			else
			{
				drawSelectionMarker(g);
			}

			if (iconFigure != null)
			{
				iconFigure.draw(g);
			}

			if (iconOverlayFigure != null)
			{
				iconOverlayFigure.draw(g);
			}

			if (fixedTitleFigure != null)
			{
				fixedTitleFigure.draw(g);
			}
		}
	}

	/**
	 * Checks if the presentation figure of the process figure should be drawn.
	 * @nowarn
	 */
	protected boolean isPresentationFigureVisible()
	{
		if (node instanceof MultiSocketNode)
		{
			if (((MultiSocketNode) node).isImageOnly())
				return false;
		}
		return true;
	}

	protected void drawSelectionMarker(Graphics g)
	{
		// Obtain a color value using the decoration manager if this figure is selected.
		Color c = (Color) DecorationMgr.decorate(this, XFigure.DECO_FRAMECOLOR, null);
		if (c != null)
		{
			Stroke stroke = (Stroke) DecorationMgr.decorate(this, XFigure.DECO_FRAMESTROKE, FigureResources.standardStroke1);
			if (stroke != null)
			{
				Graphics2D g2 = (Graphics2D) g;

				// Figure selected, draw the decoration
				Color oldColor = g.getColor();
				g2.setColor(c);

				Stroke oldStroke = g2.getStroke();
				g2.setStroke(stroke);

				Rectangle r = compactDisplayBox();
				g2.drawOval(r.x, r.y, r.width - 1, r.height - 1);

				g2.setStroke(oldStroke);
				g2.setColor(oldColor);
			}
		}
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#changed ()
	 */
	public void changed()
	{
		super.changed();

		// Update the paint in case of a gradient when changing sizes
		presentationFigure.changed();
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#connectorAt(int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return presentationFigure.connectorAt(x, y);
	}

	/**
	 * Places the given rectangle so that its center is in the given direction
	 * and it exactly touches the node, without crossing any lines.
	 * @param rect The rectangle to adjust
	 * @param angle The direction in which the rectangle should be placed
	 * @return The translated rectangle
	 */
	public Rectangle placeAdjacent(Rectangle rect, double angle)
	{
		// Forward to the presentation figure
		return presentationFigure.placeAdjacent(rect, angle);
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#containsPoint(int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (! isVisible())
			return false;

		if (presentationFigure.containsPoint(x, y))
			return true;

		for (FigureEnumeration en = figures(); en.hasMoreElements();)
		{
			if (en.nextFigure().containsPoint(x, y))
				return true;
		}
		return false;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#findFigure(int x, int y)
	 */
	public Figure findFigure(int x, int y)
	{
		if (presentationFigure.containsPoint(x, y))
			return presentationFigure;

		return super.findFigure(x, y);
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#findFigureInside(int x, int y)
	 */
	public Figure findFigureInside(int x, int y)
	{
		if (presentationFigure.containsPoint(x, y))
			return presentationFigure;

		return super.findFigureInside(x, y);
	}

	/**
	 * Returns the center of the presentation Figure.
	 *
	 * @nowarn
	 */
	public Point center()
	{
		return presentationFigure.center();
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		removeConnections();

		// Remove the node from the process
		node.getProcess().removeNode(node);
	}

	public void translateChildren(int dx, int dy)
	{
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			f.moveBy(dx, dy);
		}
	}

	public void setIconFigure(Figure iconFigure)
	{
		this.iconFigure = iconFigure;
	}

	public void setIconOverlayFigure(Figure iconOverlayFigure)
	{
		this.iconOverlayFigure = iconOverlayFigure;
	}

	/**
	 * @see ShadowDropper#getShadow()
	 */
	public Figure getShadow()
	{
		return (isVisible()) ? shadowFigure : null;
	}

	public void setSize(int w, int h)
	{
		willChange();

		// Adjust the display box to the new size
		Rectangle box = presentationFigure.displayBox();
		box.grow((w - box.width) / 2, (h - box.height) / 2);
		presentationFigure.displayBox(box);

		Rectangle iconRect = null;
		if (iconFigure != null)
		{
			// Make the icon fit nicely into the presentation figure

			boolean resize = true;
			if (node instanceof MultiSocketNode)
			{
				if (! ((MultiSocketNode) node).isImageResize())
					resize = false;
			}

			if (resize)
			{
				int iconSize = Math.min(w, h);
				iconRect = new Rectangle(box.x + box.width / 2 - iconSize / 2, box.y + box.height / 2 - iconSize / 2, iconSize, iconSize);
				iconRect.grow(- iconSize / 6, - iconSize / 6);
			}
			else
			{
				Image image = ((SimpleImageFigure) iconFigure).getImage();
				int iw = image.getWidth(null);
				int ih = image.getHeight(null);
				iconRect = new Rectangle(box.x + box.width / 2 - iw / 2, box.y + box.height / 2 - ih / 2, iw, ih);
			}
			iconFigure.displayBox(iconRect);
		}

		Rectangle textRect = null;
		if (fixedTitleFigure != null)
		{
			// Make the title text rectangle slightly smaller than the presentation figure
			textRect = new Rectangle(box);
			textRect.grow(- 5, - 5);
			fixedTitleFigure.displayBox(textRect);
		}

		positionOverlayFigure(box, iconRect, textRect);

		if (shadowFigure != null)
		{
			shadowFigure.displayBox(box);
		}

		// Perform the socket layout
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof AbstractTagFigure)
			{
				((AbstractTagFigure) f).layoutTag();
			}
		}
	}

	/**
	 * Positions the overlay figure, if any, according to the positions of the display box, the figure icon and the figure text.
	 *
	 * @param box Display box of the node figure
	 * @param iconRect Icon rect
	 * @param textRect Text rect
	 */
	protected void positionOverlayFigure(Rectangle box, Rectangle iconRect, Rectangle textRect)
	{
		if (iconOverlayFigure == null)
			// No overlay
			return;

		// TODO Fix 5: Position overlay according to symbolDescriptor's overlayPosition; this full-cludge at the moment!

		Rectangle overlayRect = iconOverlayFigure.displayBox();
		overlayRect.x = box.x + box.width - overlayRect.width - 20;
		overlayRect.y = box.y + box.height - overlayRect.height - 10;
		iconOverlayFigure.displayBox(overlayRect);
	}

	//////////////////////////////////////////////////
	// @@ Expandable implemenation
	//////////////////////////////////////////////////

	/**
	 * Returns the display box of this node without the sockets
	 * (i\.e\. the the display box of the presentation figure).
	 * @see org.openbp.cockpit.modeler.figures.generic.Expandable#compactDisplayBox()
	 */
	public Rectangle compactDisplayBox()
	{
		return presentationFigure.displayBox();
	}

	//////////////////////////////////////////////////
	// @@ Colorizable implementation (or at least a part of it)
	//////////////////////////////////////////////////

	public void setFillColor(Color color)
	{
		presentationFigure.setFillColor(color);
	}

	public Color getFillColor()
	{
		return presentationFigure.getFillColor();
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return node;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * No object should be selected on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y)
	{
		if (! presentationFigure.containsPoint(x, y))
		{
			for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
			{
				Figure f = fe.nextFigure();
				if (f instanceof ProcessElementContainer)
				{
					ProcessElementContainer element = (ProcessElementContainer) f;

					if (element.containsPoint(x, y))
						return element;
				}
			}
		}

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		ProcessElementContainer child = findProcessElementContainer(x, y);

		return (child != null ? child.findProcessElementContainerInside(x, y) : this);
	}

	//////////////////////////////////////////////////
	// @@ TextHolder implementation
	//////////////////////////////////////////////////

	public Rectangle textDisplayBox()
	{
		if (fixedTitleFigure != null)
			return fixedTitleFigure.displayBox();
		if (rotatingTitleFigure != null)
			return rotatingTitleFigure.displayBox();
		return displayBox();
	}

	public String getText()
	{
		return getProcessElement().getName();
	}

	public void setText(String newText)
	{
		getProcessElement().setName(newText);
	}

	public boolean acceptsTyping()
	{
		return true;
	}

	public int overlayColumns()
	{
		String text = getText();
		int columns = 20;
		if (text != null && text.length() > 17)
			columns = text.length() + 3;
		return columns;
	}

	public void connect(Figure connectedFigure)
	{
	}

	public void disconnect(Figure disconnectFigure)
	{
	}

	public Font getFont()
	{
		return ModelerGraphics.getStandardTextFont();
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing drawing)
	{
		this.drawing = drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return getDrawing();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getPresentationFigure()
	 */
	public Figure getPresentationFigure()
	{
		return presentationFigure;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		presentationFigure.changed();

		encodeGeometry();

		// Reinitialize figure
		initPresentationFigure();

		decodeGeometry();

		presentationFigure.changed();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#isVisible()
	 */
	public boolean isVisible()
	{
		return (visualStatus & VisualElement.VISUAL_VISIBLE) != 0;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setVisible(boolean)
	 */
	public void setVisible(boolean visible)
	{
		willChange();

		if (visible)
		{
			visualStatus |= VisualElement.VISUAL_VISIBLE;
		}
		else
		{
			visualStatus &= ~ VisualElement.VISUAL_VISIBLE;
		}

		// We set the visible status of all children as well
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			if (f instanceof AbstractTagFigure)
			{
				((AbstractTagFigure) f).setVisible(visible);
			}
		}

		changed();
	}

	/**
	 * Gets the flag that denotes that this figure has been created by the user using the toolbox and is not based on an item.
	 * @nowarn
	 */
	public boolean isCreatedFromScratch()
	{
		return createdFromScratch;
	}

	/**
	 * Sets the flag that denotes that this figure has been created by the user using the toolbox and is not based on an item.
	 * @nowarn
	 */
	public void setCreatedFromScratch(boolean createdFromScratch)
	{
		this.createdFromScratch = createdFromScratch;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		if (event.mouseEvent != null)
		{
			// Pass to child element, if any
			VisualElement child = findVisualElement(event.x, event.y);
			if (child != null)
				return child.handleEvent(event);
		}
		else
		{
			if (event.type == VisualElementEvent.UPDATE_STATE)
			{
				// Pass selection events to the tags of this node
				// (this will cause hidden control anchors to be displayed when the element is selected).
				boolean ret = false;
				for (FigureEnumeration fe = this.figures(); fe.hasMoreElements();)
				{
					Figure f = fe.nextFigure();

					if (f instanceof VisualElement)
					{
						if (((VisualElement) f).handleEvent(event))
							ret = true;
					}
				}
				return ret;
			}
		}

		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public VisualElement findVisualElement(int x, int y)
	{
		if (presentationFigure.containsPoint(x, y))
			return null;

		for (FigureEnumeration fe = this.figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof VisualElement && f.containsPoint(x, y))
				return (VisualElement) f;
		}

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		if (! containsPoint(x, y))
			return null;

		VisualElement next = this.findVisualElement(x, y);

		return (next == null ? this : next.findVisualElementInside(x, y));
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * In addition to synchronization all children, synchronizes the presentation figure if applicable.
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		willChange();

		initIcon();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure next = fe.nextFigure();

			if (next instanceof UpdatableFigure)
			{
				((UpdatableFigure) next).updateFigure();
			}
		}

		if (presentationFigure instanceof UpdatableFigure)
		{
			((UpdatableFigure) presentationFigure).updateFigure();
		}

		if (fixedTitleFigure != null)
		{
			fixedTitleFigure.updateFigure();
		}

		encodeGeometry();
		String textPosition = ((SymbolDescriptor) presentationFigure.getDescriptor()).getTextPosition();
		initTextFigure(textPosition);
		decodeGeometry();

		Rectangle box = presentationFigure.displayBox();
		setSize(box.width, box.height);

		changed();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return DropClientUtil.getAllDropRegions(this, flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		if (flavors.contains(ClientFlavors.TYPE_ITEM))
			return Collections.singletonList(createNodeDropRegion(REGION_TYPE, flavors));
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return DropClientUtil.getAllImportersAt(this, p);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getSubClients()
	 */
	public List getSubClients()
	{
		return FigureUtil.getTypedFigureList(this, InteractionClient.class);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (data.isDataFlavorSupported(ClientFlavors.TYPE_ITEM))
		{
			// Dropping a data type on a node means adding the type to the entry or exit sockets of the node

			// Count the number of entry and exit sockets
			int nEntry = 0;
			int nExit = 0;

			boolean found = false;
			for (Iterator it = node.getSockets(); it.hasNext();)
			{
				NodeSocket nodeSocket = (NodeSocket) it.next();

				if (nodeSocket.isEntrySocket())
					++nEntry;
				else
					++nExit;
				found = true;
			}
			if (! found)
				// No sockets, no drop
				return false;

			int DO_ENTRY = JMsgBox.TYPE_YES;
			int DO_EXIT = JMsgBox.TYPE_NO;
			int DO_ALL = JMsgBox.TYPE_YES_FOR_ALL;
			int choice = 0;

			if ((nEntry == 1 && nExit == 0) || (nEntry == 0 && nExit == 1))
			{
				// If there is only one entry or exit, we won't ask.
				choice = DO_ALL;
			}
			else
			{
				// Ask the user; we will use the 'yes' button for entry, the 'no' button for exit sockets
				int msgType = JMsgBox.TYPE_CANCEL;
				if (nEntry > 0)
					msgType |= DO_ENTRY;
				if (nExit > 0)
					msgType |= DO_EXIT;
				if (nEntry > 0 && nExit > 0)
					msgType |= DO_ALL;

				ResourceCollection res = getDrawing().getEditor().getPluginResourceCollection();
				String title = res.getRequiredString("node.addparam.title");
				String text = res.getRequiredString("node.addparam.text");

				try
				{
					DataTypeItem type = (DataTypeItem) data.getTransferData(ClientFlavors.TYPE_ITEM);
					text = MsgFormat.format(text, type.getDisplayText());
				}
				catch (UnsupportedFlavorException e)
				{
					// Silently ignore
				}
				catch (IOException e)
				{
					// Silently ignore
				}

				JMsgBox msgBox = new JMsgBox(null, title, text, msgType);
				msgBox.setResource(res);
				msgBox.setResourcePrefix("node.addparam.");

				msgBox.initDialog();
				SwingUtil.show(msgBox);

				choice = msgBox.getUserChoice();
			}

			// Add to sockets
			for (Iterator it = node.getSockets(); it.hasNext();)
			{
				NodeSocket nodeSocket = (NodeSocket) it.next();

				boolean doit = false;
				if (nodeSocket.isEntrySocket())
				{
					doit = (choice & (DO_ENTRY | DO_ALL)) != 0;
				}
				else
				{
					doit = (choice & (DO_EXIT | DO_ALL)) != 0;
				}

				if (doit)
				{
					SocketFigure socketFigure = (SocketFigure) nodeSocket.getRepresentation();
					socketFigure.importData(REGION_TYPE, data, null);
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Creates a drop region for this node.
	 *
	 * @param regionId Region id
	 * @param flavors Supported data flavors
	 * @return The new region
	 */
	protected ViewDropRegion createNodeDropRegion(String regionId, List flavors)
	{
		// We are a region
		WorkspaceDrawingView view = getDrawing().getView();

		RectangularShape shape = presentationFigure.createRectangularShape();
		Rectangle r = presentationFigure.displayBox();
		r = view.applyScale(r, false);
		shape.setFrame(r);

		Color color = ModelerColors.DROP_REGION;
		if (flavors.contains(ModelerFlavors.COLOR))
			color = null;

		ViewDropRegion region = new ViewDropRegion(regionId, this, shape, view);
		region.setPaint(color);
		return region;
	}
}
