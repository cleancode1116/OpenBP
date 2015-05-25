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
package org.openbp.cockpit.plugins.miniview;

import java.util.ArrayList;
import java.util.List;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

import CH.ifa.draw.framework.DrawingEditor;

/**
 * This Event is sent by the MiniView Plugin to query all active editors that would like to be displayed
 * in the mini view.
 *
 * @author Jens Ferchland
 */
public class MiniViewEvent extends JaspiraEvent
{
	/** List containing all editors that like to appear in the mini view */
	private List editors = new ArrayList(3);

	/**
	 * Constructor.
	 *
	 * @param source Source plugin
	 * @param eventName Event name
	 */
	public MiniViewEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	/**
	 * Adds an editor to the editor list.
	 * @param editor Editor to add
	 */
	public void addEditor(DrawingEditor editor)
	{
		editors.add(editor);
	}

	/**
	 * Returns all collected editors.
	 * @return A list of editors containing DrawingEditor objects
	 */
	public List getEditors()
	{
		return editors;
	}
}