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
package org.openbp.cockpit.modeler.drawing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This interface is used to implement a trackable component.
 * The position of the tracker is denoted by the track rectangle.
 * Most components in a scroll pane should be trackable, however, they aren't by default.
 *
 * @author Jens Ferchland
 */
public interface Trackable
{
	/**
	 * Adds a change listener to the trackable object.
	 * @nowarn
	 */
	public void addTrackChangedListener(ChangeListener listener);

	/**
	 * Removes a change listener from the trackable object.
	 * @nowarn
	 */
	public void removeTrackChangedListener(ChangeListener listener);

	/**
	 * Fires an event indicating that the tracker position has changed
	 * to the registered track change listeners.
	 *
	 * @param event Event to fire
	 */
	public void fireTrackChangedEvent(ChangeEvent event);

	/**
	 * Prevents track change events to be propagated.
	 */
	public void suspendTrack();

	/**
	 * Resumes propagation of track change events.
	 */
	public void resumeTrack();

	/**
	 * Returns true if the tracking has been suspended.
	 * @nowarn
	 */
	public boolean isTrackSuspended();

	/**
	 * Returns the size of the document that is displayed in the view.
	 * @nowarn
	 */
	public Dimension getDocumentSize();

	/**
	 * Returns the area that is currently visible.
	 * For a scroll pane, this would be the view rectangle of the viewport of the scroll pane.
	 * @return Rectangle in document coordinates
	 */
	public Rectangle getVisibleArea();

	/**
	 * Sets the area that is currently visible.
	 * @param r Rectangle in document coordinates
	 */
	public void setVisibleArea(Rectangle r);

	/**
	 * Centers the trackable component at the specified point.
	 * @param p Point in document coordinates
	 */
	public void centerTrackerAt(Point p);

	/**
	 * Moves the tracker by the given distance.
	 * @param x Hor. distance in document coordinates
	 * @param y Vert. distance in document coordinates
	 */
	public void moveTrackerBy(int x, int y);
}
