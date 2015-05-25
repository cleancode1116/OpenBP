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
package org.openbp.jaspira.event;

import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Special event that is passed when a key sequence has been recognized.
 *
 * @author Stephan Moritz
 */
public class KeySequenceEvent extends JaspiraEvent
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Keystroke event name. */
	public static final String KEYEVENTNAME = "global.keyevent";

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param sequence The key sequence to be processed
	 * @param type The type of the event (i\.e\. the mode of passing the event).
	 */
	public KeySequenceEvent(Plugin source, KeySequence sequence, int type)
	{
		super(source, KEYEVENTNAME, sequence, type, Plugin.LEVEL_APPLICATION, 0);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the KeySequence associated with this event.
	 * @nowarn
	 */
	public KeySequence getKeySequence()
	{
		return (KeySequence) getObject();
	}
}
