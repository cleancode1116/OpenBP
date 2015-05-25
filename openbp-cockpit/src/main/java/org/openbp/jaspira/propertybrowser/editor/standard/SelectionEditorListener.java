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
package org.openbp.jaspira.propertybrowser.editor.standard;

import org.openbp.jaspira.propertybrowser.editor.PropertyEditorListener;
import org.openbp.swing.components.popupfield.PopupEvent;

/**
 * Listener for the selection editor.
 * Allows dynamic configuration of the editor.
 *
 * @author Heiko Erhardt
 */
public interface SelectionEditorListener
	extends PropertyEditorListener
{
	/**
	 * Called when the popup menu of the selection editor is being displayed or hidden.
	 *
	 * @param editor Editor
	 * @param cause Cause of the event ({@link PopupEvent#POPUP_OPENING}/{@link PopupEvent#POPUP_OPENED}/
	 * {@link PopupEvent#POPUP_CLOSING}/{@link PopupEvent#POPUP_CLOSED})
	 */
	public void popup(SelectionEditor editor, int cause);
}
