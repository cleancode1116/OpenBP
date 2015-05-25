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
package org.openbp.jaspira.plugins.errordialog;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * This event can be used to open the error dialog.
 *
 * @author Andreas Putz
 */
public class ErrorEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Message displayed in a dialog */
	private String message;

	/** The error stacktrace */
	private Throwable throwable;

	/** The error stacktrace as string */
	private String exceptionString;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source The plugin where the throwable was thrown
	 * @param message This message will be displayed in a dialog
	 * @param t Throwable object
	 */
	public ErrorEvent(Plugin source, String message, Throwable t)
	{
		super(source, "plugin.errordialog.showerror");

		this.message = message;

		this.throwable = t;
	}

	/**
	 * Constructor.
	 *
	 * @param source The plugin where the throwable was thrown
	 * @param message This message will be displayed in a dialog
	 * @param exceptionString The exception as string
	 */
	public ErrorEvent(Plugin source, String message, String exceptionString)
	{
		super(source, "plugin.errordialog.showerror");

		this.message = message;

		this.exceptionString = exceptionString;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the message which will displayed in a dialog.
	 *
	 * @nowarn
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Gets the throwable which was thrown.
	 *
	 * @nowarn
	 */
	public Throwable getThrowable()
	{
		return throwable;
	}

	/**
	 * Gets the exception as string.
	 * @nowarn
	 */
	public String getExceptionString()
	{
		return exceptionString;
	}
}
