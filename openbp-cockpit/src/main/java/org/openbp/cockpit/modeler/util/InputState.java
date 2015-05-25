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
package org.openbp.cockpit.modeler.util;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Input state constants.
 *
 * @author Heiko Erhardt
 */
public class InputState
{
	// TODO Cleanup 5 This should be a member of ModelerToolSupport, not a static class

	/** Shift key down state */
	public static final int SHIFT = InputEvent.SHIFT_MASK;

	/** Control key down state */
	public static final int CTRL = InputEvent.CTRL_MASK;

	/** Meta key down state */
	public static final int META = InputEvent.META_MASK;

	/** Alt key down state */
	public static final int ALT = InputEvent.ALT_MASK;

	/** Alt key down state */
	public static final int ALT_GRAPH = InputEvent.ALT_GRAPH_MASK;

	/** Mouse button 1 down state */
	public static final int BUTTON1 = (1 << 8);

	/** Mouse button 2 down state */
	public static final int BUTTON2 = (1 << 9);

	/** Mouse button 3 down state */
	public static final int BUTTON3 = (1 << 10);

	/** Hover activation */
	public static final int HOVER = (1 << 11);

	/** Keyboard modifier states */
	public static final int KEY_STATES = (SHIFT | CTRL | META | ALT | ALT_GRAPH);

	/** Mouse button modifier states */
	public static final int MOUSE_STATES = (BUTTON1 | BUTTON2 | BUTTON3);

	/** Curent input state */
	private static int state;

	/**
	 * Private constructor prevents instantiation.
	 */
	private InputState()
	{
	}

	/**
	 * Gets the curent input state.
	 * @nowarn
	 */
	public static int getState()
	{
		return state;
	}

	/**
	 * Sets the curent input state.
	 * @nowarn
	 */
	public static void setState(int newState)
	{
		state = newState;
	}

	/**
	 * Updates the given input state in case of a keyboard input event.
	 *
	 * @param e Input event
	 */
	public static void updateStateOnInputEvent(InputEvent e)
	{
		// Clear all key states
		state &= ~KEY_STATES;

		// Update from current event
		state |= (e.getModifiers() & KEY_STATES);
	}

	/**
	 * Updates the given input state in case of a mouse down event.
	 *
	 * @param e Mouse event
	 */
	public static void updateStateOnMouseDownEvent(MouseEvent e)
	{
		// Update from current event
		int mod = e.getModifiers();
		if ((mod & MouseEvent.BUTTON1_MASK) != 0)
		{
			state |= BUTTON1;
		}
		if ((mod & MouseEvent.BUTTON2_MASK) != 0)
		{
			state |= BUTTON2;
		}
		if ((mod & MouseEvent.BUTTON3_MASK) != 0)
		{
			state |= BUTTON3;
		}
	}

	/**
	 * Updates the given input state in case of a mouse up event.
	 *
	 * @param e Mouse event
	 */
	public static void updateStateOnMouseUpEvent(MouseEvent e)
	{
		// Update from current event
		int mod = e.getModifiers();
		if ((mod & MouseEvent.BUTTON1_MASK) != 0)
		{
			state &= ~BUTTON1;
		}
		if ((mod & MouseEvent.BUTTON2_MASK) != 0)
		{
			state &= ~BUTTON2;
		}
		if ((mod & MouseEvent.BUTTON3_MASK) != 0)
		{
			state &= ~BUTTON3;
		}
	}

	/**
	 * Checks if the shift key is down.
	 * @nowarn
	 */
	public static boolean isShiftDown()
	{
		return (state & SHIFT) != 0;
	}

	/**
	 * Checks if the control key is down.
	 * @nowarn
	 */
	public static boolean isCtrlDown()
	{
		return (state & CTRL) != 0;
	}

	/**
	 * Checks if the meta key is down.
	 * @nowarn
	 */
	public static boolean isMetaDown()
	{
		return (state & META) != 0;
	}

	/**
	 * Checks if the alt key is down.
	 * @nowarn
	 */
	public static boolean isAltDown()
	{
		return (state & ALT) != 0;
	}

	/**
	 * Checks if the alt graph key is down.
	 * @nowarn
	 */
	public static boolean isAltGraphDown()
	{
		return (state & ALT_GRAPH) != 0;
	}

	/**
	 * Checks if the left mouse button is down.
	 * @nowarn
	 */
	public static boolean isLeftButtonDown()
	{
		return (state & BUTTON1) != 0;
	}

	/**
	 * Checks if the middle mouse button is down.
	 * @nowarn
	 */
	public static boolean isMiddleButtonDown()
	{
		return (state & BUTTON2) != 0;
	}

	/**
	 * Checks if the right mouse button is down.
	 * @nowarn
	 */
	public static boolean isRightButtonDown()
	{
		return (state & BUTTON3) != 0;
	}
}
