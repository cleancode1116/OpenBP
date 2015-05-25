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

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * The event is sent if a break out menu has to be shown and a provider is needed.
 * Any event receiver that feels responsible for the given break out key should set the
 * provider accordingly.
 *
 * @author Stephan Moritz
 */
public class BreakoutEvent extends JaspiraEvent
{
	/////////////////////////////////////////////////////////////////////////
	// @@ members
	/////////////////////////////////////////////////////////////////////////

	/** Key that was pressed to initiate the break out mode */
	private int key;

	/** Break out provider to be set be the event's receivers */
	private BreakoutProvider bop;

	/////////////////////////////////////////////////////////////////////////
	// @@ construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source Source plugin of the event
	 * @param key Key that was pressed to initiate the break out mode
	 */
	public BreakoutEvent(Plugin source, int key)
	{
		super(source, "global.breakout.getprovider", TYPE_FLOOD, Plugin.LEVEL_APPLICATION);

		this.key = key;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ member access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the key that was pressed.
	 * @nowarn
	 */
	public int getKey()
	{
		return key;
	}

	/**
	 * Sets the provider.
	 * This will also consume the event since the job is done here.
	 * @param bop Provider that is suitable of the given key
	 */
	public void setProvider(BreakoutProvider bop)
	{
		this.bop = bop;
		updateFlags(CONSUMED);
	}

	/**
	 * Returns the provider.
	 * @return The provider or null if no event listener has set a provider
	 */
	public BreakoutProvider getProvider()
	{
		return bop;
	}
}
