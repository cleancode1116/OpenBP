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
package org.openbp.swing.components.wizard;

/**
 * Wizard event.
 *
 * @author Heiko Erhardt
 */
public class WizardEvent
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Event type: Page is being shown */
	public static final int SHOW = 0;

	/** Event type: 'Cancel' button pressed */
	public static final int CANCEL = 1;

	/** Event type: 'Finish' button pressed */
	public static final int FINISH = 2;

	/** Event type: 'Next' button pressed */
	public static final int NEXT = 3;

	/** Event type: 'Back' button pressed */
	public static final int BACK = 4;

	/** Event type: First page displayed */
	public static final int FIRST = 5;

	/** Event type: 'Close' button pressed */
	public static final int CLOSE = 6;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Wizard that caused the event */
	public Wizard wizard;

	/** Event type (see the constants of this class) */
	public int eventType;

	/** Cancel flag */
	public boolean cancel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param wizard Wizard that caused the event
	 * @param eventType Event type (see the constants of this class)
	 */
	public WizardEvent(Wizard wizard, int eventType)
	{
		this.wizard = wizard;
		this.eventType = eventType;
	}
}
