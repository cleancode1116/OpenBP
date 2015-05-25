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
package org.openbp.cockpit.modeler.figures;

import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;

/**
 * Event that may happen to a visual element.
 *
 * @author Heiko Erhardt
 */
public class VisualElementEvent
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Event: Cursor entered the element */
	public static final String CURSOR_ENTERED = "CursorEntered";

	/** Event: Cursor left the element */
	public static final String CURSOR_LEFT = "CursorLeft";

	/** Event: Element was selected */
	public static final String SELECTED = "Selected";

	/** Event: Element was deselected */
	public static final String DESELECTED = "Deselected";

	/** Event: State should be updated */
	public static final String UPDATE_STATE = "UpdateState";

	/** Event: Element is participating in a drag and drop operation */
	public static final String SET_DND_PARTICIPANT = "SetDropTarget";

	/** Event: Element is not participating in a drag and drop operation any more */
	public static final String UNSET_DND_PARTICIPANT = "UnsetDropTarget";

	/** Event: Element was double-clicked */
	public static final String DOUBLE_CLICK = "DoubleClick";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Event type (one of the constants of this class) */
	public String type;

	/** Mouse event that caused this event or null */
	public MouseEvent mouseEvent;

	/** X Document coordinate this event refers to or -1 */
	public int x;

	/** Y Document coordinate this event refers to or -1 */
	public int y;

	/** Current editor */
	public DrawingEditorPlugin editor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param type Event type (one of the constants of this class)
	 * @param editor Current editor
	 */
	public VisualElementEvent(String type, DrawingEditorPlugin editor)
	{
		this.type = type;
		this.editor = editor;
	}

	/**
	 * Constructor for mouse-related events.
	 *
	 * @param type Event type (one of the constants of this class)
	 * @param editor Current editor
	 * @param mouseEvent Mouse event that caused this event or null
	 * @param x X Document coordinate this event refers to or -1
	 * @param y Y Document coordinate this event refers to or -1
	 */
	public VisualElementEvent(String type, DrawingEditorPlugin editor, MouseEvent mouseEvent, int x, int y)
	{
		this.type = type;
		this.editor = editor;
		this.mouseEvent = mouseEvent;
		this.x = x;
		this.y = y;
	}
}
