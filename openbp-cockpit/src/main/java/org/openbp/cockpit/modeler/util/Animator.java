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

import org.openbp.jaspira.event.JaspiraEvent;

import CH.ifa.draw.framework.DrawingView;

/**
 * The animator performs an animation on an object (the {@link Animatable}) and fires an event when finished.
 * It can be used e. g. to implment the debugger flow animation.
 *
 * @author Stephan Moritz
 */
public class Animator extends Thread
{
	/** View the contains the animatable */
	private DrawingView view;

	/** The animatable object */
	private Animatable animatable;

	/** Animation delay in ms */
	private final int delay;

	/** Running flag */
	private boolean isRunning;

	/** Event to be fired when the animation has finished */
	private JaspiraEvent endEvent;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param view View the contains the animatable
	 * @param animatable The animatable object
	 * @param delay Animation delay in ms
	 * @param endEvent Event to be fired when the animation has finished or null
	 */
	public Animator(Animatable animatable, DrawingView view, int delay, JaspiraEvent endEvent)
	{
		super("Animator");

		this.animatable = animatable;
		this.view = view;
		this.delay = delay;
		this.endEvent = endEvent;

		// This thread must not prevent VM shutdown.
		setDaemon(true);
	}

	/**
	 * Starts the animation.
	 */
	public void startAnimation()
	{
		isRunning = true;

		animatable.animationStart();

		// Run the animation thread
		start();
	}

	/**
	 * Stops the animation and fires the event.
	 */
	public void stopAnimation()
	{
		isRunning = false;

		animatable.animationEnd();

		if (endEvent != null)
		{
			endEvent.getSourcePlugin().fireEvent(endEvent);
		}
	}

	//////////////////////////////////////////////////
	// @@ Thread implementation
	//////////////////////////////////////////////////

	public void run()
	{
		while (isRunning)
		{
			long tm = System.currentTimeMillis();

			animatable.animationStep();

			view.checkDamage();

			// Delay for a while
			try
			{
				tm += delay;

				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
