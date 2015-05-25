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
package org.openbp.cockpit.modeler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.InitialNodeFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.DataLinkImpl;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.PlaceholderNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.VisualNode;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.jaspira.option.OptionMgr;

import CH.ifa.draw.framework.Figure;

/**
 * The autoconnector tries to auto-connect an inserted node or socket to the current node/socket
 * using control and data links.
 *
 * @author Heiko Erhardt
 */
public class AutoConnector
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Data link autoconnector operation mode: Off */
	public static final int DLA_OFF = 0;

	/** Data link autoconnector operation mode: Connect identical names */
	public static final int DLA_IDENTICAL_NAMES = 1;

	/** Data link autoconnector operation mode: Connect identical types */
	public static final int DLA_IDENTICAL_TYPES = 2;

	/** Data link autoconnector operation mode: Connect compatible types */
	public static final int DLA_COMPATIBLE_TYPES = 3;

	/** Data link autoconnector operation mode: Connect castable types */
	public static final int DLA_CASTABLE_TYPES = 4;

	/** Data link autoconnector operation mode: Connect convertible types */
	public static final int DLA_CONVERTIBLE_TYPES = 5;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Modeler in charge */
	private Modeler modeler;

	/** Workspace view associated with the modeler */
	private WorkspaceDrawingView workspaceView;

	/** The newly added node figure */
	private NodeFigure newNodeFigure;

	/** The currently selected figure */
	private Figure selectedFigure;

	/** Source socket figure */
	private SocketFigure sourceSocketFigure;

	/** Target socket figure */
	private SocketFigure targetSocketFigure;

	/** Source socket */
	private NodeSocket sourceSocket;

	/** Target socket */
	private NodeSocket targetSocket;

	/** List of source parameters (contains {@link NodeParam} objects) */
	private List sourceParams;

	/** List of target parameters (contains {@link NodeParam} objects) */
	private List targetParams;

	/** Flag if a data link was connected */
	private boolean connectedDataLink;

	/** Data link auto connector mode */
	private static int dataLinkAutoConnectorMode;

	static
	{
		determineDataLinkAutoConnectorMode();
	}

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for auto-connecting a newly added figure to the currently selected figure.
	 *
	 * @param modeler Modeler in charge
	 * @param newFigure The newly added {@link NodeFigure} or {@link SocketFigure}
	 */
	public AutoConnector(Modeler modeler, Figure newFigure)
	{
		this.modeler = modeler;
		workspaceView = modeler.getDrawingView();

		determineSocketFiguresFromSelection(newFigure);
	}

	/**
	 * Constructor for auto-connecting two given sockets.
	 *
	 * @param modeler Modeler in charge
	 * @param sourceSocketFigure Source socket figure
	 * @param targetSocketFigure Target socket figure
	 */
	public AutoConnector(Modeler modeler, SocketFigure sourceSocketFigure, SocketFigure targetSocketFigure)
	{
		this.modeler = modeler;
		workspaceView = modeler.getDrawingView();

		this.sourceSocketFigure = sourceSocketFigure;
		this.targetSocketFigure = targetSocketFigure;
	}

	//////////////////////////////////////////////////
	// @@ Static methods
	//////////////////////////////////////////////////

	/**
	 * Gets the data link auto connector mode.
	 * @nowarn
	 */
	public static int getDataLinkAutoConnectorMode()
	{
		return dataLinkAutoConnectorMode;
	}

	/**
	 * Called if the data link auto connector mode option has changed.
	 */
	public static void determineDataLinkAutoConnectorMode()
	{
		dataLinkAutoConnectorMode = OptionMgr.getInstance().getIntegerOption("editor.autoconnector.datalink", DLA_CONVERTIBLE_TYPES);
	}

	/**
	 * Checks if an auto conversion should be applied in order to link the given parameters.
	 *
	 * @param sourceParam Source node parameter (may not be null)
	 * @param sourceMemberPath Source data member access path (may be null)
	 * @param targetParam Target node parameter (may not be null)
	 * @param targetMemberPath Target data member access path (may be null)
	 *
	 * @return The return value denotes the source member path that is required for the auto conversion.
	 * If no auto conversion is possible, null will be returned.
	 */
	public static String checkAutoConversion(Param sourceParam, String sourceMemberPath, Param targetParam, String targetMemberPath)
	{
		if (dataLinkAutoConnectorMode < DLA_CONVERTIBLE_TYPES)
		{
			// Creation of auto-conversion switched off for data link connections
			return null;
		}

		return DataLinkImpl.checkAutoConversion(sourceParam, sourceMemberPath, targetParam, targetMemberPath);
	}

	//////////////////////////////////////////////////
	// @@ Auto connection
	//////////////////////////////////////////////////

	/**
	 * Tries to auto-connect control as well as data links if the autoconnect options allow for this.
	 */
	public void autoConnectAll()
	{
		boolean doAutoConnect = OptionMgr.getInstance().getBooleanOption("editor.autoconnector.controllink", true);
		if (! doAutoConnect)
		{
			// Autoconnector disabled
			selectedFigure = null;
		}

		adjustNodeGeometry();

		if (!autoConnectControlLink())
		{
			// Can't connect, so we don't need to try the data links
			return;
		}

		autoConnectDataLinks();
	}

	//////////////////////////////////////////////////
	// @@ Control link autoconnector
	//////////////////////////////////////////////////

	/**
	 * Tries to auto-connect an inserted node or socket to the current node/socket.
	 *
	 * @return
	 * true: Source and target sockets have been determined and successfully connected.<br>
	 * false: Autoconnect not possible.
	 */
	public boolean autoConnectControlLink()
	{
		if (checkSockets())
		{
			// Source and target socket determined
			if (sourceSocketFigure.canConnect() && targetSocketFigure.canConnect())
			{
				// Make sure the socket can connect and don't belong to the same node
				FlowConnection connection = new FlowConnection(modeler.getDrawing());
				if (connection.canConnectFigures(sourceSocketFigure, targetSocketFigure, 0) && sourceSocketFigure.getNodeSocket().getNode() != targetSocketFigure.getNodeSocket().getNode())
				{
					// We have two connection points.
					modeler.startUndo("Auto-connect sockets");

					connection.connectStart(sourceSocketFigure.connectorAt(0, 0));
					connection.connectEnd(targetSocketFigure.connectorAt(0, 0));
					modeler.getDrawing().add(connection);
					connection.layoutAndAdjustConnection();

					modeler.endUndo();

					autoAdjustPlaceholderName();

					return true;
				}
			}
		}

		return false;
	}

	private void autoAdjustPlaceholderName()
	{
		Node sourceNode = sourceSocketFigure.getNodeSocket().getNode();
		Node targetNode = targetSocketFigure.getNodeSocket().getNode();
		if (sourceNode instanceof VisualNode && targetNode instanceof PlaceholderNode)
		{
			String socketName = sourceSocketFigure.getNodeSocket().getName();
			String socketDisplayName = sourceSocketFigure.getNodeSocket().getDisplayName();

			// We have two connection points.
			modeler.startUndo("Auto-adjust placeholder name");

			targetNode.setName(NamedObjectCollectionUtil.createUniqueId(sourceNode.getProcess().getNodeList(), socketName));
			targetNode.setDisplayName(socketDisplayName);

			newNodeFigure.updateFigure();

			modeler.endUndo();
		}
	}

	//////////////////////////////////////////////////
	// @@ Data link autoconnector
	//////////////////////////////////////////////////

	/**
	 * Tries to auto-connect an inserted node or socket to the current node/socket.
	 *
	 * @return
	 * true: Source and target sockets have been determined and successfully connected.<br>
	 * false: Autoconnect not possible.
	 */
	public boolean autoConnectDataLinks()
	{
		if (!checkSockets())
		{
			// No source or target sockets determined
			return false;
		}

		if (dataLinkAutoConnectorMode == DLA_OFF)
		{
			// Autoconnector disabled
			return false;
		}

		sourceParams = sourceSocket.getParamList();
		sourceParams = excludeLinkedParams(sourceParams);
		if (sourceParams == null)
		{
			// No parameters to connect at the source socket
			return false;
		}

		targetParams = targetSocket.getParamList();
		targetParams = excludeLinkedParams(targetParams);
		if (targetParams == null)
		{
			// No parameters to connect at the target socket
			return false;
		}

		try
		{
			// Step 1: Connect parameters having identical names and compatible types
			tryConnectParams(DLA_IDENTICAL_NAMES);
			if (dataLinkAutoConnectorMode == DLA_IDENTICAL_NAMES)
			{
				// Finish here
				return false;
			}

			// Step 2: Connect parameters having identical types
			tryConnectParams(DLA_IDENTICAL_TYPES);
			if (dataLinkAutoConnectorMode == DLA_IDENTICAL_TYPES)
			{
				// Finish here
				return false;
			}

			// Step 3: Connect parameters having compatible types
			tryConnectParams(DLA_COMPATIBLE_TYPES);
			if (dataLinkAutoConnectorMode == DLA_COMPATIBLE_TYPES)
			{
				// Finish here
				return false;
			}

			// Step 4: Connect parameters having castable types
			tryConnectParams(DLA_CASTABLE_TYPES);
			if (dataLinkAutoConnectorMode == DLA_CASTABLE_TYPES)
			{
				// Finish here
				return false;
			}

			// Step 5: Connect parameters having compatible types, allowing primitive/bean conversions
			tryConnectParams(DLA_CONVERTIBLE_TYPES);
			if (dataLinkAutoConnectorMode == DLA_CONVERTIBLE_TYPES)
			{
				// Finish here
				return false;
			}
		}
		finally
		{
			if (connectedDataLink)
				modeler.endUndo();
			else
				modeler.cancelUndo();
		}
		return connectedDataLink;
	}

	/**
	 * Tries to connect parameter according to the given matching mode.
	 *
	 * @param mode Mode (see the constants of this class)
	 */
	private void tryConnectParams(int mode)
	{
		for (int i = sourceParams.size() - 1; i >= 0; --i)
		{
			NodeParam sourceParam = (NodeParam) sourceParams.get(i);

			if (!sourceParam.isVisible())
			{
				// Skip invisible parameters
				continue;
			}

			if (sourceParam.getAutoConnectorMode() == NodeParam.AUTOCONNECTOR_OFF)
			{
				// This parameter shall not be considered by the autoconnector.
				continue;
			}

			if (isConnected(sourceParam, targetSocket))
			{
				// This source parameter is already connected
				// to a parameter of the target socket, so skip it.
				continue;
			}

			NodeParam targetParam = findTargetParam(sourceParam, mode);
			if (targetParam != null)
			{
				if (isConnected(sourceSocket, targetParam))
				{
					// This source parameter is already connected
					// to a parameter of the target socket, so skip it.
					continue;
				}

				connect(sourceParam, targetParam);
			}
		}
	}

	/**
	 * Tries to find a target parameter that matches the given source parameter
	 * according to the specified matching mode.
	 *
	 * @param sourceParam Source parameter to connect
	 * @param mode Mode (see the constants of this class)
	 * @return The target parameter or null if no match could be found
	 */
	private NodeParam findTargetParam(NodeParam sourceParam, int mode)
	{
		for (int i = targetParams.size() - 1; i >= 0; --i)
		{
			NodeParam targetParam = (NodeParam) targetParams.get(i);

			if (!targetParam.isVisible())
			{
				// Skip invisible parameters
				continue;
			}

			if (targetParam.getAutoConnectorMode() == NodeParam.AUTOCONNECTOR_OFF)
			{
				// This parameter shall not be considered by the autoconnector.
				continue;
			}

			if (targetParam.getExpression() != null)
			{
				// Skip target parameters that already have an expression assigned.
				continue;
			}

			DataTypeItem sourceType = sourceParam.getDataType();
			DataTypeItem targetType = targetParam.getDataType();

			switch (mode)
			{
			case DLA_IDENTICAL_NAMES:
				// Data link autoconnector operation mode: Connect identical names
				if (sourceParam.getName().equals(targetParam.getName()))
				{
					if (sourceType == targetType || targetType.isBaseTypeOf(sourceType))
					{
						return targetParam;
					}
				}
				break;

			case DLA_IDENTICAL_TYPES:
				// Data link autoconnector operation mode: Connect identical types
				if (sourceType == targetType)
				{
					return targetParam;
				}
				break;

			case DLA_COMPATIBLE_TYPES:
				// Data link autoconnector operation mode: Connect compatible types
				if (targetType.isBaseTypeOf(sourceType))
				{
					return targetParam;
				}
				break;

			case DLA_CASTABLE_TYPES:
				// Data link autoconnector operation mode: Connect castable types
				if (sourceType.isBaseTypeOf(targetType))
				{
					return targetParam;
				}
				break;

			case DLA_CONVERTIBLE_TYPES:
				// Data link autoconnector operation mode: Connect convertible types
				// One type must be a complex type (i. e. a bean), the other must be
				// a type that can be used as lookup key.
				String newSourceMemberPath = DataLinkImpl.checkAutoConversion(sourceParam, null, targetParam, null);
				if (newSourceMemberPath != null)
				{
					return targetParam;
				}
				break;
			}
		}

		return null;
	}

	/**
	 * Connects a source and a target parameter using a data link.
	 *
	 * @param sourceParam Source parameter
	 * @param targetParam Target parameter
	 */
	private void connect(NodeParam sourceParam, NodeParam targetParam)
	{
		// Start an undo operation if not already done
		if (!modeler.isUndoRecording())
		{
			modeler.startUndo("Auto-connect parameters");
		}

		// Create a new data link and add it to the process
		ProcessItem process = sourceParam.getProcess();
		DataLink dataLink = process.createDataLink();

		// Link the parameters
		dataLink.link(sourceParam, targetParam);

		// Apply auto-conversions if necessary
		String sourceMemberPath = DataLinkImpl.checkAutoConversion(sourceParam, null, targetParam, null);
		dataLink.setSourceMemberPath(sourceMemberPath);

		// Add the link to the process
		process.addDataLink(dataLink);

		// Now create a connection figure and add it to the drawing
		ProcessDrawing drawing = modeler.getDrawing();
		ParamConnection connection = drawing.createParamConnection(dataLink);
		drawing.add(connection);

		// Layout the connection
		connection.layoutAndAdjustConnection();

		connectedDataLink = true;
	}

	private List<NodeParam> excludeLinkedParams(List<NodeParam> params)
	{
		List<NodeParam> ret = null;
		if (params != null)
		{
			for(NodeParam param : params)
			{
				// Add all parameters that do not have data links connected.
				if (param.getDataLinks().hasNext())
					continue;

				ProcessVariable pVar = modeler.getProcess().getProcessVariableByName(param.getName());
				if (pVar != null)
				{
					if (pVar.isAutoAssign())
						continue;
				}

				if (ret == null)
				{
					ret = new ArrayList<NodeParam>();
				}
				ret.add(param);
			}
		}
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Node geometry adjustment
	//////////////////////////////////////////////////

	/**
	 * Adjusts the geometry of the newly inserted node, if any.
	 *
	 * Adjusts the orientation of the node according to the processes' orientation,
	 * the position of the currently selected node and the orientation of the newly inserted node.
	 */
	public void adjustNodeGeometry()
	{
		if (newNodeFigure == null)
		{
			// No node to adjust
			return;
		}

		// By default, we use the orientation of the process as target orientation
		boolean makeVertical = true;
		if (selectedFigure != null)
		{
			// The relationship between the selected figure and the new node determines the relationship
			makeVertical = FigureUtil.isVerticalRelationship(newNodeFigure, selectedFigure);
		}

		// If the orientation of the new node figure and the drawing don't match, rotate the figure
		boolean isVertical = newNodeFigure.isVerticallyOriented();
		if (!isVertical && makeVertical)
		{
			// Rotate clockwise
			newNodeFigure.changeOrientation(NodeFigure.ROTATE_CW);
		}
		else if (isVertical && !makeVertical)
		{
			// Rotate counterclockwise
			newNodeFigure.changeOrientation(NodeFigure.ROTATE_CCW);
		}

		// Check if mirroring is necessary
		if (selectedFigure != null)
		{
			boolean flip;
			if (makeVertical)
			{
				flip = newNodeFigure.center().y - selectedFigure.center().y < 0;
			}
			else
			{
				flip = newNodeFigure.center().x - selectedFigure.center().x < 0;
			}

			if (flip)
			{
				newNodeFigure.flipOrientation();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Tries to determine the sockets that could be connected by the autoconnector
	 * from the current selection.
	 * If the method succeeds, the source and target sockets could be determined.
	 *
	 * @param newFigure The newly added {@link NodeFigure} or {@link SocketFigure}
	 */
	private void determineSocketFiguresFromSelection(Figure newFigure)
	{
		SocketFigure newSocketFigure = null;
		if (newFigure instanceof NodeFigure)
		{
			newNodeFigure = (NodeFigure) newFigure;
		}
		else if (newFigure instanceof SocketFigure)
		{
			newSocketFigure = (SocketFigure) newFigure;
		}

		// Only one element selected?
		if (workspaceView.selectionCount() != 1)
		{
			// No or multiple elements selected, can't autoconnect
			return;
		}
		selectedFigure = (Figure) workspaceView.selection().get(0);

		if (newNodeFigure != null)
		{
			// Selected is a node
			if (selectedFigure instanceof NodeFigure)
			{
				NodeFigure selectedNodeFigure = (NodeFigure) selectedFigure;
				if (newNodeFigure instanceof InitialNodeFigure)
				{
					// We inserted an initial node, link the exit socket of the new node
					// with an entry socket of the selected node (reverse way)
					sourceSocketFigure = selectedNodeFigure.getConnectableSocket(true);
					targetSocketFigure = newNodeFigure.getConnectableSocket(false);
				}
				else
				{
					// For any other nodes, link the regular way
					sourceSocketFigure = newNodeFigure.getConnectableSocket(true);
					targetSocketFigure = selectedNodeFigure.getConnectableSocket(false);
				}
			}
			else if (selectedFigure instanceof SocketFigure)
			{
				SocketFigure selectedSocketFigure = (SocketFigure) selectedFigure;
				if (selectedSocketFigure.isEntrySocket())
				{
					// An entry socket is selected
					targetSocketFigure = selectedSocketFigure;
					sourceSocketFigure = newNodeFigure.getConnectableSocket(false);
				}
				else
				{
					// We may do this if the socket of the selected figure does not have a control link
					// attached or if the node of the socket supports multiple exit links
					NodeSocket socket = selectedSocketFigure.getNodeSocket();

					if (!socket.hasControlLinks() || socket.getNode().isMultiExitLinkNode())
					{
						// An exit socket with no Links has been selected
						sourceSocketFigure = selectedSocketFigure;
						targetSocketFigure = newNodeFigure.getConnectableSocket(true);
					}
				}
			}
		}
		else if (newSocketFigure != null)
		{
			// Selected is a node
			if (selectedFigure instanceof NodeFigure)
			{
				NodeFigure selectedNodeFigure = (NodeFigure) selectedFigure;
				if (newSocketFigure.isEntrySocket())
				{
					// We inserted an initial node, link the exit socket of the new node
					// with an entry socket of the selected node (reverse way)
					sourceSocketFigure = selectedNodeFigure.getConnectableSocket(false);
					targetSocketFigure = newSocketFigure;
				}
				else
				{
					// For any other nodes, link the regular way
					sourceSocketFigure = newSocketFigure;
					targetSocketFigure = selectedNodeFigure.getConnectableSocket(true);
				}
			}
			else if (selectedFigure instanceof SocketFigure)
			{
				SocketFigure selectedSocketFigure = (SocketFigure) selectedFigure;
				if (selectedSocketFigure.isEntrySocket())
				{
					// An entry socket has been selected
					sourceSocketFigure = selectedSocketFigure;
					targetSocketFigure = newSocketFigure;
				}
				else
				{
					// An exit socket has been selected
					sourceSocketFigure = newSocketFigure;
					targetSocketFigure = selectedSocketFigure;
				}
			}
		}
	}

	/**
	 * Checks if source and target sockets and figures have been determined.
	 * The method will also swap source and target sockets if necessary.
	 *
	 * @return
	 * true: Source and target sockets have been successfully determined.<br>
	 * false: Autoconnect not possible (nothing selected or entry/exit socket pair not set or not found).
	 */
	private boolean checkSockets()
	{
		if (sourceSocketFigure == null || targetSocketFigure == null)
			return false;

		sourceSocket = sourceSocketFigure.getNodeSocket();
		targetSocket = targetSocketFigure.getNodeSocket();

		if (sourceSocket.isEntrySocket())
		{
			// Swap direction
			SocketFigure tmp = sourceSocketFigure;
			sourceSocketFigure = targetSocketFigure;
			targetSocketFigure = tmp;

			sourceSocket = sourceSocketFigure.getNodeSocket();
			targetSocket = targetSocketFigure.getNodeSocket();
		}

		return true;
	}

	/**
	 * Determines if a parameter is already connected to a parameter of the target socket.
	 *
	 * @param sourceParam Source parameter
	 * @param targetSocket Target socket
	 * @nowarn
	 */
	private static boolean isConnected(NodeParam sourceParam, NodeSocket targetSocket)
	{
		for (Iterator it = sourceParam.getDataLinks(); it.hasNext();)
		{
			DataLink dataLink = (DataLink) it.next();

			if (dataLink.getTargetParam() instanceof NodeParam && ((NodeParam) dataLink.getTargetParam()).getSocket() == targetSocket)
				return true;
		}
		return false;
	}

	/**
	 * Determines if a socket is already connected to a parameter of the target socket.
	 *
	 * @param sourceSocket Source socket
	 * @param targetParam Target parameter
	 * @nowarn
	 */
	private static boolean isConnected(NodeSocket sourceSocket, NodeParam targetParam)
	{
		for (Iterator it = sourceSocket.getNode().getProcess().getDataLinks(); it.hasNext();)
		{
			DataLink dataLink = (DataLink) it.next();

			if (dataLink.getSourceParam() instanceof NodeParam && ((NodeParam) dataLink.getSourceParam()).getSocket() == sourceSocket &&
			    dataLink.getTargetParam() == targetParam)
				return true;
		}
		return false;
	}
}
