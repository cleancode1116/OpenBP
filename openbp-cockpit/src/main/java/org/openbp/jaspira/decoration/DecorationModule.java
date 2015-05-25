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
package org.openbp.jaspira.decoration;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.plugin.EventModule;

/**
 * This is an abstract superclass that should be implemented by all
 * Plugins that want to use the decoration engine. It is used by the
 * DecorationMgr to notify clients of changed decorators.
 *
 * Implement this by subclass this as an inner subclass and overwrite
 * one or both eventHandling methods.
 *
 * @author Stephan Moritz
 */
public abstract class DecorationModule extends EventModule
{
	public String getName()
	{
		return "global.decoration";
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ EventHandling
	/////////////////////////////////////////////////////////////////////////

	/**
	 * This is called when an decorator has been added. The Object of the event contains the
	 * the key under which the decorator has been entered. Overrider should check whether this
	 * key interest them and initiate a repaint (or similar) if applicable.
	 * @param je The event
	 * @event global.decoration.added
	 * @return Returncode
	 */
	public JaspiraEventHandlerCode added(JaspiraEvent je)
	{
		return EVENT_IGNORED;
	}

	/**
	 * This is called when an decorator has been removed. The Object of the event contains the
	 * the key under which the decorator has been removed. Overrider should check whether this
	 * key interest them and initiate a repaint (or similar) if applicable.
	 * @param je The event
	 * @event global.decoration.added
	 * @return Returncode
	 */
	public JaspiraEventHandlerCode removed(JaspiraEvent je)
	{
		return EVENT_IGNORED;
	}
}
