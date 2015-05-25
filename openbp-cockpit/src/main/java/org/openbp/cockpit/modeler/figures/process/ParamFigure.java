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
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openbp.cockpit.modeler.AutoConnector;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.FixedTitleFigure;
import org.openbp.cockpit.modeler.figures.generic.GeometryUtil;
import org.openbp.cockpit.modeler.figures.generic.MoveableTitleFigure;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.SimpleImageFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor;
import org.openbp.cockpit.modeler.figures.generic.XRoundRectangleFigure;
import org.openbp.cockpit.modeler.figures.layouter.MultiplexLayouter;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.figures.tag.StraightTagFigure;
import org.openbp.cockpit.modeler.paramvaluewizard.ParamValueWizard;
import org.openbp.common.CollectionUtil;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DataLinkImpl;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.standard.AbstractHandle;
import CH.ifa.draw.standard.AbstractLocator;
import CH.ifa.draw.standard.TextHolder;

/**
 * Figure that is used to represent node params of a socket. Is also capable of displaying
 * a connection to a process variable.
 */
public class ParamFigure extends StraightTagFigure
	implements ProcessElementContainer, TextHolder
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	private static final int DFLT_LABEL_DISTANCE = 5;

	/** Name of a data type name parameter that accepts a dropped data type item */
	public static final String PARAM_TYPE_NAME = "TypeName";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The node param represented by this figure */
	private NodeParam nodeParam;

	/** A List of all paramConnections to/from this param */
	private LinkedList paramConnections;

	/** Parameter title */
	private FixedTitleFigure titleFigure;

	/** Icon figure used to represent this param */
	private Figure iconFigure;

	/** The process variable that this param connected to, if any */
	private ProcessVariableFigure processVariableFigure;

	/** Figure between the param and a process variable */
	private ProcessVariableConnection processVariableConnection;

	/** Title label of the process variable */
	private MoveableTitleFigure globalTitleLabel;

	/** Position of the process variable title label */
	GlobalDistanceLocator globalDistanceLocator;

	/** Current distance of the label */
	int labelDistance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public ParamFigure(AbstractTagFigure parent, NodeParam nodeParam, Point origin)
	{
		super(parent, nodeParam, origin);
	}

	/**
	 * Creates the presentation figure of this node.
	 * By default, this is an {@link XRoundRectangleFigure}
	 * @return The presentation figure
	 */
	protected XFigure createPresentationFigure()
	{
		XFigureDescriptor desc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_PARAM);
		if (desc != null)
		{
			return desc.createFigure();
		}

		// Revert to the regular tag figure
		return super.createPresentationFigure();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure#initTagLayouter()
	 */
	protected void initTagLayouter()
	{
		if (getDrawing().getProcessSkin().isRadialTags())
		{
			layouter = MultiplexLayouter.getRadialInstance2(this);
		}
		else
		{
			layouter = MultiplexLayouter.getUpDownInstance(this);
		}

		layouter.setInsets(new Insets(1, 1, 1, 1));
	}

	/**
	 * Creates the content of this tag.
	 * By default, this method does nothing.
	 *
	 * @param modelObject Model object this tag represents or null
	 */
	protected void initContent(Object modelObject)
	{
		nodeParam = (NodeParam) modelObject;
		nodeParam.setRepresentation(this);

		String titleFormat = getDrawing().getProcessSkin().getParamTitleFormat();
		if (titleFormat != null)
		{
			// Parameters with titles, display the text instead of the icon
			titleFigure = new FixedTitleFigure(nodeParam);
			titleFigure.setTitleFormat(titleFormat);
			titleFigure.setAutoSize(true);
			titleFigure.updateFigure();
			addContent(titleFigure, CONTENT_TEXT);
			contentState = CONTENT_TEXT;
		}
		else
		{
			// No parameter titles, display the parameter icon instead
			iconFigure = new SimpleImageFigure(ItemIconMgr.getInstance().getTypeIcon(getDrawing().getProcessSkin().getName(), nodeParam.getDataType(), FlexibleSize.SMALL));
			addContent(iconFigure, CONTENT_ICON);
			contentState = CONTENT_ICON;
		}

		decodeGeometry();
	}

	/**
	 * @see AbstractTagFigure#initShadow()
	 */
	protected void initShadow()
	{
		// Figure does not have a shadow, so do nothing.
	}

	/**
	 * Gets the the node param represented by this figure.
	 * @nowarn
	 */
	public NodeParam getNodeParam()
	{
		return nodeParam;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "nodeParam");
	}

	//////////////////////////////////////////////////
	// @@ Parameter connections
	//////////////////////////////////////////////////

	/**
	 * Adds a parameter connection.
	 *
	 * @param connection Connection to add
	 */
	public void addParamConnection(ParamConnection connection)
	{
		if (paramConnections == null)
		{
			paramConnections = new LinkedList();
		}
		else
		{
			if (paramConnections.contains(connection))
			{
				// Already present
				return;
			}
		}

		paramConnections.add(connection);
	}

	/**
	 * Removes a parameter connection.
	 *
	 * @param connection Connection to remove
	 */
	public void removeParamConnection(ParamConnection connection)
	{
		if (paramConnections != null)
		{
			paramConnections.remove(connection);

			// TODONOW Call release?

			if (paramConnections.isEmpty())
				paramConnections = null;
		}
	}

	/**
	 * Removes all connections from/to this figure.
	 */
	public void removeConnections()
	{
		while (paramConnections != null && !paramConnections.isEmpty())
		{
			ParamConnection connection = (ParamConnection) paramConnections.getFirst();
			getDrawing().remove(connection);
			connection.release();
		}

		removeProcessVariableConnection();
	}

	//////////////////////////////////////////////////
	// @@ Process variables
	//////////////////////////////////////////////////

	/**
	 * Creates a new process variable connection figure and a new process variable figure and adds them.
	 * @param param The process variable thst we want to conenct to
	 * @param data the datalink representing the connection
	 * @return ProcessVariableConnection The created connection object or null
	 */
	public ProcessVariableConnection setProcessVariableConnection(ProcessVariable param, DataLink data)
	{
		if (processVariableFigure != null)
			return null;

		processVariableConnection = new ProcessVariableConnection(data, this);
		addContent(processVariableConnection, CONTENT_STATIC);

		processVariableFigure = new ProcessVariableFigure(param, this);
		addContent(processVariableFigure, CONTENT_DATA);

		globalDistanceLocator = new GlobalDistanceLocator();

		globalTitleLabel = new MoveableTitleFigure(param);
		globalTitleLabel.setVerboseDisplay(true);
		globalTitleLabel.connect(this);

		// Setting the content state will also layout the tag
		setContentState(CONTENT_ICON | CONTENT_DATA);

		// Provide the current angle to the new child figure, so the layouters work correctly
		setAngle(getAngle());

		return processVariableConnection;
	}

	/**
	 * Removes an existing process variable connection to this param (i\.e\. a datalink between this
	 * param and a process variable).
	 */
	public void removeProcessVariableConnection()
	{
		if (processVariableFigure == null)
		{
			return;
		}

		// Remove all global content from the param tag
		for (Iterator it = content.iterator(); it.hasNext();)
		{
			TagContent content = (TagContent) it.next();
			Figure contentFigure = content.getFigure();
			if (contentFigure instanceof ProcessVariableConnection || contentFigure instanceof ProcessVariableFigure)
			{
				it.remove();
			}
		}

		invalidate();
		processVariableFigure.invalidate();
		processVariableConnection.invalidate();

		processVariableFigure = null;

		globalTitleLabel.disconnect(this);
		globalTitleLabel = null;

		globalDistanceLocator = null;

		// Added this to prevent NPE from null globalDistanceLocator
		// org.openbp.cockpit.modeler.figures.ParamFigure$MoveLableHandle.locate(ParamTagFigure.java:1070)
		// at CH.ifa.draw.standard.AbstractHandle.displayBox(Unknown Source)
		// at CH.ifa.draw.standard.AbstractHandle.containsPoint(Unknown Source)
		// at org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView.findHandle(WorkspaceDrawingView.java:744)
		// at org.openbp.cockpit.modeler.tools.ModelerToolSupport.mouseMove (NodeManipulationTool.java:266)
		getDrawing().getEditor().view().clearSelection();

		getDrawing().getProcess().removeDataLink(processVariableConnection.getDataLink());
		processVariableConnection = null;

		setContentState(CONTENT_ICON);

		getDrawing().getEditor().view().checkDamage();

		getDrawing().setModified();
	}

	/**
	 * Computes the standard title distance based on the text size.
	 *
	 * @return The distance or 0
	 */
	protected int determineStandardTitleDistance()
	{
		if (globalTitleLabel == null)
			return 0;

		String text = globalTitleLabel.getText();
		if (text == null)
			return 0;

		int dist = 0;

		FontMetrics fm = ModelerGraphics.getDefaultFontMetrics();

		Orientation orientation = getTagOrientation();
		if (orientation == Orientation.LEFT || orientation == Orientation.RIGHT)
		{
			Rectangle r = new Rectangle();
			SwingUtil.computeMultilineStringBounds(fm, text, SwingUtil.LEFT, r);
			dist = r.width / 2 + DFLT_LABEL_DISTANCE;
		}
		else
		{
			dist = DFLT_LABEL_DISTANCE + fm.getHeight();
		}

		return dist;
	}

	//////////////////////////////////////////////////
	// @@ Control link direction management
	//////////////////////////////////////////////////

	/**
	 * Gets the vertical orientation.
	 * @nowarn
	 */
	public boolean isVerticalOrientation()
	{
		return getLayouter().isVerticalLayouter();
	}

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.Figure#handles()
	 */
	public Vector handles()
	{
		if (globalTitleLabel != null)
		{
			Vector v = new Vector();
			v.add(new MoveLableHandle());
			return v;
		}

		return CollectionUtil.EMPTY_VECTOR;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#connectorAt(int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return new ParamConnector(this);
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#canConnect()
	 */
	public boolean canConnect()
	{
		return true;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (iconFigure != null && iconFigure.containsPoint(x, y))
			return true;
		if (titleFigure != null && titleFigure.containsPoint(x, y))
			return true;
		if (processVariableConnection != null && processVariableConnection.containsPoint(x, y))
			return true;
		if (processVariableFigure != null && processVariableFigure.containsPoint(x, y))
			return true;

		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#displayBox()
	 */
	public Rectangle displayBox()
	{
		Rectangle result = super.displayBox();
		if (globalTitleLabel != null)
		{
			result.add(globalTitleLabel.displayBox());
		}
		return result;
	}

	/**
	 * @see AbstractTagFigure#basicSetAngle(double)
	 */
	public void basicSetAngle(double angle)
	{
		super.basicSetAngle(angle);

		tagOrientation = determine2WayOrientation(isVerticalOrientation());
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#draw(java.awt.Graphics)
	 */
	public void draw(Graphics g)
	{
		super.draw(g);

		if (globalTitleLabel != null)
		{
			if (ViewModeMgr.getInstance().isDataLinkVisible(this))
			{
				globalTitleLabel.draw(g);
			}
		}
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		// Remove all connections
		removeConnections();

		// We remove ourselves.
		((SocketFigure) getParent()).removeParam(this);

		super.release();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Label
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.framework.Figure#connectedTextLocator(CH.ifa.draw.framework.Figure)
	 */
	public Locator connectedTextLocator(Figure text)
	{
		return globalDistanceLocator;
	}

	/**
	 * Returns the tip location of the figure.
	 * @return Point
	 */
	Point getProcessVariableConnectionDockLocation()
	{
		Rectangle db = presentationFigure.displayBox();

		int x = 0;
		int y = 0;

		Orientation orientation = getTagOrientation();
		switch (orientation)
		{
		case RIGHT:
			x = (int) db.getMaxX();
			y = (int) db.getCenterY();
			break;

		case BOTTOM:
			x = (int) db.getCenterX();
			y = (int) db.getMaxY();
			break;

		case LEFT:
			x = (int) db.getMinX();
			y = (int) db.getCenterY();
			break;

		case TOP:
			x = (int) db.getCenterX();
			y = (int) db.getMinY();
			break;
		}

		return new Point(x, y);
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return nodeParam;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * Select socket on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return parent;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#findProcessElementContainer(int, int)
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y)
	{
		if (processVariableFigure == null)
		{
			return null;
		}

		if (processVariableFigure != null && processVariableFigure.containsPoint(x, y))
			return processVariableFigure;
		if (processVariableConnection != null && processVariableConnection.containsPoint(x, y))
			return processVariableConnection;

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#findProcessElementContainerInside(int, int)
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		if (!containsPoint(x, y))
		{
			return null;
		}

		ProcessElementContainer result = findProcessElementContainer(x, y);

		return result == null ? this : result;
	}

	//////////////////////////////////////////////////
	// @@ TextHolder implementation
	//////////////////////////////////////////////////

	public Rectangle textDisplayBox()
	{
		if (titleFigure != null)
			return titleFigure.displayBox();
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
	// @@ VisualElement overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		if (event.type == VisualElementEvent.UPDATE_STATE)
		{
			// Element was selected or deselected, we might need to rebuild the socket contents.
			SocketFigure socketFigure = (SocketFigure) getParent();
			socketFigure.handleEvent(event);
			return true;
		}

		else if (event.type == VisualElementEvent.DOUBLE_CLICK)
		{
			// TODONOW
			DrawingEditorPlugin editor = getDrawing().getEditor();
			if (editor instanceof Modeler)
			{
				// Display parameter value wizard if appropriate
				SocketFigure socketFigure = (SocketFigure) getParent();
				ParamValueWizard.displayParameterValueWizard((Modeler) editor, (NodeFigure) socketFigure.getParent(), socketFigure.getNodeSocket().getName(), nodeParam.getName());
				return true;
			}
		}
		return false;
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		super.updateFigure();

		if (globalTitleLabel != null)
		{
			globalTitleLabel.updateFigure();
		}
	}

	//////////////////////////////////////////////////
	// @@ Expandable implemenation
	//////////////////////////////////////////////////

	/**
	 * Returns the display box of the param icon.
	 * @see org.openbp.cockpit.modeler.figures.generic.Expandable#compactDisplayBox()
	 */
	public Rectangle compactDisplayBox()
	{
		Rectangle r;
		if (iconFigure != null)
		{
			r = new Rectangle(iconFigure.displayBox());
		}
		else if (titleFigure != null)
		{
			r = new Rectangle(titleFigure.displayBox());
		}
		else
		{
			r = new Rectangle();
		}

		r.grow(1, 1);

		return r;
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Decodes geometric information of the param.
	 */
	public void decodeGeometry()
	{
		if (nodeParam.getGeometry() == null)
			return;

		StringTokenizer tok = new StringTokenizer(nodeParam.getGeometry(), "|");
		while (tok.hasMoreTokens())
		{
			decodeParameter(tok.nextToken());
		}
	}

	/**
	 * Decodes a single parameter.
	 * @param parameter Parameter to decode
	 */
	protected void decodeParameter(String parameter)
	{
		String errIdent = nodeParam.getQualifier().toUntypedString();
		StringTokenizer st = new StringTokenizer(parameter, ":");

		try
		{
			String ident = st.nextToken();
			if (ident.equals("distance"))
			{
				labelDistance = GeometryUtil.parseInt(st, ident, errIdent);
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Stores geometric information in the paramobject.
	 */
	public void encodeGeometry()
	{
		String geometry = null;

		int stdDist = determineStandardTitleDistance();
		if (labelDistance != 0 && labelDistance != stdDist)
		{
			geometry = "distance:" + labelDistance;
		}

		nodeParam.setGeometry(geometry);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		boolean accept = false;

		if (flavors.contains(ClientFlavors.PROCESS_VARIABLE))
		{
			// Always only a single process variable connection - the engine would be fine with it, but not the Modeler!
			if (processVariableFigure == null)
			{
				// Drag of process variables onto a node param will create a process variable link
				try
				{
					ProcessVariable processVariable = (ProcessVariable) data.getTransferData(ClientFlavors.PROCESS_VARIABLE);

					int flags = DataLink.LINK_AUTOCONVERSION;
					if (mouseEvent != null && mouseEvent.isShiftDown())
					{
						flags = DataLink.LINK_OMIT_TYPE_CHECK;
					}
					if (DataLinkImpl.canLink(nodeParam, null, processVariable, null, flags) != DataLink.CANNOT_LINK)
					{
						accept = true;
					}
				}
				catch (UnsupportedFlavorException e)
				{
				}
				catch (IOException e)
				{
				}
			}
		}
		else if (flavors.contains(ClientFlavors.COMPLEX_TYPE_ITEM))
		{
			// Accept drag of a data type onto a node param only if the control key is pressed and
			// the target param name is 'TypeName'.
			// This is a handy shortcut for the database actions that take a data type as parameter.
			if (mouseEvent != null && mouseEvent.isControlDown())
			{
				if (nodeParam.getName().equals(PARAM_TYPE_NAME))
				{
					accept = true;
				}
			}
		}

		if (accept)
		{
			// We are a region
			WorkspaceDrawingView view = getDrawing().getView();

			Rectangle r;
			if (iconFigure != null)
			{
				r = iconFigure.displayBox();
			}
			else if (titleFigure != null)
			{
				r = titleFigure.displayBox();
			}
			else
			{
				r = new Rectangle();
			}
			r = view.applyScale(r, false);

			ViewDropRegion region = new ViewDropRegion("global", this, r, view);
			region.setPaint(ModelerColors.DROP_REGION);
			return Collections.singletonList(region);
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		p = SwingUtil.convertFromGlassCoords(p, getDrawing().getView());

		try
		{
			if (data.isDataFlavorSupported(ClientFlavors.PROCESS_VARIABLE))
			{
				// Drag of process variables onto a node param will create a process variable link
				ProcessVariable processVariable = (ProcessVariable) data.getTransferData(ClientFlavors.PROCESS_VARIABLE);

				getDrawing().getEditor().startUndo("Add Global Link");

				DataLink dataLink = getDrawing().getProcess().createDataLink();

				// Determine source and target parameter depending if we have an in- our outgoing global link
				Param sourceParam;
				Param targetParam;

				SocketFigure socketFigure = (SocketFigure) getParent();
				if (socketFigure.isEntrySocket())
				{
					sourceParam = processVariable;
					targetParam = nodeParam;
				}
				else
				{
					sourceParam = nodeParam;
					targetParam = processVariable;
				}

				// Link the global to the node parameter
				dataLink.link(sourceParam, targetParam);

				// Apply auto-conversions if necessary
				String sourceMemberPath = AutoConnector.checkAutoConversion(sourceParam, null, targetParam, null);
				dataLink.setSourceMemberPath(sourceMemberPath);

				getDrawing().getProcess().addDataLink(dataLink);

				((WorkspaceDrawingView) getDrawing().getEditor().view()).singleSelect(setProcessVariableConnection(processVariable, dataLink));

				((Component) getDrawing().getEditor().view()).requestFocus();

				getDrawing().getEditor().endUndo();

				return true;
			}

			if (data.isDataFlavorSupported(ClientFlavors.COMPLEX_TYPE_ITEM))
			{
				// Drag of a data type onto a node param will store the name of the data type
				// in the expression of the node parameter.
				// This is a handy shortcut for the database actions that take a data type as parameter.
				DataTypeItem type = (DataTypeItem) data.getTransferData(ClientFlavors.COMPLEX_TYPE_ITEM);

				getDrawing().getEditor().startUndo("Assign Type Name");

				String typeName = getDrawing().getProcess().determineItemRef(type);

				// Save the type name as expression surrounded by quotes
				nodeParam.setExpression('"' + typeName + '"');

				// Select the node parameter to visualize the change
				((WorkspaceDrawingView) getDrawing().getEditor().view()).singleSelect(this);

				((Component) getDrawing().getEditor().view()).requestFocus();

				getDrawing().getEditor().endUndo();

				return true;
			}

			getDrawing().getEditor().focusPlugin();
		}
		catch (UnsupportedFlavorException e)
		{
		}
		catch (IOException e)
		{
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Inner classes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Locator that returns a postion relative to the tip of the global.
	 */
	class GlobalDistanceLocator extends AbstractLocator
	{
		/**
		 * Create a new SplineLocator that defaults to 0\.5d.
		 */
		public GlobalDistanceLocator()
		{
			super();
		}

		/**
		 * Returns the position.
		 *
		 * @param figure Is ignored
		 */
		public Point locate(Figure figure)
		{
			Point p = getProcessVariableConnectionDockLocation();
			int dist = labelDistance != 0 ? labelDistance : determineStandardTitleDistance();

			Orientation orientation = getTagOrientation();
			if (orientation == Orientation.LEFT)
			{
				p.x -= dist;
			}
			else if (orientation == Orientation.RIGHT)
			{
				p.x += dist;
			}
			else if (orientation == Orientation.TOP)
			{
				p.y -= dist;
			}
			else
			{
				p.y += dist;
			}

			return p;
		}
	}

	/**
	 * Handle that is used to move the position of the TextLable,
	 * relative to its anchorpoint.
	 */
	class MoveLableHandle extends AbstractHandle
	{
		int lastY;

		/**
		 * Constructor.
		 */
		public MoveLableHandle()
		{
			super(ParamFigure.this);
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#invokeStart(int x, int y, Drawing drawing)
		 */
		public void invokeStart(int x, int y, Drawing drawing)
		{
			lastY = y;
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#invokeStep(int x, int y, int ax, int ay, DrawingView view)
		 */
		public void invokeStep(int x, int y, int ax, int ay, DrawingView view)
		{
			willChange();

			if (isVerticalOrientation())
			{
				labelDistance = y - getProcessVariableConnectionDockLocation().y;
			}
			else
			{
				labelDistance = x - getProcessVariableConnectionDockLocation().x;
			}

			Orientation orientation = getTagOrientation();
			if (orientation == Orientation.LEFT)
			{
				labelDistance = -labelDistance;
			}
			else if (orientation == Orientation.TOP)
			{
				labelDistance = -labelDistance;
			}
			labelDistance = Math.max(5, labelDistance);

			changed();
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#locate()
		 */
		public Point locate()
		{
			return globalDistanceLocator.locate(null);
		}

		/**
		 * @see CH.ifa.draw.standard.AbstractHandle#draw(Graphics)
		 */
		public void draw(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;

			super.draw(g);

			Point p1 = getProcessVariableConnectionDockLocation();
			Point p2 = locate();

			Stroke old = g2.getStroke();
			g2.setStroke(ModelerGraphics.labelHandleStroke);

			Color olc = g.getColor();
			g.setColor(ModelerColors.LABEL_HANDLE_LINE);

			g.drawLine(p1.x, p1.y, p2.x, p2.y);

			g2.setStroke(old);
			g.setColor(olc);
		}
	}
}
