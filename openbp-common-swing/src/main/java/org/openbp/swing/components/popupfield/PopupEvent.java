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
package org.openbp.swing.components.popupfield;

import java.util.EventObject;

import javax.swing.JComponent;

/**
 * This event occurrs when the popup menu of a {@link JSelectionField} is opened or closed.
 *
 * @author Heiko Erhardt
 */
public class PopupEvent extends EventObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Popup is being opened */
	public static final int POPUP_OPENING = 1;

	/** Popup was opened */
	public static final int POPUP_OPENED = 2;

	/** Popup is being closed */
	public static final int POPUP_CLOSING = 3;

	/** Popup was closed */
	public static final int POPUP_CLOSED = 4;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Cause of the event ({@link #POPUP_OPENING}/{@link #POPUP_OPENED}/{@link #POPUP_CLOSING}/{@link #POPUP_CLOSED}) */
	private int cause;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param source Source of the event (the {@link JSelectionField})
	 * @param cause Cause of the event ({@link #POPUP_OPENING}/{@link #POPUP_OPENED}/{@link #POPUP_CLOSING}/{@link #POPUP_CLOSED})
	 */
	public PopupEvent(JComponent source, int cause)
	{
		super(source);
		this.cause = cause;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the cause of the event ({@link #POPUP_OPENED}/{@link #POPUP_CLOSED}).
	 * @nowarn
	 */
	public int getCause()
	{
		return cause;
	}
}
