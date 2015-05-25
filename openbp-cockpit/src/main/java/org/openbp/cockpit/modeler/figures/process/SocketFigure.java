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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.cockpit.itemeditor.NodeItemEditor;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.generic.GeometryUtil;
import org.openbp.cockpit.modeler.figures.generic.MoveableTitleFigure;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.figures.tag.HorizontalRotatingTagFigure;
import org.openbp.cockpit.modeler.figures.tag.TagConnector;
import org.openbp.cockpit.modeler.paramvaluewizard.ParamValueWizard;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.TextHolder;

/**
 * Socket figure.
 *
 * @author Stephan Moritz
 */
public class SocketFigure extends HorizontalRotatingTagFigure
	implements ProcessElementContainer, TextHolder, Colorizable
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Drop region id for color region */
	public static final String REGION_COLOR = "color";

	/** Drop region id for param by type region */
	public static final String REGION_PARAM_BY_TYPE = "paramByType";

	/** Drop region id for param by other param region */
	public static final String REGION_PARAM_BY_VARIABLE = "paramByParay";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Socket represented by this tag */
	protected NodeSocket socket;

	/** List of all param figures contained in this tag */
	List paramList;

	/** Title figure */
	private MoveableTitleFigure titleFigure;

	/** List of all flow connections to / from this point (contains {@link FlowConnection} objects) */
	private LinkedList flowConnections;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param parent Parent node figure
	 * @param socket Socket the figure refers to
	 */
	public SocketFigure(NodeFigure parent, NodeSocket socket)
	{
		super(parent, socket);

		// Due to complex layouting, call layout once more
		moveBy(0, 0);
		layoutTag();
	}

	/**
	 * Creates the content of this tag.
	 *
	 * @param modelObject Model object this tag represents or null
	 */
	protected void initContent(Object modelObject)
	{
		// Add the spacer figure
		super.initContent(modelObject);

		Skin skin = getDrawing().getProcessSkin();

		// Connect the model object to this tag
		this.socket = (NodeSocket) modelObject;
		socket.setRepresentation(this);

		// Create title figure holding the name of the Socket.
		// This will return the node name in case of entry or final node sockets.
		titleFigure = new MoveableTitleFigure(getReferredProcessElement());
		titleFigure.setTitleFormat(skin.getSocketTitleFormat());
		titleFigure.setVerboseDisplay(true);
		addContent(titleFigure, CONTENT_TEXT);

		// Add the socket parameters
		initParams();

		contentState = CONTENT_TEXT | ViewModeMgr.getInstance().getTagState(this);
		if (paramList == null)
			contentState &= ~ CONTENT_DATA;

		decodeGeometry();
	}

	/**
	 * Sets up the parameters of the socket.
	 */
	private void initParams()
	{
		// Show all parameters if we display the node in the node editor (i. e. if it belongs to the node editor dummy process)
		boolean showAllParams = socket.getProcess().getName().equals(NodeItemEditor.NODEEDITOR_PROCESS_NAME);

		// Add all visible parameters of the socket to the parameter list and the tag content
		for (Iterator it = socket.getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();

			// Add the parameter if we display all parameters or if it is a parameter that is currently visible
			if (showAllParams || param.isVisible())
			{
				ParamFigure paramFigure = new ParamFigure(this, param, new Point(0, 0));
				if (paramList == null)
					paramList = new ArrayList();
				paramList.add(paramFigure);
				addContent(paramFigure, CONTENT_DATA);
			}
		}
	}

	/**
	 * Reinitializes the parameter list of the socket.
	 *
	 * @param showAllParams
	 * true: Show visible and hidden parameters<br>
	 * false: Show hidden parameters only if the socket belongs to the node editor dummy process
	 */
	public void reinitParams(boolean showAllParams)
	{
		// Show all parameters if we display the node in the node editor (i. e. if it belongs to the node editor dummy process)
		if (socket.getProcess().getName().equals(NodeItemEditor.NODEEDITOR_PROCESS_NAME))
		{
			showAllParams = true;
		}

		// We must not rebuild the params completely in order not to loose the data links connected to the parameters.
		// Instead we have to add or remove the invisible parameters, i. e.
		// updateFigure the socket figure's parameter list and content list with the socket's parameter list.

		invalidate();

		// Add invisible parameters
		List pList = socket.getParamList();
		if (pList != null)
		{
			int paramFigureIndex = 0;

			int n = pList.size();
			for (int i = 0; i < n; ++i)
			{
				NodeParam param = (NodeParam) pList.get(i);

				if (param.isVisible() || showAllParams)
				{
					// Display this parameter
					if (param.getRepresentation() == null)
					{
						// Parameter not present as figure, add it
						addParam(param, paramFigureIndex + 1);
					}
					++paramFigureIndex;
				}
			}
		}

		if (! showAllParams)
		{
			// Remove invisible parameters
			for (Iterator it = content.iterator(); it.hasNext();)
			{
				TagContent content = (TagContent) it.next();
				if (content.getFigure() instanceof ParamFigure)
				{
					ParamFigure paramFigure = (ParamFigure) content.getFigure();
					NodeParam param = paramFigure.getNodeParam();
					if (! param.isVisible())
					{
						// Remove the invisible parameter from the content list
						it.remove();

						// Remove the parameter figure from the parameter figure list
						paramList.remove(paramFigure);

						// Remove the link from the parameter to the representation
						param.setRepresentation(null);
					}
				}
			}
		}

		// Add all visible content to the socket
		applyContentState();

		invalidate();
	}

	private static class HiddenSocketInfo
	{
		public static final int VISIBLE_WHEN_LINKED = (1 << 0);

		public static final int VISIBLE_WHEN_NOT_LINKED = (1 << 1);

		/** Name of the node/socket */
		public String name;

		/** Object type ({@link Node} or {@link Socket}) */
		public Class objectType;

		/** Control link constraint */
		public int constraint;

		/**
		 * Constructor.
		 *
		 * @param name Name of the node/socket
		 * @param objectType Object type ({@link Node} or {@link Socket})
		 * @param constraint Control link constraint
		 */
		public HiddenSocketInfo(String name, Class objectType, int constraint)
		{
			this.name = name;
			this.objectType = objectType;
			this.constraint = constraint;
		}
	}

	private static HiddenSocketInfo[] hiddenSockets = new HiddenSocketInfo[]
	{
		new HiddenSocketInfo(CoreConstants.SOCKET_OUT, NodeSocket.class, HiddenSocketInfo.VISIBLE_WHEN_NOT_LINKED),
		new HiddenSocketInfo(CoreConstants.SOCKET_IN, NodeSocket.class, HiddenSocketInfo.VISIBLE_WHEN_NOT_LINKED),
		new HiddenSocketInfo(CoreConstants.SOCKET_TASK_PUBLISHED, NodeSocket.class, HiddenSocketInfo.VISIBLE_WHEN_LINKED),

		new HiddenSocketInfo(CoreConstants.DEFAULT_INITIAL_NODE_NAME, Node.class, HiddenSocketInfo.VISIBLE_WHEN_NOT_LINKED),
		new HiddenSocketInfo(CoreConstants.DEFAULT_FINAL_NODE_NAME, Node.class, HiddenSocketInfo.VISIBLE_WHEN_NOT_LINKED),
	};

	/**
	 * Override that applies for hiding standard in/out names.
	 * When reinitializing the contents of the socket, the text information may be hidden if the name of the
	 * underlying process element is 'In' or 'Out' and the skin tells us to do so.
	 */
	public void applyContentState()
	{
		ProcessObject pe = getReferredProcessElement();
		String name = pe.getName();

		boolean anchorVisible = ViewModeMgr.getInstance().isControlAnchorVisible(this) || getDrawing().isDisplayAll();
		boolean hideText = false;

		// When control anchors are hidden, we also hide the socket name
		// if it is a standard socket name ('In' or 'Out').
		if (! anchorVisible)
		{
			for (int i = 0; i < hiddenSockets.length; ++i)
			{
				HiddenSocketInfo info = hiddenSockets[i];

				if (! matchSocketName(name, info.name))
					continue;

				if (! info.objectType.isInstance(pe))
					continue;

				boolean linked = socket.hasControlLinks();
				if ((info.constraint & HiddenSocketInfo.VISIBLE_WHEN_NOT_LINKED) != 0 && ! linked)
					continue;
				if ((info.constraint & HiddenSocketInfo.VISIBLE_WHEN_LINKED) != 0 && linked)
					continue;

				hideText = true;
				break;
			}

			if (pe instanceof FinalNode)
			{
				if (((FinalNode) pe).getJumpTarget() != null)
				{
					hideText = false;
				}
			}
		}

		if (hideText)
		{
			contentState &= ~ CONTENT_TEXT;
		}
		else
		{
			contentState |= CONTENT_TEXT;
		}

		super.applyContentState();
	}

	/**
	 * Checks if the given name starts with the pattern and consists of remaining digits only.
	 * @nowarn
	 */
	private boolean matchSocketName(String name, String pattern)
	{
		if (! name.startsWith(pattern))
			return false;

		String remainder = name.substring(pattern.length());
		int l = remainder.length();
		for (int i = 0; i < l; ++i)
		{
			char c = remainder.charAt(i);
			if (! Character.isDigit(c))
				return false;
		}

		return true;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "nodeSocket");
	}

	//////////////////////////////////////////////////
	// @@ Associated object access
	//////////////////////////////////////////////////

	/**
	 * Gets the socket represented by this tag.
	 * @nowarn
	 */
	public NodeSocket getNodeSocket()
	{
		return socket;
	}

	/**
	 * Determines if this socket figure represents an entry socket.
	 *
	 * @return
	 * true: The figure represents an entry socket.<br>
	 * false: The figure represents an exit socket.
	 */
	public boolean isEntrySocket()
	{
		return socket.isEntrySocket();
	}

	//////////////////////////////////////////////////
	// @@ Flow connections
	//////////////////////////////////////////////////

	/**
	 * Gets the list of all flow connections to / from this point.
	 * @nowarn
	 */
	public List getFlowConnections()
	{
		return flowConnections;
	}

	/**
	 * Adds a flow connection.
	 *
	 * @param connection Connection to add
	 */
	public void addFlowConnection(FlowConnection connection)
	{
		if (flowConnections == null)
		{
			flowConnections = new LinkedList();
		}
		else
		{
			if (flowConnections.contains(connection))
				// Already present
				return;
		}

		flowConnections.add(connection);
	}

	/**
	 * Removes a flow connection.
	 *
	 * @param connection Connection to remove
	 */
	public void removeFlowConnection(FlowConnection connection)
	{
		if (flowConnections != null)
		{
			flowConnections.remove(connection);

			// TODONOW Call release?

			if (flowConnections.isEmpty())
				flowConnections = null;
		}
	}

	//////////////////////////////////////////////////
	// @@ Parameter management
	//////////////////////////////////////////////////

	/**
	 * Adds a figure for the given param to the socket figure.
	 *
	 * @param param Param to add
	 * @param pos Position to add the parameter at (starting with 1) or -1 to add it to the end of the parameter list
	 * @return The new param figure
	 */
	public ParamFigure addParam(NodeParam param, int pos)
	{
		ParamFigure paramFigure = new ParamFigure(this, param, new Point(0, 0));

		if (pos < 0)
		{
			pos = getContent().size();
		}

		if (paramList == null)
			paramList = new ArrayList();
		paramList.add(pos - 1, paramFigure);

		addContentAt(paramFigure, pos, CONTENT_DATA);

		// Setting the content state will also layout the tag
		setContentState(getContentState() | CONTENT_DATA);

		// Provide the current angle to the new child figure, so the layouters work correctly
		paramFigure.setAngle(getAngle());

		return paramFigure;
	}

	/**
	 * Removes the parameter from this socket.
	 *
	 * @param paramFigure Parameter to remove
	 */
	public void removeParam(ParamFigure paramFigure)
	{
		willChange();

		socket.removeParam(paramFigure.getNodeParam());

		for (Iterator it = content.iterator(); it.hasNext();)
		{
			TagContent content = (TagContent) it.next();

			if (content.getFigure() == paramFigure)
			{
				if (paramList != null)
				{
					paramList.remove(content.getFigure());

					if (paramList.isEmpty())
						paramList = null;
				}

				it.remove();
				break;
			}
		}

		applyContentState();

		changed();
	}

	/**
	 * Moves the parameter with the given index so that it assumes the new
	 * index. Replaced parameters have their indices increased by one.
	 * @nowarn
	 */
	public void moveParameter(int oldIndex, int newIndex)
	{
		if (oldIndex == newIndex)
			// No moving necessary
			return;

		if (oldIndex < newIndex)
		{
			newIndex--;
		}

		// 3 steps necessary...

		// 1. Move of the actual figure component
		paramList.add(newIndex, paramList.remove(oldIndex));

		// 2. Move the parameter in the underlaying socket
		List socketParams = socket.getParamList();
		socketParams.add(newIndex, socketParams.remove(oldIndex));

		moveContent(oldIndex + 1, newIndex + 1);

		getDrawing().getEditor().view().clearSelection();

		getDrawing().setModified();
	}

	/**
	 * Removes all connections from/to this figure.
	 */
	protected void removeConnections()
	{
		// Remove data links
		if (paramList != null)
		{
			for (Iterator it = paramList.iterator(); it.hasNext();)
			{
				((ParamFigure) it.next()).removeConnections();
			}
		}

		// Remove control links
		while (flowConnections != null && ! flowConnections.isEmpty())
		{
			FlowConnection connection = (FlowConnection) flowConnections.getFirst();
			getDrawing().remove(connection);
			connection.release();
		}
	}

	/*
	private Iterator paramFigureIterator()
	{
		return new ParamFigureIterator(false);
	}

	private Iterator reverseParamFigureIterator()
	{
		return new ParamFigureIterator(true);
	}

	private class ParamFigureIterator
		implements Iterator
	{
		int index;

		boolean reverse;

		public ParamFigureIterator(boolean reverse)
		{
			this.reverse = reverse;
			findNext(reverse ? getNumberOfContents() - 1 : 0);
		}

		public boolean hasNext()
		{
			return index >= 0;
		}

		public Object next()
		{
			if (index < 0)
				return null;

			Object ret = getContentFigureAt(index);
			int nextIndex = reverse ? index - 1 : index + 1;
			findNext(nextIndex);
			return ret;
		}

		private void findNext(int i)
		{
			if (i >= 0)
			{
				for (;;)
				{
					if (i >= getNumberOfContents())
					{
						index = -1;
						break;
					}

					if (getContentFigureAt(i) instanceof ParamFigure)
					{
						index = i;
						break;
					}

					if (reverse)
						--i;
					else
						++i;
				}
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException("remove not supported by ParamFigureIterator");
		}
	}
	*/

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////
	public void basicSetAngle(double angle)
	{
		super.basicSetAngle(angle);

		for (Iterator it = content.iterator(); it.hasNext();)
		{
			TagContent next = (TagContent) it.next();

			if (next.getFigure() instanceof AbstractTagFigure)
			{
				((AbstractTagFigure) next.getFigure()).setAngle(angle);
			}
		}
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#connectorAt(int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return new TagConnector(this);
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#canConnect()
	 */
	public boolean canConnect()
	{
		if (isEntrySocket())
			// We can always connect to an entry socket
			return true;

		if (flowConnections == null || flowConnections.isEmpty())
			// We can connect to an exit socket if there is no control link attached to it
			return true;

		if (socket.getNode().isMultiExitLinkNode())
			// We can connect to an exit socket if it belongs to a node that support multiple exit links on a socket at a time
			return true;

		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y)
	{
		if (presentationFigure.containsPoint(x, y))
			return true;

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			if (fe.nextFigure().containsPoint(x, y))
				return true;
		}

		return false;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		// Remove the connections to/from our parameter and flow access point
		removeConnections();

		// We remove ourselves.
		if (getParent() instanceof MultiSocketNodeFigure)
		{
			((MultiSocketNodeFigure) getParent()).removeSocket(this);
		}

		super.release();
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	public void decodeGeometry()
	{
		String geometry = socket.getGeometry();
		if (geometry == null)
			return;

		for (StringTokenizer tok = new StringTokenizer(geometry, "|"); tok.hasMoreTokens();)
		{
			decodeParameter(tok.nextToken());
		}
	}

	protected void decodeParameter(String parameter)
	{
		String errIdent = socket.getQualifier().toUntypedString();
		StringTokenizer st = new StringTokenizer(parameter, ":");

		try
		{
			// Note: Until V 1.2, the 'state' property was saved. In order to prevent conflicts with processes saved
			// with these version, do not use a property named 'state' here!
			String ident = st.nextToken();

			if (ident.equals("angle"))
			{
				setAngle(GeometryUtil.parseAngle(st, ident, errIdent));
			}
			else if (ident.equals("fillcolor"))
			{
				setFillColor(GeometryUtil.parseColor(st, ident, errIdent));
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Stores geometric information in the socket object.
	 */
	public void encodeGeometry()
	{
		String geometry = "angle:" + GeometryUtil.printAngle(angle);

		Color c = getFillColor();
		if (c != null)
		{
			geometry += "|fillcolor:" + c.getRed() + ":" + c.getGreen() + ":" + c.getBlue();
		}

		socket.setGeometry(geometry);

		if (paramList != null)
		{
			for (Iterator it = paramList.iterator(); it.hasNext();)
			{
				((ParamFigure) it.next()).encodeGeometry();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return socket;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		Node node = socket.getNode();
		if (node instanceof InitialNode || node instanceof FinalNode)
			// Entry and final node socket figures return their parent node as referred object.
			return node;

		// All other socket figures return the socket.
		return socket;
	}

	/**
	 * select node on deletion.
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
		if (paramList != null && (getContentState() & AbstractTagFigure.CONTENT_DATA) != 0)
		{
			for (Iterator iter = paramList.iterator(); iter.hasNext();)
			{
				ProcessElementContainer element = (ProcessElementContainer) iter.next();

				if (element.containsPoint(x, y))
					return element;
			}
		}

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#findProcessElementContainerInside(int, int)
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		ProcessElementContainer pec = findProcessElementContainer(x, y);

		return pec != null ? pec.findProcessElementContainerInside(x, y) : this;
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
		boolean checkState = false;

		if (event.type == VisualElementEvent.UPDATE_STATE)
		{
			// Element was selected or deselected, we might need to rebuild the socket contents.
			checkState = true;
		}

		else if (event.type == VisualElementEvent.SET_DND_PARTICIPANT)
		{
			// Element is participating in a drag and drop operation, we might need to rebuild the socket contents.
			checkState = true;
			visualStatus |= VisualElement.VISUAL_DND_PARTICIPANT;
		}
		else if (event.type == VisualElementEvent.UNSET_DND_PARTICIPANT)
		{
			// Element is not participating in a drag and drop operation any more, we might need to rebuild the socket contents.
			checkState = true;
			visualStatus &= ~ VisualElement.VISUAL_DND_PARTICIPANT;
		}

		else if (event.type == VisualElementEvent.DOUBLE_CLICK)
		{
			// TODONOW
			DrawingEditorPlugin editor = getDrawing().getEditor();
			if (editor instanceof Modeler)
			{
				// Display parameter value wizard if appropriate
				ParamValueWizard.displayParameterValueWizard((Modeler) editor, (NodeFigure) getParent(), socket.getName(), null);
				return true;
			}
		}

		// If control anchors are to be hidden, we might need to rebuild the socket contents.
		if (checkState && ! ViewModeMgr.getInstance().isControlAnchorVisible(this))
		{
			checkDecoratedContentState();
			return true;
		}

		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		presentationFigure.willChange();

		// Re-set the origin and layout the tag
		origin = new Point(parent.center());

		encodeGeometry();

		// Reinitialize figure
		initPresentationFigure();

		decodeGeometry();

		layoutTag();

		presentationFigure.changed();
	}

	//////////////////////////////////////////////////
	// @@ Colorizable implementation
	//////////////////////////////////////////////////

	/**
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
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
		if (transferable.isDataFlavorSupported(ModelerFlavors.COLOR) || transferable.isDataFlavorSupported(ClientFlavors.TYPE_ITEM))
		{
			handleEvent(new VisualElementEvent(VisualElementEvent.SET_DND_PARTICIPANT, getDrawing().getEditor()));
		}

		super.dragStarted(transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
		if (transferable.isDataFlavorSupported(ModelerFlavors.COLOR) || transferable.isDataFlavorSupported(ClientFlavors.TYPE_ITEM))
		{
			handleEvent(new VisualElementEvent(VisualElementEvent.UNSET_DND_PARTICIPANT, getDrawing().getEditor()));
		}

		super.dragEnded(transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		WorkspaceDrawingView view = getDrawing().getView();

		if (flavors.contains(ModelerFlavors.COLOR))
		{
			// Use the whole section as target
			Rectangle r = presentationFigure.displayBox();
			r = view.applyScale(r, false);
			return Collections.singletonList(new ViewDropRegion(REGION_COLOR, this, r, view));
		}

		String regionId = null;
		if (flavors.contains(ClientFlavors.TYPE_ITEM))
		{
			regionId = REGION_PARAM_BY_TYPE;
		}
		else if (flavors.contains(ClientFlavors.PROCESS_VARIABLE))
		{
			regionId = REGION_PARAM_BY_VARIABLE;
		}

		if (regionId != null)
		{
			if (mouseEvent == null || ! mouseEvent.isControlDown())
			{
				// Accept type items only if the CTRL button is not pressed to prevent overlaying the region
				// exposed by the ParamFigure in this case.
				Rectangle[] bounds = getParamRegions();

				if (bounds.length > 1)
				{
					List result = new ArrayList();
					for (int i = 0; i < bounds.length; i++)
					{
						// Add a green drop region with a small black border around each parameter
						Rectangle r = view.applyScale(bounds[i], false);

						ViewDropRegion region = new ViewDropRegion(regionId + ":" + i, this, r, view);
						region.setPaint(ModelerColors.DROP_REGION);
						region.setFrameColor(Color.BLACK);

						result.add(region);
					}
					return result;
				}

				// Params are not shown, so we need only a single region.
				// Use the whole section as target
				Rectangle r = presentationFigure.displayBox();
				r = view.applyScale(r, false);
				ViewDropRegion region = new ViewDropRegion(regionId, this, r, view);
				region.setPaint(ModelerColors.DROP_REGION);
				return Collections.singletonList(region);
			}
		}

		return null;
	}

	/**
	 * Returns an array of Rectangles containing the coordinates of the regions
	 * between the parameters. The size of the returned list is (number of
	 * parameters) + 1.
	 * @return An array of Rectangles
	 */
	public Rectangle[] getParamRegions()
	{
		int nParams = paramList != null ? paramList.size() : 0;
		if ((getContentState() & AbstractTagFigure.CONTENT_DATA) == 0)
		{
			// No parameter displayed
			nParams = 0;
		}

		Rectangle[] result = new Rectangle[nParams + 1];

		if (nParams > 0)
		{
			// Generate a region for each segment of the tag.
			// Each segment has the height of the tag itself and a
			// horizontal reach from the center of bordering params
			// or the edge of the socket itself respectively.

			// counter for the left side of the actual region
			Rectangle db = presentationFigure.displayBox();

			if (isVerticalOrientation())
			{
				// Orient according to position...
				if (angle >= Math.PI)
				{
					int y = db.y;

					// North
					if (paramList != null)
					{
						// we are in the top half, i.e. reverse
						for (int i = nParams; i > 0; i--)
						{
							ParamFigure paramFigure = (ParamFigure) paramList.get(i - 1);

							Rectangle r = new Rectangle(db.x, y, db.width, 0);
							y = (int) paramFigure.displayBox().getCenterY();
							r.add(db.x, y);

							result[i] = r;
						}
					}

					// Create the last (bottom) region.
					Rectangle r = new Rectangle(db.x, y, db.width, 0);
					r.add(db.x, db.y + db.height);

					result[0] = r;
				}
				else
				{
					int y = db.y;

					// South
					if (paramList != null)
					{
						for (int i = 0; i < nParams; i++)
						{
							ParamFigure paramFigure = (ParamFigure) paramList.get(i);

							Rectangle r = new Rectangle(db.x, y, db.width, 0);
							y = (int) paramFigure.displayBox().getCenterY();
							r.add(db.x, y);

							result[i] = r;
						}
					}

					// Create the last (top) region.
					Rectangle r = new Rectangle(db.x, y, db.width, 0);
					r.add(db.x, db.y + db.height);

					result[nParams] = r;
				}
			}
			else
			{
				// Orient according to position...
				if (angle >= Math.PI / 2 && angle < 3 * Math.PI / 2)
				{
					int x = db.x;

					// West
					if (paramList != null)
					{
						// we are in the left half, i.e. reverse
						for (int i = nParams; i > 0; i--)
						{
							ParamFigure paramFigure = (ParamFigure) paramList.get(i - 1);

							Rectangle r = new Rectangle(x, db.y, 0, db.height);
							x = (int) paramFigure.displayBox().getCenterX();
							r.add(x, db.y);

							result[i] = r;
						}
					}

					// Create the last (leftmost) region.
					Rectangle r = new Rectangle(x, db.y, 0, db.height);
					r.add(db.x + db.width, db.y);

					result[0] = r;
				}
				else
				{
					int x = db.x;

					// East
					if (paramList != null)
					{
						for (int i = 0; i < nParams; i++)
						{
							ParamFigure paramFigure = (ParamFigure) paramList.get(i);

							Rectangle r = new Rectangle(x, db.y, 0, db.height);
							x = (int) paramFigure.displayBox().getCenterX();
							r.add(x, db.y);

							result[i] = r;
						}
					}

					// Create the last (rightmost) region.
					Rectangle r = new Rectangle(x, db.y, 0, db.height);
					r.add(db.x + db.width, db.y);

					result[nParams] = r;
				}
			}
		}
		else
		{
			// params are not shown, so we need only a single region.
			// Use the whole section as target
			result[0] = presentationFigure.displayBox();
		}

		return result;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		try
		{
			if (REGION_COLOR.equals(regionId))
			{
				// Set the color
				Color color = (Color) data.getTransferData(ModelerFlavors.COLOR);

				getDrawing().getEditor().startUndo("Set Color");

				setFillColor(color);
				invalidate();

				getDrawing().getEditor().endUndo();
			}
			else
			{
				String regionStr = (String) regionId;
				String paramName = null;
				DataTypeItem paramType = null;
				ProcessVariable processVariable = null;

				if (regionStr.startsWith(REGION_PARAM_BY_TYPE))
				{
					paramType = (DataTypeItem) data.getTransferData(ClientFlavors.TYPE_ITEM);
					paramName = paramType.getName();
				}
				else if (regionStr.startsWith(REGION_PARAM_BY_VARIABLE))
				{
					processVariable = (ProcessVariable) data.getTransferData(ClientFlavors.PROCESS_VARIABLE);
					paramType = processVariable.getDataType();
					paramName = processVariable.getName();
					if (socket.getParamByName(paramName) != null)
					{
						String msg = "The socket already has a parameter named '" + paramName
							+ "'.\nPlease connect the process variable to the existing parameter.";
						JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
						return true;
					}
				}

				int pos = getContent().size();
				int sepIndex = regionStr.indexOf(':');
				if (sepIndex > 0)
				{
					// We need to add 1 to skip the label
					pos = Integer.parseInt(regionStr.substring(sepIndex + 1)) + 1;
				}

				getDrawing().getEditor().startUndo("Add Parameter");

				NodeParam param = socket.createParam(paramName);
				param.setDataType(paramType);

				// Add the param to the socket
				socket.addParam(param, pos - 1);
				param.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

				// Add the parameter figure to the socket figure
				ParamFigure paramFigure = addParam(param, pos);

				((WorkspaceDrawingView) getDrawing().getEditor().view()).singleSelect(paramFigure);

				if (processVariable != null)
				{
					// Link the process variable to the new parameter
					DataLink dataLink = getDrawing().getProcess().createDataLink();

					// Determine source and target parameter depending if we have an in- our outgoing global link
					Param sourceParam;
					Param targetParam;

					if (socket.isEntrySocket())
					{
						sourceParam = processVariable;
						targetParam = param;
					}
					else
					{
						sourceParam = param;
						targetParam = processVariable;
					}

					// Link the global to the node parameter
					dataLink.link(sourceParam, targetParam);

					getDrawing().getProcess().addDataLink(dataLink);
					paramFigure.setProcessVariableConnection(processVariable, dataLink);
				}

				getDrawing().getEditor().endUndo();

				getDrawing().getEditor().fireEvent("modeler.drawing.paramadded", paramFigure);
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

		return true;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		WorkspaceDrawingView view = getDrawing().getView();

		Point docPoint = SwingUtil.convertFromGlassCoords(p, view);
		if (presentationFigure.containsPoint(docPoint.x, docPoint.y))
			return Collections.singletonList(new Importer(socket.getQualifier(), this, new DataFlavor[]
			{
				ClientFlavors.TYPE_ITEM, ModelerFlavors.COLOR
			}));

		return null;
	}
}
