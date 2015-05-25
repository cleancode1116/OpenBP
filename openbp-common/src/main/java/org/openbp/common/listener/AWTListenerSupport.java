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
package org.openbp.common.listener;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;

/**
 * Convenience class that adds event firing methods for listeners of the java.awt package to the listener support class.
 *
 * @author Heiko Erhardt
 */
public class AWTListenerSupport extends ListenerSupport
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public AWTListenerSupport()
	{
	}

	//////////////////////////////////////////////////
	// @@ AWTEventListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'event dispatched' message to all registered AWT event listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireEventDispatched(AWTEvent e)
	{
		for (Iterator it = getListenerIterator(AWTEventListener.class); it.hasNext();)
		{
			((AWTEventListener) it.next()).eventDispatched(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ActionListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'action performed' message to all registered action listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireActionPerformed(ActionEvent e)
	{
		for (Iterator it = getListenerIterator(ActionListener.class); it.hasNext();)
		{
			((ActionListener) it.next()).actionPerformed(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ AdjustmentListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'adjustment value changed' message to all registered adjustment listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAdjustmentValueChanged(AdjustmentEvent e)
	{
		for (Iterator it = getListenerIterator(AdjustmentListener.class); it.hasNext();)
		{
			((AdjustmentListener) it.next()).adjustmentValueChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ComponentListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'component resized' message to all registered component listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentResized(ComponentEvent e)
	{
		for (Iterator it = getListenerIterator(ComponentListener.class); it.hasNext();)
		{
			((ComponentListener) it.next()).componentResized(e);
		}
	}

	/**
	 * Fires an 'component moved' message to all registered component listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentMoved(ComponentEvent e)
	{
		for (Iterator it = getListenerIterator(ComponentListener.class); it.hasNext();)
		{
			((ComponentListener) it.next()).componentMoved(e);
		}
	}

	/**
	 * Fires an 'component shown' message to all registered component listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentShown(ComponentEvent e)
	{
		for (Iterator it = getListenerIterator(ComponentListener.class); it.hasNext();)
		{
			((ComponentListener) it.next()).componentShown(e);
		}
	}

	/**
	 * Fires an 'component hidden' message to all registered component listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentHidden(ComponentEvent e)
	{
		for (Iterator it = getListenerIterator(ComponentListener.class); it.hasNext();)
		{
			((ComponentListener) it.next()).componentHidden(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ContainerListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'component added' message to all registered container listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentAdded(ContainerEvent e)
	{
		for (Iterator it = getListenerIterator(ContainerListener.class); it.hasNext();)
		{
			((ContainerListener) it.next()).componentAdded(e);
		}
	}

	/**
	 * Fires an 'component removed' message to all registered container listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireComponentRemoved(ContainerEvent e)
	{
		for (Iterator it = getListenerIterator(ContainerListener.class); it.hasNext();)
		{
			((ContainerListener) it.next()).componentRemoved(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ FocusListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'focus gained' message to all registered focus listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireFocusGained(FocusEvent e)
	{
		for (Iterator it = getListenerIterator(FocusListener.class); it.hasNext();)
		{
			((FocusListener) it.next()).focusGained(e);
		}
	}

	/**
	 * Fires an 'focus lost' message to all registered focus listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireFocusLost(FocusEvent e)
	{
		for (Iterator it = getListenerIterator(FocusListener.class); it.hasNext();)
		{
			((FocusListener) it.next()).focusLost(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ HierarchyBoundsListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'ancestor moved' message to all registered hierarchy bounds listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAncestorMoved(HierarchyEvent e)
	{
		for (Iterator it = getListenerIterator(HierarchyBoundsListener.class); it.hasNext();)
		{
			((HierarchyBoundsListener) it.next()).ancestorMoved(e);
		}
	}

	/**
	 * Fires an 'ancestor resized' message to all registered hierarchy bounds listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireAncestorResized(HierarchyEvent e)
	{
		for (Iterator it = getListenerIterator(HierarchyBoundsListener.class); it.hasNext();)
		{
			((HierarchyBoundsListener) it.next()).ancestorResized(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ HierarchyListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'hierarchy changed' message to all registered hierarchy listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireHierarchyChanged(HierarchyEvent e)
	{
		for (Iterator it = getListenerIterator(HierarchyListener.class); it.hasNext();)
		{
			((HierarchyListener) it.next()).hierarchyChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ InputMethodListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'input method text changed' message to all registered input method listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireInputMethodTextChanged(InputMethodEvent e)
	{
		for (Iterator it = getListenerIterator(InputMethodListener.class); it.hasNext();)
		{
			((InputMethodListener) it.next()).inputMethodTextChanged(e);
		}
	}

	/**
	 * Fires an 'caret position changed' message to all registered input method listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireCaretPositionChanged(InputMethodEvent e)
	{
		for (Iterator it = getListenerIterator(InputMethodListener.class); it.hasNext();)
		{
			((InputMethodListener) it.next()).caretPositionChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ ItemListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'item state changed' message to all registered item listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireItemStateChanged(ItemEvent e)
	{
		for (Iterator it = getListenerIterator(ItemListener.class); it.hasNext();)
		{
			((ItemListener) it.next()).itemStateChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ KeyListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'key typed' message to all registered key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireKeyTyped(KeyEvent e)
	{
		for (Iterator it = getListenerIterator(KeyListener.class); it.hasNext();)
		{
			((KeyListener) it.next()).keyTyped(e);
		}
	}

	/**
	 * Fires an 'key pressed' message to all registered key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireKeyPressed(KeyEvent e)
	{
		for (Iterator it = getListenerIterator(KeyListener.class); it.hasNext();)
		{
			((KeyListener) it.next()).keyPressed(e);
		}
	}

	/**
	 * Fires an 'key released' message to all registered key listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireKeyReleased(KeyEvent e)
	{
		for (Iterator it = getListenerIterator(KeyListener.class); it.hasNext();)
		{
			((KeyListener) it.next()).keyReleased(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ MouseListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'mouse clicked' message to all registered mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseClicked(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseListener.class); it.hasNext();)
		{
			((MouseListener) it.next()).mouseClicked(e);
		}
	}

	/**
	 * Fires an 'mouse pressed' message to all registered mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMousePressed(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseListener.class); it.hasNext();)
		{
			((MouseListener) it.next()).mousePressed(e);
		}
	}

	/**
	 * Fires an 'mouse released' message to all registered mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseReleased(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseListener.class); it.hasNext();)
		{
			((MouseListener) it.next()).mouseReleased(e);
		}
	}

	/**
	 * Fires an 'mouse entered' message to all registered mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseEntered(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseListener.class); it.hasNext();)
		{
			((MouseListener) it.next()).mouseEntered(e);
		}
	}

	/**
	 * Fires an 'mouse exited' message to all registered mouse listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseExited(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseListener.class); it.hasNext();)
		{
			((MouseListener) it.next()).mouseExited(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ MouseMotionListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'mouse dragged' message to all registered mouse motion listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseDragged(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseMotionListener.class); it.hasNext();)
		{
			((MouseMotionListener) it.next()).mouseDragged(e);
		}
	}

	/**
	 * Fires an 'mouse moved' message to all registered mouse motion listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireMouseMoved(MouseEvent e)
	{
		for (Iterator it = getListenerIterator(MouseMotionListener.class); it.hasNext();)
		{
			((MouseMotionListener) it.next()).mouseMoved(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TextListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'text value changed' message to all registered text listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireTextValueChanged(TextEvent e)
	{
		for (Iterator it = getListenerIterator(TextListener.class); it.hasNext();)
		{
			((TextListener) it.next()).textValueChanged(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ WindowListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'window opened' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowOpened(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowOpened(e);
		}
	}

	/**
	 * Fires an 'window closing' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowClosing(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowClosing(e);
		}
	}

	/**
	 * Fires an 'window closed' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowClosed(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowClosed(e);
		}
	}

	/**
	 * Fires an 'window iconified' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowIconified(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowIconified(e);
		}
	}

	/**
	 * Fires an 'window deiconified' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowDeiconified(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowDeiconified(e);
		}
	}

	/**
	 * Fires an 'window activated' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowActivated(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowActivated(e);
		}
	}

	/**
	 * Fires an 'window deactivated' message to all registered window listeners.
	 *
	 * @param e Event to fire
	 */
	public void fireWindowDeactivated(WindowEvent e)
	{
		for (Iterator it = getListenerIterator(WindowListener.class); it.hasNext();)
		{
			((WindowListener) it.next()).windowDeactivated(e);
		}
	}
}
