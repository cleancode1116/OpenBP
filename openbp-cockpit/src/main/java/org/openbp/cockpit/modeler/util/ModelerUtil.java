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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessUtil;
import org.openbp.core.model.item.process.SingleSocketNode;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Modeler util.
 *
 * @author Heiko Erhardt
 */
public class ModelerUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ModelerUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Node positioning
	//////////////////////////////////////////////////

	/**
	 * Positions the figure within the process drawing so that is doesn't completely overlap another figure.
	 * The method will check if the position of the given figure is already occupied by another figure
	 * of the same type. If yes, it will recursively try an offset 50 pixels to the lower right.
	 *
	 * @param drawing Drawing to operate on
	 * @param figure Figure to position
	 */
	public static void preventOverlap(ProcessDrawing drawing, Figure figure)
	{
		preventOverlap(drawing, figure, new Point(0, 0));
	}

	/**
	 * Positions the figure within the process drawing so that is doesn't completely overlap another figure.
	 * The method will check if the position of the given figure (moved by the given offset)
	 * is already occupied by another figure of the same type.
	 * If yes, it will recursively try an offset 50 pixels to the lower right.
	 *
	 * @param drawing Drawing to operate on
	 * @param figure Figure to position
	 * @param offset Offset to start with
	 */
	private static void preventOverlap(ProcessDrawing drawing, Figure figure, Point offset)
	{
		Point pos = new Point(figure.center());
		pos.x += offset.x;
		pos.y += offset.y;

		for (FigureEnumeration fe = drawing.figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f == figure)
				continue;

			if (f.getClass() != figure.getClass())
				continue;

			Point fPos = f.center();
			if (fPos.equals(pos))
			{
				// There is already another figure there at this position, try 50 pixels to the lower right
				offset.x += 50;
				offset.y += 50;
				preventOverlap(drawing, figure, offset);
				return;
			}
		}

		// Unoccupied position, move it here
		figure.moveBy(offset.x, offset.y);
	}

	//////////////////////////////////////////////////
	// @@ Socket to node
	//////////////////////////////////////////////////

	/**
	 * Creates an initial node figure from a socket figure.
	 * The initial node will have the same name, display name, description and role definitions
	 * and the same parameters on its socket as the given socket
	 * The node figure will be placed to the left or right of the socket figure (according
	 * to the socket's angle).
	 *
	 * @param patternSocketFigure Pattern socket figure
	 * @return The new node figure
	 */
	public static NodeFigure createInitialNodeFromSocket(SocketFigure patternSocketFigure)
	{
		// Create a new node
		InitialNode node = ProcessUtil.createStandardInitialNode();

		// Create the corresponding figure, add it to the process and return it
		return createNodeFromSocket(node, patternSocketFigure, 100);
	}

	/**
	 * Creates an final node figure from a socket figure.
	 * The final node will have the same name, display name and description
	 * and the same parameters on its socket as the given socket
	 * The node figure will be placed to the left or right of the socket figure (according
	 * to the socket's angle).
	 *
	 * @param patternSocketFigure Pattern socket figure
	 * @return The new node figure
	 */
	public static NodeFigure createFinalNodeFromSocket(SocketFigure patternSocketFigure)
	{
		// Create a new node
		FinalNode node = ProcessUtil.createStandardFinalNode();

		// Create the corresponding figure, add it to the process and return it
		return createNodeFromSocket(node, patternSocketFigure, 200);
	}

	/**
	 * Creates an initial or final node figure that reflects the given socket and adds it to the process of the socket.
	 * The node will have the same name, display name and description
	 * and the same parameters on its socket as the given socket.
	 * The node figure will be placed to the left or right of the socket figure (according
	 * to the socket's angle).
	 *
	 * @param node The new node
	 * @param patternSocketFigure Pattern socket figure
	 * @param offset Offset to the left or right of the socket
	 * @return The new node figure
	 */
	private static NodeFigure createNodeFromSocket(SingleSocketNode node, SocketFigure patternSocketFigure, int offset)
	{
		// Create a node figure for the new node
		ProcessDrawing drawing = patternSocketFigure.getDrawing();

		NodeSocket patternSocket = patternSocketFigure.getNodeSocket();

		// Copy name and parameters from the given socket to the node
		copySocketPropertiesToNode(node, patternSocket);

		// Add the node to the process
		patternSocket.getProcess().addNode(node);

		// Create a node figure for the new node
		NodeFigure nodeFigure = drawing.createNodeFigure(node);

		// Position the new node
		positionNode(nodeFigure, patternSocketFigure, offset);

		// Add the node to the drawing
		drawing.add(nodeFigure);

		// Create the corresponding figure, add it to the process and return it
		return nodeFigure;
	}

	/**
	 * Positions the given node to the left or right of the socket according to the socket's angle.
	 *
	 * @param nodeFigure Node figure to position
	 * @param socketFigure Socket figure
	 * @param offset Offset to the left or right of the socket
	 */
	private static void positionNode(NodeFigure nodeFigure, SocketFigure socketFigure, int offset)
	{
		Rectangle socketBox = socketFigure.displayBox();
		Rectangle nodeBox = nodeFigure.compactDisplayBox();
		double angle = socketFigure.getAngle();
		Point nodeCenter = new Point(0, socketBox.y + socketBox.height / 2);

		if (angle >= Math.PI / 2 && angle < 3 * Math.PI / 2)
		{
			// Position node to the left of the socket
			nodeCenter.x = socketBox.x - offset - nodeBox.width / 2;
		}
		else
		{
			nodeCenter.x = socketBox.x + offset + nodeBox.width / 2;
		}

		// Make sure the node figure is within the drawing's bounds
		if (nodeCenter.x - nodeBox.width / 2 - 10 < 0)
			nodeCenter.x = nodeBox.width / 2 + 10;
		if (nodeCenter.y - nodeBox.height / 2 - 10 < 0)
			nodeCenter.y = nodeBox.height / 2 + 10;

		// Place the figure at the calculated position
		nodeFigure.displayBox(nodeCenter, nodeCenter);

		// Make sure the figure doesn't overlap a similar one
		preventOverlap(socketFigure.getDrawing(), nodeFigure);
	}

	/**
	 * Copies the name, display name and description and the parameters
	 * of the given socket to the given node.
	 * The method will also make sure that the node has a unique name for the node.
	 *
	 * @param node Node to copy the properties to
	 * @param patternSocket Pattern socket
	 */
	private static void copySocketPropertiesToNode(SingleSocketNode node, NodeSocket patternSocket)
	{
		ProcessItem process = patternSocket.getProcess();

		// Copy name etc. from the given socket
		node.setName(patternSocket.getName());
		node.setDisplayName(patternSocket.getDisplayName());
		node.setDescription(patternSocket.getDescription());

		// Make sure it has a name unique among all nodes of the process
		NamedObjectCollectionUtil.createUniqueName(node, process.getNodeList());

		NodeSocket socket = node.getSocket();

		// Copy the parameters of the given socket to the socket of the node
		for (Iterator it = patternSocket.getParams(); it.hasNext();)
		{
			NodeParam param = (NodeParam) it.next();

			try
			{
				param = (NodeParam) param.clone();
			}
			catch (CloneNotSupportedException e)
			{
				param = null;
			}

			if (param != null)
			{
				socket.addParam(param);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////


	/**
	 * Gets a list of class names from the specified setting element.
	 *
	 * @param settingName Name of the property setting
	 * @return An iterator of class names
	 */
	public static List<String> getStringListFromSettings(String settingName)
	{
		ArrayList<String> classNames = new ArrayList<String>();
		String classNameList = SettingUtil.getStringSetting("openbp.cockpit.modelObjectConfigurators");
		if (classNameList != null)
		{
			StringTokenizer st = new StringTokenizer(classNameList, " ,");
			while (st.hasMoreTokens())
			{
				String className = st.nextToken();
				if (className.length() != 0)
				{
					classNames.add(className);
				}
			}
		}
		return classNames;
	}
}
