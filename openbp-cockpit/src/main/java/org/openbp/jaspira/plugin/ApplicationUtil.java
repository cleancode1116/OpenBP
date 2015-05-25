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

import java.awt.Component;
import java.awt.Window;

import javax.swing.FocusManager;
import javax.swing.JFrame;

import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.swing.SwingUtil;

/**
 * App-related utility methods.
 *
 * @author Heiko Erhardt
 */
public class ApplicationUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ApplicationUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Returns the currently active frame of the application.
	 *
	 * @return The active window is either the window returned by the focus manager
	 * (if it is a JFrame) or otherwise the first frame in the list of frames of the
	 * application base or null if no frames have been added to the application yet.
	 */
	public static JFrame getActiveWindow()
	{
		Window win = FocusManager.getCurrentManager().getActiveWindow();
		if (win instanceof JFrame)
		{
			return (JFrame) win;
		}
		return ApplicationBase.getInstance().getActiveFrame();
	}

	/**
	 * Turns the waits cursor on.
	 * This method is a convenience method for the org.openbp.swing.SwingUtil.waitCursorOn
	 * method that uses the return value of the {@link #getActiveWindow} method as argument.
	 */
	public static void waitCursorOn()
	{
		Component comp = getActiveWindow();
		if (comp != null)
		{
			SwingUtil.waitCursorOn(comp);
		}
	}

	/**
	 * Turns the waits cursor off.
	 * This method is a convenience method for the org.openbp.swing.SwingUtil.waitCursorOff
	 * method that uses the return value of the {@link #getActiveWindow} method as argument.
	 */
	public static void waitCursorOff()
	{
		Component comp = getActiveWindow();
		if (comp != null)
		{
			SwingUtil.waitCursorOff(comp);
		}
	}
}
