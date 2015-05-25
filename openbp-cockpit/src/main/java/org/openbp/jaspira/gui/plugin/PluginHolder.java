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
package org.openbp.jaspira.gui.plugin;

/**
 * Container that holds a single visible plugin.
 *
 * @author Stephan Moritz
 */
public interface PluginHolder
{
	/**
	 * Rebuilds the title, tool and menu bars of the holder.
	 *
	 * @param fullRebuild
	 *		true	Causes a full environment rebuild including menu and toolbar rebuild<br>
	 *		false	Updates the holder's title only and checks for toolbar size change
	 */
	public void updateHolder(boolean fullRebuild);

	/**
	 * Requests the holder to become visible.
	 *
	 * @param changePage
	 *		true	Switches the page if the holder is not a part of the current page.<br>
	 *		false	Makes the holder the active holder on its page, but does not switch to the new page.
	 */
	public void showHolder(boolean changePage);

	/**
	 * Unlinks the holder from its container.
	 * If the holder is a top-level plugin holder (such as a dialog or a frame),
	 * this will remove the holder itself.
	 */
	void unlinkHolder();
}
