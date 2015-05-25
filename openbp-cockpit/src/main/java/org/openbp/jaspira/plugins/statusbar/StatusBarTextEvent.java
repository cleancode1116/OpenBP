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
package org.openbp.jaspira.plugins.statusbar;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * This event is used to display a text in a status bar plugin.
 *
 * @author Jens Ferchland
 */
public class StatusBarTextEvent extends JaspiraEvent
{
	/** Text to display */
	private String text;

	/**
	 * Constructor.
	 *
	 * @param plugin Source plugin
	 * @param text Text to display
	 */
	public StatusBarTextEvent(Plugin plugin, String text)
	{
		super(plugin, "statusbar.updatetext");

		this.text = text;
	}

	/**
	 * Gets the text to display.
	 * @nowarn
	 */
	public String getText()
	{
		return text;
	}
}
