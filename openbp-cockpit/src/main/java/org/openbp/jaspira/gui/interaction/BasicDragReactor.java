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
package org.openbp.jaspira.gui.interaction;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.geom.RectangularShape;
import java.util.Timer;
import java.util.TimerTask;

import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.swing.SwingUtil;

/**
 * A drop region that delays drag enter events to the drop client.
 * This prevents flickering of e. g. plugin containers while a plugin is dragged over it.
 * The delay time can be specified in the constructor. After timer expiration, the
 * {@link InteractionClient#dragActionTriggered} method of the parent of this drop region is called.
 *
 * @author Stephan Moritz
 */
public class BasicDragReactor extends BasicDropRegion
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Delay timer */
	private Timer timer;

	/** Delay in milliseconds */
	private long delay;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param delay Delay in milliseconds
	 * @copy BasicDropRegion.BasicDropRegion (Object,InteractionClient,RectangularShape,Component)
	 * @nowarn
	 */
	public BasicDragReactor(Object id, InteractionClient parent, RectangularShape shape, Component origin, long delay)
	{
		super(id, parent, shape, origin);
		this.delay = delay;
	}

	/**
	 * Constructor.
	 *
	 * @param delay Delay in milliseconds
	 * @copy BasicDropRegion.BasicDropRegion (Object,InteractionClient,Component)
	 * @nowarn
	 */
	public BasicDragReactor(Object id, InteractionClient parent, Component source, long delay)
	{
		super(id, parent, source);
		this.delay = delay;
	}

	public boolean canImport()
	{
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ BasicDropRegion overrides
	/////////////////////////////////////////////////////////////////////////

	/**
	 * The mouse enters the drop region while in a dragging action.
	 * Starts the timer.
	 * @return Always true
	 */
	public boolean dragEnter()
	{
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				Point p = ApplicationBase.getInstance().getActiveFrame().getDragDropPane().getLastLocation();
				parent.dragActionTriggered(id, SwingUtil.convertFromGlassCoords(p, origin));
			}
		}, delay);

		return true;
	}

	/**
	 * The mouse exits the drop region while in a dragging action.
	 * Stops the delay timer
	 * @return Always true
	 */
	public boolean dragExit()
	{
		if (timer != null)
		{
			timer.cancel();
		}
		return true;
	}

	/**
	 * Import will be performed by the parent itself, so we do nothing here
	 * @param data Data to import
	 * @param p Current mouse position
	 * @return Always false
	 */
	public boolean importData(Transferable data, Point p)
	{
		return false;
	}
}
