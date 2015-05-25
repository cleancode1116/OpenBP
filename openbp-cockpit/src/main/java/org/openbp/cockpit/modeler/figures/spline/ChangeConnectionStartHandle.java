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
package org.openbp.cockpit.modeler.figures.spline;

import java.awt.Point;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;

/**
 * Handle to reconnect the
 * start of a connection to another figure.
 *
 * @author Stephan Moritz
 */
public class ChangeConnectionStartHandle extends ChangeConnectionHandle
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructs the connection handle for the given start figure.
	 * @see org.openbp.cockpit.modeler.figures.spline.ChangeConnectionHandle#target()
	 */
	public ChangeConnectionStartHandle(Figure owner)
	{
		super(owner);
	}

	//////////////////////////////////////////////////
	// @@ ChangeConnectionHandle implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the start figure of a connection.
	 * @see org.openbp.cockpit.modeler.figures.spline.ChangeConnectionHandle#disconnect()
	 */
	protected Connector target()
	{
		return connectionFigure.getStartConnector();
	}

	/**
	 * Sets the start of the connection.
	 * @see org.openbp.cockpit.modeler.figures.spline.ChangeConnectionHandle#connect(Connector c)
	 */
	protected void connect(Connector c)
	{
		connectionFigure.connectStart(c);
	}

	/**
	 * Disconnects the start figure.
	 */
	protected void disconnect()
	{
		connectionFigure.disconnectStart();
	}

	/**
	 * Sets the start point of the connection.
	 * @param x Document coordinate
	 * @param y Document coordinate
	 * @see org.openbp.cockpit.modeler.figures.spline.ChangeConnectionHandle#setPoint(int x, int y)
	 */
	protected void setPoint(int x, int y)
	{
		connectionFigure.startPoint(x, y);
	}

	/**
	 * Returns the start point of the connection.
	 * @see CH.ifa.draw.standard.AbstractHandle#locate()
	 */
	public Point locate()
	{
		return connectionFigure.startPoint();
	}
}
