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
package org.openbp.jaspira.plugin;

import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;

/**
 * Abstract super module for all menu-, toolbar- and popup menu contributors.
 * Override the event handling methods to implement handling.
 *
 * @author Stephan Moritz
 */
public abstract class InteractionModule extends EventModule
{
	/**
	 * Get ths name of this interaction module.
	 *
	 * @return "global.interaction"
	 */
	public final String getName()
	{
		return "global.interaction";
	}

	/**
	 * Standard event handler that is called when a popup menu is to be shown.
	 * Override this to add your own actions to a popup menu.
	 *
	 * @event global.interaction.popup
	 * @param ie Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode popup(InteractionEvent ie)
	{
		return EVENT_IGNORED;
	}

	/**
	 * Standard event handler that is called when a toolbar is (re-)generated.
	 * Override this to add your own actions to the toolbar.
	 *
	 * @event global.interaction.toolbar
	 * @param ie Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode toolbar(InteractionEvent ie)
	{
		return EVENT_IGNORED;
	}
}
