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

import java.awt.Point;

import org.openbp.cockpit.modeler.figures.tag.TagConnector;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;

/**
 * Connector for a parameter connection.
 *
 * @author Stephan Moritz
 */
public class ParamConnector extends TagConnector
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Flow control connector of the socket we belong to */
	private Connector socketConnector;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param paramTagFigure Figure that owns the connector
	 */
	public ParamConnector(ParamFigure paramTagFigure)
	{
		super(paramTagFigure);

		socketConnector = getSocketFigure().connectorAt(0, 0);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractConnector#findPoint(CH.ifa.draw.framework.ConnectionFigure)
	 */
	public Point findStart(ConnectionFigure connection)
	{
		if (connection instanceof FlowConnection)
		{
			// Trying to establish a flow connection; forward to the socket's flow connector
			return socketConnector.findStart(connection);
		}

		return super.findStart(connection);
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractConnector#findPoint(CH.ifa.draw.framework.ConnectionFigure)
	 */
	public Point findEnd(ConnectionFigure connection)
	{
		if (connection instanceof FlowConnection)
		{
			// Trying to establish a flow connection; forward to the socket's flow connector
			return socketConnector.findEnd(connection);
		}

		return super.findEnd(connection);
	}
}
