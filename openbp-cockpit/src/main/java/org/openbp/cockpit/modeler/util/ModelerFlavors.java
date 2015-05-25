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
import java.awt.datatransfer.DataFlavor;

import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.process.HLineFigure;
import org.openbp.cockpit.modeler.figures.process.LineFigure;
import org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.ProcessVariableConnection;
import org.openbp.cockpit.modeler.figures.process.ProcessVariableFigure;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.process.VLineFigure;

import CH.ifa.draw.framework.Figure;

/**
 * This class contains constants for data flavors used by OpenBP clients.
 * Data flavors are used for drag and drop or copy/paste operations.
 *
 * @author Heiko Erhardt
 */
public class ModelerFlavors
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private ModelerFlavors()
	{
	}

	/** Color flavor */
	public static final DataFlavor COLOR = new DataFlavor(Color.class, "Color");

	/** Colorizable object flavor */
	public static final DataFlavor COLORIZABLE = new DataFlavor(Colorizable.class, "Colorizable object");

	/** Line flavor */
	public static final DataFlavor LINE = new DataFlavor(LineFigure.class, "Line");

	/** Horizontal line flavor */
	public static final DataFlavor HLINE = new DataFlavor(HLineFigure.class, "Horizontal line");

	/** Vertical line flavor */
	public static final DataFlavor VLINE = new DataFlavor(VLineFigure.class, "Vertical line");

	/** Process element container flavor */
	public static final DataFlavor PROCESS_ELEMENT_CONTAINER = new DataFlavor(ProcessElementContainer.class, "Process element container");

	/** Figure */
	public static final DataFlavor FIGURE = new DataFlavor(Figure.class, "Figure");

	/** Node figure flavor */
	public static final DataFlavor NODE_FIGURE = new DataFlavor(NodeFigure.class, "Node figure");

	/** Multi socket node figure flavor */
	public static final DataFlavor MULTI_SOCKET_NODE_FIGURE = new DataFlavor(MultiSocketNodeFigure.class, "Multi socket node figure");

	/** Socket figure */
	public static final DataFlavor SOCKET_FIGURE = new DataFlavor(SocketFigure.class, "Node socket figure");

	/** Parameter figure */
	public static final DataFlavor PARAM_FIGURE = new DataFlavor(ParamFigure.class, "Node parameter figure");

	/** Process variable figure flavor */
	public static final DataFlavor PROCESS_VARIABLE_FIGURE = new DataFlavor(ProcessVariableFigure.class, "Process variable figure");

	/** Global connenction figure flavor */
	public static final DataFlavor PROCESS_VARIABLE_CONNECTION_FIGURE = new DataFlavor(ProcessVariableConnection.class, "Process variable connection");

	/** Process global connenction figure flavor */
	public static final DataFlavor PARAM_CONNECTION_FIGURE = new DataFlavor(ParamConnection.class, "Data link connection");
}
