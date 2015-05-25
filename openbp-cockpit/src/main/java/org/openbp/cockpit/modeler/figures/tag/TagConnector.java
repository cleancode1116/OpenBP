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
package org.openbp.cockpit.modeler.figures.tag;

import java.awt.Point;
import java.awt.Rectangle;

import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.generic.ChildFigure;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.standard.AbstractConnector;

/**
 * Connector used within a tag.
 * The orientation of the connector is determined by the orientation of the socket it belongs to.
 *
 * @author Stephan Moritz
 */
public class TagConnector extends AbstractConnector
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Tag figure that owns the connector (= owner) */
	private AbstractTagFigure connectorFigure;

	/** Socket figure that owns the connector figure (or the connector figure itself) */
	private SocketFigure socketFigure;

	/** Fixed direction of the connector or -1 for dynamic orientation */
	private Orientation lockedOrientation = Orientation.UNDETERMINED;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param connectorFigure Figure that owns the connector
	 */
	public TagConnector(AbstractTagFigure connectorFigure)
	{
		super(connectorFigure);

		this.connectorFigure = connectorFigure;

		for (Figure f = connectorFigure; f != null;)
		{
			if (f instanceof SocketFigure)
			{
				socketFigure = (SocketFigure) f;
				break;
			}

			if (f instanceof ChildFigure)
				f = ((ChildFigure) f).getParent();
			else
				f = null;
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractConnector overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.AbstractConnector#findPoint(CH.ifa.draw.framework.ConnectionFigure)
	 */
	protected Point findPoint(ConnectionFigure connection)
	{
		Orientation direction = getOrientation();

		Rectangle cb = connectorFigure.compactDisplayBox();
		if (!connectorFigure.isVerticalOrientation())
		{
			return new Point((int) (direction == Orientation.LEFT ? cb.getMinX() : cb.getMaxX()), (int) cb.getCenterY());
		}
		else
		{
			return new Point((int) cb.getCenterX(), (int) (direction == Orientation.TOP ? cb.getMinY() : cb.getMaxY()));
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Returns the owning tag.
	 * @nowarn
	 */
	public SocketFigure getSocketFigure()
	{
		return socketFigure;
	}

	/**
	 * Determines the direction the connector is facing to.
	 *
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public Orientation getOrientation()
	{
		// Use the fixed quarter setting if given
		Orientation lo = getLockedOrientation();
		if (lo != Orientation.UNDETERMINED)
			return lo;

		// Determine the quarter from the socket's orientation
		return socketFigure.determine2WayOrientation(connectorFigure.isVerticalOrientation());
	}

	/**
	 * Checks if the direction is locked.
	 * @nowarn
	 */
	public boolean isOrientationLocked()
	{
		return lockedOrientation != Orientation.UNDETERMINED;
	}

	/**
	 * Toggles the orientation lock.
	 */
	public void toggleOrientationLock()
	{
		Orientation lo = getLockedOrientation();
		if (lo == Orientation.UNDETERMINED)
		{
			// Lock direction
			lo = connectorFigure.determine2WayOrientation(connectorFigure.isVerticalOrientation());
		}
		else
		{
			// Unlock direction
			lo = Orientation.UNDETERMINED;
		}
		setLockedOrientation(lo);

		// Communicate the change to the tag connectors of the connected links
		updateOrientation();
	}

	/**
	 * Flips the locked orientation.
	 */
	public void flipOrientation()
	{
		// Lock if unlocked
		if (!isOrientationLocked())
		{
			toggleOrientationLock();
		}

		// Flip
		Orientation lo = getLockedOrientation();
		if (lo == Orientation.TOP)
		{
			lo = Orientation.BOTTOM;
		}
		else if (lo == Orientation.BOTTOM)
		{
			lo = Orientation.TOP;
		}
		else if (lo == Orientation.LEFT)
		{
			lo = Orientation.RIGHT;
		}
		else if (lo == Orientation.RIGHT)
		{
			lo = Orientation.LEFT;
		}
		setLockedOrientation(lo);

		// Communicate the change to the tag connectors of the connected links
		updateOrientation();
	}

	/**
	 * Gets the locked orientation of the connector, if any.
	 * @return {@link Orientation#UNDETERMINED}/{@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public Orientation getLockedOrientation()
	{
		// We convert vertical to horizontal orientation constants for locked orientations.
		Orientation ret = lockedOrientation;
		switch (lockedOrientation)
		{
		case UNDETERMINED:
			break;

		case RIGHT:
			if (connectorFigure.isVerticalOrientation())
				ret = Orientation.BOTTOM;
			break;

		case BOTTOM:
			if (!connectorFigure.isVerticalOrientation())
				ret = Orientation.RIGHT;
			break;

		case LEFT:
			if (connectorFigure.isVerticalOrientation())
				ret = Orientation.TOP;
			break;

		case TOP:
			if (!connectorFigure.isVerticalOrientation())
				ret = Orientation.LEFT;
			break;
		}
		return ret;
	}

	/**
	 * Sets the locked orientation of the connector, if any.
	 * @param lockedOrientation {@link Orientation#UNDETERMINED}/{@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public void setLockedOrientation(Orientation lockedOrientation)
	{
		this.lockedOrientation = lockedOrientation;
	}

	/**
	 * Updates the orientation of attached connections.
	 */
	public void updateOrientation()
	{
		// Signalize figure change
		socketFigure.changed();

		if (socketFigure.getDrawing() != null)
		{
			WorkspaceDrawingView view = socketFigure.getDrawing().getView();
			if (view != null)
			{
				view.checkDamage();
			}
		}
	}
}
