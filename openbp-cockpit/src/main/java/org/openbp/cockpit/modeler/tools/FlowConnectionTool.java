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

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;

/**
 * Connection tool for flow connections.
 *
 * @author Stephan Pauxberger
 */
public class FlowConnectionTool extends XConnectionTool
{
	public FlowConnectionTool(ModelerToolSupport toolSupport)
	{
		super(toolSupport);
	}

	public PolySplineConnection createConnection()
	{
		FlowConnection connection = new FlowConnection((ProcessDrawing) getDrawing());
		connection.setDrawDecorations(false);
		return connection;
	}
}
